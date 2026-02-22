package com.example.todo_api_v2.controller;

import com.example.todo_api_v2.dto.TodoCreateRequest;
import com.example.todo_api_v2.dto.TodoResponse;
import com.example.todo_api_v2.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController//窓口を示すアノテーション
@RequestMapping("/todos")//URL共通のプレフィックス
public class TodoController {
    private final TodoService todoService;
    public TodoController(TodoService todoService){//TodoServiceを受け取るコンストラクタ
        this.todoService=todoService;
    }

    @PostMapping // POST/todosを受け付ける
    public ResponseEntity<TodoResponse> createTodo(@RequestBody TodoCreateRequest todoCreateRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(todoService.createTodo(todoCreateRequest));
    }

    @GetMapping//Todoすべてのの中身を確認する
    public ResponseEntity<List<TodoResponse>> getTodos(){
        return ResponseEntity.status(HttpStatus.OK).body(todoService.findAll());
    }
}
