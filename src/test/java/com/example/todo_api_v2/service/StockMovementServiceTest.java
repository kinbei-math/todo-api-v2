package com.example.todo_api_v2.service;

import com.example.todo_api_v2.dto.StockMovementCreateRequest;
import com.example.todo_api_v2.dto.StockMovementResponse;
import com.example.todo_api_v2.entity.MovementType;
import com.example.todo_api_v2.entity.StockMovement;
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
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockMovementServiceTest {

    @Mock
    private StockMovementMapper stockMovementMapper;
    @Mock
    private ItemMapper itemMapper;
    @InjectMocks private StockMovementService stockMovementService;

    @Test
    @DisplayName("品目が存在する場合、入出庫が登録できる")
    void createMovement_shouldReturnResponse_whenItemExists(){
        // itemMapperの挙動
        when(itemMapper.existsById(1L)).thenReturn(true);
        // Requestの準備
        StockMovementCreateRequest testRequest = new StockMovementCreateRequest(
                1L, MovementType.INBOUND,new BigDecimal("10.000"), LocalDate.parse("2026-01-01")
        );

        // 実行
        StockMovementResponse testResponse = stockMovementService.createMovement(testRequest);

        // 検証
        verify(itemMapper,times(1)).existsById(any(Long.class));
        verify(stockMovementMapper,times(1)).insert(any(StockMovement.class));
        assertThat(testResponse.id()).isNull(); // 自動採番なしのためidが空
        assertThat(testResponse.itemId()).isEqualTo(1L);
        assertThat(testResponse.movementType()).isEqualTo(MovementType.INBOUND);
        assertThat(testResponse.qty()).isEqualByComparingTo("10.000");
        assertThat(testResponse.movementDate()).isEqualTo(LocalDate.parse("2026-01-01"));
        assertThat(testResponse.createdBy()).isEqualTo("system");
    }

    @Test
    @DisplayName("品目が存在しない場合、ItemNotFoundExceptionを投げる(例外時に副作用なし)")
    void createMovement_shouldThrowItemNotFoundException_whenItemNotExists(){
        // itemMapperの挙動
        when(itemMapper.existsById(999L)).thenReturn(false);
        // Requesetの準備
        StockMovementCreateRequest testRequest = new StockMovementCreateRequest(
                999L, MovementType.INBOUND,new BigDecimal("10.000"), LocalDate.parse("2026-01-01")
        );

        // 実行
        Exception exception = assertThrows(ItemNotFoundException.class,
                () -> stockMovementService.createMovement(testRequest));

        // 検証
        verify(itemMapper,times(1)).existsById(any(Long.class));
        verify(stockMovementMapper,never()).insert(any(StockMovement.class)); // inserが実行されていない = 例外時の副作用がない
        assertThat(exception.getMessage()).isEqualTo("品目IDが存在しません。itemId=999");
    }


}
