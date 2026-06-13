package com.example.todo_api_v2.dto.item;

import com.example.todo_api_v2.entity.UomType;

import java.math.BigDecimal;

public record ReorderAlertResponse(
        Long id, String itemCode, String name, UomType uom, BigDecimal currentStock, Integer reorderPoint
) {
}
