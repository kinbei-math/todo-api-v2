package com.example.todo_api_v2.controller;

import com.example.todo_api_v2.dto.item.ItemCreateRequest;
import com.example.todo_api_v2.dto.item.ItemResponse;
import com.example.todo_api_v2.dto.stock.StockResponse;
import com.example.todo_api_v2.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // 1. GET byId
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItem(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(itemService.findById(id));
    }

    // 2. GET All
    @GetMapping
    public ResponseEntity<List<ItemResponse>> getItems() {
        return ResponseEntity.status(HttpStatus.OK).body(itemService.findAll());
    }

    // 3. POST insert
    @PostMapping
    public ResponseEntity<ItemResponse> createItem(
            @Validated @RequestBody ItemCreateRequest itemCreateRequest) {
        return  ResponseEntity.status(HttpStatus.CREATED).body(itemService.createItem(itemCreateRequest));
    }

    // 4. GET currentStock
    @GetMapping("/{id}/stock")
    public ResponseEntity<StockResponse> getCurrentStock(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(itemService.getCurrentStock(id));
    }
}
