package com.example.todo_api_v2.mapper;

import com.example.todo_api_v2.dto.item.ReorderAlertResponse;
import com.example.todo_api_v2.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ItemMapperTest {
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private StockMovementMapper stockMovementMapper;

    @Test
    @DisplayName("insertが正しく行われ、自動採番されたIDが取得可能")
    void insert_shouldGenerateId(){
        // Insert用 Itemの作成
        Item item = createTestItem("TEST-0001","testName");
        // 実行
        itemMapper.insert(item);

        // 検証
        assertThat(item.getId()).isNotNull();       // Idの存在確認
        assertThat(item.getId()).isPositive();      // Idが正であることの確認
    }

    @Test
    @DisplayName("findByIdでINSERTした品目が全フィールド一致で取得できる")
    void findById_shouldReturnInsertedItem() {
        // Insert用 Itemの作成
        Item item = createTestItem("TEST-0001","testName");
        // 実行
        itemMapper.insert(item);
        Optional<Item> itemOptional = itemMapper.findById(item.getId());

        // 検証
        assertThat(itemOptional).isPresent();                                    // 存在確認
        Item testItem = itemOptional.get();                                      // 取り出し
        assertThat(testItem.getItemCode()).isEqualTo("TEST-0001");      // itemCode
        assertThat(testItem.getName()).isEqualTo("testName");           // itemName
        assertThat(testItem.getUom()).isEqualTo(UomType.PC);                    // Uom
        assertThat(testItem.getCategory()).isEqualTo(Category.RAW_MATERIAL);    // Category
        assertThat(testItem.getCreatedAt()).isNotNull();                        // CreatedAt
        assertThat(testItem.getUpdatedAt()).isNotNull();                        // UpdatedAt
    }

    @Test
    @DisplayName("findAllでINSERTした品目が一覧取得できる")
    void findAll_shouldReturnAllItems() {
        // Insert用 Itemの作成
        Item item1 = createTestItem("TEST-0001","testName1");
        Item item2 = createTestItem("TEST-0002","testName2");

        // 実行
        itemMapper.insert(item1);
        itemMapper.insert(item2);
        List<Item> itemList = itemMapper.findAll();

        // 検証
        assertThat(itemList).hasSize(2);
        assertThat(itemList.get(0).getItemCode()).isEqualTo("TEST-0001");
        assertThat(itemList.get(1).getItemCode()).isEqualTo("TEST-0002");
    }

    @Test
    @DisplayName("findByItemCodeで存在する品目が取得できる")
    void findByItemCode_shouldReturnItem_whenItemExists() {
        // Insert用 Itemの作成
        Item item = createTestItem("TEST-0001", "testName");
        itemMapper.insert(item);

        // 実行
        Optional<Item> found = itemMapper.findByItemCode("TEST-0001");

        // 検証
        assertThat(found).isPresent();
        Item testItem = found.get();
        assertThat(testItem.getItemCode()).isEqualTo("TEST-0001");
    }

    @Test
    @DisplayName("findByItemCodeで存在しない品目はEmptyが返る")
    void findByItemCode_shouldReturnEmpty_whenItemNotExists() {
        // 実行
        Optional<Item> found = itemMapper.findByItemCode("NONEXISTENT-9999");

        // 検証
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByIdで存在する品目はtrueを返す")
    void existsById_shouldReturnTrue_whenItemExists() {
        // 準備
        Item item = createTestItem("TEST-0001", "testName");
        itemMapper.insert(item);

        // 実行
        Boolean exists = itemMapper.existsById(item.getId());

        // 検証
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByIdで存在しない品目はfalseを返す")
    void existsById_shouldReturnFalse_whenItemNotExists() {
        // 実行
        Boolean exists = itemMapper.existsById(999L);

        // 検証
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("reorderItemsで在庫が発注点と等しい品目は発注対象に含まれる")
    void reorderItems_shouldReturnItem_whenStockEqualsReorderPoint() {
        // item・stockMovementの準備
        Item testItem = createTestItem("TEST-0001", "testItem");
        testItem.setReorderPoint(10);
        itemMapper.insert(testItem);
        StockMovement testStockMovement = createTestMovement(testItem.getId(), MovementType.INBOUND, new BigDecimal("10.000"));
        stockMovementMapper.insert(testStockMovement);

        // 実行
        List<ReorderAlertResponse> reorderAlertResponseList = itemMapper.reorderItems();

        // 検証
        assertThat(reorderAlertResponseList)
                .extracting(ReorderAlertResponse::id)
                .contains(testItem.getId());
    }

    @Test
    @DisplayName("reorderItemsで在庫が発注点を超える品目は発注対象に含まれない")
    void reorderItems_shouldNotReturnItem_whenStockExceedsReorderPoint() {
        // item・stockMovementの準備
        Item testItem = createTestItem("TEST-0001", "testItem");
        testItem.setReorderPoint(10);
        itemMapper.insert(testItem);
        StockMovement testStockMovement1 = createTestMovement(testItem.getId(), MovementType.INBOUND, new BigDecimal("20.000"));
        StockMovement testStockMovement2 = createTestMovement(testItem.getId(), MovementType.OUTBOUND, new BigDecimal("9.000"));
        stockMovementMapper.insert(testStockMovement1);
        stockMovementMapper.insert(testStockMovement2);

        // 実行
        List<ReorderAlertResponse> reorderAlertResponseList = itemMapper.reorderItems();

        // 検証
        assertThat(reorderAlertResponseList)
                .extracting(ReorderAlertResponse::id)
                .doesNotContain(testItem.getId());
    }

    @Test
    @DisplayName("reorderItemsで在庫履歴がない品目（現在庫0）も発注対象に含まれる")
    void reorderItems_shouldReturnItem_whenNoStockMovementExists(){
        // itemの準備
        Item testItem = createTestItem("TEST-0001", "testItem");
        testItem.setReorderPoint(10);
        itemMapper.insert(testItem);

        // 実行
        List<ReorderAlertResponse> reorderAlertResponseList = itemMapper.reorderItems();

        // 検証
        assertThat(reorderAlertResponseList)
                .extracting(ReorderAlertResponse::id)
                .contains(testItem.getId());
    }

    // item作成のヘルプメソッド
    private Item createTestItem(String itemCode,String name){
        Item item = new Item();
        item.setItemCode(itemCode);
        item.setName(name);
        item.setUom(UomType.PC);
        item.setSafetyStock(0);
        item.setReorderPoint(0);
        item.setCategory(Category.RAW_MATERIAL);
        return item;
    }

    // stockMovement作成のヘルプメソッド
    private StockMovement createTestMovement(Long itemId, MovementType type, BigDecimal qty){
        StockMovement stockMovement = new StockMovement();
        stockMovement.setItemId(itemId);
        stockMovement.setMovementType(type);
        stockMovement.setQty(qty);
        stockMovement.setMovementDate(LocalDate.parse("2026-01-01"));
        stockMovement.setCreatedBy("USER");
        return stockMovement;
    }
}
