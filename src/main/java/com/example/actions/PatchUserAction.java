package com.example.actions;

import com.amazonaws.services.lambda.runtime.*;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.Collections;

/**
 * ユーザー部分更新アクション
 * 指定されたユーザーIDの情報を部分的に更新します。
 * 
 * <p>このアクションは PATCH /admin/users/{userId} エンドポイントで呼び出され、
 * リクエストボディに含まれるフィールドだけを更新します。
 * PUTとは異なり、送信されなかったフィールドは変更されず、
 * 既存の値が維持されます。</p>
 * 
 * @author Sample Project
 * @version 1.0
 */
public class PatchUserAction {
    /**
     * 指定されたユーザーIDの情報を部分的に更新します。
     * 
     * <p>リクエストボディに含まれたフィールドだけを更新し、
     * その他のフィールドは既存の値を維持します。
     * これにPATCHメソッドの本来の意味である部分更新を実現します。</p>
     * 
     * @param userId 更新対象のユーザーID
     * @param requestBody 部分更新情報を含むJSONリクエストボディ
     * @param context Lambda実行コンテキスト
     * @return 更新結果を含むAPI Gatewayレスポンス
     */
    public APIGatewayProxyResponseEvent execute(String userId, String requestBody, Context context) {
        // 本来はDBの特定ユーザー情報の一部を更新する
        String dummyResponse = String.format("{\"userId\":\"%s\", \"status\":\"patched\"}", userId);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Collections.singletonMap("Content-Type", "application/json"))
                .withBody(dummyResponse);
    }
}