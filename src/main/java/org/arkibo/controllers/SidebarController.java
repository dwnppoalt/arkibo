package org.arkibo.controllers;

import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class SidebarController {

    @FXML
    private VBox sidebar;

    @FXML
    private Pane overlayPane;

    private AppController appController;

    @FXML
    public void initialize() {
        enableExitOnEscape(sidebar);
        sidebar.getStylesheets().add(
                getClass().getResource("/css/sidebar.css").toExternalForm());
        
    }

    public void setPane(Pane pane) {
        this.overlayPane = pane;
    }

    public void setAppController(AppController controller) {
        this.appController = controller;
    }

    private void enableExitOnEscape(VBox sidebar) {
        sidebar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                closeSidebar();
            }
        });
    }

    @FXML
    private void closeSidebar() {
        appController.setSidebarOpen(false);
    }

}
