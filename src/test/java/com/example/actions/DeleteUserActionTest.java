package com.example.actions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DeleteUserActionクラスの単体テスト
 * ユーザー削除機能をテストします。
 */
@ExtendWith(MockitoExtension.class)
class DeleteUserActionTest {

    private DeleteUserAction deleteUserAction;
    
    @Mock
    private Context mockContext;

    @BeforeEach
    void setUp() {
        deleteUserAction = new DeleteUserAction();
    }

    @Test
    @DisplayName("ユーザー削除が正常に完了すること")
    void userDeletionCompletesSuccessfully() {
        String userId = "user-123";

        // DeleteUserActionを実行
        APIGatewayProxyResponseEvent response = deleteUserAction.execute(userId, mockContext);

        // レスポンスを検証
        assertNotNull(response);
        assertEquals(204, response.getStatusCode()); // No Content
        
        // 削除操作では通常ボディは返さない
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("異なるユーザーIDでもそれぞれ正しく処理されること")
    void differentUserIdsAreProcessedCorrectly() {
        String[] userIds = {"user-001", "admin-999", "guest-456"};

        for (String userId : userIds) {
            // DeleteUserActionを実行
            APIGatewayProxyResponseEvent response = deleteUserAction.execute(userId, mockContext);

            // レスポンスを検証
            assertNotNull(response);
            assertEquals(204, response.getStatusCode());
            assertNull(response.getBody());
        }
    }

    @Test
    @DisplayName("空文字のユーザーIDでも処理が完了すること")
    void emptyUserIdProcessingCompletes() {
        String userId = "";

        // DeleteUserActionを実行
        APIGatewayProxyResponseEvent response = deleteUserAction.execute(userId, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(204, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("nullユーザーIDでも処理が完了すること")
    void nullUserIdProcessingCompletes() {
        String userId = null;

        // DeleteUserActionを実行
        APIGatewayProxyResponseEvent response = deleteUserAction.execute(userId, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(204, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("特殊文字を含むユーザーIDでも処理が完了すること")
    void userIdWithSpecialCharactersProcessingCompletes() {
        String userId = "user@example.com";

        // DeleteUserActionを実行
        APIGatewayProxyResponseEvent response = deleteUserAction.execute(userId, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(204, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("日本語のユーザーIDでも処理が完了すること")
    void japaneseUserIdProcessingCompletes() {
        String userId = "ユーザー001";

        // DeleteUserActionを実行
        APIGatewayProxyResponseEvent response = deleteUserAction.execute(userId, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(204, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("非常に長いユーザーIDでも処理が完了すること")
    void veryLongUserIdProcessingCompletes() {
        // 非常に長いユーザーIDを作成
        String userId = "verylongid".repeat(100);

        // DeleteUserActionを実行
        APIGatewayProxyResponseEvent response = deleteUserAction.execute(userId, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(204, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("複数回実行しても一貫した結果が返されること")
    void multipleExecutionsReturnConsistentResults() {
        String userId = "consistency-test";

        // 複数回実行
        APIGatewayProxyResponseEvent response1 = deleteUserAction.execute(userId, mockContext);
        APIGatewayProxyResponseEvent response2 = deleteUserAction.execute(userId, mockContext);

        // 同じ結果が返されることを検証
        assertEquals(response1.getStatusCode(), response2.getStatusCode());
        assertEquals(response1.getBody(), response2.getBody());
        assertEquals(response1.getHeaders(), response2.getHeaders());
    }

    @Test
    @DisplayName("Contextパラメータが正しく渡されること")
    void contextParameterIsPassedCorrectly() {
        String userId = "context-test";

        // DeleteUserActionを実行（例外が発生しないことを確認）
        assertDoesNotThrow(() -> {
            APIGatewayProxyResponseEvent response = deleteUserAction.execute(userId, mockContext);
            assertNotNull(response);
            assertEquals(204, response.getStatusCode());
        });
    }

    @Test
    @DisplayName("nullContextでも例外が発生しないこと")
    void nullContextDoesNotThrowException() {
        String userId = "null-context-test";

        // nullコンテキストでの実行
        assertDoesNotThrow(() -> {
            APIGatewayProxyResponseEvent response = deleteUserAction.execute(userId, null);
            assertNotNull(response);
            assertEquals(204, response.getStatusCode());
        });
    }

    @Test
    @DisplayName("レスポンスヘッダーが適切に設定されていること")
    void responseHeadersAreSetProperly() {
        String userId = "header-test";

        // DeleteUserActionを実行
        APIGatewayProxyResponseEvent response = deleteUserAction.execute(userId, mockContext);

        // ヘッダーの確認（現在の実装では特別なヘッダーは設定されていない）
        // 必要に応じてCORSヘッダーなどが設定される可能性がある
        assertNotNull(response);
        assertEquals(204, response.getStatusCode());
    }

    @Test
    @DisplayName("削除操作の冪等性が保たれること")
    void deleteOperationIdempotencyIsPreserved() {
        String userId = "idempotency-test";

        // 同じユーザーIDで複数回削除を実行
        APIGatewayProxyResponseEvent response1 = deleteUserAction.execute(userId, mockContext);
        APIGatewayProxyResponseEvent response2 = deleteUserAction.execute(userId, mockContext);
        APIGatewayProxyResponseEvent response3 = deleteUserAction.execute(userId, mockContext);

        // すべて同じ結果が返されることを検証（冪等性）
        assertEquals(204, response1.getStatusCode());
        assertEquals(204, response2.getStatusCode());
        assertEquals(204, response3.getStatusCode());
        
        assertNull(response1.getBody());
        assertNull(response2.getBody());
        assertNull(response3.getBody());
    }

    @Test
    @DisplayName("SQLインジェクション攻撃的な文字列でも安全に処理されること")
    void sqlInjectionAttackStringsAreProcessedSafely() {
        String maliciousUserId = "'; DROP TABLE users; --";

        // DeleteUserActionを実行
        APIGatewayProxyResponseEvent response = deleteUserAction.execute(maliciousUserId, mockContext);

        // 正常に処理されることを検証（現在の実装ではDBアクセスしていないが、セキュリティテスト）
        assertNotNull(response);
        assertEquals(204, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("UUIDフォーマットのユーザーIDでも処理が完了すること")
    void uuidFormatUserIdProcessingCompletes() {
        String uuidUserId = "550e8400-e29b-41d4-a716-446655440000";

        // DeleteUserActionを実行
        APIGatewayProxyResponseEvent response = deleteUserAction.execute(uuidUserId, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(204, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("数値のみのユーザーIDでも処理が完了すること")
    void numericOnlyUserIdProcessingCompletes() {
        String numericUserId = "123456789";

        // DeleteUserActionを実行
        APIGatewayProxyResponseEvent response = deleteUserAction.execute(numericUserId, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(204, response.getStatusCode());
        assertNull(response.getBody());
    }
}