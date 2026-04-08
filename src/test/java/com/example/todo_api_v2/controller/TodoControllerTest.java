package com.example.todo_api_v2.controller;

import com.example.todo_api_v2.dto.TodoResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest //ControllerからDBまでテスト用に準備する
public class TodoControllerTest {
    //mockは注入せずアプリ全体の箱(context)を注入
    @Autowired
    private WebApplicationContext context;

    //mockの設定は下でやる。自動注入×
    private MockMvc mockMvc;

    //すべてのテストの前にmockを作成
    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity()) //SecurityChainを注入
                .build();
    }

    @Autowired
    private ObjectMapper objectMapper;//綺麗にデータを詰め替えることが出来るクラス

    @Autowired
    private JdbcTemplate jdbcTemplate;//テストクラスの中でSQLを直接実行できるツール

    // どのテストが終わった後も呼ばれるメソッド
    @AfterEach
    void tearDown() {
        // todosテーブルのデータを全件削除して、DBをまっさらな状態に戻す
        jdbcTemplate.execute("DELETE FROM todos");

        //DBがH2やMySQLで、IDの連番(AUTO_INCREMENT)も「1」にリセットしたい場合
        jdbcTemplate.execute("ALTER TABLE todos ALTER COLUMN id RESTART WITH 1");
    }

    //get(idが存在しない)のテスト
    @Test
    @WithMockUser(roles = "USER")
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
    @WithMockUser(roles = "USER")
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
    @WithMockUser(roles = "ADMIN")
    void testDeleteTodo_NotFound_WhenIdDoesNotExist() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.delete("/todos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Todoが見つかりません。"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    //post(titleが未入力)のテスト
    @Test
    @WithMockUser(roles = "USER")
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
    @WithMockUser(roles = "USER")
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
    @WithMockUser(roles = "USER")
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

    //findAllの正常系
    @Test
    @WithMockUser(roles="USER")
    void testFindAll_shouldReturnTodoList_whenCalled() throws Exception{
        //test用にDBに2件登録
        TodoResponse test1 = createTodoForTest("test1","2027-04-01");
        TodoResponse test2 = createTodoForTest("test2","2027-04-02");

        //get/todosの場合、全件取得でListで返す。
        mockMvc.perform(MockMvcRequestBuilders.get("/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("test1"))
                .andExpect(jsonPath("$[1].title").value("test2"))
                .andExpect(jsonPath("$[0].dueDate").value("2027-04-01"))
                .andExpect(jsonPath("$[1].dueDate").value("2027-04-02"));
    }


    //findByKeywordのKeyWord有り無し1本ずつ
    @Test
    @WithMockUser(roles="USER")
    void testFindByKeyword_shouldReturn200AndTodoResponse_whenKeywordExists() throws Exception{
        //test用にDBに4件登録
        TodoResponse test1 = createTodoForTest("test1","2027-04-01");
        TodoResponse test2 = createTodoForTest("test2","2027-04-02");
        TodoResponse test3 = createTodoForTest("test1-1","2027-05-01");
        TodoResponse test4 = createTodoForTest("test2-2","2027-05-02");

        //get/todos?keywordで検索してListを返す。
        mockMvc.perform(MockMvcRequestBuilders.get("/todos?keyword=test1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("test1"))
                .andExpect(jsonPath("$[1].title").value("test1-1"))
                .andExpect(jsonPath("$[0].dueDate").value("2027-04-01"))
                .andExpect(jsonPath("$[1].dueDate").value("2027-05-01"));
    }

    @Test
    @WithMockUser(roles="USER")
    void testFindByKeyword_shouldReturn200AndEmptyList_whenKeywordNotExists() throws Exception{
        //test用にDBに2件登録
        TodoResponse test1 = createTodoForTest("test1","2027-04-01");
        TodoResponse test2 = createTodoForTest("test2","2027-04-02");

        //get/todos?keywordでEmptyListが返ること確かめる
        mockMvc.perform(MockMvcRequestBuilders.get("/todos?keyword=todo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    //patch doingへの正常遷移テスト　統合ver
    @Test
    @WithMockUser(roles = "USER")
    @Transactional//テストの後にDBを空にする。
    void testPatchTodo_Return200_WhenNextStatusDoing() throws Exception {
        TodoResponse createTodoResponse = createTodoForTest("test","2026-04-01");

        String patchJson="""
                {
                "nextStatus":"DOING"
                }
                """;

        //patchのテストが通るかを確認
        mockMvc.perform(MockMvcRequestBuilders.patch("/todos/"+createTodoResponse.id()+"/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todoStatus").value("DOING"));
    }

    //patch todo→doneへの不正遷移テスト　統合Ver baseは上の正常遷移テストと同じ
    @Test
    @WithMockUser(roles = "USER")
    @Transactional//テストの後にDBを空にする
    void testPatchTodo_Return409_WhenNextStatusDone() throws Exception{
        TodoResponse createTodoResponse = createTodoForTest("test","2026-04-01");

        String patchJson="""
                {
                "nextStatus":"DONE"
                }
                """;

        //patchのテストが通るかを確認
        mockMvc.perform(MockMvcRequestBuilders.patch("/todos/"+createTodoResponse.id()+"/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.message").value("許可されていない状態遷移です"));
    }

    //patch 一括変更　正常遷移　統合テスト
    @Test
    @WithMockUser(roles = "USER")
    @Transactional
    void testPatchTodo_Return200_WhenBulkNextStatusDoing()throws Exception{
        //post2件
        TodoResponse createTodoResponse1 = createTodoForTest("test1","2026-04-01");
        TodoResponse createTodoResponse2 = createTodoForTest("test2","2026-05-01");


        String patchJson="""
                {
                "ids":[%d,%d],
                "nextStatus":"DOING"
                }
                """.formatted(createTodoResponse1.id(),createTodoResponse2.id());

        //patchのテストが通るかを確認
        mockMvc.perform(MockMvcRequestBuilders.patch("/todos/bulk-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].todoStatus").value("DOING"))
                .andExpect(jsonPath("$[1].todoStatus").value("DOING"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testPatchTodo_Return409_AndRollback_WhenBulkNextStatusDone()throws Exception{
        TodoResponse createTodoResponse1 = createTodoForTest("test1","2026-04-01");
        TodoResponse createTodoResponse2 = createTodoForTest("test2","2026-05-01");

        //test1だけDOINGの状態に遷移させる
        String patchJson="""
                {
                "nextStatus":"DOING"
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.patch("/todos/"+createTodoResponse1.id()+"/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todoStatus").value("DOING"));


        //両方DONEに遷移させる
        String patchBulkJson= """
                {
                "ids":[%d,%d],
                "nextStatus":"DONE"
                }
                """.formatted(createTodoResponse1.id(),createTodoResponse2.id());

        //test1はDONEになれるがtest2はなれないので409が返されていることを確認する。
        mockMvc.perform(MockMvcRequestBuilders.patch("/todos/bulk-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchBulkJson))
                .andExpect(status().isConflict());

        mockMvc.perform(MockMvcRequestBuilders.get("/todos/"+createTodoResponse1.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todoStatus").value("DOING"));
    }

    //USER権限にDELETEをさせて403エラー(Forbiddenエラー)が出ることを確認
    @Test
    @WithMockUser(roles = "USER")
    void testDeleteTodo_Forbidden_WhenRoleIsUser()throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.delete("/todos/1"))
                .andExpect(status().isForbidden());
    }

    //未認証でGETを求めると401エラー(Unauthorized)が出ることを確認
    @Test
    void testGet_Unauthorized_WhenNotAuthenticated()throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/todos/1"))
                .andExpect(status().isUnauthorized());
    }


    //指定されたtitleとdueDateを満たすようなjsonを作りpostするメソッド
    //簡易テスト用のメソッド
    @WithMockUser(roles = "USER")
    private TodoResponse createTodoForTest(String title,String dueDate)throws Exception{
        String createJson= """
                {
                "title":"%s",
                "dueDate":"%s"
                }
                """.formatted(title,dueDate);
        //上記のJsonをpostして、通信結果をreturnで受け取り、getResponseでサーバーからのresponse(返事)だけ取り出し、content(中身)だけをString型で取り出す。
        String responseJson = mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(responseJson,TodoResponse.class);
    }
}
