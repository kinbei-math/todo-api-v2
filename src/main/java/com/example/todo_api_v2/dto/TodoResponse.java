package com.example.todo_api_v2.dto;

import com.example.todo_api_v2.entity.TodoStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TodoResponse(
        Long id, String title, LocalDate dueDate,
        TodoStatus todoStatus, LocalDateTime completedAt
) {}
