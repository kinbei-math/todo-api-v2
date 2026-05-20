package com.example.todo_api_v2.controller;

import com.example.todo_api_v2.dto.item.ItemResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class StockMovementControllerTest {
    //mockは注入せずアプリ全体の箱(context)を注入
    @Autowired
    private WebApplicationContext context;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;
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
    @AfterEach
    void tearDown() {
        // stock_movements を先に消す（FK制約対応）
        jdbcTemplate.execute("DELETE FROM stock_movements");
        jdbcTemplate.execute("DELETE FROM items");
        jdbcTemplate.execute("ALTER TABLE stock_movements ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE items ALTER COLUMN id RESTART WITH 1");
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/stock-movements 正常系")
    void createMovement_shouldReturn201_whenValid() throws Exception{
        // 準備
        ItemResponse created = createItemForTest("ITEM-0001","test");

        // JSON準備
        String json = """
                {
                "itemId": %d,
                "movementType": "INBOUND",
                "qty": 10.000,
                "movementDate": "2026-01-01"
                }
                """.formatted(created.id());

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/stock-movements")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemId").value(created.id()))
                .andExpect(jsonPath("$.movementType").value("INBOUND"))
                .andExpect(jsonPath("$.qty").value(10.000))
                .andExpect(jsonPath("$.movementDate").value("2026-01-01"))
                .andExpect(jsonPath("$.createdBy").value("user"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/stock-movements 異常系：itemIdが空")
    void createMovement_shouldReturn400_whenItemIdIsNull() throws Exception{
        // json準備
        String json = """
                {
                "itemId": null,
                "movementType": "INBOUND",
                "qty": 10.000,
                "movementDate": "2026-01-01"
                }
                """;

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/stock-movements")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("itemId"))
                .andExpect(jsonPath("$.errors[0].message").value("品目IDを入力してください"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/stock-movements 異常系：qtyが負")
    void createMovement_shouldReturn400_whenQtyIsNegative() throws Exception{
        // 準備
        ItemResponse created = createItemForTest("ITEM-0001","test");

        // JSON準備
        String json = """
                {
                "itemId": %d,
                "movementType": "INBOUND",
                "qty": -10.000,
                "movementDate": "2026-01-01"
                }
                """.formatted(created.id());

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("qty"))
                .andExpect(jsonPath("$.errors[0].message").value("数量は0より大きい値を入力してください"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/stock-movements 異常系：Itemが存在しない")
    void createMovement_shouldReturn404_whenItemNotExists() throws Exception{
        // JSON準備
        String json = """
                {
                "itemId": 999,
                "movementType": "INBOUND",
                "qty": 10.000,
                "movementDate": "2026-01-01"
                }
                """;

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.message").value(
                        "品目IDが存在しません。itemId=999"));
    }

    @Test @DisplayName("POST/stock-movements 異常系：認証エラー")
    void createMovement_shouldReturn401_whenNotAuthenticated() throws Exception{
        // JSON準備
        String json = """
                {
                "itemId": 1,
                "movementType": "INBOUND",
                "qty": 10.000,
                "movementDate": "2026-01-01"
                }
                """;

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    // テストデータinsert　ヘルパーメソッド
    private ItemResponse createItemForTest(String itemCode, String name) throws Exception {
        String json = """
            {
              "itemCode": "%s",
              "name": "%s",
              "uom": "PC",
              "category": "RAW_MATERIAL"
            }
            """.formatted(itemCode, name);

        String responseJson = mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(responseJson, ItemResponse.class);
    }
}
