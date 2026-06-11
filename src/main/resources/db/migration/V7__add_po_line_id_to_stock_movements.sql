-- stock_movementsへ明細行を追加
ALTER TABLE stock_movements ADD po_line_id BIGINT;
ALTER TABLE stock_movements ADD CONSTRAINT fk_stock_movements_po_line_id
    FOREIGN KEY (po_line_id)
    REFERENCES purchase_order_lines(id)
    ON DELETE RESTRICT; -- ログのある注文書明細は削除できない。null許可のFK