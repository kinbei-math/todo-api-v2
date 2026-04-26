package com.example.todo_api_v2.db;

import com.example.todo_api_v2.entity.User;
import com.example.todo_api_v2.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional // DBをテストの都度リセット
public class FlywayMigrationTest {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;// 簡易テスト用(SQL実行)

    @Test
    @DisplayName("Flywayによるv2の初期データが投入されている")
    void testFindByEmail_shouldReturnInitialUser_whenFlywayV2Executed(){
        //user用の初期email
        String testemail = "user@example.com";

        //初期データの存在を確認
        Optional<User> user = userMapper.findByEmail(testemail);
        assertThat(user).isPresent();
    }

    @Test
    @DisplayName("itemsテーブルが存在する")
    void itemsTable_shouldExist() {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'ITEMS'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("stock_movementsテーブルが存在する")
    void stockMovementsTable_shouldExist() {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'STOCK_MOVEMENTS'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("qty <= 0 のINSERTはCHECK制約で拒否される")
    void shouldRejectNegativeQty_byCheckConstraint() {
        // まず親レコード（items）を1件INSERTしておく
        String insertItem = """
            INSERT INTO items (item_code, item_name, uom, category)
            VALUES ('TEST-001', 'テスト品', 'KG', 'RAW_MATERIAL')
            """;
        jdbcTemplate.update(insertItem);

        // 親レコードのIdをSQL文で引き出す。
        String selectIdSql = "SELECT id FROM items WHERE item_code = ?";
        Long itemId = jdbcTemplate.queryForObject(selectIdSql,Long.class,"TEST-001");

        // qty = -5 のINSERTをする(エラー)
        String insertMovement = """
            INSERT INTO stock_movements (item_id, movement_type, qty, movement_date, created_by)
            VALUES (?, 'IN', -5, '2026-04-26', 'test_user')
            """;

        // CHECK制約の違反を確認
        assertThrows(DataIntegrityViolationException.class,()->{
            jdbcTemplate.update(insertMovement,itemId);
        });
    }

    @Test
    @DisplayName("FK制約: 存在しないitem_idを指定した場合は登録できずエラーになる")
    void shouldReject_invalidItemIdByFkConstraint(){
        // id=9999 のINSERTをする(エラー)
        String insertMovement = """
            INSERT INTO stock_movements (item_id, movement_type, qty, movement_date, created_by)
            VALUES (9999, 'IN', 10, '2026-04-26', 'test_user')
            """;

        // item_idのみ不正のデータをINSERTして違反を確認
        assertThrows(DataIntegrityViolationException.class,()->{
            jdbcTemplate.update(insertMovement);
        });
    }
}
