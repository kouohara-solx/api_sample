package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

/**
 * AdminUsersHandlerクラスの単体テスト
 * ユーザー管理機能のルーティングをテストします。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUsersHandler Tests")
class AdminUsersHandlerTest {

    private AdminUsersHandler adminUsersHandler;
    
    @Mock
    private Context mockContext;
    
    @Mock
    private LambdaLogger mockLogger;

    @BeforeEach
    void setUp() {
        adminUsersHandler = new AdminUsersHandler();
        lenient().when(mockContext.getLogger()).thenReturn(mockLogger);
    }

    @Test
    @DisplayName("GETメソッドでuserIdなしの場合にListUsersActionが呼ばれること")
    void shouldCallListUsersActionForGetMethodWithoutUserId() {
        // テスト用のリクエストを作成（ユーザー一覧取得）
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("GET");
        request.setPathParameters(null); // userIdなし

        // AdminUsersHandlerを実行
        APIGatewayProxyResponseEvent response = adminUsersHandler.handleRequest(request, mockContext);

        // レスポンスを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("user-001")); // ListUsersActionのダミーデータ
        assertTrue(response.getBody().contains("Taro Yamada"));
    }

    @Test
    @DisplayName("GETメソッドでuserIdありの場合にGetUserActionが呼ばれること")
    void shouldCallGetUserActionForGetMethodWithUserId() {
        // テスト用のリクエストを作成（特定ユーザー取得）
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("GET");
        
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("userId", "user-123");
        request.setPathParameters(pathParameters);

        // AdminUsersHandlerを実行
        APIGatewayProxyResponseEvent response = adminUsersHandler.handleRequest(request, mockContext);

        // レスポンスを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("user-123")); // GetUserActionのダミーデータ
    }

    @Test
    @DisplayName("POSTメソッドでCreateUserActionが呼ばれること")
    void shouldCallCreateUserActionForPostMethod() {
        // テスト用のリクエストを作成（ユーザー作成）
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("POST");
        request.setBody("{\"name\":\"New User\",\"email\":\"newuser@example.com\"}");

        // AdminUsersHandlerを実行
        APIGatewayProxyResponseEvent response = adminUsersHandler.handleRequest(request, mockContext);

        // レスポンスを検証
        assertNotNull(response);
        assertEquals(201, response.getStatusCode()); // Created
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("user-003")); // CreateUserActionのダミーデータ
        assertTrue(response.getBody().contains("created"));
    }

    @Test
    @DisplayName("PUTメソッドでUpdateUserActionが呼ばれること")
    void shouldCallUpdateUserActionForPutMethod() {
        // テスト用のリクエストを作成（ユーザー更新）
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("PUT");
        request.setBody("{\"name\":\"Updated User\",\"email\":\"updated@example.com\"}");
        
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("userId", "user-456");
        request.setPathParameters(pathParameters);

        // AdminUsersHandlerを実行
        APIGatewayProxyResponseEvent response = adminUsersHandler.handleRequest(request, mockContext);

        // レスポンスを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("user-456")); // UpdateUserActionのダミーデータ
        assertTrue(response.getBody().contains("updated"));
    }

    @Test
    @DisplayName("DELETEメソッドでDeleteUserActionが呼ばれること")
    void shouldCallDeleteUserActionForDeleteMethod() {
        // テスト用のリクエストを作成（ユーザー削除）
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("DELETE");
        
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("userId", "user-789");
        request.setPathParameters(pathParameters);

        // AdminUsersHandlerを実行
        APIGatewayProxyResponseEvent response = adminUsersHandler.handleRequest(request, mockContext);

        // レスポンスを検証
        assertNotNull(response);
        assertEquals(204, response.getStatusCode()); // No Content
        // DELETEは通常ボディを返さない
    }

    @Test
    @DisplayName("PATCHメソッドでPatchUserActionが呼ばれること")
    void shouldCallPatchUserActionForPatchMethod() {
        // テスト用のリクエストを作成（ユーザー部分更新）
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("PATCH");
        request.setBody("{\"email\":\"patched@example.com\"}");
        
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("userId", "user-999");
        request.setPathParameters(pathParameters);

        // AdminUsersHandlerを実行
        APIGatewayProxyResponseEvent response = adminUsersHandler.handleRequest(request, mockContext);

        // レスポンスを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("user-999")); // PatchUserActionのダミーデータ
        assertTrue(response.getBody().contains("patched"));
    }

    @Test
    @DisplayName("サポートされていないHTTPメソッドで405エラーが返されること")
    void shouldReturn405ErrorForUnsupportedHttpMethod() {
        // テスト用のリクエストを作成（サポートされていないメソッド）
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("OPTIONS");

        // AdminUsersHandlerを実行
        APIGatewayProxyResponseEvent response = adminUsersHandler.handleRequest(request, mockContext);

        // レスポンスを検証
        assertNotNull(response);
        assertEquals(405, response.getStatusCode()); // Method Not Allowed
        assertEquals("Method Not Allowed", response.getBody());
    }

    @Test
    @DisplayName("PUTメソッドでuserIdがnullの場合の処理")
    void putMethodWithNullUserIdProcessing() {
        // テスト用のリクエストを作成（userIdなしのPUT）
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("PUT");
        request.setBody("{\"name\":\"Test User\"}");
        request.setPathParameters(null); // userIdなし

        // AdminUsersHandlerを実行
        APIGatewayProxyResponseEvent response = adminUsersHandler.handleRequest(request, mockContext);

        // レスポンスを検証（現在の実装ではnullが渡される）
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("null")); // userIdがnullとして処理される
    }

    @Test
    @DisplayName("DELETEメソッドでuserIdがnullの場合の処理")
    void deleteMethodWithNullUserIdProcessing() {
        // テスト用のリクエストを作成（userIdなしのDELETE）
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("DELETE");
        request.setPathParameters(null); // userIdなし

        // AdminUsersHandlerを実行
        APIGatewayProxyResponseEvent response = adminUsersHandler.handleRequest(request, mockContext);

        // レスポンスを検証（現在の実装ではnullが渡される）
        assertNotNull(response);
        assertEquals(204, response.getStatusCode()); // DeleteUserActionは常に204を返す
    }

    @Test
    @DisplayName("pathParametersが空のMapの場合の処理")
    void emptyPathParametersMapProcessing() {
        // テスト用のリクエストを作成（空のpathParameters）
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("GET");
        request.setPathParameters(new HashMap<>()); // 空のMap

        // AdminUsersHandlerを実行
        APIGatewayProxyResponseEvent response = adminUsersHandler.handleRequest(request, mockContext);

        // レスポンスを検証（userIdがnullなのでListUsersActionが呼ばれる）
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("user-001")); // ListUsersActionのダミーデータ
    }
}