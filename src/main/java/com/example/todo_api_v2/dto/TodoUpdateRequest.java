package com.example.todo_api_v2.dto;

import java.time.LocalDate;

public record TodoUpdateRequest(String title, LocalDate dueDate,Boolean isCompleted) {
}
