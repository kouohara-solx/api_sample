package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;
import java.util.Date;
import java.util.Map;

/**
 * 認証処理を行うLambdaハンドラークラス
 * ユーザーの認証情報を検証し、JWTトークンを発行します。
 * 
 * <p>このハンドラーは認証エンドポイント（/auth/token）に対するPOSTリクエストを処理し、
 * 有効な認証情報が提供された場合にJWTトークンを含むレスポンスを返します。</p>
 * 
 * @author Sample Project
 * @version 1.0
 */
public class AuthHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String SECRET_KEY = "your-very-secret-key"; // 本番では環境変数などから取得
    private static final Map<String, Map<String, String>> USERS = Map.of(
            "testuser", Map.of("password", "password123", "id", "user-001", "role", "editor", "org", "org-abc")
    );

    /**
     * API Gatewayからの認証リクエストを処理します。
     * 
     * <p>リクエストボディからユーザー名とパスワードを取得し、
     * 事前定義されたユーザー情報と照合して認証を行います。
     * 認証が成功した場合、ユーザー情報を含むJWTトークンを生成して返します。</p>
     * 
     * @param input API Gatewayからのプロキシリクエストイベント
     * @param context Lambda実行コンテキスト
     * @return JWTトークンを含むAPI Gatewayプロキシレスポンス
     */
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        // ... (リクエストボディからusernameとpasswordを取得する処理) ...
        // ... (USERSマップと照合して認証する処理) ...

        // 認証成功後
        String userId = "user-001";
        String role = "editor";
        String organizationId = "org-abc";

        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        String token = JWT.create()
                .withSubject(userId)
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600 * 1000))
                .withClaim("role", role)
                .withClaim("organization_id", organizationId)
                .sign(algorithm);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(new Gson().toJson(Map.of("token", token)));
    }
}