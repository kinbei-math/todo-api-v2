package com.example.todo_api_v2.exception;

import java.util.NoSuchElementException;

// Itemが見つからない。NoSuchElementExceptionでハンドリング
public class ItemNotFoundException extends NoSuchElementException {
    public ItemNotFoundException(String message) {
        super(message);
    }
}
