package org.arkibo.controllers;

import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;
import org.arkibo.repository.ThesisRepository;
import org.arkibo.repository.UserRepository;
import org.arkibo.utils.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ThesisInfoController implements StatefulController {
    
    private AppState appState;
    private UserRepository userRepository;
    private ThesisRepository thesisRepository;

    @Override
    public void setAppState(AppState appState) {
        this.appState = appState;
        changeLabel();
    }

    @Override
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void setThesisRepository(ThesisRepository thesisRepository) {
        this.thesisRepository = thesisRepository;
    }

    @FXML
    private Label thesisId;

    @FXML
    private Label thesisAbstract;

    @FXML
    private Label thesisTitle;

    @FXML
    private VBox thesisRoot;

    @FXML
    public void initialize() {
        thesisRoot.getStylesheets()
                .add(getClass().getResource("/css/thesis.css").toExternalForm());
        Logger.log("THESIS", "Initialized thesis.fxml");
    }

    public void changeLabel() {
        thesisTitle.setText("Thesis title: " + this.appState.getSearchState().getSelectedStudy().title());
        thesisAbstract.setText("Thesis abstract: " + this.appState.getSearchState().getSelectedStudy().abstractText());
        thesisId.setText("Thesis ID: " + this.appState.getSearchState().getSelectedStudy().id());
    }
}
