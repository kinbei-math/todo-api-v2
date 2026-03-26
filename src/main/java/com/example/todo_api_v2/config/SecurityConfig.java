package com.example.todo_api_v2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration//設定ファイルすべてにつける
@EnableWebSecurity//WebSecurityを有効にする
public class SecurityConfig {

    //Securityマニュアルの詳細
    @Bean//Springが提供するものを使用
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        //csrfを無効化
        http.csrf(csrf -> csrf.disable());

        //DELETEのみADMINだけに認可。
        //残りのメソッドはすべてログイン済みユーザーに認可。
        http.authorizeHttpRequests(authz ->
                authz.requestMatchers(HttpMethod.DELETE,"/todos/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated()
        );

        //Basic認証の有効化
        http.httpBasic(Customizer.withDefaults());

        //詳細を記載(完成)したマニュアルを返す
        return http.build();
    }

    //パスワードをハッシュ化(暗号化)
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
