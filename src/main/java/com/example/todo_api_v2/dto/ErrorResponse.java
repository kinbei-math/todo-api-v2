package com.example.todo_api_v2.dto;

import java.util.List;


public record ErrorResponse(Integer statusCode, String message, List<ValidationError> errors) {

    //コンパクトコンストラクタ ErrorResponseの中身が後で書き換え不可に
    public ErrorResponse{
        errors = List.copyOf(errors);
    }

    //従来通りの引数2つの場合はerrorsに空のリストを返す。返す型を統一。
    //nullはフロントエンドへの処理の負担になる。
    //オーバーロード(補助コントラクタ)
    public ErrorResponse(Integer statusCode,String message){
        this(statusCode,message,List.of());
    }
}
