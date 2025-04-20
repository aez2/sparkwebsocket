package ViewModel2ManagerAdapter;

import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import manager.Manager;
import manager.Manager.*;
import org.eclipse.jetty.websocket.api.Session;
import viewModel.*;

/**
 * Class defining the adapter pattern for interactions between ViewModel and Model layers.
 * This adapter encapsulates various operations involving user accounts, tasks, and projects,
 * enabling seamless communication between the ViewModel and Model.
 */

public class ViewModel2ManagerAdapter {
    Manager managerInstance;
    // constuctor
    public ViewModel2ManagerAdapter(viewModel vm){
        //this.vm = vm;
        this.managerInstance = new Manager(vm);

    }
        //Manager managerInstance = new Manager();
        /**
         * A null adapter instance.   Used to enable well-defined behavior of the ViewModel before
         * its operational adapter has been set.
         *//**
         * Creates a new user account with the provided details.
         *
         * @param username The username for the new account.
         * @param email The email address associated with the new account.
         * @param token The authentication token for account verification.
         * @return An instance of CreatedUser representing the newly created account.
         */
        public Object createAccount(String username, String email, String token) {
            return null;
        }

        /**
         * Logs in a user with the provided credentials.
         *
         * @param session load the session
         * @param json json object being passed with necessary information
         */
        public void login(Session session, JsonObject json) {
            this.managerInstance.handleLogin(session, json);
        }

        /**
         * Logs in a user with the provided Google credentials.
         *
         * @param session load the session
         * @param json json object being passed with necessary information
         */
        public void googleLogin(Session session, JsonObject json) {
            this.managerInstance.handleGoogleLogin(session, json);
        }

        /**
         * Creates a new task and maps it to the task manager.
         *
         * @param json json object being passed with necessary information
         * @param gson wrap the json object to braodcast the message
         * @param sessions broadcast the message the specified sessions
         */
        public void createTask2Manager(JsonObject json, Gson gson, Set<Session> sessions) {
            this.managerInstance.handleNewTask(json, gson, sessions);
        }

        /**
         * Updates a task and maps it to the task manager.
         *
         * @param json json object being passed with necessary information
         * @param gson wrap the json object to braodcast the message
         * @param sessions broadcast the message the specified sessions
         */
        public void updateTask2Manager(JsonObject json, Gson gson, Set<Session> sessions) {
            this.managerInstance.handleUpdateTask(json, gson, sessions);
        }
        /**
         * Deletes a task and maps it to the task manager.
         *
         * @param json json object being passed with necessary information
         * @param gson wrap the json object to braodcast the message
         * @param sessions broadcast the message the specified sessions
         */
        public void deleteTask2Manager(JsonObject json, Gson gson, Set<Session> sessions) {
            this.managerInstance.handleDeleteTask(json, gson, sessions);
        }
        /**
         * creates a new project for the logged-in user.
         *
         * @param session load the session
         * @param json json object being passed with necessary information
         */
        public void createProject2Manager(Session session, JsonObject json) {
            this.managerInstance.handleCreateProject(session, json);
        }
        /**
         * Join a project for the logged-in user.
         *
         * @param json json object being passed with necessary information
         * @param sessions broadcast the message the specified sessions
         */
        public void joinProject2Manager(JsonObject json, Set<Session> sessions) {
            this.managerInstance.handleJoinProject(json, sessions);
        }

        /**
         * gets all the projects for the logged-in user.
         *
         * @param session load the session
         */
        public void getAllProjects2Manager(Session session) {
            this.managerInstance.handleGetAllProjects(session);
        }

        /**
         * gets all the projects for the logged-in user.
         *
         * @param json object being passed with necessary information
         */
        public void deleteProject2Manager(JsonObject json) {
            this.managerInstance.handleDeleteProject(json);
        }

        /**
         * Deletes a specific task based on the provided user and task IDs.
         *
         * @param userId The ID of the user to whom the task belongs.
         * @param taskId The ID of the task to be deleted.
         * @return A boolean value indicating whether the task was successfully deleted.
         */
        public Object deleteTask(int userId, int taskId) {
            return null;
        }

        /**
         * Updates the personal information of a user.
         *
         * @param userId The ID of the user whose information needs to be updated.
         * @param newName The new name to be assigned to the user.
         * @param newEmail The new email address to be assigned to the user.
         * @return An instance of UserUpdate containing the updated user information.
         */
        public Object updateUserInfo(int userId, String newName, String newEmail) {
            return null;
        }



}