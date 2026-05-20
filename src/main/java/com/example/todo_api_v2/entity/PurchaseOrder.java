package com.example.todo_api_v2.entity;

import com.example.todo_api_v2.exception.EmptyPurchaseOrderLineException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 発注ヘッダ
 * <p>
 * 業務上の「発注書1枚」と同じ意味をもつ。複数の明細行を持つ。
 * 明細は {@link PurchaseOrderLine} を参照。
 * <p>
 * 不変フィールド: id, poNumber, supplier, orderDate, createdBy, createdAt
 * <br>
 * 可変フィールド: status, updatedBy, updatedAt（refreshStatus経由でのみ更新）
 *
 */
@AllArgsConstructor
@Getter
public class PurchaseOrder {
    private final Long          id;         // id(PK) DBで自動採番
    private final String        poNumber;   // 発注番号(UK) 20文字以内
    private final String        supplier;   // 仕入れ先 100字以内
    private final LocalDate     orderDate;  // 発注日
    private       PoStatus      status;     // 注文状態
    private final LocalDateTime createdAt;  // 作成時間(DB自動)
    private final String        createdBy;  // 作成者 255字以内
    private       LocalDateTime updatedAt;  // 更新時刻　初期値は作成時間と等しい(Service層で更新)
    private       String        updatedBy;  // 更新者 255字以内

    /**
     * 明細リストから発注ヘッダのステータスを再計算して反映する。
     * 全明細がRECEIVEDならRECEIVED、それ以外はORDEREDとなる。
     * 状態に変化がない場合は何もしない（updatedAt/Byも更新しない）。
     *
     * @param lines     対象発注の全明細
     * @param operator  操作者（updatedByに記録）
     * @param updatedAt 更新時刻（Service層で生成し渡す）
     * @throws NullPointerException             linesがnullの場合
     * @throws EmptyPurchaseOrderLineException  linesが空の場合（データ不整合）
     */
    public void refreshStatus(List<PurchaseOrderLine> lines, String operator, LocalDateTime updatedAt) {
        // ヌルポチェック
        Objects.requireNonNull(lines,"リストがありません。poNumber="+this.poNumber);
        // 明細0行をエラーで検出
        if (lines.isEmpty()) {throw new EmptyPurchaseOrderLineException("明細がありません。poNumber="+this.poNumber); }

        // 明細のstatusをチェックして注文のstatusを判断
        PoStatus newStatus = lines.stream()
                        .allMatch(line -> line.getStatus() == PoLineStatus.RECEIVED)
                        ? PoStatus.RECEIVED
                        : PoStatus.ORDERED;

        // 変化なしの場合はここでリターン
        if(this.status == newStatus){ return; }

        // 変化がある場合は更新
        this.status    = newStatus; // 明細がすべて入荷済みなら注文も入荷済みに変更
        this.updatedBy = operator;  // 更新者を上書き
        this.updatedAt = updatedAt; // 更新時間を上書き
        }
}

