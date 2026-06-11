-- 注文書明細テーブル
CREATE TABLE purchase_order_lines(
    id          BIGINT        AUTO_INCREMENT, -- 主キー（サロゲートキー、DB自動採番）
    po_id       BIGINT        NOT NULL,       -- FK(purchase_orders)
    item_id     BIGINT        NOT NULL,       -- FK(items)
    line_no     SMALLINT      NOT NULL,       -- 行番号
    qty         DECIMAL(12,3) NOT NULL,       -- 発注数量
    price       DECIMAL(12,2) NOT NULL,       -- 単価
    due_date    DATE          NOT NULL,       -- 納期(Business Time)
    status      VARCHAR(20)   NOT NULL,       -- enum:{ORDERED, RECEIVED} SQLでは文字列で受け取る
    received_by VARCHAR(255)  DEFAULT NULL,   -- receivedに変更させた人
    received_at DATE          DEFAULT NULL,   -- 納品日(Business Time)
    created_by  VARCHAR(255)  NOT NULL,       -- 登録者
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP, -- System Time
    updated_by  VARCHAR(255)  DEFAULT NULL,
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP, -- System Time

    CONSTRAINT pk_purchase_order_lines_id                  PRIMARY KEY (id),
    CONSTRAINT fk_purchase_order_lines_po_id           FOREIGN KEY (po_id)
        REFERENCES purchase_orders(id)
        ON DELETE RESTRICT, -- 明細が残っている注文書は削除できない
    CONSTRAINT fk_purchase_order_lines_item_id             FOREIGN KEY (item_id)
        REFERENCES items(id)
        ON DELETE RESTRICT, -- 明細が残っているitemは削除できない
    CONSTRAINT uk_purchase_order_lines_po_id_line_no   UNIQUE (po_id,line_no), -- 注文番号と明細行の組み合わせはUK
    CONSTRAINT chk_purchase_order_lines_qty                CHECK ( qty > 0 ), -- 数量は正
    CONSTRAINT chk_purchase_order_lines_price              CHECK ( price > 0 ) -- 価格は正
);