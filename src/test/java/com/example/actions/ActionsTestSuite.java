package com.example.actions;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * 全アクションクラスのテストスイート
 * 
 * <p>このテストスイートを実行することで、すべてのアクションクラスの
 * 単体テストを一括で実行できます。</p>
 * 
 * <p>実行方法:</p>
 * <pre>
 * mvn test -Dtest=ActionsTestSuite
 * </pre>
 */
@Suite
@SelectClasses({
    ListUsersActionTest.class,
    CreateUserActionTest.class,
    GetUserActionTest.class,
    UpdateUserActionTest.class,
    DeleteUserActionTest.class,
    PatchUserActionTest.class
})
public class ActionsTestSuite {
    // テストスイートクラスは空でOK
    // アノテーションでテストクラスを指定
}