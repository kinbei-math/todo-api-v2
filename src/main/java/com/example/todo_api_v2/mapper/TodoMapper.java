package com.example.todo_api_v2.mapper;

import com.example.todo_api_v2.entity.Todo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

//JPAのRepositoryのようなもの
//指示(SQL)に対して、どんな結果(java)を返すというのを先に決める
@Mapper
public interface TodoMapper {
    //テーブルにあるtodoをすべて返す。
    @Select("SELECT * FROM todos")
    List<Todo> findAll();

    //IDは自動採番
    //#{title}でgetTitle()を自動で使用
    //todoを与えて新規追加
    @Insert("INSERT INTO todos(title,due_date,is_completed) VALUES (#{title},#{dueDate},#{isCompleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")//DBで自動採番されたidを保管
    void insert(Todo todo);

    //idを参照してTodoを検索
    @Select("SELECT * FROM todos WHERE id=#{id}")
    Optional<Todo> findById(Long id);

    //idを参照してtodo更新
    @Update("UPDATE todos SET title=#{title},due_date=#{dueDate},is_completed=#{isCompleted} WHERE id=#{id}")
    void update(Todo todo);

    //Id指定でのtodo削除
    @Delete("DELETE FROM todos WHERE id=#{id}")
    void delete(Long id);

    //キーワードでのタイトル部分一致検索
    @Select("SELECT * FROM todos WHERE title LIKE CONCAT('%',#{keyword},'%')")//'%#{keyword}%' = '% 'keyword' %'を防ぐ
    List<Todo> findByKeyword(String keyword);
}
