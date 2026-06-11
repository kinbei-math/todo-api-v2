package com.example.todo_api_v2.mapper;

import com.example.todo_api_v2.entity.PurchaseOrder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * purchase_ordersテーブルに対して操作を行うためのMapper
 * <p>
 * insert,find,updateのメソッドがある。
 */
@Mapper
public interface PurchaseOrderMapper {

    /**
     * 発注ヘッダを1件登録
     */
    void insert(PurchaseOrder po);

    /**
     * 発注ヘッダをid検索
     */
    Optional<PurchaseOrder> findById(Long id);

    /**
     * 発注ヘッダの一覧を取得
     */
    List<PurchaseOrder> findAll();

    /**
     * 発注ヘッダのstatus変更(Entity:可変フィールドを更新)
     */
    void updatePoStatus(PurchaseOrder po);
}
