package com.example.todo_api_v2.entity;


import com.example.todo_api_v2.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TodoTest {

    private Todo todo;

    @BeforeEach //Todoインスタンスはすべてのテストで使うの先に準備しておく。
    void setUp(){
        todo = new Todo();
    }

    @Test //Todo→Doingの遷移テスト
    void changeStatus_Succeed_WhenTodoToDoing(){
        todo.changeStatus(TodoStatus.DOING);
        assertThat(todo.getTodoStatus()).isEqualTo(TodoStatus.DOING);
    }

    @Test //Todo→Doneが失敗
    void changeStatus_InvalidStatusTransitionException_WhenTodoToDone(){
        assertThrows(InvalidStatusTransitionException.class,
                ()-> todo.changeStatus(TodoStatus.DONE));
    }

    @Test //Doing→Doneが成功。completedAtが設定される。
    void changeStatus_SucceedAndCompletedAtIsNotNull_WhenDoingToDone(){
        todo.changeStatus(TodoStatus.DOING);
        todo.changeStatus(TodoStatus.DONE);

        assertThat(todo.getTodoStatus()).isEqualTo(TodoStatus.DONE);
        assertThat(todo.getCompletedAt()).isNotNull();
    }

    @Test //Done→Doingが成功。completedAtがNullに戻る。
    void changeStatus_SucceedAndCompletedAtIsNull_WhenDoneToDoing(){
        todo.changeStatus(TodoStatus.DOING);
        todo.changeStatus(TodoStatus.DONE);
        todo.changeStatus(TodoStatus.DOING);

        assertThat(todo.getTodoStatus()).isEqualTo(TodoStatus.DOING);
        assertThat(todo.getCompletedAt()).isNull();
    }

}
