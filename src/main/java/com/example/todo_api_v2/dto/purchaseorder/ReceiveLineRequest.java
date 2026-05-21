package com.example.todo_api_v2.dto.purchaseorder;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * POST /api/purchase-orders/{poId}/lines/{lineNo}/receive のリクエスト
 * 明細が納品された日を引数として、納品に遷移させるリクエスト
 *
 * @param receivedAt 納品日
 */
public record ReceiveLineRequest(@NotNull(message = "納品日を入力してください") LocalDate receivedAt) {
}
