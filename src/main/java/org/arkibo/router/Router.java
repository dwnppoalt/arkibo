package org.arkibo.router;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
    private static long stylesheetVersion = 0;
    private static Runnable onNavigate;
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
            refreshStylesheets(view);

            Object controller = loader.getController();
                if (controller instanceof StatefulController) {
                    ((StatefulController) controller).setThesisRepository(thesisRepository);
                    ((StatefulController) controller).setUserRepository(userRepository);
                    ((StatefulController) controller).setAppState(appState);
                }
            
            content.getChildren().setAll(view);

            if (onNavigate != null) {
                onNavigate.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setOnNavigate(Runnable onNavigateCallback) {
        onNavigate = onNavigateCallback;
    }

    public static void reload() {
        if (currentView != null) {
            stylesheetVersion = System.currentTimeMillis();
            goTo(currentView);
        }
    }

    private static void refreshStylesheets(Parent root) {
        if (root == null) {
            return;
        }

        if (!root.getStylesheets().isEmpty()) {
            List<String> updated = new ArrayList<>();
            for (String stylesheet : root.getStylesheets()) {
                updated.add(withVersion(stylesheet));
            }
            root.getStylesheets().setAll(updated);
        }

        for (Node child : root.getChildrenUnmodifiable()) {
            if (child instanceof Parent childParent) {
                refreshStylesheets(childParent);
            }
        }
    }

    private static String withVersion(String stylesheet) {
        if (stylesheetVersion == 0) {
            return stylesheet;
        }

        String withoutVersion = stylesheet
                .replaceAll("([?&])v=\\d+(&|$)", "$1")
                .replaceAll("[?&]$", "");

        String separator = withoutVersion.contains("?") ? "&" : "?";
        return withoutVersion + separator + "v=" + stylesheetVersion;
    }
}
