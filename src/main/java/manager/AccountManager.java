package manager;
/**
 * Handles account creation, login, and authentication.
 */
public class AccountManager {

    /**
     * Creates a new user account.
     * @param username User's username.
     * @param email User's email address.
     * @param token Authentication token.
     * @return Object representing the newly created account.
     */
    public Object createAccount(String username, String email, String token) {
        return null;
    }

    /**
     * Logs a user in.
     * @param username User's username.
     * @param email User's email.
     * @return Object representing login details.
     */
    public Object login(String username, String email) {
        return null;
    }

    /**
     * Authenticates a user's token.
     * @param token User's token.
     * @return true if authentication is successful, false otherwise.
     */
    public boolean authenticate(String token) {
        return false;
    }

    /**
     * Stores a new account in the database.
     * @param createdUser User details.
     */
    public void createAccount2psql(Object createdUser) {
    }

    /**
     * Authenticates user credentials against database records.
     * @param login Login details.
     */
    public void authenticate2psql(Object login) {
    }
}