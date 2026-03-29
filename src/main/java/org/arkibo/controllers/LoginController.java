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
    private Button loginButton; // our fx:id for the button is "loginButton", so we need
                                // the variable to be named as "loginButton" :)

    @Override
    public void setAppState(AppState appState) {
        this.appState = appState;
    }

    @FXML
    public void initialize() {
        Logger.log("LOGIN-CONTROLLER", "initialize called");
        login.getStylesheets().add(
                getClass().getResource("/css/login.css").toExternalForm());
        
        loginButton.setOnMouseClicked(e -> Logger.log("LOGIN", "Mouse clicked"));

    }

    @FXML
    private void loginButtonOnClick() {
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
}
