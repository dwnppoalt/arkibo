package org.arkibo.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import org.arkibo.utils.FontLoader;
import org.arkibo.utils.Logger;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {

        primaryStage = stage;

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/app.fxml"));

        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 600);

        FontLoader.loadFonts("/fonts");

        // Enable F5 reload
        enableReload(scene);

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
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/app.fxml"));

            Parent newRoot = loader.load();
            primaryStage.getScene().setRoot(newRoot);
            Logger.log("MAIN", "UI Reloaded");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
