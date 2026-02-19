## 開発環境
- Java: 25
- Spring Boot: 4.0.2
- ビルドツール: Gradle
- Intellij IDEA: 2025.03

## ローカルでの起動コマンド
**Mac / Linux の場合**
```bash
./gradlew bootRun
```
**Windows の場合**
```bash
gradlew.bat bootRun
```

## 動作確認用のエンドポイント一覧
| HTTPメソッド | パス | 役割 |
| :--- | :--- | :--- |
| GET | `/health` | アプリケーションが正常に稼働しているか（ヘルスチェック）を確認する |
| GET | `/todos` | 現在登録されているTodoの一覧（リスト）を取得する |


### 23. W4: Gradle基盤整備・Mockitoテスト・GitHub Actions CI構築

- **日付**: 2026/02/18
- **ファイル**: [build.gradle](build.gradle), [TodoService.java](src/main/java/com/example/todo_api_v2/service/TodoService.java), [TodoController.java](src/main/java/com/example/todo_api_v2/controller/TodoController.java), [TodoControllerTest.java](src/test/java/com/example/todo_api_v2/controller/TodoControllerTest.java), [ci.yml](.github/workflows/ci.yml)
- **学習内容**:
  - `todo-api-v2`プロジェクトをSpring Initializrで新規作成、GitHubにpush
  - `build.gradle`の各依存関係（implementation/testImplementation）の役割を理解
  - `TodoService`・`TodoController`を一から実装（`@Service`・`@RestController`・`@PostMapping`）
  - Mockitoで`@Mock`/`@InjectMocks`を使ったControllerの単体テストを実装
  - GitHub Actionsで`ci.yml`を作成、mainへのpush時に自動テストが走るCIを構築

### 24. W5: Spring Boot REST API基礎（ヘルスチェック・Todo一覧）

- **日付**: 2026/02/19
- **ファイル**: [HealthController.java](src/main/java/com/example/todo_api_v2/controller/HealthController.java), [TodoController.java](src/main/java/com/example/todo_api_v2/controller/TodoController.java), [TodoService.java](src/main/java/com/example/todo_api_v2/service/TodoService.java), [README.md](README.md)
- **学習内容**:
  - `HealthController` を新規作成し、`GET /health` エンドポイントを実装（`ResponseEntity<String>`で200+"ok"を返す）
  - `TodoService` に `findAll()` を追加、`TodoController` に `GET /todos` を追加してダミーデータを返す一覧取得を実装
  - Controller/Service/Repositoryの責務分割を言語化（変更の影響範囲を閉じ込める設計意図まで）
  - READMEに開発環境・起動手順（Mac/Windows両対応）・エンドポイント一覧を記載

---
Last Updated: 2026/02/19
