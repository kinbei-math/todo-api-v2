package com.example.todo_api_v2.dto;

import com.example.todo_api_v2.entity.Category;
import com.example.todo_api_v2.entity.UomType;

import java.time.LocalDateTime;

// すべての情報を載せて返す
public record ItemResponse(
        Long id, String itemCode, String name, UomType uom, Category category,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {}
