package unitTests;

import com.example.webappsparkjava.DatabaseUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import manager.ProjectHandler;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProjectHandlerTest {

    private ProjectHandler handler;
    private final String testUser = "user";
    private final String testUser2 = "user2";

    @BeforeAll
    public void setup() {
        handler = new ProjectHandler();
    }

    // Clean up test data from the DB after each test run
    @AfterEach
    public void cleanUp() {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // First delete from projectmembers
            String deleteMembers = "DELETE FROM projectmembers WHERE project_name LIKE 'test_project_unit_%'";
            try (PreparedStatement stmt = conn.prepareStatement(deleteMembers)) {
                stmt.executeUpdate();
            }

            // Then delete from createdprojects
            String deleteProjects = "DELETE FROM createdprojects WHERE project_name LIKE 'test_project_unit_%'";
            try (PreparedStatement stmt = conn.prepareStatement(deleteProjects)) {
                stmt.executeUpdate();
            }

        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Test that a new project can be created successfully
    @Test
    public void testCreateProject() {
        JsonObject request = new JsonObject();
        JsonObject data = new JsonObject();
        String uniqueName = "test_project_unit_" + UUID.randomUUID(); // Unique name for each test run
        data.addProperty("title", uniqueName);
        data.addProperty("creatorId", 1);

        JsonArray users = new JsonArray();
        users.add(testUser);
        data.add("users", users);

        request.add("data", data);

        JsonObject response = handler.createProject(request);

        assertNotNull(response, "Response should not be null");
        assertEquals(uniqueName, response.get("name").getAsString());
        assertTrue(response.has("id"));
    }

    // Test that a user can successfully join a project
    @Test
    public void testJoinProject() {
        // First, create a project that can be joined
        String uniqueName = "test_project_unit_" + UUID.randomUUID();
        JsonObject requestCreate = new JsonObject();
        JsonObject data = new JsonObject();
        data.addProperty("title", uniqueName);
        data.addProperty("creatorId", 1);

        JsonArray users = new JsonArray();
        users.add(testUser);
        data.add("users", users);
        requestCreate.add("data", data);

        JsonObject created = handler.createProject(requestCreate);
        assertNotNull(created, "Project creation failed — cannot test joinProject");

        // Now test joining that project
        JsonObject joinRequest = new JsonObject();
        joinRequest.addProperty("project_name", uniqueName);
        joinRequest.addProperty("user_name", testUser2);

        JsonObject response = handler.joinProject(joinRequest);
        assertNotNull(response, "Join response should not be null");
        assertEquals(uniqueName, response.get("project_name").getAsString());
        assertTrue(response.has("id"));
    }

    // Test that deleting a project also removes it from the database
    @Test
    public void testDeleteProject() {
        // Create a temporary project to delete
        String uniqueName = "test_project_unit_" + UUID.randomUUID();
        JsonObject request = new JsonObject();
        JsonObject data = new JsonObject();
        data.addProperty("title", uniqueName);
        data.addProperty("creatorId", 1);

        JsonArray users = new JsonArray();
        users.add(testUser);
        data.add("users", users);
        request.add("data", data);

        JsonObject created = handler.createProject(request);
        assertNotNull(created);

        int idToDelete = created.get("id").getAsInt();

        // Delete it using the handler
        handler.deleteProject(idToDelete, uniqueName);

        // Validate it’s gone from createdprojects
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM createdprojects WHERE id = ?")) {
            stmt.setInt(1, idToDelete);
            var rs = stmt.executeQuery();
            assertFalse(rs.next(), "Project should be deleted");
        } catch (Exception e) {
            fail("DB error during validation: " + e.getMessage());
        }
    }
}
