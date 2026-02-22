package com.example.todo_api_v2.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


import java.time.LocalDate;

@Entity
public class Todo {
    @GeneratedValue(strategy = GenerationType.IDENTITY)//idの自動採番　唯一性がある採番方法
    @Id//Idが主要なキー(検索をかける)であることを明示
    private Long id;  //idのフィールド
    private String title;  //タイトルのフィールド
    private LocalDate dueDate;  //日付のフィールド
    private Boolean isCompleted;  //完了状態を示すフィールド

    //空のコンストラクタ作成
    public Todo() {
    }

    //3種類変数に対してのGetterとSetter+Idのgetter
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
