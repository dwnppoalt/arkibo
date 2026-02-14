package org.arkibo.app.state;

import org.arkibo.models.User.User;

public class UserSession {
    private User currentUser;

    public User get() {
        return currentUser;
    }

    public void set(User user) {
        this.currentUser = user;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void clear() {
        currentUser = null;
    }
    
}
