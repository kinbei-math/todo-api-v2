package com.example.todo_api_v2.controller;

import com.example.todo_api_v2.dto.ErrorResponse;
import com.example.todo_api_v2.dto.TodoCreateRequest;
import com.example.todo_api_v2.dto.TodoResponse;
import com.example.todo_api_v2.dto.TodoUpdateRequest;
import com.example.todo_api_v2.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;


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

    @GetMapping("/{id}")//idに対してTodoを取り出す
    public ResponseEntity<?> getTodo(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(todoService.findById(id));
        } catch(NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404,"Todoが見つかりません"));
        }
    }

    @PutMapping("/{id}")//idに対してTodoを更新する
    public ResponseEntity<?> updateTodo(@RequestBody TodoUpdateRequest todoUpdateRequest, @PathVariable Long id){
        try{
            return ResponseEntity.status(HttpStatus.OK).body(todoService.updateTodo(todoUpdateRequest,id));
        }catch(NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404,"Todoが見つかりません"));
        }
    }

    @DeleteMapping("/{id}")//idに対してTodoを削除する
    public ResponseEntity<?> deleteTodo(@PathVariable Long id){
        try{
            todoService.deleteTodo(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }catch (NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404,"Todoが見つかりません"));
        }
    }
}
