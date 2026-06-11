package com.example.todo_api_v2.dto.stock;

import com.example.todo_api_v2.entity.MovementType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockMovementCreateRequest(
        @NotNull(message = "品目IDを入力してください") Long itemId,
        @NotNull(message = "入出庫種別を選択してください") MovementType movementType,
        @NotNull(message = "移動数量を入力してください")
        @Digits(integer =9, fraction = 3, message = "数量は整数部分は9桁、小数部分は3桁以内で入力してください")
        @Positive(message = "数量は0より大きい値を入力してください") BigDecimal qty,
        @NotNull(message = "移動日を入力してください") LocalDate movementDate
) {}
