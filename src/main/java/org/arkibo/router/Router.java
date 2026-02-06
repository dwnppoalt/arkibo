package org.arkibo.router;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
// FULL SCREEN;
public class Router {
    private static StackPane content;

    public static void init(StackPane root) {
        content = root;
    }

    public static void goTo(String fxml) {
        try {
            Parent view = FXMLLoader.load(
                    Router.class.getResource("/" + fxml)
            );
            content.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
