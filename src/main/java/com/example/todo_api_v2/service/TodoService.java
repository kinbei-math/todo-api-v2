package com.example.todo_api_v2.service;

import com.example.todo_api_v2.dto.TodoCreateRequest;
import com.example.todo_api_v2.dto.TodoResponse;
import com.example.todo_api_v2.entity.Todo;
import com.example.todo_api_v2.repository.TodoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TodoService {
    private final TodoRepository todoRepository;

    //TodoRepositoryを受け取るコンストラクタ
    public TodoService(TodoRepository todoRepository){
        this.todoRepository=todoRepository;
    }

    //TodoをRepositoryに渡す。
    //todoをControllerから受け取る。レコードの型で(TodoCreateRequest)
    //受け取ったレコードの型からTodoというEntityの箱に詰める
    public TodoResponse createTodo(TodoCreateRequest todoCreateRequest){
        //保存用のTodo(Entity)の箱を用意
        Todo todo = new Todo();

        //Entityにデータを詰める
        todo.setTitle(todoCreateRequest.title());
        todo.setDueDate(todoCreateRequest.dueDate());
        todo.setCompleted(false);

        //データを詰めた箱を保管庫へ保存
        Todo saveTodo = todoRepository.save(todo);

        //出力用のrecordを返信
        return new TodoResponse(saveTodo.getId(),saveTodo.getTitle(),saveTodo.getDueDate(),saveTodo.getCompleted());
    }

    //保管庫にあるデータ(TodoというEntity)を取り出す。findAll
    //取り出した箱にあるデータをTodoResponseの形に整形して出力する。
    //整形方法はStreamで
    public List<TodoResponse> findAll(){
        //Todo(Entity型)のリストをStreamに並べて、Todoたちをベルトコンベアに載せる。
        return todoRepository.findAll().stream()
                .map(todo -> new TodoResponse(todo.getId(),todo.getTitle(),todo.getDueDate(),todo.getCompleted()))
                .toList();
    }
}
