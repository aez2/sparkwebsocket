package viewModel;

import Model2ViewModelAdapter.Model2ViewModelAdapter;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ViewModel2ManagerAdapter.ViewModel2ManagerAdapter;
import org.eclipse.jetty.websocket.api.Session;

/**
 * The ViewModel of the MVVM architecture
 */
public class viewModel {
    ViewModel2ManagerAdapter viewModel2ManagerAdapterInstance;
    //Model2ViewModelAdapter.Model2ViewModelAdapter model2ViewModelAdapterInstance;

    public viewModel(){
        this.viewModel2ManagerAdapterInstance = new ViewModel2ManagerAdapter(this);
        //this.model2ViewModelAdapterInstance = new Model2ViewModelAdapter.Model2ViewModelAdapter(this); // <-- passing 'this'
    }
    /**
     * Web socket manager for the MVVM architecture
     */
    public String identifyActionType(Object actionObject){
      return "actionType";//actionType
    };
    /**
     * Message parser for the MVVM architecture
     */
    public Object parseIncomingMessage(String rawMessage){
      return Object.class;//parsedMessage
    };
    /**
     * Create Account handler for the MVVM architecture
     */
    public Object handleCreateAccount(String username, String email, String token){
        return Object.class;
    };
    /**
     * login handler for the MVVM architecture
     */
    public void handleLogin(Session session, JsonObject json){
        this.viewModel2ManagerAdapterInstance.login(session, json);
    };
    /**
     * Google login handler for the MVVM architecture
     */
    public void handleGoogleLogin(Session session, JsonObject json){
        this.viewModel2ManagerAdapterInstance.googleLogin(session, json);
    };
    /**
     * Create Tasks handler for the MVVM architecture
     */
    public void handleCreateTask(JsonObject json, Gson gson, Set<Session> sessions){
        this.viewModel2ManagerAdapterInstance.createTask2Manager(json, gson, sessions);
    }
    /**
     * handles updated tasks for the MVVM architecture
     */
    public void handleUpdateTask(JsonObject json, Gson gson, Set<Session> sessions){
        this.viewModel2ManagerAdapterInstance.updateTask2Manager(json, gson, sessions);
    };
    /**
     * handles deleted tasks for the MVVM architecture
     */
    public void handleDeleteTask(JsonObject json, Gson gson, Set<Session> sessions){
        this.viewModel2ManagerAdapterInstance.deleteTask2Manager(json, gson, sessions);
    };
    /**
     * handles created tasks for the MVVM architecture
     */
    public void handleCreateProject(Session session, JsonObject json) {
        this.viewModel2ManagerAdapterInstance.createProject2Manager(session, json);
    }
    /**
     * handles joining a project for the MVVM architecture
     */
    public void handleJoinProject(JsonObject json, Set<Session> sessions){
        this.viewModel2ManagerAdapterInstance.joinProject2Manager(json, sessions);
    };
    /**
     * handles getting all projects for the MVVM architecture
     */
    public void handleGetAllProject(Session session){
        this.viewModel2ManagerAdapterInstance.getAllProjects2Manager(session);
    };
    /**
     * handles deleted tasks for the MVVM architecture
     */
    public void handleDeleteProject(JsonObject json){
        this.viewModel2ManagerAdapterInstance.deleteProject2Manager(json);
    };

    public void broadcast(String message, Set<Session> sessions) {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getRemote().sendString(message);
                    } catch (IOException e) {
                        System.err.println("Broadcast error: " + e.getMessage());
                    }
                }
            }
        }
    }

    public void sendTo(Session session, String msg) {
        try {
            if (session.isOpen()) {
                session.getRemote().sendString(msg);
            }
            System.out.println("Sending to client: " + msg);
        } catch (IOException e) {
            System.err.println("Send error: " + e.getMessage());
        }
    }
    /**
     * Filter Tasks handler for the MVVM architecture
     */
    public List<Object> handleFilterTasks(String filter){
        return new ArrayList<>();//Filtered Tasks
    }
    /**
     * Filter Tasks by user handler for the MVVM architecture
     */
    public List<Object> handleUserTasks(String filter){
        return new ArrayList<>();//Filtered Tasks
    }
    /**
     * handles deleted tasks for the MVVM architecture
     */
    public Boolean handleDeleteTask(int taskId){
        return true;// Task Deleted
    };
    /**
     * handles created tasks for the MVVM architecture
     */
    public Object handleCreateProject(String projectTitle, List<String> users, Object authObj) {
        return Object.class; // createdProject
    }
    /**
     * handles updates to projects for the MVVM architecture
     */
    public Object handleUpdateProject(int projectId, String field, String newValue){
      return Object.class;//updatedProject
    };
    /**
     * handles updates to a given user for the MVVM architecture
     */
    public Object handleUpdateUser(String userId, String field, String value){
      return Object.class;
    };
    /**
     * handles Broadcasting the desired message for the MVVM architecture
     */
    public Object handleMessageBroadcast(String messageType, Object messageInformation){
      return Object.class;
    };


}
