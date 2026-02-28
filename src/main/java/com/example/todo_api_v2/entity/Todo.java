package com.example.todo_api_v2.entity;
import java.time.LocalDate;

public class Todo {
    private Long id;  //idのフィールド
    private String title;  //タイトルのフィールド
    private LocalDate dueDate;  //日付のフィールド
    private Boolean isCompleted;  //完了状態を示すフィールド

    //空のコンストラクタ作成
    public Todo() {
    }

    //4種類変数に対してのGetterとSetter
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

    public Boolean getCompleted() {
        return isCompleted;
    }

    public void setCompleted(Boolean completed) {
        isCompleted = completed;
    }
}
