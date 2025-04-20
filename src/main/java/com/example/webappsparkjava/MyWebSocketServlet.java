package com.example.webappsparkjava;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class MyWebSocketServlet extends WebSocketServlet {
    @Override
    public void configure(WebSocketServletFactory factory) {
        // Optionally set timeout policies
        factory.getPolicy().setIdleTimeout(1000000); // add a higher timeout if necessary
        // Register your WebSocket endpoint class
        factory.register(WebSocketMgr.class);
    }
}