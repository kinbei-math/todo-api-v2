package com.example.todo_api_v2.exception;

import com.example.todo_api_v2.dto.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.todo_api_v2.dto.ErrorResponse;

import java.util.List;
import java.util.NoSuchElementException;

//ResponeBody(JSON)で返す。Exceptionを総合的に処理するクラスであることを示すアノテーション
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //validationExceptionを処理するメソッド
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex){
        //validExceptionのList<FiledError>=FiledErrosをStreamにのせる
        //その中のFieldErrorの中身の一部(field,defaultmessage)をnew ValidationError(record)に詰めなおす。map()
        //詰めなおしたものを並べてListを作る。
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors()
                                                    .stream().map(fieldError-> new ValidationError(fieldError.getField(),fieldError.getDefaultMessage()))
                                                    .toList();

        //errorsに複数データが入っている場合があるので詳細をすべて返す。
        log.warn("Validation Error [details = {}]",errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400,"入力が不正です。",errors));
    }

    //NoSuchElementExceptionを処理するメソッド
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException ex){

        log.warn("NoSuchElementException [message = {}]",ex.getMessage());
        //Validationと比較して、エラーの種類が今は１つしかないのでシンプルに整理する。
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404,"Todoが見つかりません。"));
    }

    //InvalidStatusTransitionExceptionを処理するメソッド
    //入力は正しいが、不正な状態遷移を表す
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransitionException(InvalidStatusTransitionException ex){
        log.warn("InvalidStatusTransitionException [message = {}]",ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409,"許可されていない状態遷移です"));
    }

    //Exception.classを捕まえる汎用ハンドラ
    //情報漏洩のため、詳細は返さない。
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleSystemError(Exception e){

        //Errorのログにはスタックトレースを残す
        log.error("システムエラーが発生しました",e);

        //セキュリティのためエラーの詳細は返さない。
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500,"サーバー内部で予期せぬエラーが発生しました。"));
    }
}
