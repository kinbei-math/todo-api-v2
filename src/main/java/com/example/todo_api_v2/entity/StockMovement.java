package com.example.todo_api_v2.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class StockMovement {
    private Long id;                    // id(PK) DBで自動採番
    private Long itemId;                // FK(items.id)
    private MovementType movementType;  // 入出庫種別 enum
    private BigDecimal qty;             // 数量(CHECK制約 qty > 0)
    private LocalDate movementDate;     // Business Time
    private LocalDateTime createdAt;    // System Time
    private String createdBy;           // 操作ユーザー(usernameをVARCHAR型で保存)

    public StockMovement(){}
}
