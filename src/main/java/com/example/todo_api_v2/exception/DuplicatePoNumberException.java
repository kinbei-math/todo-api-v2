package com.example.todo_api_v2.exception;

import org.springframework.dao.DuplicateKeyException;

/**
 * PurchaseOrderをinsertするとき、poNumberのUk制約で衝突したとき用の例外
 */
public class DuplicatePoNumberException extends DuplicateKeyException {
    public DuplicatePoNumberException(String message, Throwable cause) {
        super(message, cause);
    }
}
