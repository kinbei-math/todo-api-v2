-- itemsのテーブル
CREATE TABLE items(
    id         BIGINT AUTO_INCREMENT, -- 主キー（サロゲートキー、DB自動採番）
    item_code  VARCHAR(20)  NOT NULL,
    item_name  VARCHAR(100) NOT NULL,
    uom        VARCHAR(10)  NOT NULL, -- enum:UomType{SET,PC,KG,G,METER}
    category   VARCHAR(50)  NOT NULL, -- enum:Category
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 作成日時はDB登録時に
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 初期値はDB登録時と同じ。

    CONSTRAINT pk_items_id        PRIMARY KEY (id),
    CONSTRAINT uk_items_item_code UNIQUE      (item_code)
);