package com.example.todo_api_v2.mapper;

import com.example.todo_api_v2.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

//Entity　User用のMapper(SQL言語とJavaの接続)
@Mapper
public interface UserMapper {

    //テーブルにあるemailでユーザーを1件取得する
    @Select("SELECT * FROM users WHERE email=#{email}")
    Optional<User> findByEmail(String email);
}
