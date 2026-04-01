package com.example.todo_api_v2.service;

import com.example.todo_api_v2.dto.TodoCreateRequest;
import com.example.todo_api_v2.dto.TodoResponse;
import com.example.todo_api_v2.dto.TodoStatusUpdateRequest;
import com.example.todo_api_v2.dto.TodoUpdateRequest;
import com.example.todo_api_v2.entity.Todo;
import com.example.todo_api_v2.exception.InvalidStatusTransitionException;
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

import static com.example.todo_api_v2.entity.TodoStatus.*;
import static org.assertj.core.api.Assertions.as;
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

    //upadateTodoの正常系・異常系
    @Test
    @DisplayName("todoUpdateRequestとidを渡したときに、更新されたTodoResponseが返ってくる。また、updateが実行されているかを確認する")
    void testUpdateTodo_shouldReturnTodoResponse_whenTodoUpdateGiven(){
        //todoMapperに動作を定義
        when(todoMapper.findById(1L)).thenReturn(createTestTodo(1L,"test",LocalDate.parse("2026-04-01")));

        //UpdateRequestを定めて、updateTodoを実行
        TodoUpdateRequest todoUpdateRequest = new TodoUpdateRequest("test2",LocalDate.parse("2026-05-01"));
        TodoResponse todoResponse = todoService.updateTodo(todoUpdateRequest,1L);

        //todoMapperのupdateの実行確認
        verify(todoMapper,times(1)).update(any(Todo.class));

        //更新がなされているかを中身で確認
        assertThat(todoResponse.id()).isEqualTo(1L);
        assertThat(todoResponse.title()).isEqualTo("test2");
        assertThat(todoResponse.dueDate()).isEqualTo(LocalDate.parse("2026-05-01"));
        assertThat(todoResponse.todoStatus()).isEqualTo(TODO);
        assertThat(todoResponse.completedAt()).isNull();
    }
    @Test
    @DisplayName("存在しないidを渡したときに、NoSuchElementExcepptionをを投げること")
    void testUpdateTodo_shouldThrowException_whenIdNotExists(){
        //存在しないIdの場合、Optional.empty()を返す
        when(todoMapper.findById(999L)).thenReturn(Optional.empty());

        //updateRequesetとIdを渡して、Exceptionを受け取る
        TodoUpdateRequest todoUpdateRequest = new TodoUpdateRequest("test2",LocalDate.parse("2026-05-01"));
        Exception exception = assertThrows(NoSuchElementException.class,
                ()->todoService.updateTodo(todoUpdateRequest,999L));
        //受け取ったExceptionのメッセージが正しいことを確認する。
        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_MESSAGE);
    }

    //changeTodoStatusの正常系1本と異常系2本(Todo::changeStatusはテスト済みで問題ない前提)
    @Test
    @DisplayName("正しい遷移先を渡すとき(TODO→DOING)、更新されたTodoResponseが返ってくること")
    void testChangeTodoStatus_shouldReturnTodoResponse_whenTodoStatusDoingGiven(){
        TodoStatusUpdateRequest todoStatusUpdateRequest = new TodoStatusUpdateRequest(DOING);
        //todoMapperの動作を定義
        when(todoMapper.findById(1L)).thenReturn(createTestTodo(1L,"test",LocalDate.parse("2026-04-01")));

        //changeTodoStatusを実行
        TodoResponse todoResponse = todoService.changeTodoStatus(todoStatusUpdateRequest,1L);
        //updateStatusが実行されたかの確認
        verify(todoMapper,times(1)).updateStatus(any(Todo.class));

        assertThat(todoResponse.id()).isEqualTo(1L);
        assertThat(todoResponse.title()).isEqualTo("test");
        assertThat(todoResponse.dueDate()).isEqualTo(LocalDate.parse("2026-04-01"));
        assertThat(todoResponse.todoStatus()).isEqualTo(DOING);
        assertThat(todoResponse.completedAt()).isNull();
    }
    @Test
    @DisplayName("存在しないIdを渡したときに、Exceptionを投げること")
    void testChangeTodoStatus_shouldThrowNoSuchElementException_whenIdNotExists(){
        //存在しないIdの場合、Optional.empty()を返す
        when(todoMapper.findById(999L)).thenReturn(Optional.empty());

        //todoStatusUpdateRequestとIdを渡して、Exceptionを受け取る
        TodoStatusUpdateRequest todoStatusUpdateRequest =new TodoStatusUpdateRequest(DONE);
        Exception exception = assertThrows(NoSuchElementException.class,
                ()->todoService.changeTodoStatus(todoStatusUpdateRequest,999L));
        //受け取ったExceptionのメッセージが正しいことを確認する。
        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_MESSAGE);
    }
    @Test
    @DisplayName("不正な遷移先を渡すとき(TODO→DONE)、Exceptionを投げること")
    void testChangeTodoStatus_shouldThrowInvalidStatusTransitionException_whenTodoStatusDoneGiven(){
        TodoStatusUpdateRequest todoStatusUpdateRequest = new TodoStatusUpdateRequest(DONE);
        //todoMapperの動作を定義
        when(todoMapper.findById(1L)).thenReturn(createTestTodo(1L,"test",LocalDate.parse("2026-04-01")));

        //changeTodoStatusを実行して、Exceptionを受け取る。
        Exception exception = assertThrows(InvalidStatusTransitionException.class,
                ()-> todoService.changeTodoStatus(todoStatusUpdateRequest,1L));
        assertThat(exception.getMessage()).isEqualTo("許可されていない状態遷移です");
    }

    //deleteTodoの正常系と異常系
    @Test
    @DisplayName("存在するIdを渡したときに、deleteTodoが実行され、Todoが消去される")
    void testDeleteTodo_shouldDeleteTodo_whenIdExists(){
        //findById
        when(todoMapper.findById(1L)).thenReturn(createTestTodo(1L,"test",LocalDate.parse("2026-04-01")));

        //deleteTodoを実行
        todoService.deleteTodo(1L);
        //todoMapper.deleteを実行されたのを確認
        verify(todoMapper,times(1)).delete(any(Long.class));
    }
    @Test
    @DisplayName("存在しないIdを渡したときに、Exceptionを投げること")
    void testDeleteTodo_shouldThrowException_WhenIdNotExists(){
        //存在しないIdの場合、Optional.empty()を返す
        when(todoMapper.findById(999L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(NoSuchElementException.class,
                ()->todoService.deleteTodo(999L));
        //受け取ったExceptionのメッセージが正しいことを確認する。
        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_MESSAGE);
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
