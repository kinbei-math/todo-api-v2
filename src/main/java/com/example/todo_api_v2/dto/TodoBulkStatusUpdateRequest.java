package com.example.todo_api_v2.dto;

import com.example.todo_api_v2.entity.TodoStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TodoBulkStatusUpdateRequest(
        @NotEmpty(message="変更するtodoのidを選択してください")
        List<Long> ids,

        @NotNull(message="適切な状態を送ってください")
        TodoStatus nextStatus)
{}
