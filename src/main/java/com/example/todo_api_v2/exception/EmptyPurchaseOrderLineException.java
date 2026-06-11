package com.example.todo_api_v2.exception;

public class EmptyPurchaseOrderLineException extends IllegalStateException {
    public EmptyPurchaseOrderLineException(String message) {
        super(message);
    }
}
