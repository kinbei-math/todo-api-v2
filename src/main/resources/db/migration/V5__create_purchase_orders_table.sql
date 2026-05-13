-- 注文書テーブル
CREATE TABLE purchase_orders(
    id          BIGINT       AUTO_INCREMENT,                     -- 主キー(サロゲートキー、DB自動採番)
    po_number   VARCHAR(20)  NOT NULL,                           -- 発注番号(業務コード) PO-{yyyyMMdd}-{連番3桁}
    supplier    VARCHAR(100) NOT NULL,                           -- 仕入れ先
    status      VARCHAR(20)  NOT NULL,                           -- enum:{ORDERED, RECEIVED} SQLでは文字列で受け取る
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP, -- System Time
    created_by  VARCHAR(255) NOT NULL,                           -- 作成者
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 初期値はDB登録時と同じ(Bisiness Time)
    updated_by  VARCHAR(255) DEFAULT NULL,                       -- 更新者

    CONSTRAINT pk_purchase_orders_id        PRIMARY KEY (id),
    CONSTRAINT uk_purchase_orders_po_number UNIQUE(po_number)
);