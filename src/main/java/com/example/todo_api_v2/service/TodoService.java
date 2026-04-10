package com.example.todo_api_v2.service;

import com.example.todo_api_v2.dto.*;
import com.example.todo_api_v2.entity.Todo;
import com.example.todo_api_v2.mapper.TodoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j//ログメッセージを出力する。Lombok
public class TodoService {

    //TodoMapperをDIする
    private final TodoMapper todoMapper;
    //TodoMapperを受け取るコンストラクタ
    public TodoService(TodoMapper todoMapper){
        this.todoMapper=todoMapper;
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

        //データを詰めた箱を保管庫へ保存
        todoMapper.insert(todo);

        log.info("Todo created [UserID: {},TodoId: {}, title: {} ]",getCurrentUsername(),todo.getId(),todo.getTitle());//日時は自動でログに記載。失敗の場合はこのメソッドは出ないので成功判定も不要

        //出力用のrecordを返信
        return convertTodoResponse(todo);
    }

    //保管庫にあるデータ(TodoというEntity)を取り出す。findAll
    //取り出した箱にあるデータをTodoResponseの形に整形して出力する。
    //整形方法はStreamで
    public List<TodoResponse> findAll(){
        //Todo(Entity型)のリストをStreamに並べて、Todoたちをベルトコンベアに載せる。
        return todoMapper.findAll().stream()
                .map(this::convertTodoResponse)
                .toList();
    }

    //保管庫にあるデータをfindById(Repository標準メソッド)で取り出す。(Entity型が入っている。)
    //orElseThrowでOptionalで取り出したデータが空の場合、NoSuchElementの例外を返す。
    //中身がある場合はTodoResponseに変換して返す。
    public TodoResponse findById(Long id){
        return todoMapper.findById(id)
                .map(this::convertTodoResponse)
                .orElseThrow(()-> new NoSuchElementException("Todoが見つかりません。"));

    }

    //保管庫にあるデータを探して置き換える
    //保管庫にないデータの場合は例外を投げる
    public TodoResponse updateTodo(TodoUpdateRequest todoUpdateRequest,Long id){
        Todo todo= todoMapper.findById(id).orElseThrow(()->new NoSuchElementException("Todoが見つかりません。"));
        todo.setTitle(todoUpdateRequest.title());
        todo.setDueDate(todoUpdateRequest.dueDate());

        todoMapper.update(todo);

        log.info("Todo updated [UserID: {},TodoId: {}, title: {} ]",getCurrentUsername(),todo.getId(),todo.getTitle());

        return convertTodoResponse(todo);
    }

    //保管庫にあるデータのstatusを変化させる
    //保管庫にない場合は例外を投げる
    public TodoResponse changeTodoStatus(TodoStatusUpdateRequest todoStatusUpdateRequest,Long id){
        Todo todo= todoMapper.findById(id).orElseThrow(()->new NoSuchElementException("Todoが見つかりません。"));
        todo.changeStatus(todoStatusUpdateRequest.nextStatus());

        todoMapper.updateStatus(todo);

        log.info("Todo changed status [UserID: {},TodoId: {},TodoStatus: {}]",getCurrentUsername(),todo.getId(),todo.getTodoStatus());

        return convertTodoResponse(todo);
    }

    //statusの一括変更メソッド
    //検査例外(IOException　ファイル読み込みエラー)もロールバックする
    @Transactional(rollbackFor = Exception.class)
    public List<TodoResponse> bulkChangeTodoStatus(TodoBulkStatusUpdateRequest todoBulkStatusUpdateRequest){
        List<TodoResponse> todoResponsesList =
                todoBulkStatusUpdateRequest.ids().stream()
                .map(id ->{
                    //idが実際に保管庫にあることを確認する
                    Todo todo= todoMapper.findById(id).orElseThrow(()->new NoSuchElementException("Todoが見つかりません。"));

                    //このtodoに対して状態遷移が適切であるかを確認する
                    todo.changeStatus(todoBulkStatusUpdateRequest.nextStatus());

                    //Mapperクラスで保管庫の中を置き換える
                    todoMapper.updateStatus(todo);

                    //TodoReseponseクラスにうつしかえ
                    return convertTodoResponse(todo);
                })
                //TodoReponseをリスト化
                .toList();

        //transactionalのため、全処理が完全に終了してからログを出す。
        log.info("Todo changed status [UserID: {},number of changed TodoStatus: {}]",getCurrentUsername(),todoResponsesList.size());

        return todoResponsesList;
    }

    //保管庫にあればデータを削除　返り値はなし
    //保管庫にない場合は例外を投げる
    public void deleteTodo(Long id){
        todoMapper.findById(id).orElseThrow(()->new NoSuchElementException("Todoが見つかりません。"));
        todoMapper.delete(id);

        log.info("Todo deleted [UserID: {},TodoId: {} ]",getCurrentUsername(),id);
    }

    //titleで部分一致するものを検索する
    //検索したものがList<Todo>で返ってくるので、それをtodoResponseに詰めなおして、リストにして出力
    public List<TodoResponse> findByKeyword(String keyword){
        //Todo(Entity型)のリストをStreamに並べて、Todoたちをベルトコンベアに載せる。
        return todoMapper.findByKeyword(keyword).stream()
                .map(this::convertTodoResponse)
                .toList();
    }

    //TodoResponseに詰めなおすメソッド
    //毎回変数の中で詰めなおす作業を減らせる
    private TodoResponse convertTodoResponse(Todo todo){
        return new TodoResponse(todo.getId(),todo.getTitle(),todo.getDueDate(),todo.getTodoStatus(),todo.getCompletedAt());
    }

    //認証情報のUserIdを返すメソッド
    private String getCurrentUsername(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();//先に認証情報を拾う。
        //本来ここまで未認証はきませんが、ciで通すためにnullチェックを入れました。
        return  (auth != null) ? auth.getName() : "system";
    }
}
