-- Userの登録
INSERT INTO users(email,password_hash,role)
VALUES ('user@example.com',
        '$2a$10$X5wFBtLrL/kHcmrOGGTrGufsBX8CJ0WpQpF3pgeuxBB/H73BK1DW6',
        'USER');

-- Adminの登録
INSERT INTO users(email,password_hash,role)
VALUES ('admin@example.com',
        '$2a$10$X5wFBtLrL/kHcmrOGGTrGufsBX8CJ0WpQpF3pgeuxBB/H73BK1DW6',
        'ADMIN');