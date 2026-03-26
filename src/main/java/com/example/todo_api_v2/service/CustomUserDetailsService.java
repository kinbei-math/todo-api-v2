package com.example.todo_api_v2.service;

import com.example.todo_api_v2.entity.User;
import com.example.todo_api_v2.mapper.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//ログイン時にDBに存在するか、passwordが合っているか、ロールは何か、を判断するためのクラス
@Service
public class CustomUserDetailsService implements UserDetailsService {

    //UserMapperをDI(コンストラクタ)
    private final UserMapper userMapper;
    public CustomUserDetailsService(UserMapper userMapper){this.userMapper=userMapper;}

    //UserをDBからemailで探して、Userを返すメソッド。UserDetailsServiceを用いてimplementsされるクラスは必ず実装するメソッド
    @Override
    public UserDetails loadUserByUsername(String email){
        User user = userMapper.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("Userが見つかりません"));

        // ★ 犯人探しのための罠（ログ出力）を仕掛けます！
        System.out.println("★★★ 取得したEmail: " + user.getEmail());
        System.out.println("★★★ 取得したパスワード: " + user.getPasswordHash());
        System.out.println("★★★ 取得したロール: " + user.getRole());

        return convertUserDetails(user);
    }

    //UserDetailsに詰め替え
    private UserDetails convertUserDetails(User user){
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }
}
