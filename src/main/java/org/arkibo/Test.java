package org.arkibo;

import org.arkibo.app.auth.AuthService;
import org.arkibo.models.User.User;

public class Test {
    public static void main(String[] args) {
        AuthService as = new AuthService();

        try {
            // System.out.println(as.exchangeCodeForIdToken("iawuhdaw", "oiawdhwad"));
            User user = as.login();
            System.out.println(user.id());
            System.out.println(user.email());
            System.out.println(user.name());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
