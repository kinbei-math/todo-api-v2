package com.example.todo_api_v2.mapper;

import com.example.todo_api_v2.entity.PoStatus;
import com.example.todo_api_v2.entity.PurchaseOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class PurchaseOrderMapperTest {

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Test @DisplayName("正常系：INSERT後にIDが採番される")
    void insert_shouldGenerateId(){
        // 準備
        PurchaseOrder po = createTestPo("test-001");

        // 実行
        purchaseOrderMapper.insert(po);

        // 検証
        assertThat(po.getId()).isNotNull();
        assertThat(po.getId()).isPositive();
    }

    @Test @DisplayName("正常系：idで検索した発注ヘッダが全フィールド一致で取得できる")
    void findById_shouldReturnInsertPurchaseOrder(){
        // insertしてDBに登録
        PurchaseOrder po = createTestPo("test-001");
        purchaseOrderMapper.insert(po);

        // 実行
        Optional<PurchaseOrder> poOptional = purchaseOrderMapper.findById(po.getId());

        // 検証
        assertThat(poOptional).isPresent();      // 存在確認
        PurchaseOrder findPo = poOptional.get(); // 取り出し
        assertPurchaseOrder(po, findPo);
    }

    @Test @DisplayName("正常系：存在しないIdで検索した場合はEmptyが返る")
    void findById_shouldReturnEmpty_whenPoNotExists(){
        // 実行
        Optional<PurchaseOrder> poOptional = purchaseOrderMapper.findById(999L);

        // 検証
        assertThat(poOptional).isEmpty();
    }

    @Test @DisplayName("正常系：Poヘッダ一覧を取得できる")
    void findAll_shouldReturnAllPurchaseOrder(){
        // 準備
        PurchaseOrder po1 = createTestPo("test-001");
        PurchaseOrder po2 = createTestPo("test-002");
        PurchaseOrder po3 = createTestPo("test-003");

        purchaseOrderMapper.insert(po1);
        purchaseOrderMapper.insert(po2);
        purchaseOrderMapper.insert(po3);

        // 実行
        List<PurchaseOrder> findPoList = purchaseOrderMapper.findAll();

        // 検証
        assertThat(findPoList).hasSize(3);
        PurchaseOrder actualPo1 = actualPo(findPoList, po1);
        PurchaseOrder actualPo2 = actualPo(findPoList, po2);
        PurchaseOrder actualPo3 = actualPo(findPoList, po3);
        assertPurchaseOrder(po1,actualPo1);
        assertPurchaseOrder(po2,actualPo2);
        assertPurchaseOrder(po3,actualPo3);
    }

    @Test @DisplayName("正常系：データが存在しない場合、リストは0件")
    void findAll_shouldReturnEmptyList_whenNoData(){
        // 実行
        List<PurchaseOrder> findPoList = purchaseOrderMapper.findAll();

        // 検証
        assertThat(findPoList).isEmpty();
    }

    @Test @DisplayName("正常系：updateで期待するデータのみ更新")
    void updatePoStatus_shouldUpdateSpecificFields(){
        //　準備
        PoStatus      poStatus  = PoStatus.RECEIVED;
        LocalDateTime updatedAt = LocalDateTime.of(2026,5,1,9,0);
        String        updatedBy = "updatedUser";

        PurchaseOrder po = createTestPo("test-001");
        purchaseOrderMapper.insert(po);
        PurchaseOrder findPo = purchaseOrderMapper.findById(po.getId()).get();

        PurchaseOrder updatePo = new PurchaseOrder(
                po.getId(), null, null, null,
                poStatus, null, null,
                updatedAt, updatedBy);

        // 実行
        purchaseOrderMapper.updatePoStatus(updatePo);
        PurchaseOrder findUpdatePo = purchaseOrderMapper.findById(po.getId()).get();

        // 検証(poStatus, updatedAt, updatedByのみ更新されている)
        assertThat(findUpdatePo.getId()).isEqualTo(findPo.getId());
        assertThat(findUpdatePo.getPoNumber()).isEqualTo(findPo.getPoNumber());
        assertThat(findUpdatePo.getSupplier()).isEqualTo(findPo.getSupplier());
        assertThat(findUpdatePo.getOrderDate()).isEqualTo(findPo.getOrderDate());
        assertThat(findUpdatePo.getStatus()).isEqualTo(poStatus);
        assertThat(findUpdatePo.getCreatedAt()).isEqualTo(findPo.getCreatedAt());
        assertThat(findUpdatePo.getCreatedBy()).isEqualTo(findPo.getCreatedBy());
        assertThat(findUpdatePo.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(findUpdatePo.getUpdatedBy()).isEqualTo(updatedBy);
    }

    /**
     * ヘルパーメソッド
     * test用データの作成
     */
    private PurchaseOrder createTestPo(String poNumber){
        return PurchaseOrder.createNew(
                poNumber,
                "testSupplier",
                LocalDate.of(2026,6,1),
                "user");
    }

    /**
     * DBから注入されたデータがinsertしたデータと正しいことを確認する。
     * <p>
     * 中身を検証するフィールド:
     * id, poNumber, supplier, orderDate, poStatus, createdBy, updatedBy
     * <p>
     * 存在を検証するフィールド:
     * createdAt, updatedAt
     *<p>
     * @param findPo 検証されるPurchaseOrder(find)
     * @param po     期待されるPurchaseOrder(insert)
     */
    private void assertPurchaseOrder(PurchaseOrder po, PurchaseOrder findPo){
        assertThat(findPo.getId()).isEqualTo(po.getId());
        assertThat(findPo.getPoNumber()).isEqualTo(po.getPoNumber());
        assertThat(findPo.getSupplier()).isEqualTo(po.getSupplier());
        assertThat(findPo.getOrderDate()).isEqualTo(po.getOrderDate());
        assertThat(findPo.getStatus()).isEqualTo(po.getStatus());
        assertThat(findPo.getCreatedAt()).isNotNull();
        assertThat(findPo.getCreatedBy()).isEqualTo(po.getCreatedBy());
        assertThat(findPo.getUpdatedAt()).isNotNull();
        assertThat(findPo.getUpdatedBy()).isEqualTo(po.getUpdatedBy());
    }

    /**
     * Listの中からinsertしたPurchaseOrderを探すヘルパーメソッド
     *
     * @param poList findAllで得られたList<PurchaseOrder>
     * @param purchaseOrder insertしたpo
     */
    private PurchaseOrder actualPo(List<PurchaseOrder> poList,PurchaseOrder purchaseOrder){
        return   poList.stream()
                .filter(po -> po.getId().equals(purchaseOrder.getId()))
                .findFirst()
                .orElseThrow(); // 見つからなければテスト失敗になる
    }
}
