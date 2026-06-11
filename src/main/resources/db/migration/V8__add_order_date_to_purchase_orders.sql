-- purchase_ordersへ発注日を追加
ALTER TABLE purchase_orders ADD COLUMN order_date
        DATE
        NOT NULL
        DEFAULT (CURRENT_DATE) -- 発注日(伝票上の業務日付)
        AFTER supplier;
-- 注意: 既に運用中のテーブルで「既存データに正しい日付を埋めたい」場合は、
--   1. nullable で追加
--   2. アプリで既存データを正しい日付で UPDATE
--   3. NOT NULL に変更
-- という3段階リリースが必要。W16ではまだデータが入っていないため、DEFAULTで一括採番とした。