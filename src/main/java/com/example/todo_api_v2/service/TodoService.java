package com.example.todo_api_v2.service;

import com.example.todo_api_v2.dto.TodoCreateRequest;
import com.example.todo_api_v2.dto.TodoResponse;
import com.example.todo_api_v2.dto.TodoUpdateRequest;
import com.example.todo_api_v2.entity.Todo;
import com.example.todo_api_v2.repository.TodoRepository;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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

    //保管庫にあるデータをfindById(Repository標準メソッド)で取り出す。(Entity型が入っている。)
    //orElseThrowでOptionalで取り出したデータが空の場合、NoSuchElementの例外を返す。
    //中身がある場合はTodoResponseに変換して返す。
    public TodoResponse findById(Long id){
        return todoRepository.findById(id)
                .map(todo->new TodoResponse(todo.getId(),todo.getTitle(),todo.getDueDate(),todo.getCompleted()))
                .orElseThrow(()-> new NoSuchElementException("Todoが見つかりません。"));

    }

    //保管庫にあるデータを探して置き換える
    //保管庫にないデータの場合は例外を投げる
    public TodoResponse updateTodo(TodoUpdateRequest todoUpdateRequest,Long id){
        Todo todo= todoRepository.findById(id).orElseThrow(()->new NoSuchElementException("Todoが見つかりません。"));
        todo.setTitle(todoUpdateRequest.title());
        todo.setDueDate(todoUpdateRequest.dueDate());
        todo.setCompleted(todoUpdateRequest.isCompleted());

        Todo saveTodo = todoRepository.save(todo);

        return new TodoResponse(saveTodo.getId(),saveTodo.getTitle(),saveTodo.getDueDate(),saveTodo.getCompleted());
    }

    //保管庫にあればデータを削除　返り値はなし
    //保管庫にない場合は例外を投げる
    public void deleteTodo(Long id){
        todoRepository.findById(id).orElseThrow(()->new NoSuchElementException("Todoが見つかりません。"));

        todoRepository.deleteById(id);
    }
}
