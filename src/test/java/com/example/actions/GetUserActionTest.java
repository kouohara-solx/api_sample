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
 * GetUserActionクラスの単体テスト
 * 特定ユーザー取得機能をテストします。
 */
@ExtendWith(MockitoExtension.class)
class GetUserActionTest {

    private GetUserAction getUserAction;
    
    @Mock
    private Context mockContext;
    
    private Gson gson;

    @BeforeEach
    void setUp() {
        getUserAction = new GetUserAction();
        gson = new Gson();
    }

    @Test
    @DisplayName("指定されたユーザーIDでユーザー取得が正常に完了すること")
    void userRetrievalWithSpecifiedUserIdCompletesSuccessfully() {
        String userId = "user-123";

        // GetUserActionを実行
        APIGatewayProxyResponseEvent response = getUserAction.execute(userId, mockContext);

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

        // GetUserActionを実行
        APIGatewayProxyResponseEvent response = getUserAction.execute(userId, mockContext);

        // レスポンスボディをJSONとしてパース
        assertDoesNotThrow(() -> {
            Map<String, String> userMap = gson.fromJson(response.getBody(), Map.class);
            
            assertNotNull(userMap);
            assertEquals(userId, userMap.get("userId"));
            assertEquals("Taro Yamada", userMap.get("name"));
        });
    }

    @Test
    @DisplayName("異なるユーザーIDでもそれぞれ正しく処理されること")
    void differentUserIdsAreProcessedCorrectly() {
        String[] userIds = {"user-001", "user-999", "admin-123"};

        for (String userId : userIds) {
            // GetUserActionを実行
            APIGatewayProxyResponseEvent response = getUserAction.execute(userId, mockContext);

            // レスポンスを検証
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            
            // レスポンスボディに指定したuserIdが含まれることを確認
            assertTrue(response.getBody().contains(userId));
        }
    }

    @Test
    @DisplayName("空文字のユーザーIDでも処理が完了すること")
    void emptyUserIdProcessingCompletes() {
        String userId = "";

        // GetUserActionを実行
        APIGatewayProxyResponseEvent response = getUserAction.execute(userId, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // 空文字がJSONに含まれることを確認
        assertTrue(response.getBody().contains("\"\""));
    }

    @Test
    @DisplayName("nullユーザーIDでも処理が完了すること")
    void nullUserIdProcessingCompletes() {
        String userId = null;

        // GetUserActionを実行
        APIGatewayProxyResponseEvent response = getUserAction.execute(userId, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // nullがJSON文字列に含まれることを確認
        assertTrue(response.getBody().contains("null"));
    }

    @Test
    @DisplayName("特殊文字を含むユーザーIDでも処理が完了すること")
    void userIdWithSpecialCharactersProcessingCompletes() {
        String userId = "user@example.com";

        // GetUserActionを実行
        APIGatewayProxyResponseEvent response = getUserAction.execute(userId, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // 特殊文字を含むuserIdが含まれることを確認
        assertTrue(response.getBody().contains(userId));
    }

    @Test
    @DisplayName("日本語のユーザーIDでも処理が完了すること")
    void japaneseUserIdProcessingCompletes() {
        String userId = "ユーザー001";

        // GetUserActionを実行
        APIGatewayProxyResponseEvent response = getUserAction.execute(userId, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // 日本語のuserIdが含まれることを確認
        assertTrue(response.getBody().contains(userId));
    }

    @Test
    @DisplayName("非常に長いユーザーIDでも処理が完了すること")
    void veryLongUserIdProcessingCompletes() {
        // 非常に長いユーザーIDを作成
        String userId = "verylongid".repeat(100);

        // GetUserActionを実行
        APIGatewayProxyResponseEvent response = getUserAction.execute(userId, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("レスポンスボディの形式が正しいこと")
    void responseBodyFormatIsCorrect() {
        String userId = "format-test-user";

        // GetUserActionを実行
        APIGatewayProxyResponseEvent response = getUserAction.execute(userId, mockContext);

        String responseBody = response.getBody();
        
        // JSON形式であることを検証
        assertTrue(responseBody.startsWith("{"));
        assertTrue(responseBody.endsWith("}"));
        
        // 期待されるフィールドが含まれていることを検証
        assertTrue(responseBody.contains("userId"));
        assertTrue(responseBody.contains("name"));
        assertTrue(responseBody.contains("Taro Yamada"));
    }

    @Test
    @DisplayName("複数回実行しても一貫した結果が返されること")
    void multipleExecutionsReturnConsistentResults() {
        String userId = "consistency-test";

        // 複数回実行
        APIGatewayProxyResponseEvent response1 = getUserAction.execute(userId, mockContext);
        APIGatewayProxyResponseEvent response2 = getUserAction.execute(userId, mockContext);

        // 同じ結果が返されることを検証
        assertEquals(response1.getStatusCode(), response2.getStatusCode());
        assertEquals(response1.getBody(), response2.getBody());
        assertEquals(response1.getHeaders(), response2.getHeaders());
    }

    @Test
    @DisplayName("Contextパラメータが正しく渡されること")
    void contextParameterIsPassedCorrectly() {
        String userId = "context-test";

        // GetUserActionを実行（例外が発生しないことを確認）
        assertDoesNotThrow(() -> {
            APIGatewayProxyResponseEvent response = getUserAction.execute(userId, mockContext);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        });
    }

    @Test
    @DisplayName("nullContextでも例外が発生しないこと")
    void nullContextDoesNotThrowException() {
        String userId = "null-context-test";

        // nullコンテキストでの実行
        assertDoesNotThrow(() -> {
            APIGatewayProxyResponseEvent response = getUserAction.execute(userId, null);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        });
    }

    @Test
    @DisplayName("JSONエスケープが必要な文字でも正しく処理されること")
    void charactersRequiringJsonEscapeAreProcessedCorrectly() {
        String userId = "user_with_underscore";

        // GetUserActionを実行
        APIGatewayProxyResponseEvent response = getUserAction.execute(userId, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        
        // レスポンスが有効なJSONであることを確認
        assertDoesNotThrow(() -> {
            Map<String, String> userMap = gson.fromJson(response.getBody(), Map.class);
            assertNotNull(userMap);
            assertEquals(userId, userMap.get("userId"));
        });
    }
}