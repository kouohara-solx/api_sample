package com.example;

import com.amazonaws.services.lambda.runtime.*;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.Map;

/**
 * JWT認証が必要な保護されたエンドポイントのLambdaハンドラークラス
 * 認証されたユーザーの情報を表示します。
 * 
 * <p>このハンドラーは/helloエンドポイントを処理し、
 * Lambda Authorizerで検証されたJWTトークンから抽出されたユーザー情報を
 * レスポンスに含めて返します。認証が正常に機能していることを確認するための
 * デモンストレーション用エンドポイントです。</p>
 * 
 * @author Sample Project
 * @version 1.0
 */
public class ProtectedHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    /**
     * 認証されたユーザーの情報を表示する保護されたエンドポイントを処理します。
     * 
     * <p>Lambda Authorizerで検証されたJWTトークンのクレーム情報から、
     * ユーザーID、役割、組織IDを取得し、ユーザーにあいさつメッセージを返します。
     * このエンドポイントは認証機能の動作確認に使用されます。</p>
     * 
     * @param input API Gatewayからのプロキシリクエストイベント
     * @param context Lambda実行コンテキスト
     * @return ユーザー情報を含むグリーティングメッセージ
     */
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        // Authorizerから渡されたコンテキスト情報を取得
        Map<String, Object> authorizerContext = input.getRequestContext().getAuthorizer();
        String userId = input.getRequestContext().getAuthorizer().get("principalId").toString();
        String role = authorizerContext.get("role").toString();
        String orgId = authorizerContext.get("organization_id").toString();

        String body = String.format("Hello user %s from organization %s! Your role is %s.", userId, orgId, role);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(body);
    }
}