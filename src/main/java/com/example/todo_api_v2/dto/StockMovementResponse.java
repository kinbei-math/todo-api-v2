package com.example.todo_api_v2.dto;

import com.example.todo_api_v2.entity.MovementType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record StockMovementResponse(
        Long id,
        Long itemId,
        MovementType movementType,
        BigDecimal qty,
        LocalDate movementDate,
        LocalDateTime createdAt,
        String createdBy
) {
}
