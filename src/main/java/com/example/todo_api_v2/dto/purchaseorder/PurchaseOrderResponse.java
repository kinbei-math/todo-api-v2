package com.example.todo_api_v2.dto.purchaseorder;

import com.example.todo_api_v2.entity.PoStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 発注書1枚に相当する伝票のレスポンス
 *
 * @param id        PK(DBで自動採番)
 * @param poNumber  発注番号
 * @param supplier  仕入先
 * @param orderDate 発注日
 * @param status    発注状態
 * @param lines     明細リスト
 * @param createdBy 作成者
 * @param createdAt 作成時間
 * @param updatedBy 更新者
 * @param updatedAt 更新時間
 */
public record PurchaseOrderResponse(
        Long                            id,
        String                          poNumber,
        String                          supplier,
        LocalDate                       orderDate,
        PoStatus                        status,
        List<PurchaseOrderLineResponse> lines,
        String                          createdBy,
        LocalDateTime                   createdAt,
        String                          updatedBy,
        LocalDateTime                   updatedAt
) {

    // コンパクトコンストラクタ
    // recordの中のリストが不変にするために記述
    // nullの可能性はないが、防御的に記述
    public PurchaseOrderResponse{
        if(lines != null){
            lines = List.copyOf(lines);
        }
    }
}
