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
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

@Configuration//設定ファイルすべてにつける
@EnableWebSecurity//WebSecurityを有効にする
public class SecurityConfig {

    //Securityマニュアルの詳細(本番環境用)
    @Bean//Springが提供するものを使用
    @Order(2)
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

    @Bean
    @Profile("dev") // dev環境でのみ有効
    @Order(1)       // 優先順位を既存の設定より上にあげる
    public SecurityFilterChain h2ConsoleFilterChain(HttpSecurity http){
        http
                .securityMatcher("/h2-console/**")                                                         // 担当URL（h2-console配下のみ）
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())        // 担当URL内は全許可
                .csrf(csrf -> csrf.disable())                                             // H2コンソールはCSRF無効
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))  // iframe同一オリジン許可
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    //パスワードをハッシュ化(暗号化)
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
