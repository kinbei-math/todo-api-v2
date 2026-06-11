package com.example.todo_api_v2.dto.purchaseorder;

import com.example.todo_api_v2.entity.PoLineStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * 発注明細1件のレスポンス
 * PurchaseOrderResponseのlinesの要素
 *
 * @param id         PK(DBで自動採番)
 * @param poId       発注番号(purchase_ordersテーブルとのFK)
 * @param lineNo     明細番号(何行目)
 * @param itemId     itemId(itemsテーブルとのFK)
 * @param qty        数量
 * @param price      単価
 * @param dueDate    納期
 * @param status     納品状態
 * @param receivedBy 受取人(未入荷の場合はnull)
 * @param receivedAt 納品日(未入荷の場合はnull)
 * @param createdBy  作成者
 * @param createdAt  作成時間
 * @param updatedBy  更新者
 * @param updatedAt  更新時間
 */
public record PurchaseOrderLineResponse(
        Long          id,
        Long          poId,
        Integer       lineNo,
        Long          itemId,
        BigDecimal    qty,
        BigDecimal    price,
        LocalDate     dueDate,
        PoLineStatus  status,
        String        receivedBy,
        LocalDate     receivedAt,
        String        createdBy,
        LocalDateTime createdAt,
        String        updatedBy,
        LocalDateTime updatedAt
) {
}
