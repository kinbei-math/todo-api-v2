package com.example.todo_api_v2;

import com.example.todo_api_v2.entity.User;
import com.example.todo_api_v2.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class FlywayMigrationTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("Flywayによるv2の初期データが投入されている")
    void testFindByEmail_shouldReturnInitialUser_whenFlywayV2Executed(){
        //user用の初期email
        String testemail = "user@example.com";

        //初期データの存在から確認
        Optional<User> user = userMapper.findByEmail(testemail);
        assertThat(user).isPresent();
        User testUser = user.get();
    }
}
