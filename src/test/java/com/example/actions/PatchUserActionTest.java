package com.example.actions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PatchUserActionクラスの単体テスト
 * ユーザー部分更新機能をテストします。
 */
@ExtendWith(MockitoExtension.class)
class PatchUserActionTest {

    private PatchUserAction patchUserAction;
    
    @Mock
    private Context mockContext;
    
    private Gson gson;

    @BeforeEach
    void setUp() {
        patchUserAction = new PatchUserAction();
        gson = new Gson();
    }

    @Test
    @DisplayName("ユーザー部分更新が正常に完了すること")
    void userPartialUpdateCompletesSuccessfully() {
        String userId = "user-123";
        String requestBody = "{\"email\":\"patched@example.com\"}";

        // PatchUserActionを実行
        APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, requestBody, mockContext);

        // レスポンスを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Content-Typeヘッダーを検証
        assertNotNull(response.getHeaders());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    @DisplayName("レスポンスボディが正しいJSON形式であること")
    void responseBodyIsInCorrectJsonFormat() {
        String userId = "test-user-456";
        String requestBody = "{\"name\":\"Patched Name\"}";

        // PatchUserActionを実行
        APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, requestBody, mockContext);

        // レスポンスボディをJSONとしてパース
        assertDoesNotThrow(() -> {
            Map<String, String> responseMap = gson.fromJson(response.getBody(), Map.class);
            
            assertNotNull(responseMap);
            assertEquals(userId, responseMap.get("userId"));
            assertEquals("patched", responseMap.get("status"));
        });
    }

    @Test
    @DisplayName("異なるユーザーIDでもそれぞれ正しく処理されること")
    void differentUserIdsAreProcessedCorrectly() {
        String[] userIds = {"user-001", "editor-999", "viewer-123"};
        String requestBody = "{\"role\":\"updated_role\"}";

        for (String userId : userIds) {
            // PatchUserActionを実行
            APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, requestBody, mockContext);

            // レスポンスを検証
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            
            // レスポンスボディに指定したuserIdが含まれることを確認
            assertTrue(response.getBody().contains(userId));
            assertTrue(response.getBody().contains("patched"));
        }
    }

    @Test
    @DisplayName("単一フィールドのパッチが正しく処理されること")
    void singleFieldPatchIsProcessedCorrectly() {
        String userId = "single-field-user";
        String requestBody = "{\"email\":\"new@example.com\"}";

        // PatchUserActionを実行
        APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, requestBody, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("patched"));
    }

    @Test
    @DisplayName("複数フィールドのパッチが正しく処理されること")
    void multipleFieldPatchIsProcessedCorrectly() {
        String userId = "multi-field-user";
        String requestBody = """
                {
                  "email": "multi@example.com",
                  "name": "Multi Field User",
                  "role": "admin"
                }""";

        // PatchUserActionを実行
        APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, requestBody, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("空のリクエストボディでも処理が完了すること")
    void emptyRequestBodyProcessingCompletes() {
        String userId = "empty-body-user";
        String requestBody = "";

        // PatchUserActionを実行
        APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, requestBody, mockContext);

        // 正常に処理されることを検証（現在の実装ではリクエストボディを使用していない）
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("nullリクエストボディでも処理が完了すること")
    void nullRequestBodyProcessingCompletes() {
        String userId = "null-body-user";
        String requestBody = null;

        // PatchUserActionを実行
        APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, requestBody, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("nullユーザーIDでも処理が完了すること")
    void nullUserIdProcessingCompletes() {
        String userId = null;
        String requestBody = "{\"name\":\"Null User ID Test\"}";

        // PatchUserActionを実行
        APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, requestBody, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // nullがJSON文字列に含まれることを確認
        assertTrue(response.getBody().contains("null"));
    }

    @Test
    @DisplayName("ネストされたJSONオブジェクトでも処理が完了すること")
    void nestedJsonObjectProcessingCompletes() {
        String userId = "nested-json-user";
        String requestBody = """
                {
                  "profile": {
                    "personal": {
                      "name": "田中花子",
                      "age": 30
                    },
                    "work": {
                      "department": "開発部",
                      "position": "シニアエンジニア"
                    }
                  }
                }""";

        // PatchUserActionを実行
        APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, requestBody, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("配列を含むJSONでも処理が完了すること")
    void jsonWithArraysProcessingCompletes() {
        String userId = "array-json-user";
        String requestBody = """
                {
                  "skills": ["Java", "Python", "JavaScript"],
                  "certifications": [
                    {"name": "AWS", "level": "Associate"},
                    {"name": "Oracle", "level": "Professional"}
                  ]
                }""";

        // PatchUserActionを実行
        APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, requestBody, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("日本語を含むデータでも処理が完了すること")
    void dataWithJapaneseCharactersProcessingCompletes() {
        String userId = "ユーザー002";
        String requestBody = "{\"氏名\":\"佐藤次郎\",\"部署\":\"営業部\"}";

        // PatchUserActionを実行
        APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, requestBody, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // 日本語のuserIdが含まれることを確認
        assertTrue(response.getBody().contains(userId));
    }

    @Test
    @DisplayName("レスポンスボディの形式が正しいこと")
    void responseBodyFormatIsCorrect() {
        String userId = "format-test";
        String requestBody = "{\"test\":\"format\"}";

        // PatchUserActionを実行
        APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, requestBody, mockContext);

        String responseBody = response.getBody();
        
        // JSON形式であることを検証
        assertTrue(responseBody.startsWith("{"));
        assertTrue(responseBody.endsWith("}"));
        
        // 期待されるフィールドが含まれていることを検証
        assertTrue(responseBody.contains("userId"));
        assertTrue(responseBody.contains("status"));
        assertTrue(responseBody.contains("patched"));
    }

    @Test
    @DisplayName("複数回実行しても一貫した結果が返されること")
    void multipleExecutionsReturnConsistentResults() {
        String userId = "consistency-test";
        String requestBody = "{\"email\":\"consistent@example.com\"}";

        // 複数回実行
        APIGatewayProxyResponseEvent response1 = patchUserAction.execute(userId, requestBody, mockContext);
        APIGatewayProxyResponseEvent response2 = patchUserAction.execute(userId, requestBody, mockContext);

        // 同じ結果が返されることを検証
        assertEquals(response1.getStatusCode(), response2.getStatusCode());
        assertEquals(response1.getBody(), response2.getBody());
        assertEquals(response1.getHeaders(), response2.getHeaders());
    }

    @Test
    @DisplayName("Contextパラメータが正しく渡されること")
    void contextParameterIsPassedCorrectly() {
        String userId = "context-test";
        String requestBody = "{\"context\":\"test\"}";

        // PatchUserActionを実行（例外が発生しないことを確認）
        assertDoesNotThrow(() -> {
            APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, requestBody, mockContext);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        });
    }

    @Test
    @DisplayName("nullContextでも例外が発生しないこと")
    void nullContextDoesNotThrowException() {
        String userId = "null-context-test";
        String requestBody = "{\"null\":\"context\"}";

        // nullコンテキストでの実行
        assertDoesNotThrow(() -> {
            APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, requestBody, null);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        });
    }

    @Test
    @DisplayName("不正なJSONでも例外が発生しないこと")
    void invalidJsonDoesNotThrowException() {
        String userId = "invalid-json-test";
        String invalidJson = "{invalid: json without quotes}";

        // PatchUserActionを実行（現在の実装では入力検証していないため例外は発生しない）
        assertDoesNotThrow(() -> {
            APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, invalidJson, mockContext);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        });
    }

    @Test
    @DisplayName("特殊文字を含むユーザーIDでも処理が完了すること")
    void userIdWithSpecialCharactersProcessingCompletes() {
        String userId = "user+special@example.com";
        String requestBody = "{\"email\":\"special@example.com\"}";

        // PatchUserActionを実行
        APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, requestBody, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("大きなJSONペイロードでも処理が完了すること")
    void largeJsonPayloadProcessingCompletes() {
        String userId = "large-payload-user";
        
        // 大きなJSONペイロードを作成
        StringBuilder largeJson = new StringBuilder("{");
        for (int i = 0; i < 100; i++) {
            if (i > 0) largeJson.append(",");
            largeJson.append("\"field").append(i).append("\":\"value").append(i).append("\"");
        }
        largeJson.append("}");

        // PatchUserActionを実行
        APIGatewayProxyResponseEvent response = patchUserAction.execute(userId, largeJson.toString(), mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}