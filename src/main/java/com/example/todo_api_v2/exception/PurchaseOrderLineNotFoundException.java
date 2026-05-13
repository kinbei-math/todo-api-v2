package com.example.todo_api_v2.exception;

import java.util.NoSuchElementException;

// 明細が見つからないときの例外
public class PurchaseOrderLineNotFoundException extends NoSuchElementException {
    public PurchaseOrderLineNotFoundException(String message) {
        super(message);
    }
}
