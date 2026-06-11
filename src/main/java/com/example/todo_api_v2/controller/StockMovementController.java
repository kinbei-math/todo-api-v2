package com.example.todo_api_v2.controller;

import com.example.todo_api_v2.dto.stock.StockMovementCreateRequest;
import com.example.todo_api_v2.dto.stock.StockMovementResponse;
import com.example.todo_api_v2.service.StockMovementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService){
        this.stockMovementService = stockMovementService;
    }

    // POST insert
    @PostMapping
    public ResponseEntity<StockMovementResponse> createMovement(
            @Validated @RequestBody StockMovementCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockMovementService.createMovement(request));
    }
}
