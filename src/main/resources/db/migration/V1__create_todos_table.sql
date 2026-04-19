-- Todo用のテーブル
CREATE TABLE todos(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,-- 自動採番かつ一意なid
    title VARCHAR(255) NOT NULL,
    due_date DATE,
    todo_status VARCHAR(20) NOT NULL,-- javaではenum　SQLでは文字列で受ける
    completed_at TIMESTAMP-- 日付と時間をUS時刻でとれる。絶対時刻でとる。
    );

-- User用のテーブル
CREATE TABLE users(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,-- 自動採番かつ一意なid
    email VARCHAR(255) UNIQUE NOT NULL,-- 一意なログインid
    role VARCHAR(20) NOT NULL,-- javaではenum　SQLでは文字列で受ける
    password_hash VARCHAR(255) NOT NULL
    );