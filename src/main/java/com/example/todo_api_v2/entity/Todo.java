package com.example.todo_api_v2.entity;
import com.example.todo_api_v2.exception.InvalidStatusTransitionException;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.example.todo_api_v2.entity.TodoStatus.*;

@Getter
public class Todo {
    //3種類変数に対してのGetterとSetter
    @Setter
    private Long id;  //idのフィールド
    @Setter
    private String title;  //タイトルのフィールド
    @Setter
    private LocalDate dueDate;  //日付のフィールド
    private TodoStatus todoStatus;  //3状態を示すフィールド（状態判定メソッドでのみ操作可能）
    //完了日時を取得 nullの可能性があるがDBにうまく登録できない場合もあるので、Serviceでnullをケアする。
    @Setter
    private LocalDateTime completedAt; //完了日時を示すフィールド（完了時の日時を自動取得、差し戻し時に削除）

    //空のコンストラクタ作成
    public Todo() {
        this.todoStatus=TODO;
    }


    //状態変遷を判定して置き換えるメソッド（Week9設計判断通り）
    //判定はTodoStatusに任せる
    public void changeStatus(TodoStatus nextStatus){
        //ガード節を意識。このif文以降は問題ない遷移に問題がないコードのみ
        if(!todoStatus.canTransitionTo(nextStatus)){
            throw new InvalidStatusTransitionException("許可されていない状態遷移です");
        }

        //次のステータスが完了なら完了日時を入力
        if(nextStatus==DONE){
            completedAt=LocalDateTime.now();
        }
        //現在のステータスが完了なら完了日時を削除
        if(todoStatus==DONE){
            completedAt=null;
        }
        todoStatus=nextStatus;//今のステータスに次のステータスを代入
    }


}
