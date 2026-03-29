package org.arkibo.controllers;

import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;
import org.arkibo.router.Router;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
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
    private TextField searchField;

    @FXML
    public void initialize() {
        root.getStylesheets().add(
            getClass().getResource("/css/home.css").toExternalForm()
        );
        searchField.setOnKeyPressed(event -> {
           if (event.getCode() == KeyCode.ENTER) searchButtonOnAction(); 
        });
    }

    @FXML
    private void searchButtonOnAction() {
        appState.getSearchState().setSearchQuery(searchField.getText());
        Router.goTo("views/search.fxml");
    }

}
