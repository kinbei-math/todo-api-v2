package com.example.todo_api_v2.controller;

import com.example.todo_api_v2.dto.TodoCreateRequest;
import com.example.todo_api_v2.dto.TodoResponse;
import com.example.todo_api_v2.dto.TodoUpdateRequest;
import com.example.todo_api_v2.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
    public ResponseEntity<TodoResponse> createTodo(@Validated @RequestBody TodoCreateRequest todoCreateRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(todoService.createTodo(todoCreateRequest));
    }

    @GetMapping("/{id}")//idに対してTodoを取り出す
    //例外NoSuchElementExceptionを投げだすときはHandlerで操作。try-catchは必要ない。
    //ResponseEntityの中の型も分かりやすくなる。例外処理のときは別々のResponseを返す必要があったので、?にしていた。
    public ResponseEntity<TodoResponse> getTodo(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.findById(id));
    }

    @PutMapping("/{id}")//idに対してTodoを更新する
    //例外NoSuchElementExceptionを投げだすときはHandlerで操作。try-catchは必要ない。
    //ResponseEntityの中の型も分かりやすくなる。例外処理のときは別々のResponseを返す必要があったので、?にしていた。
    public ResponseEntity<TodoResponse> updateTodo(@Validated @RequestBody TodoUpdateRequest todoUpdateRequest,@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(todoService.updateTodo(todoUpdateRequest,id));
    }

    @DeleteMapping("/{id}")//idに対してTodoを削除する
    //例外NoSuchElementExceptionを投げだすときはHandlerで操作。try-catchは必要ない。
    //ResponseEntityの中の型も分かりやすくなる。例外処理のときは別々のResponseを返す必要があったので、?にしていた。bodyでは何も返さないのでVoid
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id){
        todoService.deleteTodo(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping
    public ResponseEntity<List<TodoResponse>> getTodos(@RequestParam(required=false) String keyword){
        //keywordがnull(入力なし)または""(空文字)の場合は全権取得
        if(keyword==null || keyword.isEmpty()){
            return ResponseEntity.status(HttpStatus.OK).body(todoService.findAll());
        }else{
            return  ResponseEntity.status(HttpStatus.OK).body(todoService.findByKeyword(keyword));
        }
    }
}
