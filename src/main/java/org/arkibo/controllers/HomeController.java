package org.arkibo.controllers;

import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;
import org.arkibo.models.User.User;
import org.arkibo.utils.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HomeController implements StatefulController {

    private AppState appState;

    @Override
    public void setAppState(AppState appState) {
        this.appState = appState;
        updateUserInfo();
    }

    @FXML
    private VBox root;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userEmailLabel;

    @FXML
    public void initialize() {
        root.getStylesheets().add(
            getClass().getResource("/css/home.css").toExternalForm()
        );
    }

    private void updateUserInfo() {
        if (appState != null && appState.getUserSession().isLoggedIn()) {
            User user = appState.getUserSession().get();
            userNameLabel.setText("loe po, " + user.name());
            userEmailLabel.setText("email: " + user.email());
            Logger.log("HOME", "image url: " + user.imageUrl());
        } else {
            userNameLabel.setText("Not logged in");
            userEmailLabel.setText("");
        }
    }

}
