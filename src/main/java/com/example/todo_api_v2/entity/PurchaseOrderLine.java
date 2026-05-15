package com.example.todo_api_v2.entity;


import com.example.todo_api_v2.exception.InvalidStatusTransitionException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class PurchaseOrderLine {
    private final Long          id;         // id(PK) DBで自動採番
    private final Long          poId;       // FK(PurchaseOrder)
    private final Long          itemId;     // FK(items)
    private final Integer       lineNo;     // 行番号(MyBatisとの相性でnull検知できるIntegerを採用)
    private final BigDecimal    qty;        // 発注数量 (CHECK制約 qty>0)
    private final BigDecimal    price;      // 単価 (CHECK制約 price>0)
    private final LocalDate     dueDate;    // 納期(Business Time)
    private       PoLineStatus  status;     // 注文状態
    private       String        receivedBy; // receivedへの変更者
    private       LocalDate     receivedAt; // 納品日(Business Time)
    private final String        createdBy;  // 明細作成者
    private final LocalDateTime createdAt;  // 明細作成時間(System Time)
    private       String        updatedBy;  // 更新者
    private       LocalDateTime updatedAt;  // 更新時間(System Time)


    /**
     * 明細をORDERED→RECEIVED(入荷済み)へと変更する入荷登録メソッド
     *
     * @param receivedAt　納品日(Business Time)
     * @param operator　操作者(receivedBy, updatedByに記録)
     * @param updatedAt 更新時間(Service層で生成)
     * @throws InvalidStatusTransitionException 現在のstatusからRECEIVEDヘ遷移できない場合
     */
    public void markAsReceived(LocalDate receivedAt, String operator, LocalDateTime updatedAt){
        if(!this.status.canTransitionTo(PoLineStatus.RECEIVED)){
            throw new InvalidStatusTransitionException(
                    String.format("不正遷移:poId=%d, lineNo=%d, %sから%sへの遷移",
                            poId,lineNo,status,PoLineStatus.RECEIVED)
                    );
        }
        this.status = PoLineStatus.RECEIVED;
        this.receivedBy = operator;
        this.receivedAt = receivedAt;
        this.updatedBy = operator;
        this.updatedAt = updatedAt;
    }

    /**
     * 明細をRECEIVED→ORDERED(未納品)へと変更する入荷取り消しメソッド
     *
     * @param operator 操作者(UpdatedByに保存)
     * @param updatedAt 更新時間(Service層で生成)
     * @throws InvalidStatusTransitionException 現在のstatusからORDEREDヘ遷移できない場合
     */
    public void cancelReceiving(String operator, LocalDateTime updatedAt){
        if(!this.status.canTransitionTo(PoLineStatus.ORDERED)){
            throw new InvalidStatusTransitionException(
                    String.format("不正遷移:poId=%d, lineNo=%d, %sから%sへの遷移",
                            poId,lineNo,status,PoLineStatus.ORDERED)
            );
        }
        this.status = PoLineStatus.ORDERED;
        this.receivedBy = null;
        this.receivedAt = null;
        this.updatedBy = operator;
        this.updatedAt = updatedAt;
    }
}
