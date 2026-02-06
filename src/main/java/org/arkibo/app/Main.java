package org.arkibo.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app.fxml"));
        Scene scene = new Scene(loader.load(), 800, 600);

        stage.setTitle("JavaFX Router Test");
        stage.setScene(scene);
        stage.show();
        stage.setMaximized(true);
    }

    public static void main(String[] args) {
        launch();
    }
}
