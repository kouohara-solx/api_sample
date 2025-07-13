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
 * CreateUserActionクラスの単体テスト
 * ユーザー作成機能をテストします。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateUserAction Tests")
class CreateUserActionTest {

    private CreateUserAction createUserAction;
    
    @Mock
    private Context mockContext;
    
    private Gson gson;

    @BeforeEach
    void setUp() {
        createUserAction = new CreateUserAction();
        gson = new Gson();
    }

    @Test
    @DisplayName("ユーザー作成が正常に完了すること")
    void shouldCompleteUserCreationSuccessfully() {
        // テスト用のリクエストボディを作成
        String requestBody = "{\"name\":\"Test User\",\"email\":\"test@example.com\"}";

        // CreateUserActionを実行
        APIGatewayProxyResponseEvent response = createUserAction.execute(requestBody, mockContext);

        // レスポンスを検証
        assertNotNull(response);
        assertEquals(201, response.getStatusCode()); // Created
        assertNotNull(response.getBody());
        
        // Content-Typeヘッダーを検証
        assertNotNull(response.getHeaders());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    @DisplayName("レスポンスボディが正しいJSON形式であること")
    void shouldReturnValidJsonResponseBody() {
        // テスト用のリクエストボディを作成
        String requestBody = "{\"name\":\"Test User\",\"email\":\"test@example.com\"}";

        // CreateUserActionを実行
        APIGatewayProxyResponseEvent response = createUserAction.execute(requestBody, mockContext);

        // レスポンスボディをJSONとしてパース
        assertDoesNotThrow(() -> {
            Map<String, String> responseMap = gson.fromJson(response.getBody(), Map.class);
            
            assertNotNull(responseMap);
            assertEquals("user-003", responseMap.get("userId"));
            assertEquals("created", responseMap.get("status"));
        });
    }

    @Test
    @DisplayName("空のリクエストボディでも処理が完了すること")
    void shouldCompleteProcessingWithEmptyRequestBody() {
        // 空のリクエストボディ
        String requestBody = "";

        // CreateUserActionを実行
        APIGatewayProxyResponseEvent response = createUserAction.execute(requestBody, mockContext);

        // 正常に処理されることを検証（現在の実装ではリクエストボディを使用していない）
        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("nullリクエストボディでも処理が完了すること")
    void shouldCompleteProcessingWithNullRequestBody() {
        // nullリクエストボディ
        String requestBody = null;

        // CreateUserActionを実行
        APIGatewayProxyResponseEvent response = createUserAction.execute(requestBody, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("複雑なJSONリクエストボディでも処理が完了すること")
    void shouldCompleteProcessingWithComplexJsonRequestBody() {
        // 複雑なJSONリクエストボディ
        String requestBody = """
                {
                  "name": "田中太郎",
                  "email": "tanaka@example.com",
                  "role": "admin",
                  "organization": "東京支社",
                  "metadata": {
                    "created_by": "system",
                    "department": "IT"
                  }
                }""";

        // CreateUserActionを実行
        APIGatewayProxyResponseEvent response = createUserAction.execute(requestBody, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("レスポンスボディに期待される値が含まれていること")
    void shouldContainExpectedValuesInResponseBody() {
        // テスト用のリクエストボディを作成
        String requestBody = "{\"name\":\"Test User\"}";

        // CreateUserActionを実行
        APIGatewayProxyResponseEvent response = createUserAction.execute(requestBody, mockContext);

        String responseBody = response.getBody();
        
        // 期待される値が含まれていることを検証
        assertTrue(responseBody.contains("user-003"));
        assertTrue(responseBody.contains("created"));
        
        // JSON形式であることを検証
        assertTrue(responseBody.startsWith("{"));
        assertTrue(responseBody.endsWith("}"));
    }

    @Test
    @DisplayName("複数回実行しても一貫した結果が返されること")
    void shouldReturnConsistentResultsAcrossMultipleExecutions() {
        String requestBody = "{\"name\":\"Consistent Test\"}";

        // 複数回実行
        APIGatewayProxyResponseEvent response1 = createUserAction.execute(requestBody, mockContext);
        APIGatewayProxyResponseEvent response2 = createUserAction.execute(requestBody, mockContext);

        // 同じ結果が返されることを検証
        assertEquals(response1.getStatusCode(), response2.getStatusCode());
        assertEquals(response1.getBody(), response2.getBody());
        assertEquals(response1.getHeaders(), response2.getHeaders());
    }

    @Test
    @DisplayName("Contextパラメータが正しく渡されること")
    void shouldPassContextParameterCorrectly() {
        String requestBody = "{\"name\":\"Context Test\"}";

        // CreateUserActionを実行（例外が発生しないことを確認）
        assertDoesNotThrow(() -> {
            APIGatewayProxyResponseEvent response = createUserAction.execute(requestBody, mockContext);
            assertNotNull(response);
            assertEquals(201, response.getStatusCode());
        });
    }

    @Test
    @DisplayName("不正なJSONでも例外が発生しないこと")
    void shouldNotThrowExceptionWithInvalidJson() {
        // 不正なJSONリクエストボディ
        String invalidJson = "{invalid json}";

        // CreateUserActionを実行（現在の実装では入力検証していないため例外は発生しない）
        assertDoesNotThrow(() -> {
            APIGatewayProxyResponseEvent response = createUserAction.execute(invalidJson, mockContext);
            assertNotNull(response);
            assertEquals(201, response.getStatusCode());
        });
    }

    @Test
    @DisplayName("長いリクエストボディでも処理が完了すること")
    void shouldCompleteProcessingWithLongRequestBody() {
        // 非常に長いリクエストボディを作成
        String longRequestBody = "{\"name\":\"" + "LongName".repeat(1000) +
                "\"}";

        // CreateUserActionを実行
        APIGatewayProxyResponseEvent response = createUserAction.execute(longRequestBody, mockContext);

        // 正常に処理されることを検証
        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("nullContextでも例外が発生しないこと")
    void shouldNotThrowExceptionWithNullContext() {
        String requestBody = "{\"name\":\"Null Context Test\"}";

        // nullコンテキストでの実行
        assertDoesNotThrow(() -> {
            APIGatewayProxyResponseEvent response = createUserAction.execute(requestBody, null);
            assertNotNull(response);
            assertEquals(201, response.getStatusCode());
        });
    }
}