package manager;

/**
 * Handles user information updates.
 */
public class UserInfoHandler {

    /**
     * Updates user information.
     * @param userID User ID.
     * @param newName New name for the user.
     * @param newEmail New email address.
     * @param authorizationActionObject Authorization details.
     * @return Updated user information object.
     */
    public Object updateUserInfo(int userID, String newName, String newEmail, Object authorizationActionObject) {
        return null;
    }

    /**
     * Updates user information in the database.
     * @param authorizationActionObject Object containing authorization and updated user details.
     */
    public void updateUserInfo2psql(Object authorizationActionObject) {
    }

}