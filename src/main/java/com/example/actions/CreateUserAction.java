package com.example.actions;

import com.amazonaws.services.lambda.runtime.*;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.Collections;

/**
 * ユーザー作成アクション
 * 新しいユーザーをシステムに登録します。
 * 
 * <p>このアクションは POST /admin/users エンドポイントで呼び出され、
 * リクエストボディに含まれるユーザー情報を解析して
 * 新しいユーザーをデータベースに保存します。
 * 現在はダミーレスポンスを返しています。</p>
 * 
 * @author Sample Project
 * @version 1.0
 */
public class CreateUserAction {
    /**
     * ユーザー作成処理を実行します。
     * 
     * <p>リクエストボディに含まれるユーザー情報を解析し、
     * データベースに新しいユーザーを作成します。
     * 作成が成功した場合は201ステータスとともに
     * 作成されたユーザーのIDを返します。</p>
     * 
     * @param requestBody ユーザー作成情報を含むJSONリクエストボディ
     * @param context Lambda実行コンテキスト
     * @return 作成されたユーザー情報を含むAPI Gatewayレスポンス
     */
    public APIGatewayProxyResponseEvent execute(String requestBody, Context context) {
        // 本来はrequestBodyをパースしてDBにユーザーを作成する
        String dummyResponse = "{\"userId\":\"user-003\", \"status\":\"created\"}";

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(201) // 201 Created
                .withHeaders(Collections.singletonMap("Content-Type", "application/json"))
                .withBody(dummyResponse);
    }
}