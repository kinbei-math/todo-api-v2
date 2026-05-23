# todo-api-v2 — CLAUDE.md

Java学習プロジェクト。TodoAPIを起点に、在庫管理・発注管理まで段階的に拡張している。

---

## スタック

| 分類 | 技術 |
|------|------|
| 言語 | Java 25 |
| フレームワーク | Spring Boot 4.0.2 |
| ビルド | Gradle |
| O/Rマッパー | MyBatis（JPA は不使用） |
| DB マイグレーション | Flyway（V1〜V8） |
| DB | H2（dev）/ MySQL（prod・docker-compose） |
| 認証 | Spring Security Basic認証 |
| テスト | JUnit 5 + Mockito |
| 静的解析 | Checkstyle 13.4.0 + SpotBugs 6.4.8 |
| その他 | Lombok |

---

## アーキテクチャ

```
Client → Security → Controller → Service → Mapper → DB
                                              ↓
                                    GlobalExceptionHandler
```

パッケージ構成:

```
com.example.todo_api_v2/
├── config/          # SecurityConfig（@Order + @Profile で環境切替）
├── controller/      # プレゼンテーション層
├── service/         # ビジネスロジック
├── mapper/          # MyBatisインターフェース
├── entity/          # エンティティ・enum
├── dto/
│   ├── common/      # ErrorResponse, ValidationError
│   ├── todo/
│   ├── item/
│   ├── stock/
│   └── purchaseorder/
└── exception/       # 独自例外 + GlobalExceptionHandler
```

---

## コーディング規約

### 基本
- DI はコンストラクタインジェクション（`@Autowired` 不使用）
- `@Getter` クラス全体 + `@Setter` 個別フィールドに限定（Lombok）
- `ResponseEntity` に `HttpStatus` を明示する

### 状態遷移
- 状態遷移ロジックはエンティティのメソッドに持たせる（Service に書かない）
- `canTransitionTo()` でガード節を入れ、許可されていない遷移は `InvalidStatusTransitionException` を投げる

### 例外処理
- Controller で try-catch しない
- `@RestControllerAdvice` の `GlobalExceptionHandler` に集約
- ドメインごとに独自例外クラスを作成（例: `ItemNotFoundException`, `DuplicateItemCodeException`）
- 500系はスタックトレースのみログに残し、レスポンスには詳細を返さない

### バリデーション
- リクエストDTOには `@Validated` + Bean Validation アノテーションを付与

### Security
- 本番用 `SecurityFilterChain`（`@Order(2)`）と dev用 H2コンソール用（`@Order(1)`, `@Profile("dev")`）に分離
- DELETE は ADMIN ロールのみ許可
- BCryptPasswordEncoder 使用

### DB
- カラム名はスネークケース、Javaフィールドはキャメルケース（`map-underscore-to-camel-case: true`）
- スキーマ変更は必ず Flyway マイグレーションファイルで管理（直接DDL実行禁止）

---

## テスト規約

| テスト種別 | 対象 | ツール |
|-----------|------|--------|
| ControllerTest | `@WebMvcTest` でSliceテスト | MockMvc |
| ServiceTest | Serviceのユニットテスト | Mockito |
| MapperTest | DB統合テスト（H2） | MyBatis Test |
| EntityTest | 状態遷移ロジックのユニットテスト | JUnit 5 |
| FlywayMigrationTest | マイグレーション完全性チェック | Spring Boot Test |

---

## ブランチ・コミット規約

**ブランチ名**: `feature/w{週番号}-{トピック}`  
例: `feature/w16-purchase-order`

**コミットメッセージ**: `type(scope): 内容`  
例: `feat(W16): PurchaseOrder と PurchaseOrderLine の Entity を追加`

type: `feat` / `fix` / `test` / `refactor` / `chore` / `doc`

---

## 起動方法

```bash
# dev（H2）
gradlew.bat bootRun

# prod（MySQL via Docker）
docker-compose up -d
gradlew.bat bootRun --args='--spring.profiles.active=prod'
```

---

## Lessons Learned

### 例外処理
- Controller で try-catch していたが、`GlobalExceptionHandler` に集約することで Controller がシンプルになり、`ResponseEntity<?>` の `?` ワイルドカードも不要になった

### 状態遷移設計
- 最初は Service に `if-else` で遷移ロジックを書いていたが、Entity の `changeStatus()` + enum `canTransitionTo()` パターンに移行。遷移ルールが一箇所に集まり追跡しやすくなった

### SecurityConfig の分離
- `@Profile("dev")` + `@Order(1)` でH2コンソール用フィルタチェーンを本番設定と分離。dev環境での確認作業がしやすくなった

### MyBatis 採用理由
- Spring Data JPA は一旦コメントアウトし、MyBatis を選択。SQLを直接書くことでSQL学習と並行できるため

### Flyway でのスキーマ管理
- MySQLへの移行（W14）以降、直接DDLでなくFlywayで全変更を管理。マイグレーション番号はVn__で通し番号管理

### SpotBugs の誤検知対策
- `record` の `List` フィールドや、テスト用テキストブロックなど誤検知しやすいパターンは `config/spotbugs/exclude.xml` に追加

### DTOのパッケージ分割（W16）
- 当初 dto/ フラットに置いていたが、ドメインが増えたため `dto/item/`, `dto/stock/`, `dto/purchaseorder/`, `dto/common/` にサブパッケージ分割

### Docker Compose + .env での環境変数管理（W14）
- MySQL接続情報を `.env` ファイルに外出し、`docker-compose.yml` で参照。`.env` は `.gitignore` 対象
