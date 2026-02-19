package com.example.todo_api_v2.controller;

import com.example.todo_api_v2.service.TodoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TodoControllerTest {

    @Mock//代役を務める　仮のもの
    TodoService todoService;

    @InjectMocks//仮のものを注入される。テストしたいクラス
    TodoController todoController;

    @Test
    void createTodo_shouldReturnMessage_whenTitleIsGiven(){
        TodoController.CreateTodoRequest request = new TodoController.CreateTodoRequest("買い物");
        //Mockの動きを定義する、title:買い物を登録したら買い物を登録しましたと返す。
        //本来のServiceと同じような動きを設定する。
        when(todoService.registerTodo("買い物")).thenReturn("買い物を登録しました");

        //実際に実行する。resultに買い物を登録しましたがinput
        String result = todoController.createTodo(request);

        //結果が等しいかを確認する。
        assertEquals("買い物を登録しました", result);
    }
}
