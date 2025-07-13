package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.actions.*; // 作成したアクションクラスをインポート
import java.util.Map;

/**
 * ユーザー管理機能を統括するLambdaハンドラークラス
 * ユーザーのCRUD操作（作成、読み取り、更新、削除）を処理します。
 * 
 * <p>このハンドラーは/admin/users以下のすべてのエンドポイントを処理し、
 * HTTPメソッドとパスパラメータに基づいて適切なアクションクラスに処理を委譲します。
 * すべての操作には認証が必要で、Lambda Authorizerによる認可チェックを経て実行されます。</p>
 * 
 * @author Sample Project
 * @version 1.0
 */
public class AdminUsersHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    /**
     * API Gatewayからのユーザー管理リクエストを処理します。
     * 
     * <p>HTTPメソッド（GET、POST、PUT、DELETE、PATCH）とパスパラメータ（userId）に基づいて、
     * 対応するアクションクラスに処理を委譲します。</p>
     * 
     * <ul>
     * <li>GET /admin/users - ユーザー一覧取得</li>
     * <li>GET /admin/users/{userId} - 特定ユーザー取得</li>
     * <li>POST /admin/users - ユーザー作成</li>
     * <li>PUT /admin/users/{userId} - ユーザー更新（完全）</li>
     * <li>PATCH /admin/users/{userId} - ユーザー更新（部分）</li>
     * <li>DELETE /admin/users/{userId} - ユーザー削除</li>
     * </ul>
     * 
     * @param event API Gatewayからのプロキシリクエストイベント
     * @param context Lambda実行コンテキスト
     * @return 処理結果を含むAPI Gatewayプロキシレスポンス
     */
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {

        String httpMethod = event.getHttpMethod();
        Map<String, String> pathParameters = event.getPathParameters();
        String userId = (pathParameters != null) ? pathParameters.get("userId") : null;

        // 各アクションクラスにcontextを渡して実行
        return switch (httpMethod) {
            case "GET" ->
                    (userId != null) ? new GetUserAction().execute(userId, context) : new ListUsersAction().execute(context);
            case "POST" -> new CreateUserAction().execute(event.getBody(), context);
            case "PUT" -> new UpdateUserAction().execute(userId, event.getBody(), context);
            case "DELETE" -> new DeleteUserAction().execute(userId, context);
            case "PATCH" -> new PatchUserAction().execute(userId, event.getBody(), context);
            default -> new APIGatewayProxyResponseEvent().withStatusCode(405).withBody("Method Not Allowed");
        };
    }
}