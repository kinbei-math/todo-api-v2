package com.example.todo_api_v2.mapper;


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
                items(item_code, item_name, uom, category)
                VALUES(#{itemCode}, #{name}, #{uom}, #{category})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Item item);
}
