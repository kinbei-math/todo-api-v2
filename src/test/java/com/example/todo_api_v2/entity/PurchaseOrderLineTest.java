package com.example.todo_api_v2.entity;


import com.example.todo_api_v2.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PurchaseOrderLineTest {
    private static final LocalDateTime CREATED_AT  = LocalDateTime.of(2026, 5, 1, 9, 0);
    private static final LocalDate     DUE_DATE    = LocalDate.of(2026, 6, 1);
    private static final LocalDate     RECEIVED_AT = LocalDate.of(2026,5,31);
    private static final LocalDateTime UPDATED_AT  = LocalDateTime.of(2026,5,31,12,0);

    @Test
    @DisplayName("正常系：OrderedからReceivedへの遷移")
    void markAsReceived_shouldChangeStatusToReceived_whenOrdered(){
        // 準備
        PurchaseOrderLine line = createOrderedLine();

        // 実行
        line.markAsReceived(RECEIVED_AT,"byTest",UPDATED_AT);

        // 検証
        assertThat(line.getStatus()).isEqualTo(PoLineStatus.RECEIVED);
        assertThat(line.getReceivedBy()).isEqualTo("byTest");
        assertThat(line.getReceivedAt()).isEqualTo(RECEIVED_AT);
        assertThat(line.getUpdatedBy()).isEqualTo("byTest");
        assertThat(line.getUpdatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    @DisplayName("異常系：入荷済みの明細を再入荷すると例外")
    void markAsReceived_shouldThrowException_whenAlreadyReceived(){
        // 準備
        PurchaseOrderLine line = createReceivedLine();

        // 実行・検証
        assertThatThrownBy(() -> line.markAsReceived(RECEIVED_AT,"byTest",UPDATED_AT))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    @DisplayName("異常系：遷移失敗時は状態を変更しない")
    void markAsReceived_shouldNotChangeState_whenTransitionFails(){
        // 準備
        PurchaseOrderLine line = createReceivedLine();

        // 実行・検証
        assertThatThrownBy(() -> line.markAsReceived(
                LocalDate.of(2026,5,10),
                "byTest",
                LocalDateTime.of(2026,5,10,12,0)))
                .isInstanceOf(InvalidStatusTransitionException.class);

        // 追加検証
        assertThat(line.getStatus()).isEqualTo(PoLineStatus.RECEIVED);
        assertThat(line.getReceivedBy()).isEqualTo("receiver");
        assertThat(line.getReceivedAt()).isEqualTo(RECEIVED_AT);
        assertThat(line.getUpdatedBy()).isEqualTo("updator");
        assertThat(line.getUpdatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    @DisplayName("正常系：ReceivedからOrderedへの遷移")
    void cancelReceiving_shouldChangeStatusToOrdered_whenReceived(){
        // 準備
        PurchaseOrderLine line = createReceivedLine();
        LocalDateTime updatedAtTest = LocalDateTime.of(2026,6,2,9,0);

        // 実行
        line.cancelReceiving("byTest", updatedAtTest);

        // 検証
        assertThat(line.getStatus()).isEqualTo(PoLineStatus.ORDERED);
        assertThat(line.getReceivedBy()).isNull();
        assertThat(line.getReceivedAt()).isNull();
        assertThat(line.getUpdatedBy()).isEqualTo("byTest");
        assertThat(line.getUpdatedAt()).isEqualTo(updatedAtTest);
    }

    @Test
    @DisplayName("異常系：未入荷の明細を取消すると例外")
    void cancelReceiving_shouldThrowException_whenAlreadyOrdered(){
        // 準備
        PurchaseOrderLine line = createOrderedLine();

        // 実行・検証
        assertThatThrownBy(() -> line.cancelReceiving("byTest", UPDATED_AT))
                .isInstanceOf(InvalidStatusTransitionException.class);

        // 追加検証
        assertThat(line.getStatus()).isEqualTo(PoLineStatus.ORDERED);
        assertThat(line.getReceivedBy()).isNull();
        assertThat(line.getReceivedAt()).isNull();
        assertThat(line.getUpdatedBy()).isEqualTo("creator");
        assertThat(line.getUpdatedAt()).isEqualTo(CREATED_AT);
    }

    // ヘルパーメソッド
    private PurchaseOrderLine createOrderedLine() {
        return new PurchaseOrderLine(
                1L, 10L, 100L, 1,
                new BigDecimal("5.000"), new BigDecimal("100.00"),
                DUE_DATE,                       // dueDate
                PoLineStatus.ORDERED,
                null, null, // receivedBy, receivedAt（未入荷なので null）
                "creator",
                CREATED_AT,                     // createdAt
                "creator",
                CREATED_AT                      // updatedAt（初期は createdAt と同じ）
        );
    }

    private PurchaseOrderLine createReceivedLine() {
        return new PurchaseOrderLine(
                1L, 10L, 100L, 1,
                new BigDecimal("5.000"), new BigDecimal("100.00"),
                DUE_DATE, // dueDate
                PoLineStatus.RECEIVED,
                "receiver",
                RECEIVED_AT,
                "creator",
                CREATED_AT, // createdAt
                "updator",
                UPDATED_AT // updatedAt
        );
    }
}
