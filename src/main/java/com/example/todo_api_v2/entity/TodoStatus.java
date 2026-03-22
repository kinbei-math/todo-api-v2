package com.example.todo_api_v2.entity;

public enum TodoStatus {
    TODO,DOING,DONE;

    //変数の設定時には遷移を決めれない。(先に定義しなければいけないため)
    //メソッドで遷移可能かを判断する。
    //boolean型ではnullを返せない。今回はnullの可能性がないのでboolean
    public boolean canTransitionTo(TodoStatus next){
        return switch (this){
            case TODO, DONE -> next==DOING;
            case DOING -> next==TODO || next==DONE;
        };
    }
}
