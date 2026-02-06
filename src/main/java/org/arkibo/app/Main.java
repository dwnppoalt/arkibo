package org.arkibo.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import org.arkibo.utils.Logger;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app.fxml"));
        Scene scene = new Scene(loader.load(), 800, 600);

        Font montserrat = Font.loadFont(getClass().getResourceAsStream("/fonts/Montserrat/Montserrat-VariableFont_wght.ttf"), 14);
        Font montserratItalic = Font.loadFont(getClass().getResourceAsStream("/fonts/Montserrat/Montserrat-Italic-VariableFont_wght.ttf"), 14);
        Font readexPro = Font.loadFont(getClass().getResourceAsStream("/fonts/Readex Pro/ReadexPro-VariableFont_HEXP.ttf"), 14);

        Logger.log("MAIN", montserrat.getFamily());
        Logger.log("MAIN", montserratItalic.getFamily());
        Logger.log("MAIN", readexPro.getFamily());

        stage.setTitle("JavaFX Router Test");
        stage.setScene(scene);
        stage.show();
        stage.setMaximized(true);
    }

    public static void main(String[] args) {
        launch();
    }
}
