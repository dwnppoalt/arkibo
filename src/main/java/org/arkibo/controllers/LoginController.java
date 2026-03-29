package org.arkibo.controllers;

import java.util.concurrent.TimeoutException;

import org.arkibo.app.auth.AuthService;
import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;
import org.arkibo.models.User.User;
import org.arkibo.router.Router;
import org.arkibo.utils.Logger;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

public class LoginController implements StatefulController {

    private AuthService authService = new AuthService();
    private AppState appState;

    @FXML
    private VBox login;

    @FXML
    private Button googleLogin;

    @FXML
    private Button guestLogin;

    @Override
    public void setAppState(AppState appState) {
        this.appState = appState;
    }

    @FXML
    public void initialize() {
        Logger.log("LOGIN-CONTROLLER", "initialize called");
        login.getStylesheets().add(
                getClass().getResource("/css/login.css").toExternalForm());

    }

    @FXML
    private void googleButtonOnClick() {
        Logger.log("LOGIN-CONTROLLER", "Logging in with Google...");

        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                return authService.login();
            }
        };

        loginTask.setOnSucceeded(event -> {
            User user = loginTask.getValue();
            appState.getUserSession().set(user);
            Router.goTo("views/home.fxml");
        });

        loginTask.setOnFailed(event -> {
            Throwable ex = loginTask.getException();
            if (ex instanceof TimeoutException) {
                Logger.log("LOGIN", "Login with Google timeout.");
            } else {
                ex.printStackTrace();
            }
        });

        new Thread(loginTask).start();
    }
    

    @FXML
    private void guestButtonOnClick() {
        Logger.log("LOGIN", "Logging in as guest...");
        User user = new User("-1", "Guest User", null, null, null);
        appState.getUserSession().set(user);
        Router.goTo("views/home.fxml");
    }
}
