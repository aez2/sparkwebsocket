package manager;

import com.example.webappsparkjava.DatabaseUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles project-related operations.
 */
public class ProjectHandler {

    /**
     * Creates a new project in the database and links it to member users.
     *
     * @param json JSON object containing project title and list of usernames
     * @return JSON object representing the newly created project (id and name), or null on failure
     */
    public JsonObject createProject(JsonObject json) {
        JsonObject data = json.getAsJsonObject("data");

        String title = data.get("title").getAsString();
        JsonArray usersArray = data.getAsJsonArray("users");

        // ✅ Fallback for creatorId in case it's missing
        int creatorId = 1; // default fallback
        if (data.has("creatorId") && !data.get("creatorId").isJsonNull()) {
            creatorId = data.get("creatorId").getAsInt();
        }

        String creatorName = usersArray.size() > 0 ? usersArray.get(0).getAsString() : "unknown";

        List<String> memberUsernames = new ArrayList<>();
        for (JsonElement user : usersArray) {
            memberUsernames.add(user.getAsString());
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false); // transaction block

            // 1. Insert into createdprojects table
            int projectId;
            String insertProjectSQL = "INSERT INTO createdprojects (project_name, creator_id, creator_name) VALUES (?, ?, ?) RETURNING id";
            try (PreparedStatement stmt = conn.prepareStatement(insertProjectSQL)) {
                stmt.setString(1, title);
                stmt.setInt(2, creatorId);
                stmt.setString(3, creatorName);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                projectId = rs.getInt("id");
            }

            // 2. Insert members into project_members table
            String insertMemberSQL = "INSERT INTO projectmembers (project_id, project_name, member_username) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertMemberSQL)) {
                for (String username : memberUsernames) {
                    stmt.setInt(1, projectId);
                    stmt.setString(2, title);
                    stmt.setString(3, username);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit();

            // 3. Build and return response
            JsonObject projectJson = new JsonObject();
            projectJson.addProperty("id", projectId);
            projectJson.addProperty("name", title);
            return projectJson;

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject response = new JsonObject();
            response.addProperty("type", "projectCreated");
            response.addProperty("error", "existing_project");
            return response;
        }
    }


    /**
     * Retrieves all existing projects from the database.
     *
     * @return A JsonArray of project objects (id and name)
     */
    public JsonArray getAllProjects() {
        // this needs to be updated
        JsonArray projectArray = new JsonArray();
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT id, project_name FROM createdprojects";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JsonObject proj = new JsonObject();
                proj.addProperty("id", rs.getInt("id"));
                proj.addProperty("name", rs.getString("project_name"));
                projectArray.add(proj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return projectArray;
    }

    /**
     * Deletes a project and all associated project members from the database.
     *
     * @param projectId ID of the project to delete
     */
    public void deleteProject(int projectId, String projectName) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // 1. Delete tasks related to the project
            String deleteTasksSQL = "DELETE FROM tasks WHERE project = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteTasksSQL)) {
                stmt.setString(1, projectName);
                stmt.executeUpdate();
            }

            // 2. Delete related rows from projectmembers first
            String deleteMembersSQL = "DELETE FROM projectmembers WHERE project_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteMembersSQL)) {
                stmt.setInt(1, projectId);
                stmt.executeUpdate();
            }

            // 3. Then delete the project from createdprojects
            String deleteProjectSQL = "DELETE FROM createdprojects WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteProjectSQL)) {
                stmt.setInt(1, projectId);
                stmt.executeUpdate();
            }

            System.out.println("Project " + projectName + " deleted successfully");
        } catch (Exception e) {
            System.err.println("Failed to delete project:");
            e.printStackTrace();
        }
    }

    public JsonObject joinProject(JsonObject json) {
        String projectName = json.get("project_name").getAsString();
        String userName = json.get("user_name").getAsString();

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Get user ID
            int userId;
            String userSql = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setString(1, userName);
                try (ResultSet rs = userStmt.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("id");
                    } else {
                        System.err.println("User not found: " + userName);
                        return null;
                    }
                }
            }

            // Get project ID
            int projectId;
            String projectSql = "SELECT id FROM createdProjects WHERE project_name = ?";
            try (PreparedStatement projectStmt = conn.prepareStatement(projectSql)) {
                projectStmt.setString(1, projectName);
                try (ResultSet rs = projectStmt.executeQuery()) {
                    if (rs.next()) {
                        projectId = rs.getInt("id");
                    } else {
                        System.err.println("Project not found: " + projectName);
                        return null;
                    }
                }
            }

            // Insert into projectMembers
            String insertSql = "INSERT INTO projectMembers (project_id, project_name, member_username) VALUES (?, ?, ?) RETURNING TRUE";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, projectId);
                stmt.setString(2, projectName);
                stmt.setString(3, userName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    JsonObject project = new JsonObject();
                    project.addProperty("id", projectId);
                    project.addProperty("project_name", projectName);
                    project.addProperty("user_id", userId);
                    project.addProperty("user_name", userName);
                    return project;
                }
            }

        } catch (SQLException | URISyntaxException e) {
            e.printStackTrace();
            JsonObject response = new JsonObject();
            response.addProperty("type", "projectJoined");
            response.addProperty("error", "project_already_joined");
            return response;
        }

        return null;
    }
}

/**
 * Handles user information updates.
 */
//class UserInfoHandler {
//
//    /**
//     * Updates user information.
//     * @param userID User ID.
//     * @param newName New name for the user.
//     * @param newEmail New email address.
//     * @param authorizationActionObject Authorization details.
//     * @return Updated user information object.
//     */
//    public Object updateUserInfo(int userID, String newName, String newEmail, Object authorizationActionObject) {
//        return null;
//    }
//
//    /**
//     * Updates user information in the database.
//     * @param authorizationActionObject Object containing authorization and updated user details.
//     */
//    public void updateUserInfo2psql(Object authorizationActionObject) {
//    }
//}