package org.arkibo.router;

import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;
import org.arkibo.utils.Logger;

public class Router {
    private static StackPane content;
    private static AppState appState;

    public static void init(StackPane root, AppState state) {
        content = root;
        appState = state;
    }

    public static void goTo(String fxml) {
        try {
            URL url = Router.class.getResource("/" + fxml);
            Logger.log("ROUTER", "URL Resource: " + url);
            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof StatefulController) {
                ((StatefulController) controller).setAppState(appState);
            }
            
            content.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
