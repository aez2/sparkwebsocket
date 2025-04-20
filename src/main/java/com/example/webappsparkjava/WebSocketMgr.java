package com.example.webappsparkjava;

import com.google.gson.*;
import manager.Manager;
import ViewModel2ManagerAdapter.*;
import manager.ProjectHandler;
import viewModel.viewModel;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.sql.*;
import java.net.URISyntaxException;
import java.util.*;
import manager.Manager.*;


@WebSocket
public class WebSocketMgr {

    private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private final Gson gson = new Gson();

    private static final Map<String, Session> userSessions = new HashMap<>();

    public static void registerUserSession(String username, Session session) {
        userSessions.put(username, session);
    }

    public static Session getSessionForUser(String username) {
        return userSessions.get(username);
    }

    public static Map<String, Session> getUserSessions() {
        return userSessions;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        sessions.add(session);
        System.out.println("Connected: " + session.getRemoteAddress().getAddress());
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("Received: " + message);

        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();

            identifyActionType(session, json);

        } catch (JsonSyntaxException e) {
            System.err.println("Invalid JSON received: " + message);
        }
    }

    private void identifyActionType(Session session, JsonObject json) {
        viewModel viewModel = new viewModel();
        Manager managerInstance = new Manager(viewModel);
        //ViewModel2ManagerAdapter viewModel2ManagerAdapterInstance = new ViewModel2ManagerAdapter();
        if (json.has("type") && json.get("type").getAsString().equals("login")) {
            //managerInstance.handleLogin(session, json);
            //viewModel2ManagerAdapterInstance.login(session, json);
            viewModel.handleLogin(session, json);
        }
        else if (json.has("type") && json.get("type").getAsString().equals("googleLogin")) {
            //managerInstance.handleNewTask(json, gson, sessions);
            //viewModel2ManagerAdapterInstance.createTask2Manager(json, gson, sessions);
            viewModel.handleGoogleLogin(session, json);
        }
        else if (json.has("type") && json.get("type").getAsString().equals("newTask")) {
            //managerInstance.handleNewTask(json, gson, sessions);
            //viewModel2ManagerAdapterInstance.createTask2Manager(json, gson, sessions);
            viewModel.handleCreateTask(json, gson, sessions);
        }
        else if (json.has("type") && json.get("type").getAsString().equals("updateTask")) {
            //managerInstance.handleUpdateTask(json, gson, sessions);
            //viewModel2ManagerAdapterInstance.updateTask2Manager(json, gson, sessions);
            viewModel.handleUpdateTask(json, gson, sessions);
        }
        else if (json.has("type") && json.get("type").getAsString().equals("deleteTask")) {
            viewModel.handleDeleteTask(json, gson, sessions);
        }
        else if (json.has("actionType") && json.get("actionType").getAsString().equals("createProject")) { // Create a new project
            //managerInstance.handleCreateProject(session, json);
            //viewModel2ManagerAdapterInstance.createProject2Manager(session, json);
            viewModel.handleCreateProject(session, json);
        }
        else if (json.has("type") && json.get("type").getAsString().equals("joinProject")) {
            //managerInstance.handleJoinProject(json, sessions);
            //viewModel2ManagerAdapterInstance.joinProject2Manager(json, sessions);
            viewModel.handleJoinProject(json, sessions);
        }
        else if (json.has("actionType") && json.get("actionType").getAsString().equals("getAllProjects")) { // Fetch all existing projects
            //managerInstance.handleGetAllProjects(session);
            //viewModel2ManagerAdapterInstance.getAllProjects2Manager(session);
            viewModel.handleGetAllProject(session);

        }
        else if (json.has("actionType") && json.get("actionType").getAsString().equals("deleteProject")) { // Delete a project
            //managerInstance.handleDeleteProject( json);
            //viewModel2ManagerAdapterInstance.deleteProject2Manager(json);
            viewModel.handleDeleteProject(json);
        }
        else if (json.has("content")) {
            managerInstance.handleBroadcastMessage(json, gson, sessions);
        }
    }

}
