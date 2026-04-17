package org.arkibo.controllers;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;
import org.arkibo.models.ThesisModels.Thesis;
import org.arkibo.repository.ThesisRepository;
import org.arkibo.repository.UserRepository;
import org.arkibo.router.Router;
import org.arkibo.search.SearchService;
import org.arkibo.utils.CitationGenerator;
import org.arkibo.utils.CollegeNameMapper;
import org.arkibo.utils.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ThesisInfoController implements StatefulController {

    private AppState appState;
    private UserRepository userRepository;
    private ThesisRepository thesisRepository;
    private SearchService searchService;

    private String selectedCitationFormat;
    private Thesis selectedThesis;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Override
    public void setAppState(AppState appState) {
        this.appState = appState;
        bindStates();
        populateFields();
    }

    @Override
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.searchService = new SearchService(thesisRepository, userRepository);
    }

    @Override
    public void setThesisRepository(ThesisRepository thesisRepository) {
        this.thesisRepository = thesisRepository;
    }

    @FXML
    private StackPane thesisRoot;

    @FXML
    private Label titleLabel;

    @FXML
    private HBox contentHBox;

    @FXML
    private VBox abstractVBox;

    @FXML
    private VBox detailsVBox;

    @FXML
    private Label abstractTextLabel;

    @FXML
    private VBox authorVBox;

    @FXML
    private VBox publishedVBox;

    @FXML
    private VBox researchTypeVBox;

    @FXML
    private VBox keywordsVBox;

    @FXML
    private VBox collegeVBox;

    @FXML
    private Button readButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button citeButton;

    @FXML
    private Pane citationPane;

    @FXML
    // private VBox citationVBox;
    private StackPane citationStackPane;

    @FXML
    private Button closeCitationButton;

    @FXML
    private TextArea citationTextArea;

    @FXML
    private ChoiceBox<String> citationChoiceBox;

    @FXML
    public void initialize() {
        thesisRoot.getStylesheets()
                .add(getClass().getResource("/css/thesis.css").toExternalForm());
        Logger.log("THESIS", "Initialized thesis.fxml");

        this.abstractVBox.prefWidthProperty().bind(contentHBox.widthProperty().multiply(2.0 / 3.0));
        this.detailsVBox.prefWidthProperty().bind(contentHBox.widthProperty().multiply(1.0 / 3.0));

        this.thesisRoot.setFocusTraversable(true);
        this.thesisRoot.requestFocus();

        this.thesisRoot.setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (citationStackPane.isVisible())
                    toggleCitations();
            }
        });

    }

    private void bindStates() {
        this.citationChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            this.selectedCitationFormat = newVal;

            String formatKey = mapCitation(this.selectedCitationFormat);
            String citation = CitationGenerator.generateCitation(this.selectedThesis, formatKey);

            this.citationTextArea.setText(citation);
        });

        this.citeButton.setOnAction((action) -> {
            toggleCitations();
        });

        this.selectedThesis = this.appState.getSearchState().getSelectedStudy();

        this.saveButton.setOnAction((action) -> {
            toggleSaved();
        });

        this.readButton.setOnAction((action) -> {
            Router.goTo("views/read.fxml");
        });

        this.closeCitationButton.setOnAction((action) -> {
            toggleCitations();
        });

    }

    public void populateFields() {
        String publishedYear = String.valueOf(this.selectedThesis.year());
        String college = CollegeNameMapper.mapName(this.selectedThesis.college());
        String researchType = this.selectedThesis.researchType().type();
        String title = this.selectedThesis.title();
        String abstractText = this.selectedThesis.abstractText();

        this.citationChoiceBox.setItems(
                FXCollections.observableArrayList("APA 7th Edition", "MLA 9th Edition", "Chicago 17th Edition",
                        "Harvard"));

        this.titleLabel.setText(title);

        this.abstractTextLabel.setText(abstractText);

        this.citationChoiceBox.getSelectionModel().selectFirst();

        this.selectedThesis.authors().forEach((author) -> {
            Label authorLabel = new Label(author.name());
            this.authorVBox.getChildren().add(authorLabel);
        });

        this.publishedVBox.getChildren().add(
                new Label(publishedYear));

        this.collegeVBox.getChildren().add(
                new Label(college));

        this.researchTypeVBox.getChildren().add(
                new Label(researchType));

        this.selectedThesis.keywords().forEach((keyword) -> {
            Label keywordLabel = new Label(keyword.word());
            this.keywordsVBox.getChildren().add(keywordLabel);
        });

        this.saveButton.setText(isThesisInSaved(this.selectedThesis.id()) ? "Saved" : "Save");


        if (this.appState.getUserSession().get().id() == "-1") {
            this.saveButton.setDisable(true);
            this.saveButton.setText("Login to save");
        }

    }

    private void toggleCitations() {
        Logger.log("THESIS", "Toggled citation panel, isVisible: " + this.citationStackPane.isVisible());
        this.citationStackPane.setVisible(!this.citationStackPane.isVisible());
        this.citationPane.setVisible(!this.citationPane.isVisible());
        this.citationPane.setMouseTransparent(true);
        citationPane.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
    }

    private void toggleSaved() {
        boolean isSaved = isThesisInSaved(this.selectedThesis.id());

        saveButton.setDisable(true);

        CompletableFuture
                .supplyAsync(() -> isSaved
                        ? this.searchService.removeThesisFromSaved(this.appState.getUserSession().get()
                                .id(), this.selectedThesis.id())
                        : this.searchService.addThesisToSaved(this.appState.getUserSession().get()
                                .id(), this.selectedThesis.id()),
                        executor)
                .thenAccept(response -> {
                    if (!response.ok()) {
                        throw new RuntimeException("Saving failed.");
                    }

                    Platform.runLater(() -> {
                        if (isSaved) {
                            this.appState.getUserSession().get().savedTheses()
                                    .removeIf(t -> t.id() == this.selectedThesis.id());
                            saveButton.setText("Save");
                        } else {
                            this.appState.getUserSession().get().savedTheses().add(this.selectedThesis);
                            saveButton.setText("Saved");
                        }
                        saveButton.setDisable(false);
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        saveButton.setDisable(false);
                    });
                    return null;
                });
    }

    // Helpers
    private String mapCitation(String label) {
        return switch (label) {
            case "APA 7th Edition" -> "apa";
            case "MLA 9th Edition" -> "mla";
            case "Chicago 17th Edition" -> "chicago";
            case "Harvard" -> "harvard";
            default -> "apa";
        };
    }

    private boolean isThesisInSaved(long thesisId) {
        return appState.getUserSession().get().savedTheses()
                .stream().anyMatch(t -> t.id() == thesisId);
    }
}
