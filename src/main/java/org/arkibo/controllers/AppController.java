package org.arkibo.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.effect.BoxBlur;

import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
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
    private Pane paneOverlay;

    @FXML
    private BorderPane mainContent;

    @FXML
    private VBox sidebar;

    @FXML
    private SidebarController sidebarController;

    private AppState appState;
    private boolean isSidebarOpen = false;
    private static final double MAX_BLUR = 5;
    private final BoxBlur blur = new BoxBlur(MAX_BLUR, MAX_BLUR, 2);

    public void initialize() {
        sidebar.setTranslateX(-260);
        sidebarController.setAppController(this);

        paneOverlay.setOnMouseClicked(e -> toggleSidebar());

        Router.init(content, appState);
        Router.goTo("views/home.fxml");
    }

    public void setAppState(AppState appState) {
        this.appState = appState;
        Router.init(content, appState);
    }

    @FXML
    private void goHome() {
        Router.goTo("views/home.fxml");
    }

    @FXML
    private void goSearch() {
        Router.goTo("views/search.fxml");
    }

    @FXML
    private void toggleSidebar() {
        setSidebarOpen(!isSidebarOpen);
    }

    public void setSidebarOpen(boolean open) {

        if (open == isSidebarOpen) return;
        isSidebarOpen = open;

        sidebar.setVisible(true);
        paneOverlay.setVisible(true);

        TranslateTransition slide = new TranslateTransition(Duration.millis(220), sidebar);
        slide.setToX(open ? 0 : -sidebar.getWidth());

        FadeTransition fade = new FadeTransition(Duration.millis(200), paneOverlay);
        fade.setFromValue(open ? 0 : 1);
        fade.setToValue(open ? 1 : 0);

        Timeline blurAnim = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(blur.widthProperty(), open ? MAX_BLUR : 0),
                        new KeyValue(blur.heightProperty(), open ? MAX_BLUR : 0)
                    )
                );

        if (open) {
            mainContent.setEffect(blur);
        }

        ParallelTransition animation = new ParallelTransition(slide, fade, blurAnim);

        animation.setOnFinished(e -> {
            if (!open) {
                sidebar.setVisible(false);
                paneOverlay.setVisible(false);
                mainContent.setEffect(null);
            }
        });

        animation.play();
    }

}
