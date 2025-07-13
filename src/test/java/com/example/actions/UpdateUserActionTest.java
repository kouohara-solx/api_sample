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
 * UpdateUserActionクラスの単体テスト
 * ユーザー完全更新機能をテストします。
 */
@ExtendWith(MockitoExtension.class)
class UpdateUserActionTest {

    private UpdateUserAction updateUserAction;
    
    @Mock
    private Context mockContext;
    
    private Gson gson;

    @BeforeEach
    void setUp() {
        updateUserAction = new UpdateUserAction();
        gson = new Gson();
    }

    @Test
    @DisplayName("ユーザー更新が正常に完了すること")
    void userUpdateCompletesSuccessfully() {
        String userId = "user-123";
        String requestBody = "{\"name\":\"Updated User\",\"email\":\"updated@example.com\"}";

        // UpdateUserActionを実行
        APIGatewayProxyResponseEvent response = updateUserAction.execute(userId, requestBody, mockContext);

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
        String requestBody = "{\"name\":\"Test User\"}";

        // UpdateUserActionを実行
        APIGatewayProxyResponseEvent response = updateUserAction.execute(userId, requestBody, mockContext);

        // レスポンスボディをJSONとしてパース
        assertDoesNotThrow(() -> {
            Map<String, String> responseMap = gson.fromJson(response.getBody(), Map.class);
            
            assertNotNull(responseMap);
            assertEquals(userId, responseMap.get("userId"));
            assertEquals("updated", responseMap.get("status"));
        });
    }

    @Test
    @DisplayName("異なるユーザーIDでもそれぞれ正しく処理されること")
    void differentUserIdsAreProcessedCorrectly() {
        String[] userIds = {"user-001", "admin-999", "editor-123"};
        String requestBody = "{\"name\":\"Updated Name\"}";

        for (String userId : userIds) {
            // UpdateUserActionを実行
            APIGatewayProxyResponseEvent response = updateUserAction.execute(userId, requestBody, mockContext);

            // レスポンスを検証
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            
            // レスポンスボディに指定したuserIdが含まれることを確認
            assertTrue(response.getBody().contains(userId));
            assertTrue(response.getBody().contains("updated"));
        }
    }

    @Test
    @DisplayName("空のリクエストボディでも処理が完了すること")
    void emptyRequestBodyProcessingCompletes() {
        String userId = "user-empty-body";
        String requestBody = "";

        // UpdateUserActionを実行
        APIGatewayProxyResponseEvent response = updateUserAction.execute(userId, requestBody, mockContext);

        // 正常に処理されることを検証（現在の実装ではリクエストボディを使用していない）
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("nullリクエストボディでも処理が完了すること")
    void nullRequestBodyProcessingCompletes() {
        String userId = "user-null-body";
        String requestBody = null;

        // UpdateUserActionを実行
        APIGatewayProxyResponseEvent response = updateUserAction.execute(userId, requestBody, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("nullユーザーIDでも処理が完了すること")
    void nullUserIdProcessingCompletes() {
        String userId = null;
        String requestBody = "{\"name\":\"Test User\"}";

        // UpdateUserActionを実行
        APIGatewayProxyResponseEvent response = updateUserAction.execute(userId, requestBody, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // nullがJSON文字列に含まれることを確認
        assertTrue(response.getBody().contains("null"));
    }

    @Test
    @DisplayName("複雑なJSONリクエストボディでも処理が完了すること")
    void complexJsonRequestBodyProcessingCompletes() {
        String userId = "complex-update-user";
        String requestBody = """
                {
                  "name": "田中次郎",
                  "email": "jiro@example.com",
                  "role": "manager",
                  "department": "営業部",
                  "metadata": {
                    "last_login": "2023-12-01",
                    "preferences": ["email", "sms"]
                  }
                }""";

        // UpdateUserActionを実行
        APIGatewayProxyResponseEvent response = updateUserAction.execute(userId, requestBody, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("日本語を含むデータでも処理が完了すること")
    void dataWithJapaneseCharactersProcessingCompletes() {
        String userId = "ユーザー001";
        String requestBody = "{\"name\":\"山田太郎\",\"department\":\"技術部\"}";

        // UpdateUserActionを実行
        APIGatewayProxyResponseEvent response = updateUserAction.execute(userId, requestBody, mockContext);

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
        String requestBody = "{\"name\":\"Format Test User\"}";

        // UpdateUserActionを実行
        APIGatewayProxyResponseEvent response = updateUserAction.execute(userId, requestBody, mockContext);

        String responseBody = response.getBody();
        
        // JSON形式であることを検証
        assertTrue(responseBody.startsWith("{"));
        assertTrue(responseBody.endsWith("}"));
        
        // 期待されるフィールドが含まれていることを検証
        assertTrue(responseBody.contains("userId"));
        assertTrue(responseBody.contains("status"));
        assertTrue(responseBody.contains("updated"));
    }

    @Test
    @DisplayName("複数回実行しても一貫した結果が返されること")
    void multipleExecutionsReturnConsistentResults() {
        String userId = "consistency-test";
        String requestBody = "{\"name\":\"Consistent User\"}";

        // 複数回実行
        APIGatewayProxyResponseEvent response1 = updateUserAction.execute(userId, requestBody, mockContext);
        APIGatewayProxyResponseEvent response2 = updateUserAction.execute(userId, requestBody, mockContext);

        // 同じ結果が返されることを検証
        assertEquals(response1.getStatusCode(), response2.getStatusCode());
        assertEquals(response1.getBody(), response2.getBody());
        assertEquals(response1.getHeaders(), response2.getHeaders());
    }

    @Test
    @DisplayName("Contextパラメータが正しく渡されること")
    void contextParameterIsPassedCorrectly() {
        String userId = "context-test";
        String requestBody = "{\"name\":\"Context Test User\"}";

        // UpdateUserActionを実行（例外が発生しないことを確認）
        assertDoesNotThrow(() -> {
            APIGatewayProxyResponseEvent response = updateUserAction.execute(userId, requestBody, mockContext);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        });
    }

    @Test
    @DisplayName("nullContextでも例外が発生しないこと")
    void nullContextDoesNotThrowException() {
        String userId = "null-context-test";
        String requestBody = "{\"name\":\"Null Context Test\"}";

        // nullコンテキストでの実行
        assertDoesNotThrow(() -> {
            APIGatewayProxyResponseEvent response = updateUserAction.execute(userId, requestBody, null);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        });
    }

    @Test
    @DisplayName("不正なJSONでも例外が発生しないこと")
    void invalidJsonDoesNotThrowException() {
        String userId = "invalid-json-test";
        String invalidJson = "{invalid json}";

        // UpdateUserActionを実行（現在の実装では入力検証していないため例外は発生しない）
        assertDoesNotThrow(() -> {
            APIGatewayProxyResponseEvent response = updateUserAction.execute(userId, invalidJson, mockContext);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        });
    }

    @Test
    @DisplayName("特殊文字を含むユーザーIDでも処理が完了すること")
    void userIdWithSpecialCharactersProcessingCompletes() {
        String userId = "user@example.com";
        String requestBody = "{\"email\":\"new@example.com\"}";

        // UpdateUserActionを実行
        APIGatewayProxyResponseEvent response = updateUserAction.execute(userId, requestBody, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // 特殊文字を含むuserIdが含まれることを確認
        assertTrue(response.getBody().contains(userId));
    }
}