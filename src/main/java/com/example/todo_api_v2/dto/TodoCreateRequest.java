package com.example.todo_api_v2.dto;

import java.time.LocalDate;

public record TodoCreateRequest(String title, LocalDate dueDate) {
}
