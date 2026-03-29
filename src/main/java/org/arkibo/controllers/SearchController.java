package org.arkibo.controllers;

import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SearchController implements StatefulController{
    private AppState appState;

    @Override
    public void setAppState(AppState appState) {
        this.appState = appState;
        setLabel();
    }
    
    @FXML
    private Label searchLabel;

    private void setLabel() {
        searchLabel.setText("You searched for: " + appState.getSearchState().getSearchQuery());
    }
}
