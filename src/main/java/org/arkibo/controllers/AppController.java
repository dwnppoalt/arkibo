package org.arkibo.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import org.arkibo.router.Router;

public class AppController {

    @FXML
    private StackPane content;

    public void initialize() {
        Router.init(content);
        Router.goTo("home.fxml");
    }

    @FXML
    private void goHome() {
        Router.goTo("home.fxml");
    }

    @FXML
    private void goSearch() {
        Router.goTo("search.fxml");
    }
}
