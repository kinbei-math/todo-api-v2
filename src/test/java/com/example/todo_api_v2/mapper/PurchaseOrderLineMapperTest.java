package com.example.todo_api_v2.mapper;

import com.example.todo_api_v2.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class PurchaseOrderLineMapperTest {

    @Autowired
    private PurchaseOrderLineMapper purchaseOrderLineMapper;
    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;
    @Autowired
    private ItemMapper itemMapper;

    @Test @DisplayName("正常系：複数の明細行をinsertしたときに正常に保存できている")
    void insertLines_shouldBulkInsert(){
        // 準備
        Item          item1 = createTestItem("testItem-001", "item1");
        Item          item2 = createTestItem("testItem-002", "item2");
        PurchaseOrder po    = createTestPo("testPo-001");
        itemMapper.insert(item1);
        itemMapper.insert(item2);
        purchaseOrderMapper.insert(po);

        PurchaseOrderLine line1 = createTestPoLine(po.getId(), item1.getId(), 1);
        PurchaseOrderLine line2 = createTestPoLine(po.getId(), item2.getId(), 2);
        List<PurchaseOrderLine> lines = List.of(line1, line2);

        // 実行
        purchaseOrderLineMapper.insertLines(lines);
        List<PurchaseOrderLine> findLine = purchaseOrderLineMapper.findByPoId(po.getId());

        // 検証
        assertThat(findLine).hasSize(2);
        assertPoLine(line1 ,findLine.get(0));
        assertPoLine(line2 ,findLine.get(1));
    }

    @Test @DisplayName("正常系：特定のpoIdのみの発注明細だけ返す")
    void findByPoId_shouldReturnOnlyMatchingPoLines_whenMultiplePoExist(){
        // 準備
        Item          item1 = createTestItem("testItem-001", "item1");
        Item          item2 = createTestItem("testItem-002", "item2");
        Item          item3 = createTestItem("testItem-003", "item3");
        PurchaseOrder po1   = createTestPo("testPo-001");
        PurchaseOrder po2   = createTestPo("testPo-002");
        itemMapper.insert(item1);
        itemMapper.insert(item2);
        itemMapper.insert(item3);
        purchaseOrderMapper.insert(po1);
        purchaseOrderMapper.insert(po2);

        PurchaseOrderLine line1 = createTestPoLine(po2.getId(), item1.getId(), 1);
        PurchaseOrderLine line2 = createTestPoLine(po2.getId(), item2.getId(), 2);
        PurchaseOrderLine line3 = createTestPoLine(po1.getId(), item3.getId(), 1);
        List<PurchaseOrderLine> lines1 = List.of(line1, line2);
        List<PurchaseOrderLine> lines2 = List.of(line3);

        // 実行
        purchaseOrderLineMapper.insertLines(lines1);
        purchaseOrderLineMapper.insertLines(lines2);
        List<PurchaseOrderLine> findLine = purchaseOrderLineMapper.findByPoId(po2.getId());

        // 検証
        assertThat(findLine).hasSize(2);
        assertPoLine(line1 ,findLine.get(0));
        assertPoLine(line2 ,findLine.get(1));
    }

    @Test @DisplayName("正常系：findByPoIdでlineNo順で返る")
    void findByPoId_shouldReturnLinesOrderedByLineNo(){
        // 準備
        Item          item1 = createTestItem("testItem-001", "item1");
        Item          item2 = createTestItem("testItem-002", "item2");
        Item          item3 = createTestItem("testItem-003", "item3");
        PurchaseOrder po    = createTestPo("testPo-001");
        itemMapper.insert(item1);
        itemMapper.insert(item2);
        itemMapper.insert(item3);
        purchaseOrderMapper.insert(po);

        PurchaseOrderLine line1 = createTestPoLine(po.getId(), item1.getId(), 3);
        PurchaseOrderLine line2 = createTestPoLine(po.getId(), item2.getId(), 1);
        PurchaseOrderLine line3 = createTestPoLine(po.getId(), item3.getId(), 2);
        List<PurchaseOrderLine> lines = List.of(line1, line2, line3);

        // 実行
        purchaseOrderLineMapper.insertLines(lines);
        List<PurchaseOrderLine> findLine = purchaseOrderLineMapper.findByPoId(po.getId());

        // 検証
        assertThat(findLine).hasSize(3);
        assertPoLine(line2 ,findLine.get(0));
        assertPoLine(line3 ,findLine.get(1));
        assertPoLine(line1 ,findLine.get(2));
    }

    @Test @DisplayName("正常系：明細が存在しないとき、0件で返る")
    void findByPoId_shouldReturnEmptyList_whenNoLines(){
        // 実行
        List<PurchaseOrderLine> findLine = purchaseOrderLineMapper.findByPoId(999L);

        // 検証
        assertThat(findLine).isEmpty();
    }

    @Test @DisplayName("正常系：statusを入荷済みに更新。期待するデータのみ更新")
    void updateReceipt_shouldUpdateSpecificFields(){
        // 準備
        Item          item1 = createTestItem("testItem-001", "item1");
        Item          item2 = createTestItem("testItem-002", "item2");
        PurchaseOrder po    = createTestPo("testPo-001");
        itemMapper.insert(item1);
        itemMapper.insert(item2);
        purchaseOrderMapper.insert(po);

        PurchaseOrderLine line1 = createTestPoLine(po.getId(), item1.getId(), 1);
        PurchaseOrderLine line2 = createTestPoLine(po.getId(), item2.getId(), 2);
        List<PurchaseOrderLine> lines = List.of(line1, line2);
        purchaseOrderLineMapper.insertLines(lines);

        PurchaseOrderLine updatePoLine = new PurchaseOrderLine(
                line1.getId(),
                null,
                null,
                1,
                null,
                null,
                null,
                PoLineStatus.RECEIVED,
                "testUser",
                LocalDate.of(2026,5,31),
                null,
                null,
                "testUser",
                LocalDateTime.of(2026,5,31,9,0)
        );

        // 実行
        List<PurchaseOrderLine> beforePoLines = purchaseOrderLineMapper.findByPoId(po.getId());
        purchaseOrderLineMapper.updateReceipt(updatePoLine);
        List<PurchaseOrderLine> afterPoLines = purchaseOrderLineMapper.findByPoId(po.getId());

        // 検証
        assertUpdatePoLine(beforePoLines.get(0), updatePoLine, afterPoLines.get(0));
        assertPoLine(beforePoLines.get(1),afterPoLines.get(1));
    }

    @Test @DisplayName("正常系：入荷を取消、ORDEREDに戻す。")
    void updateReceiptCancellation_shouldUpdateSpecificFields(){
        // 準備
        Item          item1 = createTestItem("testItem-001", "item1");
        Item          item2 = createTestItem("testItem-002", "item2");
        PurchaseOrder po    = createTestPo("testPo-001");
        itemMapper.insert(item1);
        itemMapper.insert(item2);
        purchaseOrderMapper.insert(po);

        PurchaseOrderLine line1 = createTestPoLine(po.getId(), item1.getId(), 1);
        PurchaseOrderLine line2 = createTestPoLine(po.getId(), item2.getId(), 2);
        List<PurchaseOrderLine> lines = List.of(line1, line2);
        purchaseOrderLineMapper.insertLines(lines);

        PurchaseOrderLine updateReceivedPoLine = new PurchaseOrderLine(
                line1.getId(),
                null,
                null,
                1,
                null,
                null,
                null,
                PoLineStatus.RECEIVED,
                "testUser",
                LocalDate.of(2026,5,31),
                null,
                null,
                "testUser",
                LocalDateTime.of(2026,5,31,9,0)
        );
        PurchaseOrderLine updateReceiveCancelPoLine =new PurchaseOrderLine(
                line1.getId(),
                null,
                null,
                1,
                null,
                null,
                null,
                PoLineStatus.ORDERED,
                null,
                null,
                null,
                null,
                "testCancel",
                LocalDateTime.of(2026,6,10,9,0)
        );

        // 実行
        purchaseOrderLineMapper.updateReceipt(updateReceivedPoLine);
        List<PurchaseOrderLine> beforePoLines = purchaseOrderLineMapper.findByPoId(po.getId());

        purchaseOrderLineMapper.updateReceiptCancellation(updateReceiveCancelPoLine);
        List<PurchaseOrderLine> afterPoLines = purchaseOrderLineMapper.findByPoId(po.getId());

        // 検証
        assertUpdatePoLine(beforePoLines.get(0), updateReceiveCancelPoLine, afterPoLines.get(0));
        assertThat(afterPoLines.get(0).getReceivedAt()).isNull();
        assertThat(afterPoLines.get(0).getReceivedBy()).isNull();
        assertPoLine(beforePoLines.get(1),afterPoLines.get(1));
    }



    /**
     * PurchaseOrderLineクラス作成のヘルプメソッド
     * lineNoはUKなので気を付ける
     */
    private PurchaseOrderLine createTestPoLine(Long poId, Long itemId, Integer lineNo){
        return PurchaseOrderLine.createNew(
                poId, itemId, lineNo, new BigDecimal("10.000"), new BigDecimal("100.00"),
                LocalDate.of(2026,6,1),"user"
        );
    }
    /**
     * PurchaseOrderクラス作成のヘルプメソッド(PurchaseOrderMapperTestと同じ)
     */
    private PurchaseOrder createTestPo(String poNumber){
        return PurchaseOrder.createNew(
                poNumber,
                "testSupplier",
                LocalDate.of(2026,6,1),
                "user");
    }
    /**
     * itemクラス作成用のヘルプメソッド(ItemMapperTestと同じ)
     * itemCodeはUKに気を付ける
     */
    private Item createTestItem(String itemCode, String name){
        Item item = new Item();
        item.setItemCode(itemCode);
        item.setName(name);
        item.setUom(UomType.PC);
        item.setCategory(Category.RAW_MATERIAL);
        return item;
    }

    /**
     * insertされたデータが正しく保存されているかを検証するヘルパーメソッド
     *
     * @param line 期待するline
     * @param findLine 検索結果
     */
    private void assertPoLine(PurchaseOrderLine line, PurchaseOrderLine findLine){
        // newCretaeの時に指定する値
        assertThat(findLine.getId()).isEqualTo(line.getId());
        assertThat(findLine.getPoId()).isEqualTo(line.getPoId());
        assertThat(findLine.getItemId()).isEqualTo(line.getItemId());
        assertThat(findLine.getLineNo()).isEqualTo(line.getLineNo());
        assertThat(findLine.getQty()).isEqualByComparingTo(line.getQty());
        assertThat(findLine.getPrice()).isEqualByComparingTo(line.getPrice());
        assertThat(findLine.getDueDate()).isEqualTo(line.getDueDate());
        assertThat(findLine.getStatus()).isEqualTo(line.getStatus());
        assertThat(findLine.getCreatedBy()).isEqualTo(line.getCreatedBy());
        assertThat(findLine.getUpdatedBy()).isEqualTo(line.getUpdatedBy());

        // DBで自動で付与されるので存在確認のみ
        assertThat(findLine.getCreatedAt()).isNotNull();
        assertThat(findLine.getUpdatedAt()).isNotNull();

        // insert時にはnull
        assertThat(findLine.getReceivedAt()).isNull();
        assertThat(findLine.getReceivedBy()).isNull();
    }

    /**
     * updateで更新されていてほしい値だけが更新されているかを確認するヘルパーメソッド
     * <p>
     * 更新フィールド: status,receiveBy,receiveAt,updateBy,updateAt
     * 非更新フィールド: id,poId,itemId,lineNo,qty,price,dueDate,createdBy,createdAt
     *
     * @param beforePoLine update前のPoLineのデータ
     * @param updatePoLine updateに使用するPoLineデータ
     * @param afterPoLine update後のDB登録PoLineデータ
     */
    private void assertUpdatePoLine(
            PurchaseOrderLine beforePoLine,PurchaseOrderLine updatePoLine, PurchaseOrderLine afterPoLine){
        // 更新フィールドの確認
        assertThat(afterPoLine.getStatus()).isEqualTo(updatePoLine.getStatus());
        assertThat(afterPoLine.getReceivedBy()).isEqualTo(updatePoLine.getReceivedBy());
        assertThat(afterPoLine.getReceivedAt()).isEqualTo(updatePoLine.getReceivedAt());
        assertThat(afterPoLine.getUpdatedBy()).isEqualTo(updatePoLine.getUpdatedBy());
        assertThat(afterPoLine.getUpdatedAt()).isEqualTo(updatePoLine.getUpdatedAt());

        // 非更新フィールド
        assertThat(afterPoLine.getId()).isEqualTo(beforePoLine.getId());
        assertThat(afterPoLine.getPoId()).isEqualTo(beforePoLine.getPoId());
        assertThat(afterPoLine.getItemId()).isEqualTo(beforePoLine.getItemId());
        assertThat(afterPoLine.getLineNo()).isEqualTo(beforePoLine.getLineNo());
        assertThat(afterPoLine.getQty()).isEqualByComparingTo(beforePoLine.getQty());
        assertThat(afterPoLine.getPrice()).isEqualByComparingTo(beforePoLine.getPrice());
        assertThat(afterPoLine.getDueDate()).isEqualTo(beforePoLine.getDueDate());
        assertThat(afterPoLine.getCreatedBy()).isEqualTo(beforePoLine.getCreatedBy());
        assertThat(afterPoLine.getCreatedAt()).isEqualTo(beforePoLine.getCreatedAt());
    }
}
