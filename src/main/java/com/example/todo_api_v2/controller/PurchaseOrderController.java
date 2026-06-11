package com.example.todo_api_v2.controller;

import com.example.todo_api_v2.dto.purchaseorder.PurchaseOrderCreateRequest;
import com.example.todo_api_v2.dto.purchaseorder.PurchaseOrderResponse;
import com.example.todo_api_v2.dto.purchaseorder.ReceiveLineRequest;
import com.example.todo_api_v2.service.PurchaseOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchase-orders")
public class PurchaseOrderController{

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService){this.purchaseOrderService = purchaseOrderService;}

    // 1 POST create
    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(
            @Validated @RequestBody PurchaseOrderCreateRequest request
            ){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                purchaseOrderService.create(request)
        );
    }

    // 2 GET findById
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrder(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(purchaseOrderService.findById(id));
    }

    // 3 GET findAll
    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponse>> getPurchaseOrders(){
        return ResponseEntity.status(HttpStatus.OK).body(purchaseOrderService.findAll());
    }

    // 4 POST receive
    @PostMapping("/{poId}/lines/{lineNo}/receive")
    public ResponseEntity<PurchaseOrderResponse> receivePurchaseOrderLine(
            @PathVariable Long poId,
            @PathVariable Integer lineNo,
            @Validated @RequestBody ReceiveLineRequest request
            ){
        return ResponseEntity.status(HttpStatus.OK).body(purchaseOrderService.receive(poId, lineNo, request));
    }
}
