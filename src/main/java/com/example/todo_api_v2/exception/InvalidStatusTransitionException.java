package com.example.todo_api_v2.exception;

// クラス名は「不正な状態遷移」
public class InvalidStatusTransitionException extends IllegalStateException {

    // コンストラクタ（エラーメッセージを受け取って、親クラスに渡す）
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
