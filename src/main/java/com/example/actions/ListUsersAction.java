package com.example.actions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.Collections;

/**
 * ユーザー一覧取得アクション
 * 登録されているユーザーの一覧を取得します。
 * 
 * <p>このアクションは GET /admin/users エンドポイントで呼び出され、
 * システムに登録されているすべてのユーザーの基本情報を返します。
 * 現在はダミーデータを返していますが、実際の実装では
 * データベースからユーザー情報を取得する必要があります。</p>
 * 
 * @author Sample Project
 * @version 1.0
 */
public class ListUsersAction {

    /**
     * ユーザー一覧取得処理を実行します。
     * 
     * <p>システムに登録されているすべてのユーザーの基本情報を取得し、
     * JSON形式で返します。処理の開始と終了時にログを出力します。</p>
     * 
     * @param context Lambda実行コンテキスト（ロギング用）
     * @return ユーザー一覧を含むAPI Gatewayレスポンス
     */
    public APIGatewayProxyResponseEvent execute(Context context) {
        // Contextからロガーを取得
        LambdaLogger logger = context.getLogger();

        // ログを出力
        logger.log("--- ListUsersAction: Processing started ---");

        // 実際のビジネスロジック (DBアクセスなど)
        String dummyResponse = "[{\"userId\":\"user-001\", \"name\":\"Taro Yamada\"}, {\"userId\":\"user-002\", \"name\":\"Hanako Suzuki\"}]";

        logger.log("Successfully retrieved " + 2 + " users.");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Collections.singletonMap("Content-Type", "application/json"))
                .withBody(dummyResponse);

        logger.log("--- ListUsersAction: Processing finished. Status code: " + response.getStatusCode() + " ---");

        return response;
    }
}