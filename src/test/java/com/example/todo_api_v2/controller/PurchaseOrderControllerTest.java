package com.example.todo_api_v2.controller;

import com.example.todo_api_v2.dto.item.ItemCreateRequest;
import com.example.todo_api_v2.dto.item.ItemResponse;
import com.example.todo_api_v2.dto.purchaseorder.PurchaseOrderCreateRequest;
import com.example.todo_api_v2.dto.purchaseorder.PurchaseOrderLineCreateRequest;
import com.example.todo_api_v2.dto.purchaseorder.PurchaseOrderResponse;
import com.example.todo_api_v2.dto.purchaseorder.ReceiveLineRequest;
import com.example.todo_api_v2.entity.Category;
import com.example.todo_api_v2.entity.UomType;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class PurchaseOrderControllerTest {
    //mockは注入せずアプリ全体の箱(context)を注入
    @Autowired
    private WebApplicationContext context;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;
    //mockの設定は下でやる。自動注入×
    private MockMvc mockMvc;

    // テスト用変数の準備
    String     poNumber    = "TEST-PO-001";
    String     supplier    = "testSupllier";
    LocalDate  orderDate   = LocalDate.of(2026,5,1);
    BigDecimal qty1        = new BigDecimal("10.000");
    BigDecimal qty2        = new BigDecimal("20.000");
    BigDecimal price1      = new BigDecimal("100.00");
    BigDecimal price2      = new BigDecimal("200.00");
    LocalDate  dueDate1    = LocalDate.of(2026, 6, 1);
    LocalDate  dueDate2    = LocalDate.of(2026, 6, 2);
    LocalDate  receivedAt1 = LocalDate.of(2026,5,30);
    LocalDate  receivedAt2 = LocalDate.of(2026,5,31);


    //すべてのテストの前にmockを作成
    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity()) //SecurityChainを注入
                .build();
    }
    // テストデータ削除
    @AfterEach
    void tearDown() {
        // FK制約順に削除
        jdbcTemplate.execute("DELETE FROM stock_movements");
        jdbcTemplate.execute("DELETE FROM purchase_order_lines");
        jdbcTemplate.execute("DELETE FROM purchase_orders");
        jdbcTemplate.execute("DELETE FROM items");
        jdbcTemplate.execute("ALTER TABLE stock_movements ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE purchase_order_lines ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE purchase_orders ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE items ALTER COLUMN id RESTART WITH 1");
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/purchase-orders 正常系")
    void testCreatePurchaseOrder_shouldReturn201_whenValidRequest() throws Exception{
        // テストデータ準備
        ItemResponse response = createItemForTest("TEST-ITEM-001","testItem");
        PurchaseOrderLineCreateRequest lineCreateRequest1
                = new PurchaseOrderLineCreateRequest(response.id(), qty1, price1, dueDate1);
        PurchaseOrderLineCreateRequest lineCreateRequest2
                = new PurchaseOrderLineCreateRequest(response.id(), qty2, price2, dueDate2);
        List<PurchaseOrderLineCreateRequest> requestLines
                = List.of(lineCreateRequest1, lineCreateRequest2);
        String createRequestJson = createPoCreateRequestJson(poNumber, supplier, orderDate, requestLines);

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/purchase-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ORDERED"))
                .andExpect(jsonPath("$.lines[0].lineNo").value(1))
                .andExpect(jsonPath("$.lines[0].status").value("ORDERED"))
                .andExpect(jsonPath("$.lines[1].lineNo").value(2))
                .andExpect(jsonPath("$.lines[1].status").value("ORDERED"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/purchase-orders 異常系：poNumberが空")
    void testCreatePurchaseOrder_shouldReturn400_whenPoNumberIsBlank()throws Exception{
        // データの準備
        PurchaseOrderLineCreateRequest lineCreateRequest
                = new PurchaseOrderLineCreateRequest(1L, qty1, price1, dueDate1);
        List<PurchaseOrderLineCreateRequest> lines = List.of(lineCreateRequest);
        String createRquestJson = createPoCreateRequestJson(null, supplier, orderDate, lines);

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/purchase-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRquestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("poNumber"))
                .andExpect(jsonPath("$.errors[0].message").value("空白なしで発注番号を入力してください"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/purchase-orders 異常系：poNumberが21字以上")
    void testCreatePurchaseOrder_shouldReturn400_whenPoNumberIsTooLong()throws Exception{
        // データの準備
        PurchaseOrderLineCreateRequest lineCreateRequest
                = new PurchaseOrderLineCreateRequest(1L, qty1, price1, dueDate1);
        List<PurchaseOrderLineCreateRequest> lines = List.of(lineCreateRequest);
        String badPoNumber = "a".repeat(21);
        String createRquestJson = createPoCreateRequestJson(badPoNumber, supplier, orderDate, lines);

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRquestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("poNumber"))
                .andExpect(jsonPath("$.errors[0].message").value("20字以内で入力してください"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/purchase-orders 異常系：supplierが空")
    void testCreatePurchaseOrder_shouldReturn400_whenSupplierIsBlank()throws Exception{
        // データの準備
        PurchaseOrderLineCreateRequest lineCreateRequest
                = new PurchaseOrderLineCreateRequest(1L, qty1, price1, dueDate1);
        List<PurchaseOrderLineCreateRequest> lines = List.of(lineCreateRequest);
        String createRquestJson = createPoCreateRequestJson(poNumber, null, orderDate, lines);

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRquestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("supplier"))
                .andExpect(jsonPath("$.errors[0].message").value("空白なしで仕入先を入力してください"));

    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/purchase-orders 異常系：supplierが101字以上")
    void testCreatePurchaseOrder_shouldReturn400_whenSupplierIsTooLong()throws Exception{
        // データの準備
        PurchaseOrderLineCreateRequest lineCreateRequest
                = new PurchaseOrderLineCreateRequest(1L, qty1, price1, dueDate1);
        List<PurchaseOrderLineCreateRequest> lines = List.of(lineCreateRequest);
        String badSupllier = "a".repeat(101);
        String createRquestJson = createPoCreateRequestJson(poNumber, badSupllier, orderDate, lines);

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRquestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("supplier"))
                .andExpect(jsonPath("$.errors[0].message").value("100字以内で入力してください"));

    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/purchase-orders 異常系：明細が空リスト")
    void testCreatePurchaseOrder_shouldReturn400_whenLinesIsEmpty()throws Exception{
        // データの準備
        String createRquestJson = createPoCreateRequestJson(poNumber, supplier, orderDate, List.of());

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRquestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("lines"))
                .andExpect(jsonPath("$.errors[0].message").value("明細を追加してください"));

    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/purchase-orders 異常系：明細itemIdが空（カスケード確認）")
    void testCreatePurchaseOrder_shouldReturn400_whenLineItemIdIsNull()throws Exception{
        // データの準備
        PurchaseOrderLineCreateRequest lineCreateRequest
                = new PurchaseOrderLineCreateRequest(null, qty1, price1, dueDate1);
        List<PurchaseOrderLineCreateRequest> lines = List.of(lineCreateRequest);
        String createRquestJson = createPoCreateRequestJson(poNumber, supplier, orderDate, lines);

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRquestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("lines[0].itemId"))
                .andExpect(jsonPath("$.errors[0].message").value("品目IDを入力してください"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/purchase-orders 異常系：明細qtyが負")
    void testCreatePurchaseOrder_shouldReturn400_whenLineQtyIsNegative()throws Exception{
        // データの準備
        PurchaseOrderLineCreateRequest lineCreateRequest
                = new PurchaseOrderLineCreateRequest(1L, new BigDecimal("-10.000"), price1, dueDate1);
        List<PurchaseOrderLineCreateRequest> lines = List.of(lineCreateRequest);
        String createRquestJson = createPoCreateRequestJson(poNumber, supplier, orderDate, lines);

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRquestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("lines[0].qty"))
                .andExpect(jsonPath("$.errors[0].message").value("数量は0より大きい値を入力してください"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/purchase-orders 異常系：明細qtyが桁あふれ")
    void testCreatePurchaseOrder_shouldReturn400_whenLineQtyExceedsDigits()throws Exception{
        // データの準備
        PurchaseOrderLineCreateRequest lineCreateRequest
                = new PurchaseOrderLineCreateRequest(1L, new BigDecimal("10.1234"), price1, dueDate1);
        List<PurchaseOrderLineCreateRequest> lines = List.of(lineCreateRequest);
        String createRquestJson = createPoCreateRequestJson(poNumber, supplier, orderDate, lines);

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRquestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("lines[0].qty"))
                .andExpect(jsonPath("$.errors[0].message").value("数量の整数部分は9桁, 小数部分は3桁以内で入力してください"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST/purchase-orders 異常系：発注番号の重複")
    void testCreatePurchaseOrder_shouldReturn409_whenPoNumberIsDuplicate()throws Exception{
        // テストデータ準備
        ItemResponse response = createItemForTest("TEST-ITEM-001","testItem");
        PurchaseOrderLineCreateRequest lineCreateRequest
                = new PurchaseOrderLineCreateRequest(response.id(), qty1, price1, dueDate1);
        List<PurchaseOrderLineCreateRequest> requestLines = List.of(lineCreateRequest);
        String createRequestJson = createPoCreateRequestJson(poNumber, supplier, orderDate, requestLines);

        // 事前にDBへ登録
        mockMvc.perform(MockMvcRequestBuilders.post("/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestJson))
                .andExpect(status().isCreated());

        // 検証
        mockMvc.perform(MockMvcRequestBuilders.post("/purchase-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.message").value("poNumber:"+poNumber+"は使用されています"));

    }

    @Test @WithMockUser(roles = "USER") @DisplayName("GET/purchase-orders/{id} 正常系")
    void testGetPurchaseOrder_shouldReturn200_whenIdExists()throws Exception{
        // 準備
        ItemResponse response = createItemForTest("TEST-ITEM-001","testItem");
        PurchaseOrderLineCreateRequest lineCreateRequest1
                = new PurchaseOrderLineCreateRequest(response.id(), qty1, price1, dueDate1);
        PurchaseOrderLineCreateRequest lineCreateRequest2
                = new PurchaseOrderLineCreateRequest(response.id(), qty2, price2, dueDate2);
        List<PurchaseOrderLineCreateRequest> requestLines
                = List.of(lineCreateRequest1, lineCreateRequest2);
        PurchaseOrderResponse orderResponse = createPoForTest(poNumber, supplier, orderDate, requestLines);

        // 実行
        mockMvc.perform(MockMvcRequestBuilders.get("/purchase-orders/"+orderResponse.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderResponse.id()))
                .andExpect(jsonPath("$.poNumber").value(poNumber))
                .andExpect(jsonPath("$.lines[0].lineNo").value(1))
                .andExpect(jsonPath("$.lines[1].lineNo").value(2));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("GET/purchase-orders/{id} 異常系：存在しないID")
    void testGetPurchaseOrder_shouldReturn404_whenIdNotExists()throws Exception{
        // 実行
        mockMvc.perform(MockMvcRequestBuilders.get("/purchase-orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("発注ヘッダがDBに存在しません。poId=999"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("GET/purchase-orders 正常系：一覧取得")
    void testGetPurchaseOrders_shouldReturn200AndList_whenCalled()throws Exception{
        // フィールド設定
        String     poNumber2  = "TEST-PO-002";
        String     supplier2  = "testSupllier2";
        LocalDate  orderDate2 = LocalDate.of(2026,5,2);

        // 準備
        ItemResponse response = createItemForTest("TEST-ITEM-001","testItem");
        PurchaseOrderLineCreateRequest lineCreateRequest1
                = new PurchaseOrderLineCreateRequest(response.id(), qty1, price1, dueDate1);
        PurchaseOrderLineCreateRequest lineCreateRequest2
                = new PurchaseOrderLineCreateRequest(response.id(), qty2, price2, dueDate2);
        List<PurchaseOrderLineCreateRequest> requestLines
                = List.of(lineCreateRequest1, lineCreateRequest2);
        PurchaseOrderResponse orderResponse1 = createPoForTest(poNumber, supplier, orderDate, requestLines);
        PurchaseOrderResponse orderResponse2 = createPoForTest(poNumber2, supplier2, orderDate2, requestLines);

        // 実行
        mockMvc.perform(MockMvcRequestBuilders.get("/purchase-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].id").value(orderResponse1.id()))
                .andExpect(jsonPath("$.[1].id").value(orderResponse2.id()))
                .andExpect(jsonPath("$.[0].poNumber").value(poNumber))
                .andExpect(jsonPath("$.[1].poNumber").value(poNumber2));
    }

    @Test @DisplayName("GET/purchase-orders 異常系：認証エラー")
    void testGetPurchaseOrders_shouldReturn401_whenNotAuthenticated()throws Exception{
        // 実行
        mockMvc.perform(MockMvcRequestBuilders.get("/purchase-orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST .../receive 正常系：部分入荷")
    void testReceivePurchaseOrderLine_shouldReturn200AndHeaderOrdered_whenPartiallyReceived()throws Exception{
        // 準備
        ItemResponse response = createItemForTest("TEST-ITEM-001","testItem");
        PurchaseOrderLineCreateRequest lineCreateRequest1
                = new PurchaseOrderLineCreateRequest(response.id(), qty1, price1, dueDate1);
        PurchaseOrderLineCreateRequest lineCreateRequest2
                = new PurchaseOrderLineCreateRequest(response.id(), qty2, price2, dueDate2);
        List<PurchaseOrderLineCreateRequest> requestLines
                = List.of(lineCreateRequest1, lineCreateRequest2);
        PurchaseOrderResponse orderResponse = createPoForTest(poNumber, supplier, orderDate, requestLines);

        String requestJson = createReceiveLineRequestJson(receivedAt1);
        Long poId = orderResponse.id();
        Integer lineNo1 = orderResponse.lines().get(0).lineNo();
        Integer lineNo2 = orderResponse.lines().get(1).lineNo();

        // 実行・検証
        mockMvc.perform(MockMvcRequestBuilders.post(
                "/purchase-orders/"+poId+"/lines/"+lineNo1+"/receive")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ORDERED"))
                .andExpect(jsonPath("$.lines[0].lineNo").value(lineNo1))
                .andExpect(jsonPath("$.lines[0].status").value("RECEIVED"))
                .andExpect(jsonPath("$.lines[1].lineNo").value(lineNo2))
                .andExpect(jsonPath("$.lines[1].status").value("ORDERED"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST .../receive 正常系：完了入荷")
    void testReceivePurchaseOrderLine_shouldReturn200AndHeaderReceived_whenAllReceived()throws Exception{
        // 準備
        ItemResponse response = createItemForTest("TEST-ITEM-001","testItem");
        PurchaseOrderLineCreateRequest lineCreateRequest1
                = new PurchaseOrderLineCreateRequest(response.id(), qty1, price1, dueDate1);
        PurchaseOrderLineCreateRequest lineCreateRequest2
                = new PurchaseOrderLineCreateRequest(response.id(), qty2, price2, dueDate2);
        List<PurchaseOrderLineCreateRequest> requestLines
                = List.of(lineCreateRequest1, lineCreateRequest2);
        PurchaseOrderResponse orderResponse = createPoForTest(poNumber, supplier, orderDate, requestLines);

        String requestJson1 = createReceiveLineRequestJson(receivedAt1);
        String requestJson2 = createReceiveLineRequestJson(receivedAt2);

        Long poId = orderResponse.id();
        Integer lineNo1 = orderResponse.lines().get(0).lineNo();
        Integer lineNo2 = orderResponse.lines().get(1).lineNo();
        // 事前receive
        mockMvc.perform(MockMvcRequestBuilders.post(
                                "/purchase-orders/"+poId+"/lines/"+lineNo1+"/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson1))
                .andExpect(status().isOk());

        // 追記分
        mockMvc.perform(MockMvcRequestBuilders.post(
                                "/purchase-orders/"+poId+"/lines/"+lineNo2+"/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.lines[0].lineNo").value(lineNo1))
                .andExpect(jsonPath("$.lines[0].status").value("RECEIVED"))
                .andExpect(jsonPath("$.lines[1].lineNo").value(lineNo2))
                .andExpect(jsonPath("$.lines[1].status").value("RECEIVED"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST .../receive 異常系：二重入荷")
    void testReceivePurchaseOrderLine_shouldReturn409_whenAlreadyReceived()throws Exception{
        // 準備
        ItemResponse response = createItemForTest("TEST-ITEM-001","testItem");
        PurchaseOrderLineCreateRequest lineCreateRequest1
                = new PurchaseOrderLineCreateRequest(response.id(), qty1, price1, dueDate1);
        PurchaseOrderLineCreateRequest lineCreateRequest2
                = new PurchaseOrderLineCreateRequest(response.id(), qty2, price2, dueDate2);
        List<PurchaseOrderLineCreateRequest> requestLines
                = List.of(lineCreateRequest1, lineCreateRequest2);
        PurchaseOrderResponse orderResponse = createPoForTest(poNumber, supplier, orderDate, requestLines);

        String requestJson = createReceiveLineRequestJson(receivedAt1);
        Long poId = orderResponse.id();
        Integer lineNo = orderResponse.lines().get(0).lineNo();

        // 事前receive
        mockMvc.perform(MockMvcRequestBuilders.post(
                                "/purchase-orders/"+poId+"/lines/"+lineNo+"/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        // 追記
        mockMvc.perform(MockMvcRequestBuilders.post(
                        "/purchase-orders/"+poId+"/lines/"+lineNo+"/receive")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.message")
                        .value("不正遷移:poId="+poId + ", lineNo=" + lineNo +
                                ", RECEIVEDからRECEIVEDへの遷移"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST .../receive 異常系：receivedAtが空")
    void testReceivePurchaseOrderLine_shouldReturn400_whenReceivedAtIsNull()throws Exception{
        String requestJson = createReceiveLineRequestJson(null);
        mockMvc.perform(MockMvcRequestBuilders.post(
                                "/purchase-orders/1/lines/1/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("receivedAt"))
                .andExpect(jsonPath("$.errors[0].message").value("納品日を入力してください"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST .../receive 異常系：発注が存在しない")
    void testReceivePurchaseOrderLine_shouldReturn404_whenPoIdNotExists()throws Exception{
        String requestJson = createReceiveLineRequestJson(receivedAt1);
        mockMvc.perform(MockMvcRequestBuilders.post(
                                "/purchase-orders/999/lines/1/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("発注ヘッダがDBに存在しません。PoId:999"));
    }

    @Test @WithMockUser(roles = "USER") @DisplayName("POST .../receive 異常系：明細行が存在しない")
    void testReceivePurchaseOrderLine_shouldReturn404_whenLineNoNotExists()throws Exception{
        // 準備
        ItemResponse response = createItemForTest("TEST-ITEM-001","testItem");
        PurchaseOrderLineCreateRequest lineCreateRequest1
                = new PurchaseOrderLineCreateRequest(response.id(), qty1, price1, dueDate1);
        PurchaseOrderLineCreateRequest lineCreateRequest2
                = new PurchaseOrderLineCreateRequest(response.id(), qty2, price2, dueDate2);
        List<PurchaseOrderLineCreateRequest> requestLines
                = List.of(lineCreateRequest1, lineCreateRequest2);
        PurchaseOrderResponse orderResponse = createPoForTest(poNumber, supplier, orderDate, requestLines);

        String requestJson = createReceiveLineRequestJson(receivedAt1);
        Long poId = orderResponse.id();

        mockMvc.perform(MockMvcRequestBuilders.post(
                                "/purchase-orders/"+poId+"/lines/100/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message")
                        .value("明細が見つかりません。PoId:" + poId + ", LineNo:100"));
    }


    /**
     * PurchaseOrderCreateRequest(Json)のためのヘルパーメソッド
     * @param poNumber 発注番号(UK)
     * @param supplier 仕入先
     * @param orderDate 発注日
     * @param lines 明細リスト
     * @return String(Json)
     */
    private String createPoCreateRequestJson (
            String poNumber, String supplier, LocalDate orderDate, List<PurchaseOrderLineCreateRequest> lines
    ){
        PurchaseOrderCreateRequest purchaseOrderCreateRequest
                = new PurchaseOrderCreateRequest(poNumber, supplier, orderDate, lines);
        return objectMapper.writeValueAsString(purchaseOrderCreateRequest);
    }

    /**
     * ReceiveLineRequest(Json)のためのヘルパーメソッド
     * @param receivedAt 納品日
     * @return String(Json)
     */
    private String createReceiveLineRequestJson(LocalDate receivedAt){
        ReceiveLineRequest receiveLineRequest = new ReceiveLineRequest(receivedAt);
        return objectMapper.writeValueAsString(receiveLineRequest);
    }

    /**
     * PurchaseOrderを登録するヘルパーメソッド
     * @param poNumber 発注番号(UK)
     * @param supplier 仕入先
     * @param orderDate 発注日
     * @return PurchaseOrderResponse
     * @throws Exception MockMvc実行時のチェック例外
     */
    private PurchaseOrderResponse createPoForTest(
            String poNumber, String supplier, LocalDate orderDate, List<PurchaseOrderLineCreateRequest> requestLines
    )throws Exception{
        String createRequestJson = createPoCreateRequestJson(poNumber, supplier, orderDate, requestLines);

        String responseJson = mockMvc.perform(MockMvcRequestBuilders.post("/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(responseJson, PurchaseOrderResponse.class);
    }

    /**
     * ItemクラスをDBに登録するためのヘルパーメソッド
     * @param itemCode itemCode(Uk)
     * @param name itemName
     * @return ItemResponse
     * @throws Exception MockMvc実行時のチェック例外
     */
    private ItemResponse createItemForTest(String itemCode, String name) throws Exception {
        ItemCreateRequest itemCreateRequest = new ItemCreateRequest(itemCode,name, UomType.PC, Category.RAW_MATERIAL);
        String json = objectMapper.writeValueAsString(itemCreateRequest);

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
