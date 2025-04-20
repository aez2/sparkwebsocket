package com.example.webappsparkjava;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class WebSocketServerMain {

    public static void main(String[] args) throws Exception {
        // Create a Jetty server on port 8081
        //Server server = new Server(8081);
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8081"));
        Server server = new Server(port);

        // Set up the context handler
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Add CORS filter to allow requests from your front end
        FilterHolder cors = context.addFilter(CrossOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        cors.setInitParameter("allowedOrigins", "*"); // In production, consider restricting this to specific origins.
        cors.setInitParameter("allowedMethods", "GET,POST,PUT,DELETE,OPTIONS,HEAD");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("preflightMaxAge", "86400");
        cors.setInitParameter("allowCredentials", "true");

        server.setHandler(context);

        // Map the WebSocket servlet to the /ws endpoint
        ServletHolder wsHolder = new ServletHolder("ws-events", MyWebSocketServlet.class);
        context.addServlet(wsHolder, "/ws/*");

        // Add ping endpoint for connection checking
        context.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                resp.setContentType("text/plain");
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("pong");
            }
        }), "/ping");

        // Start the server
        server.start();
        System.out.println("WebSocket server started on port 8081 at /ws");
        server.join();
    }
}

