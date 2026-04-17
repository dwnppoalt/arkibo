package org.arkibo.controllers;

import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;
import org.arkibo.repository.ThesisRepository;
import org.arkibo.repository.UserRepository;
import org.arkibo.router.Router;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

public class SidebarController implements StatefulController {
    private AppState appState;
    private AppController appController;

    @Override
    public void setAppState(AppState appState) {
        this.appState = appState;
        bindStates();
        populateFields();
    }

    @Override
    public void setUserRepository(UserRepository userRepository) {}

    @Override
    public void setThesisRepository(ThesisRepository thesisRepository) {}

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    private VBox sidebar;

    @FXML
    public void initialize() {
        sidebar.setAlignment(Pos.TOP_LEFT);
        sidebar.getStylesheets()
                .add(getClass().getResource("/css/sidebar.css").toExternalForm());        
    }

    public void bindStates() {
        this.sidebar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                closeSidebar();
            }
        });
    }

    public void populateFields() {
        if (sidebar == null || appState == null) {
            return;
        }

        sidebar.getChildren().removeIf(node -> node.getProperties().containsKey("dynamic-sidebar-item"));

        if (isRealUserSignedIn()) {
            Button profile = new Button("My Profile");
            Button library = new Button("My Library");
            Separator separator = new Separator();
            Button signOut = new Button("Sign out");

            profile.getProperties().put("dynamic-sidebar-item", true);
            library.getProperties().put("dynamic-sidebar-item", true);
            separator.getProperties().put("dynamic-sidebar-item", true);
            signOut.getProperties().put("dynamic-sidebar-item", true);

            profile.setMaxWidth(Double.MAX_VALUE);
            library.setMaxWidth(Double.MAX_VALUE);
            signOut.setMaxWidth(Double.MAX_VALUE);
            profile.setAlignment(Pos.CENTER_LEFT);
            library.setAlignment(Pos.CENTER_LEFT);
            signOut.setAlignment(Pos.CENTER_LEFT);

            VBox.setMargin(profile, new Insets(10, 0, 0, 0));
            VBox.setMargin(library, new Insets(0, 0, 0, 0));
            VBox.setMargin(separator, new Insets(10, 0, 0, 0));
            VBox.setMargin(signOut, new Insets(0, 0, 0, 0));

            profile.setOnAction(event -> goProfile());
            library.setOnAction(event -> goLibrary());
            signOut.setOnAction(event -> signOut());

            sidebar.getChildren().addAll(profile, library, separator, signOut);
            return;
        }

        Button signIn = new Button("Sign In");
        signIn.getProperties().put("dynamic-sidebar-item", true);
        signIn.setMaxWidth(Double.MAX_VALUE);
        signIn.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(signIn, new Insets(10, 0, 0, 0));
        signIn.setOnAction(event -> goSignIn());
        sidebar.getChildren().add(signIn);
    }

    private boolean isRealUserSignedIn() {
        if (!appState.getUserSession().isLoggedIn()) {
            return false;
        }

        return !"-1".equals(appState.getUserSession().get().id());
    }

    @FXML
    private void goProfile() {
        closeSidebar();
        Router.goTo("views/profile.fxml");
    }

    @FXML
    private void goLibrary() {
        closeSidebar();
        Router.goTo("views/library.fxml");
    }

    @FXML
    private void signOut() {
        if (appState != null) {
            appState.getUserSession().clear();
        }
        populateFields();
        closeSidebar();
        Router.goTo("views/login.fxml");
    }

    @FXML
    private void goSignIn() {
        closeSidebar();
        Router.goTo("views/login.fxml");
    }

    @FXML
    private void closeSidebar() {
        appController.setSidebarOpen(false);
    }
}