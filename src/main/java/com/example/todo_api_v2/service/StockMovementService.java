package com.example.todo_api_v2.service;

import com.example.todo_api_v2.dto.stock.StockMovementCreateRequest;
import com.example.todo_api_v2.dto.stock.StockMovementResponse;
import com.example.todo_api_v2.entity.StockMovement;
import com.example.todo_api_v2.exception.ItemNotFoundException;
import com.example.todo_api_v2.mapper.ItemMapper;
import com.example.todo_api_v2.mapper.StockMovementMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class StockMovementService {

    // MapperをDI
    private final ItemMapper itemMapper;
    private final StockMovementMapper stockMovementMapper;
    public StockMovementService(ItemMapper itemMapper, StockMovementMapper stockMovementMapper){
        this.itemMapper = itemMapper;
        this.stockMovementMapper = stockMovementMapper;
    }

    // 1. 入出庫履歴を1件登録
    @Transactional // SQL文が2つ以上あるので、Transactionalをつける
    public StockMovementResponse createMovement(StockMovementCreateRequest request){
        // itemIdの存在を確認
        if(!itemMapper.existsById(request.itemId())){
            throw new ItemNotFoundException(
                    "品目IDが存在しません。itemId=" + request.itemId()
            );
        }

        // insert用のstockMovement準備
        String username = getCurrentUsername();
        StockMovement movement = convertStockMovement(request, username);

        // 実行
        stockMovementMapper.insert(movement);

        // ログ出力
        log.info("StockMovement created [UserID: {}, ItemId: {}, MovementType: {}, Qty: {}, MovementDate: {}]",
                getCurrentUsername(),movement.getItemId(),movement.getMovementType(),movement.getQty(),movement.getMovementDate());

        // 出力用に詰め替え
        return convertStockMovementResponse(movement);
    }

    // ヘルパーメソッド3種
    private StockMovement convertStockMovement(StockMovementCreateRequest request,String username){
        // stockMovement準備
        StockMovement stockMovement = new StockMovement();
        stockMovement.setItemId(request.itemId());
        stockMovement.setMovementType(request.movementType());
        stockMovement.setQty(request.qty());
        stockMovement.setMovementDate(request.movementDate());
        stockMovement.setCreatedBy(username);

        return stockMovement;
    }

    private StockMovementResponse convertStockMovementResponse(StockMovement movement){
        return new StockMovementResponse(
                movement.getId(),
                movement.getItemId(),
                movement.getMovementType(),
                movement.getQty(),
                movement.getMovementDate(),
                movement.getCreatedAt(),
                movement.getCreatedBy()
                );
    }

    private String getCurrentUsername(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "system";
    }
}
