package DatabaseAbstraction;
/**
 * DatabaseAbstraction is a class that provides an abstraction layer for database operations.
 */
public class DatabaseAbstraction {

    // Constructor
    DatabaseAbstraction() {}

    /**
     * Creates a new account in the database.
     * @param username The username for the new account.
     * @param password The password for the new account.
     * @return A new account object.
     */
    public Object createAccountQuery(String username, String googleToken) {

        return null;
    }

    /**
     * Authenticates a user in the database.
     * @param username The username of the account to authenticate.
     * @param password The password of the account to authenticate.
     * @return An authenticated user object.
     */
    public Object authenticateQuery(Object loginObj) {
        return null;
    }

    /**
     * Creates a new task in the database.
     * @param taskObj The task object to create.
     * @return taskObj new task object that was created.
     */
    public Object createTaskQuery(Object taskObj) {
        return null;
    }

    /**
     * Updates a task in the database.
     * @param taskId The task id from the database.
     * @return boolean if query was successful.
     */
    public boolean deleteTaskQuery(String taskId) {
        return true;
    }

    /**
     * Updates a task in the database.
     * @param taskObj The task object to update.
     * @return boolean if query was successful.
     */
    public boolean updateTaskQuery(Object taskObj) {
        return true;
    }

    /**
     * Creates a new project in the database.
     * @param projectObj The project object to create.
     * @return projectObj that was created.
     */
    public Object createProjectQuery(Object projectObj) {
        return null;
    }

    /**
     * Updates a project in the database.
     * @param projectObj The project object to update.
     * @return boolean if query was successful.
     */
    public boolean updateProjectQuery(Object projectObj) {
        return true;
    }

    /**
     * Creates a user profile in the database.
     * @param userObject The user object to create.
     * @return userObj that was created.
     */
    public Object createUserProfileQuery (Object userObject) {
        return null;
    }

    /**
     * Updates a user profile in the database.
     * @param userObject The user object to update.
     * @return boolean if query was successful.
     */
    public Object updateUserProfileInfoQuery(Object userObject) {
        return null;
    }
}