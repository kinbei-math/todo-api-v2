package com.example.todo_api_v2.dto;

import java.util.List;

public record ErrorResponse(Integer statusCode, String message, List<ValidationError> errors) {
    //従来通りの引数2津の場合はerrorsにnulを入れて返す。
    //オーバーロード(補助コントラクタ)
    public ErrorResponse(Integer statusCode,String message){
        this(statusCode,message,null);
    }
}
