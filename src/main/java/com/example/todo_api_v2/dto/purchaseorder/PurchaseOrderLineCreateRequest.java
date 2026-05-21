package com.example.todo_api_v2.dto.purchaseorder;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 発注作成時の明細1行分
 * PurchaseOrderCreateRequestのlinesの要素になっている
 *
 * @param itemId  品目ID(itemsテーブルとのFK)
 * @param qty     数量
 * @param price   単価
 * @param dueDate 納期
 */
public record PurchaseOrderLineCreateRequest(
        @NotNull(message = "品目IDを入力してください") Long itemId,
        @NotNull(message = "数量を入力してください")
        @Positive(message = "数量は0より大きい値を入力してください")
        @Digits(integer = 9, fraction = 3, message = "数量の整数部分は9桁, 小数部分は3桁以内で入力してください")
        BigDecimal qty,
        @NotNull(message = "単価を入力してください")
        @Positive(message = "単価は0より大きい値を入力してください")
        @Digits(integer = 10, fraction = 2, message = "単価の整数部分は10桁, 小数部分は2桁以内で入力してください")
        BigDecimal price,
        @NotNull(message = "納期を入力してください") LocalDate dueDate
        ) {
}
