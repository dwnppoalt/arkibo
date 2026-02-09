package org.arkibo.router;

import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import org.arkibo.utils.Logger;

public class Router {
    private static StackPane content;

    public static void init(StackPane root) {
        content = root;
    }

    public static void goTo(String fxml) {
        try {
            URL url = Router.class.getResource("/" + fxml);
            Logger.log("ROUTER", "URL Resource: " + url);
            Parent view = FXMLLoader.load(
                url
            );
            content.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
