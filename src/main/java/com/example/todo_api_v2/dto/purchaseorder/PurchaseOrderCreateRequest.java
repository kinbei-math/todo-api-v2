package com.example.todo_api_v2.dto.purchaseorder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;


/**
 * POST /api/purchase-orders　のリクエスト
 * 発注書1枚に相当する伝票の作成リクエスト
 *
 * @param poNumber  発注番号(20字以内)
 * @param supplier  仕入先(100字以内)
 * @param orderDate 発注日(null許容) nullの場合はService層でnow()で補完
 * @param lines     明細リスト 空リスト以外のValidationはPurchaseOrderLineCreateRequestに委ねる
 */
public record PurchaseOrderCreateRequest(
        @NotBlank(message = "空白なしで発注番号を入力してください")
        @Size(max = 20, message = "20字以内で入力してください")
        String poNumber,
        @NotBlank(message = "空白なしで仕入先を入力してください")
        @Size(max = 100, message = "100字以内で入力してください")
        String supplier,
        LocalDate orderDate,
        @NotEmpty(message = "明細を追加してください")
        @Valid
        List<PurchaseOrderLineCreateRequest> lines
) {
    // コンパクトコンストラクタ
    // recordの中のリストが不変にするために記述
    // nullの場合はValidationに処理をゆだねる
    public PurchaseOrderCreateRequest{
        if(lines != null){
            lines = List.copyOf(lines);
        }
    }
}
