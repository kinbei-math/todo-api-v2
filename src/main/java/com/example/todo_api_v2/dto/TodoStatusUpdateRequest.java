package com.example.todo_api_v2.dto;

import com.example.todo_api_v2.entity.TodoStatus;
import jakarta.validation.constraints.NotNull;

public record TodoStatusUpdateRequest(@NotNull(message="適切な状態を送ってください") TodoStatus nextStatus) {
}
