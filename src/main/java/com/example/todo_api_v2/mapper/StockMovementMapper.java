package com.example.todo_api_v2.mapper;

import com.example.todo_api_v2.entity.StockMovement;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface StockMovementMapper {

    // 1. 入出庫履歴を1件登録
    @Insert("""
            INSERT INTO
                stock_movements(item_id,movement_type,qty,movement_date,created_by)
                VALUES(#{itemId},#{movementType},#{qty},#{movementDate},#{createdBy})
            """)
    @Options(useGeneratedKeys = true , keyProperty = "id")
    void insert(StockMovement movement);

    // 2. 在庫集計
    @Select("""
            SELECT
                COALESCE(
                    SUM(CASE WHEN movement_type = 'INBOUND' THEN qty
                                                            ELSE -qty END),
                    0
                ) As current_stock
            FROM stock_movements
            WHERE item_id = #{itemId}
            """)
    BigDecimal sumByItemId(Long itemId);
}
