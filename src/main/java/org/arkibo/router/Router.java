package org.arkibo.router;

import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;
import org.arkibo.repository.ThesisRepository;
import org.arkibo.repository.UserRepository;
import org.arkibo.utils.Logger;

public class Router {
    private static StackPane content;
    private static AppState appState;
    private static String currentView;
    private static final ThesisRepository thesisRepository = new ThesisRepository();
    private static final UserRepository userRepository = new UserRepository();

    public static void init(StackPane root, AppState state) {
        content = root;
        appState = state;
    }

    public static void goTo(String fxml) {
        currentView = fxml;
        try {
            URL url = Router.class.getResource("/" + fxml);
            Logger.log("ROUTER", "URL Resource: " + url);
            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            Object controller = loader.getController();
                if (controller instanceof StatefulController) {
                    ((StatefulController) controller).setThesisRepository(thesisRepository);
                    ((StatefulController) controller).setUserRepository(userRepository);
                    ((StatefulController) controller).setAppState(appState);
                }
            
            content.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reload() {
        if (currentView != null) {
            goTo(currentView);
        }
    }
}
