package com.example.todo_api_v2.mapper;

import com.example.todo_api_v2.entity.Todo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

//JPAのRepositoryのようなもの
//指示(SQL)に対して、どんな結果(java)を返すというのを先に決める
@Mapper
public interface TodoMapper {
    //Java findAll()を使うと下のSQLを使用
    @Select("SELECT * FROM todos")
    List<Todo> findAll();

    //IDは自動採番
    //#{title}でgetTitle()を自動で使用
    //Java insert(Todo todo)を使うと下のSQLを使用
    @Insert("INSERT INTO todos(title,due_date,is_completed) VALUES (#{title},#{dueDate},#{isCompleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Todo todo);

    //Java findById(Long id)を使うと下のSQLを使用
    @Select("SELECT * FROM todos WHERE id=#{id}")
    Optional<Todo> findById(Long id);

    //Java update(Todo todo)を使うと下のSQLを使用
    @Update("UPDATE todos SET title=#{title},due_date=#{dueDate},is_completed=#{isCompleted} WHERE id=#{id}")
    void update(Todo todo);

    //Java delete(Long id)を使うと下のSQLを使用
    @Delete("DELETE FROM todos WHERE id=#{id}")
    void delete(Long id);
}
