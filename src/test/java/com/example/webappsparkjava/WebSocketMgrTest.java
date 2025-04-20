package com.example.webappsparkjava;

import com.google.gson.JsonObject;
import manager.UserInfoHandler;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class WebSocketMgrTest {
    @Mocked
    private Session mockSession;

    @Mocked
    private RemoteEndpoint mockRemote;

    @Mocked
    private UserInfoHandler mockUserInfoHandler;

    private WebSocketMgr webSocketMgr;

    @Before
    public void setUp() {
        webSocketMgr = new WebSocketMgr();
        // Use reflection to set the private userInfoHandler field
        try {
            java.lang.reflect.Field field = WebSocketMgr.class.getDeclaredField("userInfoHandler");
            field.setAccessible(true);
            field.set(webSocketMgr, mockUserInfoHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Expectations() {{
            mockSession.getRemote();
            result = mockRemote;
        }};
    }

//    @Test
//    public void testHandleUpdateUserInfo_Success() throws IOException {
//        // Arrange
//        JsonObject message = new JsonObject();
//        message.addProperty("type", "updateUserInfo");
//        message.addProperty("userId", 1);
//        JsonObject updates = new JsonObject();
//        updates.addProperty("email", "new@example.com");
//        message.add("updates", updates);
//
//        JsonObject expectedResponse = new JsonObject();
//        expectedResponse.addProperty("type", "userInfoUpdated");
//        expectedResponse.addProperty("success", true);
//        expectedResponse.addProperty("email", "new@example.com");
//
//        new Expectations() {{
//            mockUserInfoHandler.updateUserInfo(1, "new@example.com", (JsonObject) any);
//            result = expectedResponse;
//        }};
//
//        // Act
//        webSocketMgr.handleUpdateUserInfo(mockSession, message);
//
//        // Assert
//        new Verifications() {{
//            mockRemote.sendString(expectedResponse.toString());
//        }};
//    }

//    @Test
//    public void testHandleUpdateUserInfo_InvalidUserId() throws IOException {
//        // Arrange
//        JsonObject message = new JsonObject();
//        message.addProperty("type", "updateUserInfo");
//        JsonObject updates = new JsonObject();
//        updates.addProperty("email", "new@example.com");
//        message.add("updates", updates);
//
//        JsonObject expectedResponse = new JsonObject();
//        expectedResponse.addProperty("type", "userInfoUpdated");
//        expectedResponse.addProperty("success", false);
//        expectedResponse.addProperty("error", "Failed to update email");
//
//        // Act
//        webSocketMgr.handleUpdateUserInfo(mockSession, message);
//
//        // Assert
//        new Verifications() {{
//            mockRemote.sendString(expectedResponse.toString());
//        }};
//    }
//
//    @Test
//    public void testHandleUpdateUserInfo_MissingEmail() throws IOException {
//        // Arrange
//        JsonObject message = new JsonObject();
//        message.addProperty("type", "updateUserInfo");
//        message.addProperty("userId", 1);
//        JsonObject updates = new JsonObject();
//        message.add("updates", updates);
//
//        JsonObject expectedResponse = new JsonObject();
//        expectedResponse.addProperty("type", "userInfoUpdated");
//        expectedResponse.addProperty("success", false);
//        expectedResponse.addProperty("error", "Failed to update email");
//
//        // Act
//        webSocketMgr.handleUpdateUserInfo(mockSession, message);
//
//        // Assert
//        new Verifications() {{
//            mockRemote.sendString(expectedResponse.toString());
//        }};
//    }

//    @Test
//    public void testHandleUpdateUserInfo_DatabaseError() throws IOException {
//        // Arrange
//        JsonObject message = new JsonObject();
//        message.addProperty("type", "updateUserInfo");
//        message.addProperty("userId", 1);
//        JsonObject updates = new JsonObject();
//        updates.addProperty("email", "new@example.com");
//        message.add("updates", updates);
//
//        JsonObject handlerResponse = new JsonObject();
//        handlerResponse.addProperty("type", "userInfoUpdated");
//        handlerResponse.addProperty("success", false);
//        handlerResponse.addProperty("error", "Database error occurred");
//
//        new Expectations() {{
//            mockUserInfoHandler.updateUserInfo(1, "new@example.com", (JsonObject) any);
//            result = handlerResponse;
//        }};
//
//        // Act
//        webSocketMgr.handleUpdateUserInfo(mockSession, message);
//
//        // Assert
//        new Verifications() {{
//            mockRemote.sendString(handlerResponse.toString());
//        }};
//    }
}
