package com.example.actions;

import com.amazonaws.services.lambda.runtime.*;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.Collections;

/**
 * ユーザー更新アクション（完全更新）
 * 指定されたユーザーIDの情報を完全に更新します。
 * 
 * <p>このアクションは PUT /admin/users/{userId} エンドポイントで呼び出され、
 * リクエストボディに含まれる情報でユーザーの全フィールドを
 * 置き換えます。PATCHとは異なり、送信されなかったフィールドは
 * クリアされる可能性があります。</p>
 * 
 * @author Sample Project
 * @version 1.0
 */
public class UpdateUserAction {
    /**
     * 指定されたユーザーIDの情報を完全に更新します。
     * 
     * <p>リクエストボディに含まれる情報でユーザーの全データを置き換えます。
     * PUTメソッドの意味に従い、送信されなかったフィールドは
     * デフォルト値やnullで上書きされる可能性があります。</p>
     * 
     * @param userId 更新対象のユーザーID
     * @param requestBody 更新情報を含むJSONリクエストボディ
     * @param context Lambda実行コンテキスト
     * @return 更新結果を含むAPI Gatewayレスポンス
     */
    public APIGatewayProxyResponseEvent execute(String userId, String requestBody, Context context) {
        // 本来はDBの特定ユーザー情報を更新する
        String dummyResponse = String.format("{\"userId\":\"%s\", \"status\":\"updated\"}", userId);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Collections.singletonMap("Content-Type", "application/json"))
                .withBody(dummyResponse);
    }
}