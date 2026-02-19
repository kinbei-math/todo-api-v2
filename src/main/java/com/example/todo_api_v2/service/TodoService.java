package com.example.todo_api_v2.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TodoService {
    private final List<String> todoList = List.of("牛乳を買う","洗濯をする");

    public List<String> findAll(){
        return todoList;
    }

    public String registerTodo(String title){
        return title+"を登録しました";
    }


}
