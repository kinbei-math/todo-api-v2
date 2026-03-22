-- GitHub用の修正
CREATE TABLE IF NOT EXISTS todos(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    due_date DATE,
    todo_status VARCHAR(20) NOT NULL,
    completed_at TIMESTAMP
);





