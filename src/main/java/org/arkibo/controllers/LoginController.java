package org.arkibo.controllers;

import org.arkibo.app.auth.AuthService;
import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;
import org.arkibo.models.User.User;
import org.arkibo.router.Router;
import org.arkibo.utils.Logger;

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
        
        googleLogin.setOnMouseClicked(e -> Logger.log("LOGIN", "Mouse clicked"));

    }

    @FXML
    private void googleButtonOnClick() {
        Logger.log("LOGIN-CONTROLLER", "Logging in...");
            new Thread(() -> {
                try {
                User user = authService.login();
                
                javafx.application.Platform.runLater(() -> {
                    appState.getUserSession().set(user);
                    Router.goTo("views/home.fxml");
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    

    @FXML
    private void guestButtonOnClick() {
        User user = new User("-1", "Guest User", null, null, null);
        appState.getUserSession().set(user);
        Router.goTo("views/home.fxml");
    }
}
