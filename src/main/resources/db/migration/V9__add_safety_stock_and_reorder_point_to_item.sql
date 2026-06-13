-- itemsへ安全在庫と発注点を追加
ALTER TABLE items ADD COLUMN safety_stock
        INTEGER
        NOT NULL
        DEFAULT 0 -- defaultの安全在庫を0に設定しておく
        AFTER category;

ALTER TABLE items ADD COLUMN reorder_point
        INTEGER
        NOT NULL
        DEFAULT 0 -- defaultの発注点も0に設定
        AFTER safety_stock;
