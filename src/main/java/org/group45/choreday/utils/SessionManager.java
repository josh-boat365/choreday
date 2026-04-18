package org.group45.choreday.utils;

import org.group45.choreday.models.UserModel;

public class SessionManager {
    private static UserModel currentUser;

    public static void setCurrentUser(UserModel user) {
        currentUser = user;
    }

    public static UserModel getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }
}
