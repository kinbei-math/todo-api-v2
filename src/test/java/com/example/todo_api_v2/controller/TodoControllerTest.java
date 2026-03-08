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

    //get(idが存在しない)のテスト
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

    //put(idが存在しない)のテスト
    @Test
    void testPutTodo_NotFound_WhenIdDoesNotExist() throws Exception{

        String json = """
                {
                "title":"更新テスト",
                "dueDate":"2026-04-10",
                "isCompleted":true
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.put("/todos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Todoが見つかりません。"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    //delete(idが存在しない)のテスト
    @Test
    void testDeleteTodo_NotFound_WhenIdDoesNotExist() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.delete("/todos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Todoが見つかりません。"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    //post(titleが未入力)のテスト
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

    //post(空白のみtitle)のテスト
    @Test
    void testPostTodo_BadRequest_WhenTitleIsBlank() throws Exception{
        //Jsonを先にテキストブロックで指定しておく
        String json = """
                {
                "title":"　　　",
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

    //post(256文字以上)のテスト
    @Test
    void testPostTodo_BadRequest_WhenTitleIsTooLong() throws Exception{
        //256文字の文字列を先に生成
        String overSizeTitle = "a".repeat(256);

        //Jsonを先にテキストブロックで指定しておく
        String json = """
                {
                "title":"%s",
                "dueDate":"2026-03-01"
                }
                """.formatted(overSizeTitle);

        mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("title"))
                .andExpect(jsonPath("$.errors[0].message").value("titleが長すぎます"));
    }


}//テスト再実行用
