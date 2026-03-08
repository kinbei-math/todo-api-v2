package com.example.todo_api_v2.exception;

import com.example.todo_api_v2.dto.ValidationError;
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

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400,"入力が不正です。",errors));
    }

    //NoSuchElementExceptionを処理するメソッド
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException ex){
        //Validationと比較して、エラーの種類が今は１つしかないのでシンプルに整理する。
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404,"Todoが見つかりません。"));
    }
}
