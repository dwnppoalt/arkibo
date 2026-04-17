package org.arkibo.controllers;

import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;
import org.arkibo.repository.ThesisRepository;
import org.arkibo.repository.UserRepository;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ProfileController implements StatefulController {
    private AppState appState;

    @Override
    public void setAppState(AppState appState) {
        this.appState = appState;
        populateFields();
    }

    @Override
    public void setUserRepository(UserRepository userRepository) {}

    @Override
    public void setThesisRepository(ThesisRepository thesisRepository) {}

    @FXML
    public VBox profileRoot;

    @FXML
    public Label profileLabel;

    @FXML
    public void initialize() {
        profileRoot.getStylesheets().add(getClass().getResource("/css/profile.css").toExternalForm());
    }

    private void populateFields() {
        profileLabel.setText("Hi, " + appState.getUserSession().get().name() + "!");
    }
}
