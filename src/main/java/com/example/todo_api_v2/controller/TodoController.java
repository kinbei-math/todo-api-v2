package com.example.todo_api_v2.controller;

import com.example.todo_api_v2.service.TodoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController//窓口を示すアノテーション
@RequestMapping("/todos")//URL共通のプレフィックス
public class TodoController {
    private final TodoService todoService;

    public TodoController(TodoService todoService){//TodoServiceを受け取るコンストラクタ
        this.todoService=todoService;
    }

    @PostMapping // POST/todosを受け付ける
    public String createTodo(String todo){
        return todoService.registerTodo(todo);
    }
}
