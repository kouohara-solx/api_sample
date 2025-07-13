package com.example;

import com.amazonaws.services.lambda.runtime.Context;
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

/**
 * ProtectedHandlerクラスの単体テスト
 * 保護されたエンドポイントの機能をテストします。
 */
@ExtendWith(MockitoExtension.class)
class ProtectedHandlerTest {

    private ProtectedHandler protectedHandler;
    
    @Mock
    private Context mockContext;

    @BeforeEach
    void setUp() {
        protectedHandler = new ProtectedHandler();
    }

    @Test
    @DisplayName("認証されたユーザー情報が正しく表示されること")
    void authenticatedUserInfoDisplayedCorrectly() {
        // テスト用のリクエストを作成
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        
        // RequestContextとAuthorizerを設定
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext = 
            new APIGatewayProxyRequestEvent.ProxyRequestContext();
        
        // Authorizerから渡されるコンテキスト情報をモック
        Map<String, Object> authorizerContext = new HashMap<>();
        authorizerContext.put("principalId", "user-001");
        authorizerContext.put("role", "editor");
        authorizerContext.put("organization_id", "org-abc");
        
        requestContext.setAuthorizer(authorizerContext);
        request.setRequestContext(requestContext);

        // ProtectedHandlerを実行
        APIGatewayProxyResponseEvent response = protectedHandler.handleRequest(request, mockContext);

        // レスポンスを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // レスポンスボディに期待される情報が含まれていることを確認
        String responseBody = response.getBody();
        assertTrue(responseBody.contains("user-001"));
        assertTrue(responseBody.contains("editor"));
        assertTrue(responseBody.contains("org-abc"));
        assertTrue(responseBody.contains("Hello user"));
    }

    @Test
    @DisplayName("異なるユーザー情報でも正しく処理されること")
    void differentUserInfoProcessedCorrectly() {
        // テスト用のリクエストを作成
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        
        // RequestContextとAuthorizerを設定
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext = 
            new APIGatewayProxyRequestEvent.ProxyRequestContext();
        
        // 異なるユーザー情報をモック
        Map<String, Object> authorizerContext = new HashMap<>();
        authorizerContext.put("principalId", "user-999");
        authorizerContext.put("role", "admin");
        authorizerContext.put("organization_id", "org-xyz");
        
        requestContext.setAuthorizer(authorizerContext);
        request.setRequestContext(requestContext);

        // ProtectedHandlerを実行
        APIGatewayProxyResponseEvent response = protectedHandler.handleRequest(request, mockContext);

        // レスポンスを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        
        String responseBody = response.getBody();
        assertTrue(responseBody.contains("user-999"));
        assertTrue(responseBody.contains("admin"));
        assertTrue(responseBody.contains("org-xyz"));
    }

    @Test
    @DisplayName("日本語の組織名とロールでも正しく処理されること")
    void japaneseOrganizationNameAndRoleProcessedCorrectly() {
        // テスト用のリクエストを作成
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        
        // RequestContextとAuthorizerを設定
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext = 
            new APIGatewayProxyRequestEvent.ProxyRequestContext();
        
        // 日本語を含むユーザー情報をモック
        Map<String, Object> authorizerContext = new HashMap<>();
        authorizerContext.put("principalId", "user-jp-001");
        authorizerContext.put("role", "管理者");
        authorizerContext.put("organization_id", "東京支社");
        
        requestContext.setAuthorizer(authorizerContext);
        request.setRequestContext(requestContext);

        // ProtectedHandlerを実行
        APIGatewayProxyResponseEvent response = protectedHandler.handleRequest(request, mockContext);

        // レスポンスを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        
        String responseBody = response.getBody();
        assertTrue(responseBody.contains("user-jp-001"));
        assertTrue(responseBody.contains("管理者"));
        assertTrue(responseBody.contains("東京支社"));
    }

    @Test
    @DisplayName("レスポンスメッセージの形式が正しいこと")
    void responseMessageFormatIsCorrect() {
        // テスト用のリクエストを作成
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        
        // RequestContextとAuthorizerを設定
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext = 
            new APIGatewayProxyRequestEvent.ProxyRequestContext();
        
        Map<String, Object> authorizerContext = new HashMap<>();
        authorizerContext.put("principalId", "test-user");
        authorizerContext.put("role", "viewer");
        authorizerContext.put("organization_id", "test-org");
        
        requestContext.setAuthorizer(authorizerContext);
        request.setRequestContext(requestContext);

        // ProtectedHandlerを実行
        APIGatewayProxyResponseEvent response = protectedHandler.handleRequest(request, mockContext);

        // レスポンスメッセージの形式を検証
        String expectedMessage = "Hello user test-user from organization test-org! Your role is viewer.";
        assertEquals(expectedMessage, response.getBody());
    }

    @Test
    @DisplayName("特殊文字を含むユーザー情報でも正しく処理されること")
    void userInfoWithSpecialCharactersProcessedCorrectly() {
        // テスト用のリクエストを作成
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        
        // RequestContextとAuthorizerを設定
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext = 
            new APIGatewayProxyRequestEvent.ProxyRequestContext();
        
        // 特殊文字を含むユーザー情報をモック
        Map<String, Object> authorizerContext = new HashMap<>();
        authorizerContext.put("principalId", "user@example.com");
        authorizerContext.put("role", "super-admin");
        authorizerContext.put("organization_id", "org-001-dev");
        
        requestContext.setAuthorizer(authorizerContext);
        request.setRequestContext(requestContext);

        // ProtectedHandlerを実行
        APIGatewayProxyResponseEvent response = protectedHandler.handleRequest(request, mockContext);

        // レスポンスを検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        
        String responseBody = response.getBody();
        assertTrue(responseBody.contains("user@example.com"));
        assertTrue(responseBody.contains("super-admin"));
        assertTrue(responseBody.contains("org-001-dev"));
    }

    @Test
    @DisplayName("空文字のユーザー情報でも例外が発生しないこと")
    void emptyUserInfoDoesNotThrowException() {
        // テスト用のリクエストを作成
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        
        // RequestContextとAuthorizerを設定
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext = 
            new APIGatewayProxyRequestEvent.ProxyRequestContext();
        
        // 空文字のユーザー情報をモック
        Map<String, Object> authorizerContext = new HashMap<>();
        authorizerContext.put("principalId", "");
        authorizerContext.put("role", "");
        authorizerContext.put("organization_id", "");
        
        requestContext.setAuthorizer(authorizerContext);
        request.setRequestContext(requestContext);

        // ProtectedHandlerを実行（例外が発生しないことを確認）
        assertDoesNotThrow(() -> {
            APIGatewayProxyResponseEvent response = protectedHandler.handleRequest(request, mockContext);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        });
    }

    @Test
    @DisplayName("ContextパラメータがMockで正しく渡されること")
    void contextParameterPassedCorrectlyWithMock() {
        // テスト用のリクエストを作成
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        
        // RequestContextとAuthorizerを設定
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext = 
            new APIGatewayProxyRequestEvent.ProxyRequestContext();
        
        Map<String, Object> authorizerContext = new HashMap<>();
        authorizerContext.put("principalId", "user-001");
        authorizerContext.put("role", "editor");
        authorizerContext.put("organization_id", "org-abc");
        
        requestContext.setAuthorizer(authorizerContext);
        request.setRequestContext(requestContext);

        // ProtectedHandlerを実行（例外が発生しないことを確認）
        assertDoesNotThrow(() -> {
            APIGatewayProxyResponseEvent response = protectedHandler.handleRequest(request, mockContext);
            assertNotNull(response);
            // Contextが正しく渡されていれば処理が完了する
            assertEquals(200, response.getStatusCode());
        });
    }
}