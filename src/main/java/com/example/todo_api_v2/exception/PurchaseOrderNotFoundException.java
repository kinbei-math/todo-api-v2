package com.example.todo_api_v2.exception;

import java.util.NoSuchElementException;

// 注文が見つからないときの例外
public class PurchaseOrderNotFoundException extends NoSuchElementException {
    public PurchaseOrderNotFoundException(String message) {
        super(message);
    }
}
