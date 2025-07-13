package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

/**
 * AuthorizerHandlerクラスの単体テスト
 * JWT検証とIAMポリシー生成機能をテストします。
 */
@ExtendWith(MockitoExtension.class)
class AuthorizerHandlerTest {

    private AuthorizerHandler authorizerHandler;
    
    @Mock
    private Context mockContext;
    
    @Mock
    private LambdaLogger mockLogger;
    
    private static final String SECRET_KEY = "your-very-secret-key";
    private static final String SAMPLE_METHOD_ARN = "arn:aws:execute-api:ap-northeast-1:123456789012:abcdef123/Prod/GET/hello";

    @BeforeEach
    void setUp() {
        authorizerHandler = new AuthorizerHandler();
        lenient().when(mockContext.getLogger()).thenReturn(mockLogger);
    }

    @Test
    @DisplayName("有効なJWTトークンでAllowポリシーが生成されること")
    void validJwtTokenGeneratesAllowPolicy() {
        // 有効なJWTトークンを生成
        String validToken = createValidJWT();
        
        // テスト用のイベントを作成
        APIGatewayCustomAuthorizerEvent event = new APIGatewayCustomAuthorizerEvent();
        event.setAuthorizationToken("Bearer " + validToken);
        event.setMethodArn(SAMPLE_METHOD_ARN);

        // AuthorizerHandlerを実行
        Map<String, Object> response = authorizerHandler.handleRequest(event, mockContext);

        // レスポンスを検証
        assertNotNull(response);
        assertEquals("user-001", response.get("principalId"));
        
        // ポリシードキュメントを検証
        @SuppressWarnings("unchecked")
        Map<String, Object> policyDocument = (Map<String, Object>) response.get("policyDocument");
        assertNotNull(policyDocument);
        assertEquals("2012-10-17", policyDocument.get("Version"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> statements = (List<Map<String, Object>>) policyDocument.get("Statement");
        assertNotNull(statements);
        assertEquals(1, statements.size());
        assertEquals("Allow", statements.getFirst().get("Effect"));
        
        // コンテキストを検証
        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) response.get("context");
        assertNotNull(context);
        assertEquals("user-001", context.get("principalId"));
        assertEquals("editor", context.get("role"));
        assertEquals("org-abc", context.get("organization_id"));
    }

    @Test
    @DisplayName("Bearerプレフィックスが無いトークンでDenyポリシーが生成されること")
    void tokenWithoutBearerPrefixGeneratesDenyPolicy() {
        // Bearerプレフィックスなしのトークン
        String invalidToken = "invalid-token-without-bearer";
        
        // テスト用のイベントを作成
        APIGatewayCustomAuthorizerEvent event = new APIGatewayCustomAuthorizerEvent();
        event.setAuthorizationToken(invalidToken);
        event.setMethodArn(SAMPLE_METHOD_ARN);

        // AuthorizerHandlerを実行
        Map<String, Object> response = authorizerHandler.handleRequest(event, mockContext);

        // Denyポリシーが生成されることを検証
        assertNotNull(response);
        assertEquals("unauthorized", response.get("principalId"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> policyDocument = (Map<String, Object>) response.get("policyDocument");
        assertNotNull(policyDocument);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> statements = (List<Map<String, Object>>) policyDocument.get("Statement");
        assertEquals("Deny", statements.getFirst().get("Effect"));
    }

    @Test
    @DisplayName("トークンがnullの場合にDenyポリシーが生成されること")
    void nullTokenGeneratesDenyPolicy() {
        // テスト用のイベントを作成
        APIGatewayCustomAuthorizerEvent event = new APIGatewayCustomAuthorizerEvent();
        event.setAuthorizationToken(null);
        event.setMethodArn(SAMPLE_METHOD_ARN);

        // AuthorizerHandlerを実行
        Map<String, Object> response = authorizerHandler.handleRequest(event, mockContext);

        // Denyポリシーが生成されることを検証
        assertNotNull(response);
        assertEquals("unauthorized", response.get("principalId"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> policyDocument = (Map<String, Object>) response.get("policyDocument");
        assertNotNull(policyDocument);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> statements = (List<Map<String, Object>>) policyDocument.get("Statement");
        assertEquals("Deny", statements.getFirst().get("Effect"));
    }

    @Test
    @DisplayName("無効な署名のJWTトークンでDenyポリシーが生成されること")
    void invalidSignatureJwtTokenGeneratesDenyPolicy() {
        // 異なる秘密鍵で署名されたJWTトークンを生成
        String invalidToken = createInvalidJWT();
        
        // テスト用のイベントを作成
        APIGatewayCustomAuthorizerEvent event = new APIGatewayCustomAuthorizerEvent();
        event.setAuthorizationToken("Bearer " + invalidToken);
        event.setMethodArn(SAMPLE_METHOD_ARN);

        // AuthorizerHandlerを実行
        Map<String, Object> response = authorizerHandler.handleRequest(event, mockContext);

        // Denyポリシーが生成されることを検証
        assertNotNull(response);
        assertEquals("unauthorized", response.get("principalId"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> policyDocument = (Map<String, Object>) response.get("policyDocument");
        assertNotNull(policyDocument);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> statements = (List<Map<String, Object>>) policyDocument.get("Statement");
        assertEquals("Deny", statements.getFirst().get("Effect"));
    }

    @Test
    @DisplayName("期限切れのJWTトークンでDenyポリシーが生成されること")
    void expiredJwtTokenGeneratesDenyPolicy() {
        // 期限切れのJWTトークンを生成
        String expiredToken = createExpiredJWT();
        
        // テスト用のイベントを作成
        APIGatewayCustomAuthorizerEvent event = new APIGatewayCustomAuthorizerEvent();
        event.setAuthorizationToken("Bearer " + expiredToken);
        event.setMethodArn(SAMPLE_METHOD_ARN);

        // AuthorizerHandlerを実行
        Map<String, Object> response = authorizerHandler.handleRequest(event, mockContext);

        // Denyポリシーが生成されることを検証
        assertNotNull(response);
        assertEquals("unauthorized", response.get("principalId"));
    }

    @Test
    @DisplayName("ARNから正しいリソースパターンが生成されること")
    void correctResourcePatternGeneratedFromArn() {
        // 有効なJWTトークンを生成
        String validToken = createValidJWT();
        
        // テスト用のイベントを作成
        APIGatewayCustomAuthorizerEvent event = new APIGatewayCustomAuthorizerEvent();
        event.setAuthorizationToken("Bearer " + validToken);
        event.setMethodArn(SAMPLE_METHOD_ARN);

        // AuthorizerHandlerを実行
        Map<String, Object> response = authorizerHandler.handleRequest(event, mockContext);

        // リソースARNが汎用パターンに変換されていることを検証
        @SuppressWarnings("unchecked")
        Map<String, Object> policyDocument = (Map<String, Object>) response.get("policyDocument");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> statements = (List<Map<String, Object>>) policyDocument.get("Statement");
        
        String resourceArn = (String) statements.getFirst().get("Resource");
        assertTrue(resourceArn.endsWith("/*/*"), "リソースARNは汎用パターン(/*/*)で終わる必要があります");
        assertTrue(resourceArn.contains("arn:aws:execute-api"), "リソースARNはAPI Gateway ARNの形式である必要があります");
    }

    /**
     * テスト用の有効なJWTトークンを生成
     */
    private String createValidJWT() {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        return JWT.create()
                .withSubject("user-001")
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600 * 1000)) // 1時間後
                .withClaim("role", "editor")
                .withClaim("organization_id", "org-abc")
                .sign(algorithm);
    }

    /**
     * テスト用の無効な署名のJWTトークンを生成
     */
    private String createInvalidJWT() {
        Algorithm algorithm = Algorithm.HMAC256("wrong-secret-key");
        return JWT.create()
                .withSubject("user-001")
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600 * 1000))
                .withClaim("role", "editor")
                .withClaim("organization_id", "org-abc")
                .sign(algorithm);
    }

    /**
     * テスト用の期限切れJWTトークンを生成
     */
    private String createExpiredJWT() {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        return JWT.create()
                .withSubject("user-001")
                .withExpiresAt(new Date(System.currentTimeMillis() - 3600 * 1000)) // 1時間前（期限切れ）
                .withClaim("role", "editor")
                .withClaim("organization_id", "org-abc")
                .sign(algorithm);
    }
}