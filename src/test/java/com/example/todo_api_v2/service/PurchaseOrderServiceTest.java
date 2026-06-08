package com.example.todo_api_v2.service;

import com.example.todo_api_v2.dto.purchaseorder.*;
import com.example.todo_api_v2.entity.PoLineStatus;
import com.example.todo_api_v2.entity.PoStatus;
import com.example.todo_api_v2.entity.PurchaseOrder;
import com.example.todo_api_v2.entity.PurchaseOrderLine;
import com.example.todo_api_v2.exception.DuplicatePoNumberException;
import com.example.todo_api_v2.exception.InvalidStatusTransitionException;
import com.example.todo_api_v2.exception.PurchaseOrderLineNotFoundException;
import com.example.todo_api_v2.exception.PurchaseOrderNotFoundException;
import com.example.todo_api_v2.mapper.PurchaseOrderLineMapper;
import com.example.todo_api_v2.mapper.PurchaseOrderMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PurchaseOrderServiceTest {

    // テストオブジェクト作成用フィールド
    private final String supplier = "testSupplier";
    private final LocalDate orderDate = LocalDate.of(2026,1,1);
    private final LocalDateTime createdAt = LocalDateTime.of(2026,1,1,9,0);
    private final String createdBy = "testUser";
    private final BigDecimal qty = new BigDecimal("10.000");
    private final BigDecimal qty2 = new BigDecimal("20.000");
    private final BigDecimal price = new BigDecimal("100.00");
    private final BigDecimal price2 = new BigDecimal("200.00");
    private final LocalDate dueDate = LocalDate.of(2026,6,1);
    private final LocalDate dueDate2 = LocalDate.of(2026,6,2);
    private final LocalDate receivedAt = LocalDate.of(2026,6,1);


    @Mock        private PurchaseOrderLineMapper purchaseOrderLineMapper;
    @Mock        private PurchaseOrderMapper     purchaseOrderMapper;
    @InjectMocks private PurchaseOrderService    purchaseOrderService;

    @Test @DisplayName("IDが存在する場合、ヘッダ+明細を返す")
    void testFindById_shouldReturnPurchaseOrderResponse_whenIdExists(){
        // Mapperの準備
        PurchaseOrder po = createTestPO(1L, "TEST-001");
        List<PurchaseOrderLine> lines = List.of(createTestPOL(
                1L,1L,1L,1, qty, price, dueDate));
        when(purchaseOrderMapper.findById(1L)).thenReturn(Optional.of(po));
        when(purchaseOrderLineMapper.findByPoId(1L)).thenReturn(lines);

        // 実行
        PurchaseOrderResponse testResponse = purchaseOrderService.findById(1L);

        // 検証
        assertPOResponse(testResponse, po, lines);
    }

    @Test @DisplayName("IDが存在しない場合、NotFoundを返す")
    void testFindById_shouldThrowPurchaseOrderNotFound_whenIdNotExists(){
        // Mapperの準備
        when(purchaseOrderMapper.findById(999L)).thenReturn(Optional.empty());

        // 検証
        assertThatThrownBy(()->
                purchaseOrderService.findById(999L))
                .isInstanceOf(PurchaseOrderNotFoundException.class)
                .hasMessageContaining("発注ヘッダがDBに存在しません");
    }

    @Test @DisplayName("PoNumberの重複がないとき、insertが実行できる(responseを検証)")
    void testCreate_shouldReturnPurchaseOrderResponse_whenPoNumberNotExists(){
        final Long EXPECTED_POID = 1L;
        // オブジェクトの準備
        PurchaseOrder po = createTestPO(EXPECTED_POID, "TEST-001");
        List<PurchaseOrderLine> poLines = List.of(
                createTestPOL(1L, EXPECTED_POID, 1L, 1, qty, price, dueDate));
        PurchaseOrderLineCreateRequest polRequest = new PurchaseOrderLineCreateRequest(
                1L, qty, price, dueDate);
        PurchaseOrderCreateRequest poRequest = new PurchaseOrderCreateRequest(
                "TEST-001", supplier, orderDate, List.of(polRequest));

        // Mapperの準備
        setupCreateMapper(EXPECTED_POID, po, poLines);

        // 実行
        PurchaseOrderResponse testPOResponse = purchaseOrderService.create(poRequest);

        // Response の検証
        assertPOResponse(testPOResponse, po, poLines);
    }

    @Test @DisplayName("複数明細がinsertされたとき、lineNoを連番で採番する")
    void testCreate_shouldAssignSequentialLineNo_whenMultipleLines(){
        final Long EXPECTED_POID = 1L;
        // オブジェクトの準備
        PurchaseOrder po = createTestPO(EXPECTED_POID, "TEST-001");
        List<PurchaseOrderLine> poLines = List.of(
                createTestPOL(1L, EXPECTED_POID, 1L, 1, qty, price, dueDate),
                createTestPOL(2L, EXPECTED_POID, 2L, 2, qty2, price2, dueDate2));
        PurchaseOrderLineCreateRequest polRequest1 = new PurchaseOrderLineCreateRequest(
                1L, qty, price, dueDate);
        PurchaseOrderLineCreateRequest polRequest2 = new PurchaseOrderLineCreateRequest(
                2L, qty2, price2, dueDate2);
        PurchaseOrderCreateRequest poRequest = new PurchaseOrderCreateRequest(
                "TEST-001", supplier, orderDate, List.of(polRequest1,polRequest2));

        // Mapperの準備
        setupCreateMapper(EXPECTED_POID, po, poLines);

        // 実行
        PurchaseOrderResponse testPOResponse = purchaseOrderService.create(poRequest);

        // insert直後の発注ヘッダEntityの検証(実行後のEntityをcapture)
        ArgumentCaptor<PurchaseOrder> poCaptor = ArgumentCaptor.forClass(PurchaseOrder.class);
        verify(purchaseOrderMapper).insert(poCaptor.capture());
        PurchaseOrder capturedPo = poCaptor.getValue();

        assertThat(capturedPo.getPoNumber()).isEqualTo("TEST-001");
        assertThat(capturedPo.getSupplier()).isEqualTo(supplier);
        assertThat(capturedPo.getOrderDate()).isEqualTo(orderDate);
        assertThat(capturedPo.getStatus()).isEqualTo(PoStatus.ORDERED);
        assertThat(capturedPo.getCreatedBy()).isEqualTo("system");

        // insert直後の明細リストEntityの検証
        ArgumentCaptor<List<PurchaseOrderLine>> linesCaptor = ArgumentCaptor.captor();
        verify(purchaseOrderLineMapper).insertLines(linesCaptor.capture());
        List<PurchaseOrderLine> capturedLines = linesCaptor.getValue();

        assertCapturedLines(capturedLines, poRequest, EXPECTED_POID);
    }

    @Test @DisplayName("PoNumberの重複があるとき、例外[DuplicatePoNumberException]を投げる")
    void testCreate_shouldThrowDuplicatePoNumberException_whenPoNumberExists(){
        // Reuestオブジェクトのみ準備
        PurchaseOrderLineCreateRequest polRequest = new PurchaseOrderLineCreateRequest(
                1L, qty, price, dueDate);
        PurchaseOrderCreateRequest poRequest = new PurchaseOrderCreateRequest(
                "TEST-001", supplier, orderDate, List.of(polRequest));

        // Mapperの準備(DBからのUK例外)
        doThrow(new DuplicateKeyException("")).when(purchaseOrderMapper).insert(any());

        // 実行
        assertThatThrownBy(()->
                purchaseOrderService.create(poRequest))
                .isInstanceOf(DuplicatePoNumberException.class)
                .hasMessageContaining("使用されています");
    }

    @Test @DisplayName("発注が複数件あるとき、全件をヘッダ+明細で返す")
    void testFindAll_shouldReturnPurchaseOrderResponseList_whenOrdersExist(){
        final Long EXPECTED_POID_1 = 1L;
        final Long EXPECTED_POID_2 = 2L;
        // オブジェクトの準備
        PurchaseOrder testPo1 = createTestPO(EXPECTED_POID_1, "TEST-001");
        PurchaseOrder testPo2 = createTestPO(EXPECTED_POID_2, "TEST-002");
        List<PurchaseOrder> testPoList = List.of(testPo1, testPo2);

        PurchaseOrderLine testPoL1 = createTestPOL(
                1L, EXPECTED_POID_1, 1L, 1, qty, price, dueDate
        );
        PurchaseOrderLine testPoL2 = createTestPOL(
                2L, EXPECTED_POID_1, 2L, 2, qty2, price2, dueDate2
        );
        PurchaseOrderLine testPoL3 = createTestPOL(
                3L, EXPECTED_POID_2, 1L, 1, qty, price, dueDate
        );
        List<PurchaseOrderLine> testPolList1 = List.of(testPoL1, testPoL2);
        List<PurchaseOrderLine> testPolList2 = List.of(testPoL3);
        List<List<PurchaseOrderLine>> testPolLines = List.of(testPolList1, testPolList2);

        // Mapperの準備
        when(purchaseOrderMapper.findAll()).thenReturn(testPoList);
        when(purchaseOrderLineMapper.findByPoId(EXPECTED_POID_1))
                .thenReturn(testPolList1);
        when(purchaseOrderLineMapper.findByPoId(EXPECTED_POID_2))
                .thenReturn(testPolList2);

        // 実行
        List<PurchaseOrderResponse> responses = purchaseOrderService.findAll();

        // 検証
        assertThat(responses).hasSize(2);
        for(int i=0 ; i < responses.size() ; i++){
            assertPOResponse(responses.get(i), testPoList.get(i), testPolLines.get(i));
        }

    }

    @Test @DisplayName("発注が0件のとき、空リストを返す")
    void testFindAll_shouldReturnEmptyList_whenNoOrders(){
        // Mapperの準備
        when(purchaseOrderMapper.findAll()).thenReturn(List.of());

        // 実行
        List<PurchaseOrderResponse> responses = purchaseOrderService.findAll();

        // 検証
        assertThat(responses).isEmpty();
    }

    @Test @DisplayName("一部の明細を入荷するとヘッダはORDEREDのまま、対象明細だけRECEIVEDになる")
    void receive_partialLines_keepsHeaderOrdered(){
        // オブジェクト準備
        PurchaseOrderLine line1 = createLine(1L, 1, PoLineStatus.ORDERED);
        PurchaseOrderLine line2 = createLine(2L, 2, PoLineStatus.ORDERED);
        List<PurchaseOrderLine> lines = List.of(line1, line2);
        PurchaseOrder po = createTestPO(1L, "TEST-001");
        ReceiveLineRequest request = new ReceiveLineRequest(receivedAt);

        // Mapperの準備
        when(purchaseOrderMapper.findById(1L)).thenReturn(Optional.of(po));
        when(purchaseOrderLineMapper.findByPoId(1L)).thenReturn(lines);

        // 実行
        purchaseOrderService.receive(1L, 1,request);

        // 検証
        verify(purchaseOrderMapper, times(1)).updatePoStatus(po);
        verify(purchaseOrderLineMapper, times(1)).updateReceipt(line1);
        verify(purchaseOrderLineMapper, never()).updateReceipt(line2);
        assertThat(po.getStatus()).isEqualTo(PoStatus.ORDERED);
        assertThat(line1.getStatus()).isEqualTo(PoLineStatus.RECEIVED);
        assertThat(line2.getStatus()).isEqualTo(PoLineStatus.ORDERED);
        assertThat(line1.getReceivedAt()).isEqualTo(receivedAt);
    }

    @Test @DisplayName("最後の未入荷明細を入荷するとヘッダがRECEIVEDになる")
    void receive_lastLine_marksHeaderReceived(){
        // オブジェクト準備
        PurchaseOrderLine line1 = createLine(1L, 1, PoLineStatus.RECEIVED);
        PurchaseOrderLine line2 = createLine(2L, 2, PoLineStatus.ORDERED);
        List<PurchaseOrderLine> lines = List.of(line1, line2);
        PurchaseOrder po = createTestPO(1L, "TEST-001");
        ReceiveLineRequest request = new ReceiveLineRequest(receivedAt);

        // Mapperの準備
        when(purchaseOrderMapper.findById(1L)).thenReturn(Optional.of(po));
        when(purchaseOrderLineMapper.findByPoId(1L)).thenReturn(lines);

        // 実行
        purchaseOrderService.receive(1L, 2,request);

        // 検証
        verify(purchaseOrderMapper, times(1)).updatePoStatus(po);
        verify(purchaseOrderLineMapper, never()).updateReceipt(line1);
        verify(purchaseOrderLineMapper, times(1)).updateReceipt(line2);
        assertThat(po.getStatus()).isEqualTo(PoStatus.RECEIVED);
        assertThat(line1.getStatus()).isEqualTo(PoLineStatus.RECEIVED);
        assertThat(line2.getStatus()).isEqualTo(PoLineStatus.RECEIVED);
        assertThat(line2.getReceivedAt()).isEqualTo(receivedAt);
    }

    @Test @DisplayName("存在しないpoIdを指定するとPurchaseOrderNotFoundExceptionを投げる")
    void receive_poNotFound_throwsException(){
        // Mapperの準備
        when(purchaseOrderMapper.findById(999L)).thenReturn(Optional.empty());

        // 実行・検証
        assertThatThrownBy(()->
                purchaseOrderService.receive(999L, 1, new ReceiveLineRequest(receivedAt)))
                .isInstanceOf(PurchaseOrderNotFoundException.class)
                .hasMessageContaining("DBに存在しません");
    }

    @Test @DisplayName("存在しないlineNoを指定するとPurchaseOrderLineNotFoundExceptionを投げる")
    void receive_lineNotFound_throwsException(){
        // オブジェクト準備
        PurchaseOrderLine line1 = createLine(1L, 1, PoLineStatus.ORDERED);
        PurchaseOrderLine line2 = createLine(2L, 2, PoLineStatus.ORDERED);
        List<PurchaseOrderLine> lines = List.of(line1, line2);
        PurchaseOrder po = createTestPO(1L, "TEST-001");
        ReceiveLineRequest request = new ReceiveLineRequest(receivedAt);

        // Mapperの準備
        when(purchaseOrderMapper.findById(1L)).thenReturn(Optional.of(po));
        when(purchaseOrderLineMapper.findByPoId(1L)).thenReturn(lines);

        // 実行・検証
        assertThatThrownBy(()->
                purchaseOrderService.receive(1L, 99, request))
                .isInstanceOf(PurchaseOrderLineNotFoundException.class)
                .hasMessageContaining("明細が見つかりません");
        verify(purchaseOrderMapper, never()).updatePoStatus(any());
        verify(purchaseOrderLineMapper, never()).updateReceipt(any());
    }

    @Test @DisplayName("入荷済みの明細を再度入荷するとInvalidStatusTransitionExceptionを投げる")
    void receive_alreadyReceived_throwsException(){
        // オブジェクト準備
        PurchaseOrderLine line1 = createLine(1L, 1, PoLineStatus.RECEIVED);
        PurchaseOrderLine line2 = createLine(2L, 2, PoLineStatus.ORDERED);
        List<PurchaseOrderLine> lines = List.of(line1, line2);
        PurchaseOrder po = createTestPO(1L, "TEST-001");
        ReceiveLineRequest request = new ReceiveLineRequest(receivedAt);

        // Mapperの準備
        when(purchaseOrderMapper.findById(1L)).thenReturn(Optional.of(po));
        when(purchaseOrderLineMapper.findByPoId(1L)).thenReturn(lines);

        // 実行・検証
        assertThatThrownBy(()->
                purchaseOrderService.receive(1L, 1, request))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("不正遷移");
        verify(purchaseOrderMapper, never()).updatePoStatus(any());
        verify(purchaseOrderLineMapper, never()).updateReceipt(any());
    }


    /**
     * Mapperを準備するヘルパーメソッド
     *
     * @param poId insertするPurchaseOrderのId
     * @param po insertするPurchaseOrder
     * @param poLines insertするList<PurchaseOrderLine>
     */
    private void setupCreateMapper(Long poId, PurchaseOrder po, List<PurchaseOrderLine> poLines){
        // insertしたときのDB返り値を無理やり詰める
        doAnswer(invocation ->{
            // Serviceから渡されたEntityを捕まえる
            PurchaseOrder passedPo = invocation.getArgument(0);
            // ReflectionTestUtilsを使って、privateフィールドに無理やりセットする
            ReflectionTestUtils.setField(passedPo, "id", poId);

            return null;
        }).when(purchaseOrderMapper).insert(any(PurchaseOrder.class));

        when(purchaseOrderMapper.findById(poId)).thenReturn(Optional.of(po));
        when(purchaseOrderLineMapper.findByPoId(poId)).thenReturn(poLines);
    }

    /**
     * PurchaseOrderMapper返信用のオブジェクト作成のヘルパーメソッド
     *
     * @param id Poid(PK)
     * @param poNumber poNumber(UK)
     * @return PurchaseOrder
     */
    private PurchaseOrder createTestPO(Long id, String poNumber){
        return new PurchaseOrder(
                        id,
                        poNumber,
                        supplier,
                        orderDate,
                        PoStatus.ORDERED,
                        createdAt,
                        createdBy,
                        createdAt,
                        createdBy
                );
    }

    /**
     * 明細1行分を作成
     *
     * @param id POLのid(PK)
     * @param poId POのid(FK)
     * @param itemId Itemのid(FK)
     * @param lineNo 明細行の番号
     * @param qty 数量
     * @param price 価格
     * @param dueDate 納期
     * @return PurchaseOrderLine
     */
    private PurchaseOrderLine createTestPOL(Long id, Long poId,Long itemId, Integer lineNo,
                                            BigDecimal qty, BigDecimal price, LocalDate dueDate){
        return new PurchaseOrderLine(
                id,
                poId,
                itemId,
                lineNo,
                qty,
                price,
                dueDate,
                PoLineStatus.ORDERED,
                null,
                null,
                createdBy,
                createdAt,
                createdBy,
                createdAt
        );
    }

    /**
     * statusを変更できる明細行を作るヘルパーメソッド
     *
     * @param id　PurchaseOrderLineのid(PK)
     * @param lineNo 行番号(Intger)
     * @param status PoLineStatus
     * @return PurchaseOrderLine
     */
    private PurchaseOrderLine createLine(Long id, Integer lineNo, PoLineStatus status){
        return new PurchaseOrderLine(
                id, 1L, 1L, lineNo, qty, price,
                dueDate, status, null, null, createdBy, createdAt, null, null);
    }

    /**
     * POヘッダのresponseが正しいかを検証するヘルパーメソッド
     *
     * @param response Serviceからのresponse
     * @param po Mapperで用意したPurchaseOrder
     * @param lines Mapperで用意したPurchaseOrderLine一覧
     */
    private void assertPOResponse(PurchaseOrderResponse response, PurchaseOrder po, List<PurchaseOrderLine> lines){
        assertThat(response.id()).isEqualTo(po.getId());
        assertThat(response.poNumber()).isEqualTo(po.getPoNumber());
        assertThat(response.supplier()).isEqualTo(po.getSupplier());
        assertThat(response.orderDate()).isEqualTo(po.getOrderDate());
        assertThat(response.status()).isEqualTo(po.getStatus());
        assertThat(response.createdBy()).isEqualTo(po.getCreatedBy());
        assertThat(response.createdAt()).isEqualTo(po.getCreatedAt());
        assertThat(response.updatedBy()).isEqualTo(po.getUpdatedBy());
        assertThat(response.updatedAt()).isEqualTo(po.getUpdatedAt());

        assertPOLResponse(response, lines);
    }

    /**
     * 明細responseが正しいかを検証するヘルパーメソッド
     *
     * @param response Serviceからのresponse
     * @param lines Mapperで用意したPurchaseOrderLine一覧
     */
    private void assertPOLResponse(PurchaseOrderResponse response, List<PurchaseOrderLine> lines){
        List<PurchaseOrderLineResponse> responseLines = response.lines();
        assertThat(responseLines).hasSize(lines.size());
        for(int i = 0; i < lines.size(); i++){
            assertThat(responseLines.get(i).id()).isEqualTo(lines.get(i).getId());
            assertThat(responseLines.get(i).poId()).isEqualTo(lines.get(i).getPoId());
            assertThat(responseLines.get(i).lineNo()).isEqualTo(lines.get(i).getLineNo());
            assertThat(responseLines.get(i).qty()).isEqualByComparingTo(lines.get(i).getQty());
            assertThat(responseLines.get(i).price()).isEqualByComparingTo(lines.get(i).getPrice());
            assertThat(responseLines.get(i).dueDate()).isEqualTo(lines.get(i).getDueDate());
            assertThat(responseLines.get(i).status()).isEqualTo(lines.get(i).getStatus());
            assertThat(responseLines.get(i).receivedBy()).isEqualTo(lines.get(i).getReceivedBy());
            assertThat(responseLines.get(i).receivedAt()).isEqualTo(lines.get(i).getReceivedAt());
            assertThat(responseLines.get(i).createdBy()).isEqualTo(lines.get(i).getCreatedBy());
            assertThat(responseLines.get(i).createdAt()).isEqualTo(lines.get(i).getCreatedAt());
            assertThat(responseLines.get(i).updatedBy()).isEqualTo(lines.get(i).getUpdatedBy());
            assertThat(responseLines.get(i).updatedAt()).isEqualTo(lines.get(i).getUpdatedAt());
        }
    }

    /**
     * insert後の明細をcaptureして検証するためのヘルパーメソッド
     *
     * @param captured captureされたList<PurchaseOrderLine>
     * @param expected requestの明細リストList<PurchaseOrderCreateRequest>
     * @param poId 発注ヘッダのId
     */
    private void assertCapturedLines(List<PurchaseOrderLine> captured, PurchaseOrderCreateRequest expected, Long poId){
        List<PurchaseOrderLineCreateRequest> requests = expected.lines();
        assertThat(requests).hasSize(captured.size());
        for(int i = 0; i < captured.size(); i++){
            assertThat(captured.get(i).getLineNo()).isEqualTo(i+1);
            assertThat(captured.get(i).getPoId()).isEqualTo(poId);
            assertThat(captured.get(i).getItemId()).isEqualTo(requests.get(i).itemId());
            assertThat(captured.get(i).getQty()).isEqualByComparingTo(requests.get(i).qty());
            assertThat(captured.get(i).getPrice()).isEqualByComparingTo(requests.get(i).price());
            assertThat(captured.get(i).getDueDate()).isEqualTo(requests.get(i).dueDate());
        }
    }
}
