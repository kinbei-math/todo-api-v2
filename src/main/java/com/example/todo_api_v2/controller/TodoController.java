package com.example.todo_api_v2.controller;

import com.example.todo_api_v2.service.TodoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController//窓口を示すアノテーション
@RequestMapping("/todos")//URL共通のプレフィックス
public class TodoController {
    private final TodoService todoService;
    //recordを作成。recordの中は自然とprivate final
    public record CreateTodoRequest(String title){}
    public TodoController(TodoService todoService){//TodoServiceを受け取るコンストラクタ
        this.todoService=todoService;
    }

    @PostMapping // POST/todosを受け付ける
    public String createTodo(@RequestBody CreateTodoRequest request){
        return todoService.registerTodo(request.title());
    }

    @GetMapping//Todoの中身を確認する
    public ResponseEntity<List<String>> getTodos(){
        return ResponseEntity.ok(todoService.findAll());
    }
}
