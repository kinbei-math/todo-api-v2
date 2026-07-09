package com.example.todo_api_v2.controller;

import com.example.todo_api_v2.dto.item.ItemCreateRequest;
import com.example.todo_api_v2.dto.item.ItemResponse;
import com.example.todo_api_v2.dto.stock.StockMovementCreateRequest;
import com.example.todo_api_v2.dto.stock.StockMovementResponse;
import com.example.todo_api_v2.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class ItemControllerTest {
    //mockは注入せずアプリ全体の箱(context)を注入
    @Autowired private WebApplicationContext context;
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

    @Test @WithMockUser(roles = "USER") @DisplayName("GET/items/{id} 正常系")
    void testGetItem_shouldReturn200_whenIdExists() throws Exception{
        // 準備
        ItemResponse createItem = createItemForTest("TEST-0001","test");

        mockMvc.perform(MockMvcRequestBuilders.get("/items/"+createItem.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createItem.id()))
                .andExpect(jsonPath("$.itemCode").value("TEST-0001"))
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.uom").value("PC"))
                .andExpect(jsonPath("$.category").value("RAW_MATERIAL"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("GET/items/{id} 異常系：存在しないIDで検索")
    void testGetItem_shouldReturn404_whenIdNotExists() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/items/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("品目が見つかりません。id=999"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("GET/items 正常系：一覧取得")
    void testGetItems_shouldReturn200AndItemList_whenCalled() throws Exception{
        // 準備
        ItemResponse createItem1 = createItemForTest("TEST-0001","test1");
        ItemResponse createItem2 = createItemForTest("TEST-0002","test2");

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(createItem1.id()))
                .andExpect(jsonPath("$[1].id").value(createItem2.id()))
                .andExpect(jsonPath("$[0].itemCode").value("TEST-0001"))
                .andExpect(jsonPath("$[1].itemCode").value("TEST-0002"))
                .andExpect(jsonPath("$[0].name").value("test1"))
                .andExpect(jsonPath("$[1].name").value("test2"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/items 正常系")
    void testCreateItem_shouldReturn201_whenValidRequest() throws Exception{
        // 準備
        String json = """
                {
                "itemCode":"TEST-0001",
                "name":"test",
                "uom":"PC",
                "category":"RAW_MATERIAL"
                }
                """;

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemCode").value("TEST-0001"))
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.uom").value("PC"))
                .andExpect(jsonPath("$.category").value("RAW_MATERIAL"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/items 異常系：itemCodeが空")
    void testCreateItem_shouldReturn400_whenItemCodeIsBlank() throws Exception{
        // 準備
        String json = """
                {
                "itemCode":"",
                "name":"test",
                "uom":"PC",
                "category":"RAW_MATERIAL"
                }
                """;

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("itemCode"))
                .andExpect(jsonPath("$.errors[0].message").value("品目コードを入力してください"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/items 異常系：itemCodeが20文字以上")
    void testCreateItem_shouldReturn400_whenItemCodeIsTooLong() throws Exception{
        // 準備
        String overSizeItemCode = "a".repeat(21);
        String json = """
                {
                "itemCode":"%s",
                "name":"test",
                "uom":"PC",
                "category":"RAW_MATERIAL"
                }
                """.formatted(overSizeItemCode);

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("itemCode"))
                .andExpect(jsonPath("$.errors[0].message").value("品目コードは20文字以内で入力してください"));
    }


    @Test @WithMockUser(roles = "USER") @DisplayName("POST/items 異常系：重複登録")
    void testCreateItem_shouldReturn409_whenItemCodeIsDuplicate() throws Exception{
        // 準備(itemCodeを事前登録 戻り値は捨てる)
        createItemForTest("TEST-0001","test");
        String json = """
                {
                "itemCode":"TEST-0001",
                "name":"test",
                "uom":"PC",
                "category":"RAW_MATERIAL"
                }
                """;

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.message").
                        value("品目コードが既に存在します。itemCode=TEST-0001"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("GET /items/{id}/stock 正常系：現在庫が取得できる")
    void testGetCurrentStock_shouldReturn200AndStockResponse() throws Exception{
        // 準備
        ItemResponse created = createItemForTest("TEST-0001","test");


        // 検証
        mockMvc.perform(MockMvcRequestBuilders.get("/items/" + created.id() + "/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(created.id()))
                .andExpect(jsonPath("$.itemCode").value("TEST-0001"))
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.uom").value("PC"))
                .andExpect(jsonPath("$.currentStock").value(0)); // 履歴なしのため
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("GET /items/{id}/stock 異常系：存在しないIDで404")
    void testGetCurrentStock_shouldReturn404_whenIdNotExists() throws Exception{
        // 検証
        mockMvc.perform(MockMvcRequestBuilders.get("/items/999/stock"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("品目が見つかりません。id=999"));


    }

    @Test @DisplayName("GET/items 異常系：認証エラー")
    void testGetItem_shouldReturn401_whenNotAuthenticated() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/items/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser(roles = "USER")
    @DisplayName("GET /items/reorder-alerts 正常系：発注点を下回った品目だけが返る")
    void testGetReorderAlerts_shouldReturn200AndOnlyItemsBelowReorderPoint() throws Exception {
        // 準備
        ItemResponse itemResponse1 = createItemWithReorderPoint("TEST-0001","test1",10);
        ItemResponse itemResponse2 = createItemWithReorderPoint("TEST-0002","test2",10);

        StockMovementResponse stockMovementResponse1 = createStockMovement(itemResponse1.id(), MovementType.INBOUND, new BigDecimal("5.000"));
        StockMovementResponse stockMovementResponse2 = createStockMovement(itemResponse2.id(), MovementType.INBOUND, new BigDecimal("20.000"));

        // 実行・検証
        mockMvc.perform(MockMvcRequestBuilders.get("/items/reorder-alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].id").value(itemResponse1.id()))
                .andExpect(jsonPath("$.[0].itemCode").value(itemResponse1.itemCode()))
                .andExpect(jsonPath("$.[0].name").value(itemResponse1.name()))
                .andExpect(jsonPath("$.[0].uom").value("PC"))
                .andExpect(jsonPath("$.[0].currentStock").value(5.000))
                .andExpect(jsonPath("$.[0].reorderPoint").value(10));
    }

    @Test @DisplayName("GET /items/reorder-alerts 異常系：認証エラー")
    void testGetReorderAlerts_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/items/reorder-alerts"))
                .andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser(roles = "USER")
    @DisplayName("GET /items/reorder-alerts 正常系：該当品目なしのとき空配列")
    void testGetReorderAlerts_shouldReturnEmptyList_whenNoItemsBelowReorderPoint() throws Exception {
        // 準備
        ItemResponse itemResponse = createItemWithReorderPoint("TEST-0001","test1",10);

        StockMovementResponse stockMovementResponse1 = createStockMovement(itemResponse.id(), MovementType.INBOUND, new BigDecimal("20.000"));

        // 実行・検証
        mockMvc.perform(MockMvcRequestBuilders.get("/items/reorder-alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
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

    // 新規テストデータinsert　ヘルパーメソッド
    private ItemResponse createItemWithReorderPoint(String itemCode, String name,Integer reorderPoint)throws Exception{
        ItemCreateRequest itemForTest = new ItemCreateRequest(
                itemCode, name, UomType.PC, 0, reorderPoint, Category.RAW_MATERIAL
        );
        String json = objectMapper.writeValueAsString(itemForTest);

        String responseJson = mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(responseJson, ItemResponse.class);
    }

    // 在庫を積む　ヘルパーメソッド
    private StockMovementResponse createStockMovement(Long itemId, MovementType movementType, BigDecimal qty)throws Exception{
        StockMovementCreateRequest stockMovementForTest = new StockMovementCreateRequest(
                itemId, movementType, qty, LocalDate.of(2026,6,1)
        );
        String json = objectMapper.writeValueAsString(stockMovementForTest);

        String responseJson = mockMvc.perform(MockMvcRequestBuilders.post("/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(responseJson, StockMovementResponse.class);
    }
}
