package org.arkibo.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

import org.arkibo.app.state.AppState;
import org.arkibo.router.Router;

public class AppController {

    @FXML
    private StackPane content;


    @FXML
    private BorderPane mainContent;

    @FXML
    private VBox sidebar;

    @FXML
    private SidebarController sidebarController;

    private boolean sidebarCollapsed;
    private static final double SIDEBAR_EXPANDED_WIDTH = 260;
    private static final double SIDEBAR_COLLAPSED_WIDTH = 84;

    public void initialize() {
        sidebarController.setAppController(this);
        setSidebarCollapsed(false, false);
    }

    public void setAppState(AppState appState) {
        sidebarController.setAppState(appState);
        Router.init(content, appState);
        Router.setOnNavigate(() -> sidebarController.populateFields());
        Router.goTo("views/login.fxml");
    }

    public void setThesisRepository(org.arkibo.repository.ThesisRepository thesisRepository) {
        sidebarController.setThesisRepository(thesisRepository);
    }

    public void setUserRepository(org.arkibo.repository.UserRepository userRepository) {
        sidebarController.setUserRepository(userRepository);
    }

    @FXML
    public void toggleSidebar() {
        setSidebarCollapsed(!sidebarCollapsed, true);
    }

    public void setSidebarCollapsed(boolean collapsed) {
        setSidebarCollapsed(collapsed, true);
    }

    private void setSidebarCollapsed(boolean collapsed, boolean animated) {
        if (collapsed == sidebarCollapsed && animated) {
            return;
        }

        sidebarCollapsed = collapsed;
        sidebarController.populateFields();
        sidebarController.setCollapsed(collapsed);

        double targetWidth = collapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_EXPANDED_WIDTH;
        if (!animated) {
            sidebar.setMinWidth(targetWidth);
            sidebar.setPrefWidth(targetWidth);
            sidebar.setMaxWidth(targetWidth);
            return;
        }

        Timeline widthAnimation = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(sidebar.minWidthProperty(), targetWidth),
                        new KeyValue(sidebar.prefWidthProperty(), targetWidth),
                        new KeyValue(sidebar.maxWidthProperty(), targetWidth)
                ));
        widthAnimation.play();
    }

}
