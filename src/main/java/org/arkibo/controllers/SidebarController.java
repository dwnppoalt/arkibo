package org.arkibo.controllers;

import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;
import org.arkibo.repository.ThesisRepository;
import org.arkibo.repository.UserRepository;
import org.arkibo.router.Router;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.javafx.FontIcon;

public class SidebarController implements StatefulController {
    private AppState appState;
    private AppController appController;
    private boolean collapsed;

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
    private Label overviewLabel;

    @FXML
    private Label accountLabel;

    @FXML
    private VBox userMeta;

    @FXML
    private Button collapseButton;

    @FXML
    private Button titleButton;

    @FXML
    private FontIcon collapseIcon;

    @FXML
    private Button homeButton;

    @FXML
    private Button searchButton;

    @FXML
    private Button libraryButton;

    @FXML
    private Button signInButton;

    @FXML
    private Button signOutButton;

    @FXML
    private Label userInitial;

    @FXML
    private ImageView userAvatar;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userEmailLabel;

    @FXML
    public void initialize() {
        sidebar.getStylesheets()
                .add(getClass().getResource("/css/sidebar.css").toExternalForm());

        userAvatar.setClip(new Circle(14, 14, 14));

        setCollapsed(false);
    }

    public void bindStates() {
        this.sidebar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (!collapsed) {
                    toggleSidebar();
                }
            }
        });
    }

    public void populateFields() {
        if (sidebar == null || appState == null) {
            return;
        }

        boolean signedIn = isRealUserSignedIn();

        signOutButton.setVisible(signedIn);
        signOutButton.setManaged(signedIn);

        signInButton.setVisible(!signedIn);
        signInButton.setManaged(!signedIn);

        if (signedIn) {
            String displayName = appState.getUserSession().get().name();
            String email = appState.getUserSession().get().email();
            String imageUrl = appState.getUserSession().get().imageUrl();

            if (displayName == null || displayName.isBlank()) {
                displayName = "User";
            }
            if (email == null || email.isBlank()) {
                email = "No email";
            }

            userNameLabel.setText(displayName);
            userEmailLabel.setText(email);
            userInitial.setText(displayName.substring(0, 1).toUpperCase());
            applyUserAvatar(imageUrl);
            return;
        }

        userNameLabel.setText("Guest");
        userEmailLabel.setText("Sign in to sync");
        userInitial.setText("G");
        applyUserAvatar(null);
    }

    private void applyUserAvatar(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            userAvatar.setImage(null);
            userAvatar.setVisible(false);
            userAvatar.setManaged(false);
            userInitial.setVisible(true);
            userInitial.setManaged(true);
            return;
        }

        try {
            Image profile = new Image(imageUrl, 28, 28, false, true, true);
            userAvatar.setImage(profile);
            userAvatar.setVisible(true);
            userAvatar.setManaged(true);
            userInitial.setVisible(false);
            userInitial.setManaged(false);
        } catch (Exception ignored) {
            userAvatar.setImage(null);
            userAvatar.setVisible(false);
            userAvatar.setManaged(false);
            userInitial.setVisible(true);
            userInitial.setManaged(true);
        }
    }

    private boolean isRealUserSignedIn() {
        if (!appState.getUserSession().isLoggedIn()) {
            return false;
        }

        return !"-1".equals(appState.getUserSession().get().id());
    }

    @FXML
    private void goHome() {
        Router.goTo("views/home.fxml");
    }

    @FXML
    private void goSearch() {
        Router.goTo("views/search.fxml");
    }

    @FXML
    private void goLibrary() {
        Router.goTo("views/library.fxml");
    }

    @FXML
    private void signOut() {
        if (appState != null) {
            appState.getUserSession().clear();
        }
        populateFields();
        Router.goTo("views/login.fxml");
    }

    @FXML
    private void goSignIn() {
        Router.goTo("views/login.fxml");
    }

    @FXML
    private void toggleSidebar() {
        appController.toggleSidebar();
    }

    @FXML
    private void openSidebar() {
        if (collapsed) {
            appController.setSidebarCollapsed(false);
        }
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;

        sidebar.getStyleClass().remove("collapsed");
        if (collapsed) {
            sidebar.getStyleClass().add("collapsed");
        } else {
            collapseIcon.setIconLiteral("fas-angle-left");
        }

        overviewLabel.setVisible(!collapsed);
        overviewLabel.setManaged(!collapsed);
        accountLabel.setVisible(!collapsed);
        accountLabel.setManaged(!collapsed);
        userMeta.setVisible(!collapsed);
        userMeta.setManaged(!collapsed);
        collapseButton.setVisible(!collapsed);
        collapseButton.setManaged(!collapsed);

        sidebar.applyCss();
        sidebar.requestLayout();
    }
}