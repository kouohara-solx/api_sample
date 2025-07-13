# API Sample Project

AWS SAM (Serverless Application Model) を使用したJWT認証機能付きのサーバーレスAPIプロジェクトです。

## 概要

このプロジェクトは、JWT (JSON Web Token) を使用した認証機能を持つREST APIを実装しています。AWS Lambda、API Gateway、SAMフレームワークを活用したサーバーレスアーキテクチャで構築されています。

## 機能

### 認証機能
- **JWT認証**: ユーザー認証後にJWTトークンを発行
- **Lambda Authorizer**: API Gatewayでのトークン検証
- **カスタムクレーム**: ロール（role）と組織ID（organization_id）をトークンに含有
- **APIキー認証**: 全エンドポイントでAPIキーが必要
- **使用量制限**: 企業別の利用量プランとレート制限

### ユーザー管理機能
- **ユーザー一覧取得** (GET /admin/users)
- **特定ユーザー取得** (GET /admin/users/{userId})
- **ユーザー作成** (POST /admin/users)
- **ユーザー更新** (PUT /admin/users/{userId})
- **ユーザー部分更新** (PATCH /admin/users/{userId})
- **ユーザー削除** (DELETE /admin/users/{userId})

### 保護されたエンドポイント
- **認証テスト用エンドポイント** (GET /hello)

### APIキー管理
- **A社用APIキー**: 高速プラン（1000回/分）
- **B社用APIキー**: 標準プラン（60回/分）
- **使用量監視**: API Gateway による自動制限

## 技術スタック

- **Java 21**: プログラミング言語
- **AWS SAM**: サーバーレスアプリケーションフレームワーク
- **AWS Lambda**: サーバーレス実行環境
- **API Gateway**: REST APIエンドポイント
- **Auth0 Java JWT**: JWT操作ライブラリ
- **Maven**: ビルドツール
- **JUnit 5**: テストフレームワーク
- **Mockito**: モックフレームワーク

## プロジェクト構造

```
api_sample/
├── src/
│   ├── main/java/com/example/
│   │   ├── AuthHandler.java           # 認証処理
│   │   ├── AuthorizerHandler.java     # Lambda Authorizer
│   │   ├── AdminUsersHandler.java     # ユーザー管理ルーティング
│   │   ├── ProtectedHandler.java      # 保護されたエンドポイント
│   │   ├── Main.java                  # エントリーポイント
│   │   └── actions/                   # アクションクラス
│   │       ├── ListUsersAction.java
│   │       ├── CreateUserAction.java
│   │       ├── GetUserAction.java
│   │       ├── UpdateUserAction.java
│   │       ├── DeleteUserAction.java
│   │       └── PatchUserAction.java
│   └── test/java/                     # 単体テスト
├── template.yaml                      # SAM設定ファイル
├── samconfig.toml                     # SAM設定（ap-northeast-1）
└── pom.xml                           # Maven設定
```

## API エンドポイント

### 認証エンドポイント
```
POST /auth/token
```
- **説明**: ユーザー認証とJWTトークン発行
- **認証**: APIキーが必要（JWTトークンは不要）
- **ヘッダー**: `x-api-key: <your-api-key>`
- **リクエスト例**:
```json
{
  "username": "testuser",
  "password": "password123"
}
```
- **レスポンス例**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### ユーザー管理エンドポイント
すべてのエンドポイントでAPIキーとBearer認証の両方が必要です。

**必要なヘッダー:**
- `x-api-key: <your-api-key>`
- `Authorization: Bearer <jwt-token>`

```
GET /admin/users                    # ユーザー一覧
GET /admin/users/{userId}           # 特定ユーザー取得
POST /admin/users                   # ユーザー作成
PUT /admin/users/{userId}           # ユーザー更新（完全）
PATCH /admin/users/{userId}         # ユーザー更新（部分）
DELETE /admin/users/{userId}        # ユーザー削除
```

### 保護されたエンドポイント
```
GET /hello
```
- **説明**: 認証テスト用エンドポイント
- **認証**: APIキーとBearer認証の両方が必要
- **ヘッダー**: 
  - `x-api-key: <your-api-key>`
  - `Authorization: Bearer <jwt-token>`

## セットアップ

### 必要な環境
- Java 21
- Maven 3.6+
- AWS CLI
- AWS SAM CLI

### インストール手順

1. **リポジトリのクローン**
```bash
git clone <repository-url>
cd api_sample
```

2. **依存関係のインストール**
```bash
mvn clean install
```

3. **プロジェクトのビルド**
```bash
sam build
```

4. **ローカルでの実行**
```bash
sam local start-api
```

5. **デプロイ**
```bash
# 初回デプロイ
sam deploy --guided

# 2回目以降
sam deploy
```

## APIキー管理

デプロイ後、以下の方法でAPIキーを取得できます：

### APIキーの取得

```bash
# A社用APIキー取得
aws apigateway get-api-key --api-key $(aws apigateway get-api-keys --name-query CompanyAKey --query 'items[0].id' --output text) --include-value --query 'value' --output text

# B社用APIキー取得
aws apigateway get-api-key --api-key $(aws apigateway get-api-keys --name-query CompanyBKey --query 'items[0].id' --output text) --include-value --query 'value' --output text
```

### 使用量プラン

| 企業 | レート制限 | バースト制限 | 説明 |
|------|------------|--------------|------|
| A社 | 16.67リクエスト/秒 | 50リクエスト | 高速プラン（1000回/分） |
| B社 | 5リクエスト/秒 | 5リクエスト | 標準プラン（60回/分） |

### APIキーの使用例

```bash
# 認証トークン取得
curl -X POST https://your-api-id.execute-api.ap-northeast-1.amazonaws.com/Prod/auth/token \
  -H "x-api-key: YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'

# ユーザー一覧取得
curl -X GET https://your-api-id.execute-api.ap-northeast-1.amazonaws.com/Prod/admin/users \
  -H "x-api-key: YOUR_API_KEY" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 開発

### テストの実行

```bash
# 全テスト実行
mvn test

# 特定のテストクラス実行
mvn test -Dtest=AuthHandlerTest

# アクションクラステストのみ実行
mvn test -Dtest=ActionsTestSuite
```

### コードスタイル

- **JavaDoc**: 全クラス・メソッドに日本語でドキュメント作成済み
- **テストメソッド**: 英語メソッド名 + `@DisplayName`で日本語説明
- **ログ出力**: `Context.getLogger()`を使用

### アーキテクチャパターン

#### Handler Pattern
- `AdminUsersHandler`: HTTPメソッドによるルーティング
- Action Classes: 具体的なビジネスロジック実装

#### Authorization Pattern
- `AuthorizerHandler`: JWT検証とIAMポリシー生成
- カスタムクレーム（role, organization_id）の活用

## 設定

### samconfig.toml
```toml
[default.deploy.parameters]
stack_name = "sam-app"
resolve_s3 = true
s3_prefix = "sam-app"
region = "ap-northeast-1"
confirm_changeset = true
capabilities = "CAPABILITY_IAM"
```

### JWT設定
現在はハードコードされた秘密鍵を使用しています。本番環境では環境変数からの取得に変更してください。

```java
// 本番環境では環境変数から取得
private static final String SECRET_KEY = System.getenv("JWT_SECRET_KEY");
```

## セキュリティ

### 実装済み
- JWT署名検証
- トークン有効期限チェック
- Lambda Authorizerによるリクエスト認可
- IAMポリシー生成

### 本番環境での推奨事項
- 秘密鍵を環境変数に移動
- HTTPS通信の強制
- ログ監視の設定
- レート制限の実装

## 既知の制限事項

1. **ダミーデータ**: 現在はハードコードされたテストデータを使用
2. **データベース未実装**: 実際のデータベース連携が必要
3. **エラーハンドリング**: より詳細なエラー処理の実装が必要
4. **バリデーション**: 入力データの検証機能が未実装

## ライセンス

このプロジェクトはサンプル用途で作成されています。

## 貢献

1. フォークを作成
2. フィーチャーブランチを作成 (`git checkout -b feature/AmazingFeature`)
3. 変更をコミット (`git commit -m 'Add some AmazingFeature'`)
4. ブランチにプッシュ (`git push origin feature/AmazingFeature`)
5. プルリクエストを作成

## サポート

問題や質問がある場合は、Issueを作成してください。

---

## 参考リンク

- [AWS SAM Documentation](https://docs.aws.amazon.com/serverless-application-model/)
- [JWT.io](https://jwt.io/)
- [AWS Lambda Java](https://docs.aws.amazon.com/lambda/latest/dg/lambda-java.html)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)