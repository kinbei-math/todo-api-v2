-- 在庫管理のテーブル
CREATE TABLE stock_movements(
    id            BIGINT        AUTO_INCREMENT, -- 主キー（サロゲートキー、DB自動採番）
    item_id       BIGINT        NOT NULL, -- FK
    movement_type VARCHAR(10)   NOT NULL, -- enum:MovementType{IN,OUT}
    qty           DECIMAL(12,3) NOT NULL, -- 変化量の絶対値
    movement_date DATE          NOT NULL, -- Business Time
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP, -- System Time
    created_by    VARCHAR(255)  NOT NULL, -- ユーザー名はVARCHARで保存。ユーザーの削除にも対応可能。

    CONSTRAINT pk_stock_movements_id            PRIMARY KEY (id),
    CONSTRAINT fk_stock_movements_item_id       FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE RESTRICT, -- ログのある品目は削除できない。
    CONSTRAINT chk_stock_movements_qty_positive CHECK (qty > 0)
);