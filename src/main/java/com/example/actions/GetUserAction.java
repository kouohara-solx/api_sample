package com.example.actions;

import com.amazonaws.services.lambda.runtime.*;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.Collections;

/**
 * 特定ユーザー取得アクション
 * 指定されたユーザーIDに基づいてユーザー情報を取得します。
 * 
 * <p>このアクションは GET /admin/users/{userId} エンドポイントで呼び出され、
 * パスパラメータとして渡されたユーザーIDに対応する
 * ユーザーの詳細情報をデータベースから取得して返します。
 * 現在はダミーデータを返しています。</p>
 * 
 * @author Sample Project
 * @version 1.0
 */
public class GetUserAction {
    /**
     * 指定されたユーザーIDのユーザー情報を取得します。
     * 
     * <p>パスパラメータとして渡されたユーザーIDを使用して、
     * データベースから対応するユーザーの詳細情報を取得し、
     * JSON形式で返します。ユーザーが存在しない場合は
     * 404ステータスを返す必要があります。</p>
     * 
     * @param userId 取得対象のユーザーID
     * @param context Lambda実行コンテキスト
     * @return ユーザー情報を含むAPI Gatewayレスポンス
     */
    public APIGatewayProxyResponseEvent execute(String userId, Context context) {
        // 本来はDBから特定のユーザー情報を取得する
        String dummyResponse = String.format("{\"userId\":\"%s\", \"name\":\"Taro Yamada\"}", userId);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Collections.singletonMap("Content-Type", "application/json"))
                .withBody(dummyResponse);
    }
}