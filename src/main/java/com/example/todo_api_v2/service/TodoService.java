package com.example.todo_api_v2.service;

import org.springframework.stereotype.Service;

@Service
public class TodoService {
    public String registerTodo(String title){
        return title+"を登録しました";
    }
}
