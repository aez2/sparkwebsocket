package manager;

import com.google.api.client.json.gson.GsonFactory;
import com.example.webappsparkjava.DatabaseUtil;
import com.example.webappsparkjava.WebSocketMgr;
import com.google.gson.*;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import Model2ViewModelAdapter.Model2ViewModelAdapter;
import viewModel.viewModel;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

public class Manager extends WebSocketMgr {
    Model2ViewModelAdapter model2ViewModelAdapter;
    private static final String CLIENT_ID = "277638213912-b8k1mlnnsga60m4cgau1ro1evejee3u7.apps.googleusercontent.com";
    public Manager(viewModel vm){
        this.model2ViewModelAdapter = new Model2ViewModelAdapter(vm);
    }
    //Model2ViewModelAdapter model2ViewModelAdapter = new Model2ViewModelAdapter();
    public void handleLogin(Session session, JsonObject json) {
        String username = json.get("username").getAsString();
        String password = json.get("password").getAsString();
        boolean success = false;
        int userId = -1;

        JsonArray tasksArray = new JsonArray();
        JsonArray projectMemberships = new JsonArray();

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Step 1: Authenticate user
            String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        success = true;
                        userId = rs.getInt("id");
                    }
                }
            }

            // Step 2: If successful, retrieve associated tasks
            if (success) {
                String taskSql =
                        "SELECT * FROM tasks " +
                                "WHERE deleted = FALSE and (assignee = ? " +
                                "OR project IN (" +
                                "  SELECT project_name FROM projectMembers WHERE member_username = ?" +
                                "))";
                try (PreparedStatement taskStmt = conn.prepareStatement(taskSql)) {
                    taskStmt.setString(1, username);
                    taskStmt.setString(2, username);
                    ResultSet rs = taskStmt.executeQuery();

                    while (rs.next()) {
                        JsonObject taskJson = new JsonObject();
                        taskJson.addProperty("id", rs.getInt("id"));
                        taskJson.addProperty("title", rs.getString("title"));
                        taskJson.addProperty("description", rs.getString("description"));
                        taskJson.addProperty("assignee", rs.getString("assignee"));
                        taskJson.addProperty("status", rs.getString("status"));
                        taskJson.addProperty("dueDate", rs.getTimestamp("due_date") != null
                                ? rs.getTimestamp("due_date").toLocalDateTime().toLocalDate().toString()
                                : "");
                        taskJson.addProperty("priority", rs.getString("priority"));
                        taskJson.addProperty("project", rs.getString("project"));
                        taskJson.addProperty("deleted", rs.getBoolean("deleted"));
                        tasksArray.add(taskJson);
                    }
                }
            }

            // Step 3: Retrieve project memberships
            if (success) {
                String projectSql = "SELECT project_id, project_name FROM projectMembers WHERE member_username = ?";
                try (PreparedStatement stmt = conn.prepareStatement(projectSql)) {
                    stmt.setString(1, username);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        JsonObject project = new JsonObject();
                        project.addProperty("id", rs.getInt("project_id"));
                        project.addProperty("name", rs.getString("project_name"));
                        projectMemberships.add(project);
                    }
                }
            }

        } catch (SQLException | URISyntaxException e) {
            e.printStackTrace();
        }


        // Step 3: Send login response and task data
        JsonObject response = new JsonObject();
        response.addProperty("type", "loginResponse");
        response.addProperty("success", success);
        response.addProperty("username", username);
        if (success) {
            response.addProperty("userId", userId);
            response.add("tasks", tasksArray); // add tasks
            response.add("projects", projectMemberships);  // include project memberships
            WebSocketMgr.registerUserSession(username, session);
        }

        //sendTo(session, response.toString());
        sendUpdatedInfo("sendTo", response.toString(), session, null);
    }
    public void handleGoogleLogin(Session session, JsonObject json) {
        String token = json.get("token").getAsString();
        boolean success = false;
        String email = null;
        String name = null;
        String picture = null;
        int userId = -1;

        JsonArray tasksArray = new JsonArray();
        JsonArray projectMemberships = new JsonArray();

        // Verify token
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance()) // updated here
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();
        try {
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null) {
                System.out.println("Google ID Token verified successfully.");
                GoogleIdToken.Payload payload = idToken.getPayload();
                email = payload.getEmail();
                picture = (String) payload.get("picture");
                System.out.println("Picture URL: " + picture);

                System.out.println("Token payload details: ");
                System.out.println("Email: " + email);

                try (Connection conn = DatabaseUtil.getConnection()) {
                    String sqlCheck = "SELECT id FROM users WHERE username = ?";
                    PreparedStatement checkStmt = conn.prepareStatement(sqlCheck);
                    checkStmt.setString(1, email);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        userId = rs.getInt("id");
                        System.out.println("Existing user found with userId: " + userId);
                    } else {
                        String sqlInsert = "INSERT INTO users (username, email, picture) VALUES (?, ?, ?) RETURNING id";
                        PreparedStatement insertStmt = conn.prepareStatement(sqlInsert);
                        insertStmt.setString(1, email);  // using email as username
                        insertStmt.setString(2, email);
                        insertStmt.setString(3, picture);
                        ResultSet insertRs = insertStmt.executeQuery();
                        if (insertRs.next()) {
                            userId = insertRs.getInt("id");
                            System.out.println("New user created with userId: " + userId);
                        }
                    }

                    success = true;
                    WebSocketMgr.registerUserSession(email, session);

                    String taskSql = "SELECT * FROM tasks WHERE assignee = ? OR project IN (SELECT project_name FROM projectMembers WHERE member_username = ?)";
                    PreparedStatement taskStmt = conn.prepareStatement(taskSql);
                    taskStmt.setString(1, email);
                    taskStmt.setString(2, email);
                    ResultSet taskRs = taskStmt.executeQuery();

                    while (taskRs.next()) {
                        JsonObject taskJson = new JsonObject();
                        taskJson.addProperty("id", taskRs.getInt("id"));
                        taskJson.addProperty("title", taskRs.getString("title"));
                        taskJson.addProperty("description", taskRs.getString("description"));
                        taskJson.addProperty("assignee", taskRs.getString("assignee"));
                        taskJson.addProperty("status", taskRs.getString("status"));
                        taskJson.addProperty("dueDate", taskRs.getTimestamp("due_date") != null
                                ? taskRs.getTimestamp("due_date").toLocalDateTime().toLocalDate().toString()
                                : "");
                        taskJson.addProperty("priority", taskRs.getString("priority"));
                        taskJson.addProperty("project", taskRs.getString("project"));
                        taskJson.addProperty("deleted", taskRs.getBoolean("deleted"));
                        tasksArray.add(taskJson);
                    }

                    String projectSql = "SELECT project_id, project_name FROM projectMembers WHERE member_username = ?";
                    PreparedStatement projectStmt = conn.prepareStatement(projectSql);
                    projectStmt.setString(1, email);
                    ResultSet projRs = projectStmt.executeQuery();

                    while (projRs.next()) {
                        JsonObject project = new JsonObject();
                        project.addProperty("id", projRs.getInt("project_id"));
                        project.addProperty("name", projRs.getString("project_name"));
                        projectMemberships.add(project);
                    }
                }

            } else {
                success = false;
                System.err.println("Invalid Google ID Token");
            }
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }


        // Send login response back to frontend
        JsonObject response = new JsonObject();
        response.addProperty("type", "loginResponse");
        response.addProperty("success", success);
        response.addProperty("username", email);
        if (success) {
            response.addProperty("userId", userId);
            response.add("tasks", tasksArray);
            response.add("projects", projectMemberships);
        } else {
            response.addProperty("error", "Google Authentication failed.");
        }

        //sendTo(session, response.toString());
        sendUpdatedInfo("sendTo", response.toString(), session, null);
    }
    public void handleBroadcastMessage(JsonObject json, Gson gson, Set<Session> sessions) {
        String content = json.get("content").getAsString();

        // Save to database
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "INSERT INTO messages (content, timestamp) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, content);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
            stmt.close();
        } catch (URISyntaxException | SQLException ex) {
            ex.printStackTrace();
        }

        //broadcast(gson.toJson(json), sessions);
        sendUpdatedInfo("broadcast", gson.toJson(json), null, sessions);
    }

    public void handleNewTask(JsonObject json, Gson gson, Set<Session> sessions) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "INSERT INTO tasks (title, description, assignee, status, due_date, priority, project, deleted) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, json.get("title").getAsString());
                stmt.setString(2, json.get("description").getAsString());
                stmt.setString(3, json.get("assignee").getAsString());
                stmt.setString(4, json.get("status").getAsString());

                String dueDateStr = json.has("dueDate") ? json.get("dueDate").getAsString() : null;
                Timestamp dueDate = (dueDateStr != null && !dueDateStr.isEmpty())
                        ? Timestamp.valueOf(dueDateStr + " 00:00:00")
                        : null;
                stmt.setTimestamp(5, dueDate);

                stmt.setString(6, json.has("priority") ? json.get("priority").getAsString() : "medium");
                stmt.setString(7, json.has("project") ? json.get("project").getAsString() : "None");
                stmt.setBoolean(8, false);  // Set deleted = false

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int generatedId = rs.getInt("id");

                    JsonObject taskJson = new JsonObject();
                    taskJson.addProperty("id", generatedId);
                    taskJson.addProperty("title", json.get("title").getAsString());
                    taskJson.addProperty("description", json.get("description").getAsString());
                    taskJson.addProperty("assignee", json.get("assignee").getAsString());
                    taskJson.addProperty("status", json.get("status").getAsString());
                    taskJson.addProperty("dueDate", json.get("dueDate").getAsString());
                    taskJson.addProperty("priority", json.get("priority").getAsString());
                    taskJson.addProperty("project", json.get("project").getAsString());
                    taskJson.addProperty("deleted", false);  // Include deleted field in broadcast

                    JsonObject wrapper = new JsonObject();
                    wrapper.addProperty("type", "taskCreated");
                    wrapper.add("task", taskJson);

                    //broadcast(gson.toJson(wrapper), sessions);
                    sendUpdatedInfo("broadcast", gson.toJson(wrapper), null, sessions);
                }
            }
        } catch (SQLException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void handleUpdateTask(JsonObject json, Gson gson, Set<Session> sessions) {
        int taskId = json.get("id").getAsInt();
        JsonObject updates = json.getAsJsonObject("updates");

        StringBuilder sqlBuilder = new StringBuilder("UPDATE tasks SET ");
        boolean first = true;

        for (String key : updates.keySet()) {
            if (key.equals("id")) continue; // Skip ID in SET clause
            if (!first) sqlBuilder.append(", ");
            String columnName = key.equals("dueDate") ? "due_date" : key;
            sqlBuilder.append(columnName).append(" = ?");
            first = false;
        }

        sqlBuilder.append(" WHERE id = ?");

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            int index = 1;
            for (String key : updates.keySet()) {
                if (key.equals("id")) continue;// Don't bind 'id' in SET
                JsonElement value = updates.get(key);
                switch (key) {
                    case "dueDate":
                        stmt.setTimestamp(index++, Timestamp.valueOf(value.getAsString() + " 00:00:00"));
                        break;
                    case "deleted":
                        stmt.setBoolean(index++, value.getAsBoolean());
                        break;
                    default:
                        stmt.setString(index++, value.getAsString());
                        break;
                }
            }

            stmt.setInt(index, taskId);
            stmt.executeUpdate();
            System.out.println("Task updated successfully: " + taskId);

            // Fetch and broadcast updated task
            String selectSql = "SELECT * FROM tasks WHERE id = ?";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, taskId);
                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    JsonObject updatedTask = new JsonObject();
                    updatedTask.addProperty("id", rs.getInt("id"));
                    updatedTask.addProperty("title", rs.getString("title"));
                    updatedTask.addProperty("description", rs.getString("description"));
                    updatedTask.addProperty("assignee", rs.getString("assignee"));
                    updatedTask.addProperty("status", rs.getString("status"));
                    updatedTask.addProperty("dueDate", rs.getTimestamp("due_date") != null
                            ? rs.getTimestamp("due_date").toLocalDateTime().toLocalDate().toString()
                            : "");
                    updatedTask.addProperty("priority", rs.getString("priority"));
                    updatedTask.addProperty("project", rs.getString("project"));
                    updatedTask.addProperty("deleted", rs.getBoolean("deleted"));

                    JsonObject wrapper = new JsonObject();
                    wrapper.addProperty("type", "taskUpdated");
                    wrapper.add("task", updatedTask);

                    //broadcast(gson.toJson(wrapper), sessions);
                    //sendUpdatedInfo("broadcast", gson.toJson(wrapper), null, sessions);
                    broadcastToAuthorizedUsers(updatedTask, gson.toJson(wrapper));
                }
            }

        } catch (SQLException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
    public void handleDeleteTask(JsonObject json, Gson gson, Set<Session> sessions) {
        int taskId = json.get("id").getAsInt();

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "UPDATE tasks SET deleted = true WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, taskId);
                int rowsUpdated = stmt.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("Task marked as deleted: " + taskId);

                    JsonObject wrapper = new JsonObject();
                    wrapper.addProperty("type", "taskDeleted");
                    wrapper.addProperty("id", taskId);
                    sendUpdatedInfo("broadcast", gson.toJson(wrapper), null, sessions);
                    //broadcast(gson.toJson(wrapper), sessions);
                } else {
                    System.err.println("No task found with ID: " + taskId);
                }
            }
        } catch (SQLException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
    /**
     * Handles the creation of a new project from the frontend.
     * Delegates the logic to ProjectHandler and sends a response to the client.
     */

    public void handleCreateProject(Session session, JsonObject json) {
        ProjectHandler handler = new ProjectHandler();
        JsonObject projectJson = handler.createProject(json);

        if (projectJson != null) {
            JsonObject response = new JsonObject();
            response.addProperty("type", "projectCreated");
            response.add("project", projectJson);
            //sendTo(session, response.toString());
            sendUpdatedInfo("sendTo", response.toString(), session, null);
        } else {
            System.err.println("Failed to create project");
        }
    }

    public void handleJoinProject(JsonObject json, Set<Session> sessions) {
        ProjectHandler handler = new ProjectHandler();
        JsonObject joinedProject = handler.joinProject(json);

        JsonObject response = new JsonObject();
        response.addProperty("type", "projectJoined");

        if (joinedProject != null) {
            response.add("project", joinedProject);
        } else {
            response.addProperty("error", "project_already_joined_or_invalid");
        }

        //broadcast(response.toString(), sessions);
        sendUpdatedInfo("broadcast", response.toString(), null, sessions);
    }

    /**
     * Handles the retrieval of all projects from the database.
     * Sends the list of projects back to the client.
     */
    public void handleGetAllProjects(Session session) {
        ProjectHandler handler = new ProjectHandler();
        JsonArray allProjects = handler.getAllProjects();

        JsonObject response = new JsonObject();
        response.addProperty("type", "allProjects");
        response.add("projects", allProjects);
        //sendTo(session, response.toString());
        sendUpdatedInfo("sendTo", response.toString(), session, null);
    }

    /**
     * Handles the deletion of a project from the database.
     * Extracts the project ID and delegates the deletion logic.
     */
    public void handleDeleteProject( JsonObject json) {
        int projectId = json.getAsJsonObject("data").get("projectId").getAsInt();
        String projectName = json.getAsJsonObject("data").get("title").getAsString();
        ProjectHandler handler = new ProjectHandler();
        handler.deleteProject(projectId, projectName);
    }

    private void sendUpdatedInfo(String type, String message, Session session, Set<Session> sessions) {
//        if (type.equalsIgnoreCase("broadcast")) {
//            model2ViewModelAdapter.broadcast(message, sessions);
//        } else if (type.equalsIgnoreCase("sendTo") && session != null) {
//            model2ViewModelAdapter.sendTo(session, message);
//        } else {
//            System.err.println("Invalid send type or missing session: " + type);
//        }
        this.model2ViewModelAdapter.sendAndReceiveUpdateInfo( type, message, session, sessions);
    }

    private void broadcastToAuthorizedUsers(JsonObject task, String message) {
        String assignee = task.get("assignee").getAsString();
        String project = task.get("project").getAsString();

        Set<String> recipients = new HashSet<>();
        if (assignee != null && !assignee.equals("None")) recipients.add(assignee);

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT member_username FROM projectMembers WHERE project_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, project);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    recipients.add(rs.getString("member_username"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String username : recipients) {
            Session targetSession = WebSocketMgr.getSessionForUser(username);
            if (targetSession != null && targetSession.isOpen()) {
                try {
                    targetSession.getRemote().sendString(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    private void broadcast(String message, Set<Session> sessions) {
//        synchronized (sessions) {
//            for (Session session : sessions) {
//                if (session.isOpen()) {
//                    try {
//                        session.getRemote().sendString(message);
//                    } catch (IOException e) {
//                        System.err.println("Broadcast error: " + e.getMessage());
//                    }
//                }
//            }
//        }
//    }
//
//    private void sendTo(Session session, String msg) {
//        try {
//            if (session.isOpen()) {
//                session.getRemote().sendString(msg);
//            }
//            System.out.println("Sending to client: " + msg);
//        } catch (IOException e) {
//            System.err.println("Send error: " + e.getMessage());
//        }
//    }
}
