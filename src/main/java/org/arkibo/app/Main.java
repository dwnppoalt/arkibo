package org.arkibo.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.arkibo.app.state.AppState;
import org.arkibo.controllers.AppController;
import org.arkibo.router.Router;
import org.arkibo.utils.FontLoader;
import org.arkibo.utils.Logger;


public class Main extends Application {
    private AppState appState;

    @Override
    public void start(Stage stage) throws Exception {

        appState = new AppState();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/app.fxml"));

        Parent root = loader.load();
        AppController controller = loader.getController();
        controller.setAppState(appState);
        Scene scene = new Scene(root, 800, 600);

        FontLoader.loadFonts("/fonts");

        enableReload(scene);

        stage.initStyle(StageStyle.UNDECORATED);

        stage.setTitle("arkibo");
        stage.setScene(scene);
        stage.show();
        stage.setMaximized(true);
    }

    private void enableReload(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F5) {
                reload();
            }
        });
    }

    private void reload() {
        Router.reload();
        Logger.log("MAIN", "View Reloaded");
    }

    public static void main(String[] args) {
        launch();
    }
}
