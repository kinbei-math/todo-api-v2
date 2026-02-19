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

  ---
 Last Updated:2026/02/18