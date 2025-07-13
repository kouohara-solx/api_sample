package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API Gatewayのカスタムオーソライザーとして動作するLambdaハンドラークラス
 * JWTトークンの検証とIAMポリシーの生成を行います。
 * 
 * <p>このハンドラーはAPI Gatewayからの認可リクエストを受け取り、
 * JWTトークンを検証してリクエストの許可/拒否を決定します。
 * 認証が成功した場合、ユーザー情報をコンテキストに含めたIAMポリシーを返します。</p>
 * 
 * @author Sample Project
 * @version 1.0
 */
public class AuthorizerHandler implements RequestHandler<APIGatewayCustomAuthorizerEvent, Map<String, Object>> {

    private static final String SECRET_KEY = "your-very-secret-key"; // 本番では環境変数などから安全に取得してください

    /**
     * API Gatewayからの認可リクエストを処理します。
     * 
     * <p>リクエストのAuthorizationヘッダーからJWTトークンを取得し、
     * トークンの署名検証と有効期限チェックを行います。
     * 検証が成功した場合、APIアクセスを許可するIAMポリシーを生成し、
     * JWTのクレーム情報をコンテキストに含めて返します。</p>
     * 
     * @param event API Gatewayカスタムオーソライザーイベント
     * @param context Lambda実行コンテキスト
     * @return IAMポリシーとコンテキスト情報を含むマップ
     */
    @Override
    public Map<String, Object> handleRequest(APIGatewayCustomAuthorizerEvent event, Context context) {
        String token = event.getAuthorizationToken();
        if (token == null || !token.startsWith("Bearer ")) {
            // トークンが無効な場合は即座にDeny
            return generateDenyPolicy("unauthorized", event.getMethodArn());
        }
        token = token.substring(7);

        try {
            // 1. JWTの検証
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);

            // 2. クレームの抽出
            String principalId = jwt.getSubject();
            String role = jwt.getClaim("role").asString();
            String organizationId = jwt.getClaim("organization_id").asString();

            // 3. "Allow"ポリシーを生成
            return generateAllowPolicy(principalId, event.getMethodArn(), role, organizationId);

        } catch (Exception e) {
            context.getLogger().log("JWT Verification failed: " + e.getMessage());
            // 検証失敗時はDeny
            return generateDenyPolicy("unauthorized", event.getMethodArn());
        }
    }

    /**
     * APIアクセスを許可するIAMポリシーを生成します。
     * 
     * <p>指定されたメソッドARNに基づいて汎用的なリソースARNを構築し、
     * API Gatewayの全エンドポイントにアクセス可能なAllowポリシーを生成します。
     * また、認証されたユーザーの情報をコンテキストに含めます。</p>
     * 
     * @param principalId ユーザーの一意識別子
     * @param methodArn リクエストされたメソッドのARN
     * @param role ユーザーの役割
     * @param organizationId ユーザーが所属する組織のID
     * @return Allowポリシーとコンテキスト情報を含むマップ
     */
    private Map<String, Object> generateAllowPolicy(String principalId, String methodArn, String role, String organizationId) {
        // --- ここからが修正部分 ---
        // methodArnからAPIの汎用的なARNを構築する
        // 例: arn:aws:execute-api:ap-northeast-1:123456789012:abcdef123/Prod/GET/hello
        // -> arn:aws:execute-api:ap-northeast-1:123456789012:abcdef123/Prod/*/*
        String[] arnParts = methodArn.split(":");
        String[] pathParts = arnParts[5].split("/");
        String region = arnParts[3];
        String accountId = arnParts[4];
        String apiId = pathParts[0];
        String stage = pathParts[1];
        String resource = String.format("arn:aws:execute-api:%s:%s:%s/%s/*/*", region, accountId, apiId, stage);
        // --- ここまで ---

        Map<String, Object> policyDocument = new HashMap<>();
        policyDocument.put("Version", "2012-10-17");
        policyDocument.put("Statement", List.of(
                Map.of(
                        "Action", "execute-api:Invoke",
                        "Effect", "Allow",
                        "Resource", resource // 修正した汎用的なリソースARNを使用
                )
        ));

        Map<String, Object> context = new HashMap<>();
        context.put("principalId", principalId);
        context.put("role", role);
        context.put("organization_id", organizationId);

        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("principalId", principalId);
        authResponse.put("policyDocument", policyDocument);
        authResponse.put("context", context);

        return authResponse;
    }

    /**
     * APIアクセスを拒否するIAMポリシーを生成します。
     * 
     * <p>JWTトークンの検証に失敗した場合や、
     * 認証情報が不正な場合に呼び出されます。
     * 指定されたメソッドARNに対するDenyポリシーを生成します。</p>
     * 
     * @param principalId ユーザーの一意識別子（認証失敗時は"unauthorized"など）
     * @param methodArn リクエストされたメソッドのARN
     * @return Denyポリシーを含むマップ
     */
    private Map<String, Object> generateDenyPolicy(String principalId, String methodArn) {
        Map<String, Object> policyDocument = new HashMap<>();
        policyDocument.put("Version", "2012-10-17");
        policyDocument.put("Statement", List.of(
                Map.of(
                        "Action", "execute-api:Invoke",
                        "Effect", "Deny",
                        "Resource", methodArn
                )
        ));

        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("principalId", principalId);
        authResponse.put("policyDocument", policyDocument);

        return authResponse;
    }
}