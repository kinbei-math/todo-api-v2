package com.example.todo_api_v2.service;

import com.example.todo_api_v2.dto.TodoCreateRequest;
import com.example.todo_api_v2.dto.TodoResponse;
import com.example.todo_api_v2.entity.Todo;
import com.example.todo_api_v2.mapper.TodoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.example.todo_api_v2.entity.TodoStatus.TODO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {

    @Mock //DBからの返答はmock
    TodoMapper todoMapper;

    @InjectMocks //テストするクラス
    TodoService todoService;

    private static final String NOT_FOUND_MESSAGE = "Todoが見つかりません。";

    //findByIdの正常系と異常系を1本ずつ
    @Test
    @DisplayName("存在するIDで検索した場合、該当のTodo情報が取得できること")
    void testFindById_shouldReturnTodoResponse_whenIdExists(){

        //todoMapperの動作を定義
        when(todoMapper.findById(1L)).thenReturn(createTestTodo(1L,"test",LocalDate.parse("2026-04-01")));

        //テストレイヤーの実行結果を確認する
        TodoResponse todoResponse = todoService.findById(1L);
        assertThat(todoResponse.id()).isEqualTo(1L);
        assertThat(todoResponse.title()).isEqualTo("test");
        assertThat(todoResponse.dueDate()).isEqualTo(LocalDate.parse("2026-04-01"));
        assertThat(todoResponse.todoStatus()).isEqualTo(TODO);
        assertThat(todoResponse.completedAt()).isNull();
    }

    @Test
    @DisplayName("存在しないIDで検索した場合、Excepetionを投げること")
    void testFindById_shouldThrowException_whenIdNotExists(){
        //todoMapperの動作を定義
        when(todoMapper.findById(999L)).thenReturn(Optional.empty());

        //テストレイヤーの実行結果を確認する
        Exception exception = assertThrows(NoSuchElementException.class,
                ()->todoService.findById(999L));

        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_MESSAGE);

    }

    //createTodoの正常系。異常系はなし(Serviceにガード節がないため)
    @Test
    @DisplayName("todoCreateRquestを渡したとき、登録したTodoの情報が返ってくる。また、Insertが実行されたかを確認すること")
    void testCreateTodo_shouldReturnTodoResponse_whenTodoCreateGiven(){
        TodoCreateRequest todoCreateRequest =new TodoCreateRequest("test",LocalDate.parse("2026-04-01"));
        TodoResponse todoResponse = todoService.createTodo(todoCreateRequest);

        //createTodoが行われたときにinsert(DBへの保存)が行われているかを確認する
        //Todoの中身は移し替えたものをメソッド内で作るので、Todoクラスが渡されていれば良とする
        verify(todoMapper,times(1)).insert(any(Todo.class));

        //DBの自動採番はされないので、idはnullであることで良とする
        assertThat(todoResponse.id()).isNull();

        assertThat(todoResponse.title()).isEqualTo("test");
        assertThat(todoResponse.dueDate()).isEqualTo(LocalDate.parse("2026-04-01"));
        assertThat(todoResponse.todoStatus()).isEqualTo(TODO);
        assertThat(todoResponse.completedAt()).isNull();
    }

    //テスト用のOptional<todo>作成メソッド
    private Optional<Todo> createTestTodo(Long id, String title, LocalDate dueDate){
        Todo todo = new Todo();
        todo.setId(id);
        todo.setTitle(title);
        todo.setDueDate(dueDate);
        return Optional.of(todo);
    }
}
