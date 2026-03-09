package com.example.todo_api_v2.entity;
import com.example.todo_api_v2.exception.InvalidStatusTransitionException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.todo_api_v2.entity.TodoStatus.*;

public class Todo {
    private Long id;  //idのフィールド
    private String title;  //タイトルのフィールド
    private LocalDate dueDate;  //日付のフィールド
    private TodoStatus todoStatus;  //3状態を示すフィールド（状態判定メソッドでのみ操作可能）
    private LocalDateTime completedAt; //完了日時を示すフィールド（完了時の日時を自動取得、差し戻し時に削除）

    //空のコンストラクタ作成
    public Todo() {
        this.todoStatus=TODO;
    }

    //3種類変数に対してのGetterとSetter
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }


    public TodoStatus getTodoStatus() {
        return todoStatus;
    }

    //状態変遷を判定して置き換えるメソッド（Week9設計判断通り）
    public void changeStatus(TodoStatus nextStatus){
        switch (this.todoStatus){
            case TODO -> {
                if (nextStatus == DOING) { //TODO→DOING
                    this.todoStatus = nextStatus;
                } else {
                    throw new InvalidStatusTransitionException("許可されていない状態遷移です");
                }
            }
            case DOING-> {
                if(nextStatus == TODO){ //DOING→TODO
                    this.todoStatus=nextStatus;
                } else if (nextStatus == DONE) { //DOING→DONE
                    this.todoStatus=nextStatus;
                    this.completedAt=LocalDateTime.now();
                }else{
                    throw new InvalidStatusTransitionException("許可されていない状態遷移です");
                }
            }
            case DONE->{
                if(nextStatus==DOING){//DONE→DOING
                    this.todoStatus=nextStatus;
                    this.completedAt=null;
                }else{
                    throw new InvalidStatusTransitionException("許可されていない状態遷移です");
                }
            }
        }
    }

    //完了日時を取得 nullの可能性があるのでOptionalで包む
    public Optional<LocalDateTime> getCompletedAt(){
        return Optional.ofNullable(this.completedAt);
    }
}
