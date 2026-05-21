package com.example.todo_api_v2.entity;

import com.example.todo_api_v2.exception.EmptyPurchaseOrderLineException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PurchaseOrderTest {
    private static final LocalDateTime CREATED_AT  = LocalDateTime.of(2026, 5, 1, 9, 0);
    private static final LocalDate     ORDER_DATE  = LocalDate.of(2026, 5, 1);
    private static final LocalDate     RECEIVED_AT = LocalDate.of(2026,5,31);
    private static final LocalDateTime UPDATED_AT  = LocalDateTime.of(2026,5,31,12,0);


    @Test
    @DisplayName("異常系：明細リストがnullなら例外")
    void refreshStatus_shouldThrowException_whenLinesIsNull(){
        // 準備
        PurchaseOrder po = createOrderedHeader();

        // 実行・検証
        assertThatThrownBy(() -> po.refreshStatus(null,"byTest",UPDATED_AT))
                .isInstanceOf(NullPointerException.class);

        // 追加検証
        assertThat(po.getStatus()).isEqualTo(PoStatus.ORDERED);
        assertThat(po.getUpdatedAt()).isEqualTo(CREATED_AT);
        assertThat(po.getUpdatedBy()).isEqualTo("creator");
    }

    @Test
    @DisplayName("異常系：明細リストが空なら例外")
    void refreshStatus_shouldThrowException_whenLinesIsEmpty(){
        // 準備
        PurchaseOrder po = createOrderedHeader();

        // 実行・検証
        assertThatThrownBy(() -> po.refreshStatus(List.of(),"byTest",UPDATED_AT))
                .isInstanceOf(EmptyPurchaseOrderLineException.class);

        // 追加検証
        assertThat(po.getStatus()).isEqualTo(PoStatus.ORDERED);
        assertThat(po.getUpdatedAt()).isEqualTo(CREATED_AT);
        assertThat(po.getUpdatedBy()).isEqualTo("creator");
    }

    @Test
    @DisplayName("正常系：全明細入荷済みでヘッダがReceivedに変化")
    void refreshStatus_shouldChangeStatusToReceived_whenAllLinesReceived(){
        // 準備
        PurchaseOrder po = createOrderedHeader();

        // 実行
        po.refreshStatus(createAllReceivedLines(),"byTest",UPDATED_AT);

        // 検証
        assertThat(po.getStatus()).isEqualTo(PoStatus.RECEIVED);
        assertThat(po.getUpdatedAt()).isEqualTo(UPDATED_AT);
        assertThat(po.getUpdatedBy()).isEqualTo("byTest");
    }

    @Test
    @DisplayName("正常系：計算結果が同じ(Received)なら更新しない")
    void refreshStatus_shouldNotUpdate_whenStatusUnchangedFromReceived(){
        // 準備
        PurchaseOrder po = createReceivedHeader();

        // 実行
        po.refreshStatus(createAllReceivedLines(),"byTest",UPDATED_AT);

        // 検証
        assertThat(po.getStatus()).isEqualTo(PoStatus.RECEIVED);
        assertThat(po.getUpdatedAt()).isEqualTo(CREATED_AT);
        assertThat(po.getUpdatedBy()).isEqualTo("creator");
    }

    @Test
    @DisplayName("正常系：計算結果が同じ(Ordered)なら更新しない")
    void refreshStatus_shouldNotUpdate_whenStatusUnchangedFromOrdered(){
        // 準備
        PurchaseOrder po = createOrderedHeader();

        // 実行
        po.refreshStatus(createPartiallyOrderedLines(),"byTest",UPDATED_AT);

        // 検証
        assertThat(po.getStatus()).isEqualTo(PoStatus.ORDERED);
        assertThat(po.getUpdatedAt()).isEqualTo(CREATED_AT);
        assertThat(po.getUpdatedBy()).isEqualTo("creator");
    }

    @Test
    @DisplayName("正常系：一部明細が未入荷でヘッダがOrderedに変化")
    void refreshStatus_shouldChangeStatusToOrdered_whenSomeLineOrdered(){
        // 準備
        PurchaseOrder po = createReceivedHeader();

        // 実行
        po.refreshStatus(createPartiallyOrderedLines(),"byTest",UPDATED_AT);

        // 検証
        assertThat(po.getStatus()).isEqualTo(PoStatus.ORDERED);
        assertThat(po.getUpdatedAt()).isEqualTo(UPDATED_AT);
        assertThat(po.getUpdatedBy()).isEqualTo("byTest");
    }

    // PurchaseOrderを作るヘルパーメソッド
    private PurchaseOrder createOrderedHeader(){
        return new PurchaseOrder(
                1L,
                "PO-TEST",
                "supplier",
                ORDER_DATE,
                PoStatus.ORDERED,
                CREATED_AT,
                "creator",
                CREATED_AT,
                "creator"
        );
    }

    private PurchaseOrder createReceivedHeader(){
        return new PurchaseOrder(
                1L,
                "PO-TEST",
                "supplier",
                ORDER_DATE,
                PoStatus.RECEIVED,
                CREATED_AT,
                "creator",
                CREATED_AT,
                "creator"
        );
    }

    // List<PurchaseOrderLine>を作るヘルパーメソッド
    // 全明細がRECEIVED
    private List<PurchaseOrderLine> createAllReceivedLines(){
        return List.of(
                createLine(PoLineStatus.RECEIVED),
                createLine(PoLineStatus.RECEIVED)
        );
    }

    // 一部明細にORDERED
    private List<PurchaseOrderLine> createPartiallyOrderedLines() {
        return List.of(
                createLine(PoLineStatus.RECEIVED),
                createLine(PoLineStatus.ORDERED)
        );
    }

    // PurchaseOrderLineを作るヘルパーメソッド(status以外はどうでもよい)
    private PurchaseOrderLine createLine(PoLineStatus status) {
        return new PurchaseOrderLine(
                1L, 1L, 1L, 1,
                new BigDecimal("1"), new BigDecimal("1"),
                LocalDate.of(2026,1,1),
                status, // 引数で受け取る
                null, null,
                "creator",
                LocalDateTime.of(2026,1,1,0,0),
                null,
                null
        );
    }
}
