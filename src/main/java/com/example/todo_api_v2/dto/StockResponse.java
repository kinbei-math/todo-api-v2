package com.example.todo_api_v2.dto;

import com.example.todo_api_v2.entity.UomType;

import java.math.BigDecimal;

public record StockResponse(
        Long itemId,
        String itemCode,
        String name,
        BigDecimal currentStock,
        UomType uom
) {}
