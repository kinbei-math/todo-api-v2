package com.example.todo_api_v2.service;

import com.example.todo_api_v2.dto.item.ItemCreateRequest;
import com.example.todo_api_v2.dto.item.ItemResponse;
import com.example.todo_api_v2.dto.stock.StockResponse;
import com.example.todo_api_v2.entity.Category;
import com.example.todo_api_v2.entity.Item;
import com.example.todo_api_v2.entity.UomType;
import com.example.todo_api_v2.exception.DuplicateItemCodeException;
import com.example.todo_api_v2.exception.ItemNotFoundException;
import com.example.todo_api_v2.mapper.ItemMapper;
import com.example.todo_api_v2.mapper.StockMovementMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    ItemMapper itemMapper;
    @Mock
    StockMovementMapper stockMovementMapper;
    @InjectMocks
    ItemService itemService;

    // エラーメッセージ
    private static final String NOT_FOUND_MESSAGE = "品目が見つかりません。id=";
    private static final String DUPLICATE_MESSAGE = "品目コードが既に存在します。itemCode=";

    @Test
    @DisplayName("存在するIDで検索した場合、Itemが入手できる")
    void testFindById_shouldReturnItemResponse_whenIdExists(){
        // Mapperの挙動
        when(itemMapper.findById(1L)).
                thenReturn(createTestOptionalItem(1L,"TEST-0001","test"));

        // 実行
        ItemResponse testItemResponse = itemService.findById(1L);

        // 検証
        assertThat(testItemResponse.id()).isEqualTo(1L);
        assertItem(testItemResponse,"TEST-0001","test");
    }

    @Test
    @DisplayName("存在しないIDで検索したとき、NotFoundExceptionを投げる")
    void testFindById_shouldThrowItemNotFoundException_whenIdNotExists(){
        // Mapperの挙動
        when(itemMapper.findById(999L)).
                thenReturn(Optional.empty());

        // 実行
        Exception exception = assertThrows(ItemNotFoundException.class,
                ()-> itemService.findById(999L));

        // 検証
        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_MESSAGE+"999");
    }

    @Test
    @DisplayName("ItemDBの一覧を取得できる")
    void testFindAll_shouldReturnAllItems(){
        // Mapperの挙動
        when(itemMapper.findAll()).thenReturn(List.of(
                createTestItem(1L,"TEST-0001","test1"),
                createTestItem(2L,"TEST-0002","test2")
        ));

        // 実行
        List<ItemResponse> testList = itemService.findAll();

        // 検証
        assertThat(testList).hasSize(2);
        assertThat(testList.get(0).id()).isEqualTo(1L);
        assertThat(testList.get(1).id()).isEqualTo(2L);
        assertItem(testList.get(0),"TEST-0001","test1");
        assertItem(testList.get(1),"TEST-0002","test2");
    }

    @Test
    @DisplayName("重複がないとき、Insertが実行できる")
    void testCreateItem_shouldReturnItemResponse_whenItemCodeNotExists(){
        // 準備
        ItemCreateRequest itemCreateRequest = new ItemCreateRequest(
                "TEST-0001","test",UomType.PC,Category.RAW_MATERIAL
        );

        // Mapperの挙動
        when(itemMapper.findByItemCode("TEST-0001")).
                thenReturn(Optional.empty());

        // 実行
        ItemResponse testItemResponse = itemService.createItem(itemCreateRequest);

        // 検証
        // findByItemCodeとinsertが1回ずつ呼ばれている事を確認
        verify(itemMapper,times(1)).insert(any(Item.class));
        verify(itemMapper,times(1)).findByItemCode(any(String.class));
        assertThat(testItemResponse.id()).isNull(); // 自動採番なしのためidが空
        assertItem(testItemResponse,"TEST-0001","test");
    }

    @Test
    @DisplayName("重複があるとき、DuplicateItemExceptionを投げる")
    void testCreateItem_shouldThrowDuplicateItemCodeException_whenItemCodeExists(){
        // 準備
        ItemCreateRequest itemCreateRequest = new ItemCreateRequest(
                "TEST-0001","test",UomType.PC,Category.RAW_MATERIAL
        );

        // Mapperの挙動
        when(itemMapper.findByItemCode("TEST-0001")).
                thenReturn(createTestOptionalItem(1L,"TEST-0001","test"));

        // 実行
        Exception exception = assertThrows(DuplicateItemCodeException.class,
                ()-> itemService.createItem(itemCreateRequest));

        // 検証
        // findByItemCodeが1回、insertが呼ばれていないことを確認
        verify(itemMapper,times(1)).findByItemCode(any(String.class));
        verify(itemMapper,never()).insert(any(Item.class));
        assertThat(exception.getMessage()).isEqualTo(DUPLICATE_MESSAGE+"TEST-0001");
    }

    @Test
    @DisplayName("getCurrentStock：品目が存在する場合、現在庫情報が取得できる")
    void testGetCurrentStock_shouldReturnStockResponse_whenItemExists(){
        // Mapperの挙動
        when(itemMapper.findById(1L)).thenReturn(
                createTestOptionalItem(1L,"ITEM-0001","test")
        );
        when(stockMovementMapper.sumByItemId(1L)).thenReturn(new BigDecimal("20.000"));

        // 実行
        StockResponse response = itemService.getCurrentStock(1L);

        // 検証
        assertThat(response.itemId()).isEqualTo(1L);
        assertThat(response.itemCode()).isEqualTo("ITEM-0001");
        assertThat(response.name()).isEqualTo("test");
        assertThat(response.currentStock()).isEqualByComparingTo("20.000");
        assertThat(response.uom()).isEqualTo(UomType.PC);
    }

    @Test
    @DisplayName("getCurrentStock：品目が存在しない場合、ItemNotFoundExceptionを投げる")
    void testGetCurrentStock_shouldThrowItemNotFoundException_whenItemNotExists(){
        // Mapperの挙動
        when(itemMapper.findById(999L)).thenReturn(Optional.empty());

        // 実行
        Exception exception = assertThrows(ItemNotFoundException.class,
                () -> itemService.getCurrentStock(999L));

        // 検証
        verify(itemMapper,times(1)).findById(999L);
        verify(stockMovementMapper,never()).sumByItemId(999L);
        assertThat(exception.getMessage()).isEqualTo("品目が見つかりません。id=999");
    }

    // Optional<Item>を作成するヘルパーメソッド
    private Optional<Item> createTestOptionalItem(Long id, String itemCode, String name){
        Item item = new Item();
        item.setId(id);
        item.setItemCode(itemCode);
        item.setName(name);
        item.setUom(UomType.PC);
        item.setCategory(Category.RAW_MATERIAL);
        return Optional.of(item);
    }

    // Itemを作成するヘルパーメソッド
    private Item createTestItem(Long id, String itemCode, String name){
        Item item = new Item();
        item.setId(id);
        item.setItemCode(itemCode);
        item.setName(name);
        item.setUom(UomType.PC);
        item.setCategory(Category.RAW_MATERIAL);

        return  item;
    }

    // Itemを検証するヘルパーメソッド
    private void assertItem(ItemResponse testItemResponse, String expectedItemCode, String expectedName){
        assertThat(testItemResponse.itemCode()).isEqualTo(expectedItemCode);
        assertThat(testItemResponse.name()).isEqualTo(expectedName);
        assertThat(testItemResponse.uom()).isEqualTo(UomType.PC);
        assertThat(testItemResponse.category()).isEqualTo(Category.RAW_MATERIAL);
    }
}
