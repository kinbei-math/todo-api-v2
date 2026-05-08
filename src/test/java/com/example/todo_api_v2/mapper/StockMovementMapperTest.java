package com.example.todo_api_v2.mapper;

import com.example.todo_api_v2.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class StockMovementMapperTest {
    @Autowired
    private StockMovementMapper stockMovementMapper;
    @Autowired
    private ItemMapper itemMapper;

    @Test @DisplayName("INSERT後にIDが採番される")
    void insert_shouldGenerateId(){
        // 準備
        Item item = createTestItem("TEST-0001","test");
        itemMapper.insert(item);
        StockMovement stockMovement = createTestMovement(item.getId(),MovementType.INBOUND,new BigDecimal("10.000"));

        // 実行
        stockMovementMapper.insert(stockMovement);

        // 検証
        assertThat(stockMovement.getId()).isNotNull();      // IDの存在確認
        assertThat(stockMovement.getId()).isPositive();     // IDが正であることを確認
    }

    @Test @DisplayName("履歴なし → 0が返る（COALESCEの検証）")
    void sumByItemId_shouldReturnZero_whenNoMovements(){
        // 実行
        BigDecimal currentStock = stockMovementMapper.sumByItemId(1L);

        // 検証
        assertThat(currentStock).isEqualByComparingTo("0");
    }

    @Test @DisplayName("入庫のみ → 正の値（CASE WHENの検証）")
    void sumByItemId_shouldReturnPositive_whenOnlyInbound(){
        // 準備
        Item item = createTestItem("TEST-0001","test");
        itemMapper.insert(item);
        StockMovement stockMovement1 = createTestMovement(item.getId(),MovementType.INBOUND,new BigDecimal("10.000"));
        StockMovement stockMovement2 = createTestMovement(item.getId(),MovementType.INBOUND,new BigDecimal("5.000"));
        stockMovementMapper.insert(stockMovement1);
        stockMovementMapper.insert(stockMovement2);

        // 実行
        BigDecimal currentStock = stockMovementMapper.sumByItemId(item.getId());

        // 検証
        assertThat(currentStock).isEqualByComparingTo("15");
    }

    @Test @DisplayName("入庫・出庫混在 → 差し引き計算")
    void sumByItemId_shouldCalculateNet_whenInboundAndOutbound(){
        // 準備
        Item item = createTestItem("TEST-0001","test");
        itemMapper.insert(item);
        StockMovement stockMovement1 = createTestMovement(item.getId(),MovementType.INBOUND,new BigDecimal("10.000"));
        StockMovement stockMovement2 = createTestMovement(item.getId(),MovementType.OUTBOUND,new BigDecimal("8.000"));
        StockMovement stockMovement3 = createTestMovement(item.getId(),MovementType.INBOUND,new BigDecimal("20"));
        stockMovementMapper.insert(stockMovement1);
        stockMovementMapper.insert(stockMovement2);
        stockMovementMapper.insert(stockMovement3);

        // 実行
        BigDecimal currentStock = stockMovementMapper.sumByItemId(item.getId());

        // 検証
        assertThat(currentStock).isEqualByComparingTo("22");
    }

    // テスト用のヘルパーメソッド 2つ
    private Item createTestItem(String itemCode, String name){
        Item item = new Item();
        item.setItemCode(itemCode);
        item.setName(name);
        item.setUom(UomType.PC);
        item.setCategory(Category.RAW_MATERIAL);
        return item;
    }
    private StockMovement createTestMovement(Long itemId, MovementType type, BigDecimal qty){
        StockMovement stockMovement = new StockMovement();
        stockMovement.setItemId(itemId);
        stockMovement.setMovementType(type);
        stockMovement.setQty(qty);
        stockMovement.setMovementDate(LocalDate.parse("2026-01-01"));
        stockMovement.setCreatedBy("USER@com");
        return stockMovement;
    }
}
