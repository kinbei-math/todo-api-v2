package com.example.todo_api_v2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TodoUpdateRequest(@NotBlank(message = "タイトルを入力してください")  @Size(max=255, message="titleが長すぎます") String title, LocalDate dueDate) {
}
