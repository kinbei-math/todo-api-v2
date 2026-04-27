package com.example.todo_api_v2.mapper;

import com.example.todo_api_v2.entity.Category;
import com.example.todo_api_v2.entity.Item;
import com.example.todo_api_v2.entity.UomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ItemMapperTest {
    @Autowired
    private ItemMapper itemMapper;

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


    // テスト用のヘルプメソッド
    private Item createTestItem(String itemCode,String name){
        Item item = new Item();
        item.setItemCode(itemCode);
        item.setName(name);
        item.setUom(UomType.PC);
        item.setCategory(Category.RAW_MATERIAL);
        return item;
    }
}
