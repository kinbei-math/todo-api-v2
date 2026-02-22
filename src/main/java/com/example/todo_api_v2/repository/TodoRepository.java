package com.example.todo_api_v2.repository;

import com.example.todo_api_v2.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

//JpaRepositoryが持っていた機能を継承。TodoというデータとLongで保存
public interface TodoRepository extends JpaRepository<Todo, Long>{
}
