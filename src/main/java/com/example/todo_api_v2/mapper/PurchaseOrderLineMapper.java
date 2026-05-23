package com.example.todo_api_v2.mapper;

import com.example.todo_api_v2.entity.PurchaseOrderLine;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * purchase_order_linesテーブルに対して、操作を行うためのMapper
 * <p>
 * insert(明細リスト一括),find,update(statusの遷移 2方向)
 */
@Mapper
public interface PurchaseOrderLineMapper {

    /**
     * 明細リストを一括
     */
    void insertLines(List<PurchaseOrderLine> lines);

    /**
     * 明細リストを取得(発注ヘッダidに応じて)
     */
    List<PurchaseOrderLine> findByPoId(Long poId);

    /**
     * 明細1行を入荷済みにする
     * <p>
     * status・受領者・受領日・更新者・更新日を更新する
     */
    void updateReceipt(PurchaseOrderLine line);

    /**
     * 明細1行を入荷取消する
     * <p>
     * status・更新者・更新日を更新する。受領者・受領日をクリアする
     *
     */
    void updateReceiptCancellation(PurchaseOrderLine line);
}