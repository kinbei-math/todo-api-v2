package com.example.todo_api_v2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping//GetMappingでは/healthを受け取り確認するだけ
    public ResponseEntity<String> checkHealth(){
        return ResponseEntity.ok("OK");//受け取れたら200とokで返す。
    }
}
