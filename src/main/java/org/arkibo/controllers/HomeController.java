package org.arkibo.controllers;

import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class HomeController implements StatefulController {

    private AppState appState;

    @Override
    public void setAppState(AppState appState) {
        this.appState = appState;
    }

    @FXML
    private VBox root;

    @FXML
    public void initialize() {
        root.getStylesheets().add(
            getClass().getResource("/css/home.css").toExternalForm()
        );
    }

}
