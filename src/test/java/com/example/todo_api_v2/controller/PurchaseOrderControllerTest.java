package com.example.todo_api_v2.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
public class PurchaseOrderControllerTest {
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
}
