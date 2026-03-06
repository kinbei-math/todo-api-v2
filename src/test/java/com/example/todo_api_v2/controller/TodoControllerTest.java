package com.example.todo_api_v2.controller;

import com.example.todo_api_v2.dto.ValidationError;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest //ControllerからDBまでテスト用に準備する
@AutoConfigureMockMvc //Mockを自動で作成
public class TodoControllerTest {

    @Autowired //Springが作ったMockを以下の変数に自動注入
    private MockMvc mockMvc;//ブラウザ、Postmanの代わりにHTTPリクエストを送るMock

    @Test
    void testGetTodo_NotFound_WhenIdDoesNotExist() throws Exception {

        //mockに対して今から指示するリクエストを実行(perform)させる。 mockMvc.prtform(～～)
        //実行するリクエストを選択 MockMvcRequestBuliders.～～
        //Expect(期待)する結果を出力　andExpect(～～)
        //bodyの中身を1つずつ確認する必要あり。
        mockMvc.perform(MockMvcRequestBuilders.get("/todos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Todoが見つかりません。"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void testCreateTodo_BadRequest_WhenTitleIsEmpty() throws Exception {

        //Jsonを先にテキストブロックで指定しておく
        String json = """
                {
                "title":"",
                "dueDate":"2026-03-01"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("title"))
                .andExpect(jsonPath("$.errors[0].message").value("タイトルを入力してください"));
    }
}
