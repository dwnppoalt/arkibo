package org.arkibo.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class HomeController {

    @FXML
    private VBox root;

    @FXML
    public void initialize() {
        root.getStylesheets().add(
            getClass().getResource("/css/home.css").toExternalForm()
        );
    }

}
