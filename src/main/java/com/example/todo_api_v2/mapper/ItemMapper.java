package com.example.todo_api_v2.mapper;


import com.example.todo_api_v2.dto.item.ReorderAlertResponse;
import com.example.todo_api_v2.entity.Item;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

// itemsTableに対してのMapper
@Mapper
public interface ItemMapper {

    // 1. IDで品目を取得
    @Select("""
            SELECT
                id,
                item_code,
                item_name AS name,
                uom,
                safety_stock,
                reorder_point,
                category,
                created_at,
                updated_at
            FROM items
            WHERE id = #{id}
            """
    )
    Optional<Item> findById(Long id);

    // 2. itemの一覧を取得
    @Select("""
            SELECT
                id,
                item_code,
                item_name AS name,
                uom,
                safety_stock,
                reorder_point,
                category,
                created_at,
                updated_at
            FROM items
            ORDER BY item_code
            """)
    List<Item> findAll();

    // 3. 新規登録
    @Insert("""
            INSERT INTO
                items(item_code, item_name, uom, safety_stock, reorder_point, category)
                VALUES(#{itemCode}, #{name}, #{uom}, #{safetyStock}, #{reorderPoint}, #{category})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Item item);

    // 4. ItemCodeで検索
    @Select("""
            SELECT
                id,
                item_code,
                item_name AS name,
                uom,
                safety_stock,
                reorder_point,
                category,
                created_at,
                updated_at
            FROM items
            WHERE item_code = #{itemCode}
            """
    )
    Optional<Item> findByItemCode(String itemCode);

    // 5. ItemIdが存在するかの確認
    @Select("SELECT EXISTS (SELECT 1 FROM items WHERE id = #{id})")
    boolean existsById(Long id);

    // 6. 発注点を切ったものを返す
    @Select("""
            SELECT
                items.id,
                items.item_code,
                items.item_name AS name,
                items.uom,
                COALESCE(sm.current_stock, 0) AS current_stock,
                items.reorder_point
            FROM items
            LEFT JOIN (
                SELECT
                    item_id, SUM(CASE WHEN movement_type = 'INBOUND' THEN qty ELSE -qty END) AS current_stock
                    FROM stock_movements
                    GROUP BY item_id
            ) AS sm ON items.id = sm.item_id
            WHERE COALESCE(sm.current_stock, 0) <= items.reorder_point
            """)
    List<ReorderAlertResponse> reorderItems();
}
