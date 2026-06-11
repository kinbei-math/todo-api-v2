package com.example.todo_api_v2.entity;

// 明細用
public enum PoLineStatus {
    ORDERED,     // 発注済み
    RECEIVED;    // 入荷済み

    public boolean canTransitionTo(PoLineStatus next){
        return switch (this){
            case ORDERED -> next == RECEIVED;
            case RECEIVED -> next == ORDERED;
        };
    }
}
