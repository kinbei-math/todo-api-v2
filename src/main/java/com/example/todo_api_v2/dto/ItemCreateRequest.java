package com.example.todo_api_v2.dto;

import com.example.todo_api_v2.entity.Category;
import com.example.todo_api_v2.entity.UomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ItemCreateRequest(
        @NotBlank(message = "品目コードを入力してください")
        @Size(max=20, message = "品目コードは20文字以内で入力してください")
        @Pattern(regexp = "^[\\x21-\\x7E]+$", message = "品目コードは半角英数字記号で入力してください")
        String itemCode,

        @NotBlank(message = "品名を入力してください")
        @Size(max=100, message = "品名は100文字以内で入力してください")
        String name,

        @NotNull(message = "単位を選択してください")
        UomType uom,

        @NotNull(message = "カテゴリを選択してください")
        Category category
) {
}
