package com.example.todo_api_v2.exception;

// 重複したcodeの登録を受け付けない。このクラス名でハンドリング
public class DuplicateItemCodeException extends IllegalStateException {
    public DuplicateItemCodeException(String message) {
        super(message);
    }
}
