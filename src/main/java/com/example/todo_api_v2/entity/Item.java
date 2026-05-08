package com.example.todo_api_v2.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Item {
    private Long id;                 // id(PK) DBで自動採番
    private String itemCode;         // 品名コード(Unique) 20字以内
    private String name;             // 品名 100字以内 columnと相違があるので注意
    private UomType uom;             // 単位(enum)
    private Category category;       // 分類(enum)
    private LocalDateTime createdAt; // 作成日時
    private LocalDateTime updatedAt; // 更新日時(初期値はcreatedAtと同じ)

    // コンストラクタの作成
    public Item(){}
}
