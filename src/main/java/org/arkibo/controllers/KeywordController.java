package org.arkibo.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;
import org.arkibo.models.ThesisModels.Author;
import org.arkibo.models.ThesisModels.Keyword;
import org.arkibo.models.ThesisModels.Thesis;
import org.arkibo.repository.ThesisRepository;
import org.arkibo.repository.UserRepository;
import org.arkibo.router.Router;
import org.arkibo.search.BM25;
import org.arkibo.utils.CollegeNameMapper;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class KeywordController implements StatefulController {
    private AppState appState;
    private ThesisRepository thesisRepository;

    private List<Thesis> keywordTheses = Collections.emptyList();
    private boolean isLoading = false;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @FXML
    private BorderPane keywordRoot;

    @FXML
    private Label headingLabel;

    @FXML
    private Label resultsLabel;

    @FXML
    private TextField searchBarField;

    @FXML
    private ScrollPane keywordScrollPane;

    @FXML
    private VBox keywordVBox;

    @Override
    public void setAppState(AppState appState) {
        this.appState = appState;
        loadKeywordTheses();
    }

    @Override
    public void setUserRepository(UserRepository userRepository) {}

    @Override
    public void setThesisRepository(ThesisRepository thesisRepository) {
        this.thesisRepository = thesisRepository;
    }

    @FXML
    public void initialize() {
        keywordRoot.getStylesheets().add(
                getClass().getResource("/css/author.css").toExternalForm());

        keywordVBox.getStyleClass().add("results-list");
        searchBarField.textProperty().addListener((obs, oldText, newText) -> searchKeywordTheses());
    }

    private void loadKeywordTheses() {
        keywordVBox.getChildren().clear();
        keywordTheses = Collections.emptyList();

        Long keywordId = appState.getSearchState().getSelectedKeywordId();
        String keywordName = appState.getSearchState().getSelectedKeywordName();

        if (keywordName == null || keywordName.isBlank()) {
            headingLabel.setText("Keyword Theses");
        } else {
            headingLabel.setText("Theses with " + keywordName);
        }

        if (keywordId == null) {
            resultsLabel.setText("No keyword selected");
            showEmptyMessage("No keyword selected. Open a thesis and click a keyword.");
            return;
        }

        isLoading = true;
        searchBarField.setDisable(true);
        resultsLabel.setText("Loading theses...");
        showEmptyMessage("Loading theses...");

        CompletableFuture
                .supplyAsync(() -> thesisRepository.keywordInfo(keywordId), executor)
                .thenAccept(response -> Platform.runLater(() -> {
                    isLoading = false;
                    searchBarField.setDisable(false);
                    keywordVBox.getChildren().clear();

                    if (!response.ok() || response.data() == null) {
                        resultsLabel.setText("Unable to load theses");
                        showEmptyMessage("Unable to load keyword theses right now.");
                        return;
                    }

                    if (response.data().isEmpty()) {
                        resultsLabel.setText("No theses found");
                        showEmptyMessage("No theses found for this keyword.");
                        return;
                    }

                    keywordTheses = new ArrayList<>(response.data());
                    renderTheses(keywordTheses);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        isLoading = false;
                        searchBarField.setDisable(false);
                        keywordVBox.getChildren().clear();
                        resultsLabel.setText("Unable to load theses");
                        showEmptyMessage("Unable to load keyword theses right now.");
                    });
                    return null;
                });
    }

    @FXML
    private void searchKeywordTheses() {
        if (isLoading) {
            return;
        }

        if (keywordTheses.isEmpty()) {
            return;
        }

        String query = searchBarField.getText();
        if (query == null || query.isBlank()) {
            renderTheses(keywordTheses);
            return;
        }

        renderTheses(rankWithPartialMatch(query));
    }

    private List<Thesis> rankWithPartialMatch(String query) {
        String normalizedQuery = query.toLowerCase().trim();

        List<Thesis> partialMatches = keywordTheses.stream()
                .filter(thesis -> toSearchableText(thesis).toLowerCase().contains(normalizedQuery))
                .collect(Collectors.toList());

        if (!partialMatches.isEmpty()) {
            BM25<Thesis> bm25 = new BM25<>(partialMatches, this::toSearchableText);
            List<Thesis> rankedPartial = bm25.rank(query);
            if (rankedPartial.isEmpty()) {
                return partialMatches;
            }
            return rankedPartial;
        }

        BM25<Thesis> bm25 = new BM25<>(keywordTheses, this::toSearchableText);
        return bm25.rank(query);
    }

    private void renderTheses(List<Thesis> theses) {
        keywordVBox.getChildren().clear();

        if (theses == null || theses.isEmpty()) {
            resultsLabel.setText("No matching theses");
            showEmptyMessage("No thesis matched your search.");
            return;
        }

        resultsLabel.setText(theses.size() + " theses found");
        theses.forEach(thesis -> keywordVBox.getChildren().add(buildThesisCard(thesis)));
    }

    private VBox buildThesisCard(Thesis thesis) {
        VBox card = new VBox(8);
        card.getStyleClass().add("thesis-card");

        Label title = new Label(thesis.title());
        title.getStyleClass().add("thesis-title");

        HBox authorsRow = new HBox(5);
        authorsRow.getStyleClass().add("thesis-authors");
        thesis.authors().forEach(author -> authorsRow.getChildren().add(new Label(author.name())));

        Label yearLabel = new Label("Published in " + thesis.year());
        yearLabel.getStyleClass().add("thesis-year");

        Label collegeLabel = new Label(CollegeNameMapper.mapName(thesis.college()));
        String preview = thesis.abstractText() == null ? "" : thesis.abstractText();
        if (preview.length() > 120) {
            preview = preview.substring(0, 120) + "...";
        }

        Label abstractLabel = new Label(preview);
        abstractLabel.setWrapText(true);
        abstractLabel.getStyleClass().add("thesis-abstract");

        HBox keywordRow = new HBox(6);
        keywordRow.getStyleClass().add("thesis-keywords");
        thesis.keywords().forEach(keyword -> {
            Label tag = new Label(keyword.word());
            tag.getStyleClass().add("keyword-badge");
            keywordRow.getChildren().add(tag);
        });

        HBox actionRow = new HBox(10);
        Button readButton = new Button("Read Thesis");
        readButton.getStyleClass().add("read-button");
        readButton.setOnAction(event -> readThesisButtonOnAction(thesis));
        actionRow.getChildren().add(readButton);

        card.getChildren().addAll(
                title,
                authorsRow,
                yearLabel,
                collegeLabel,
                abstractLabel,
                keywordRow,
                actionRow);

        return card;
    }

    private String toSearchableText(Thesis thesis) {
        String authors = thesis.authors().stream()
                .map(Author::name)
                .reduce("", (a, b) -> a + " " + b);

        String keywords = thesis.keywords().stream()
                .map(Keyword::word)
                .reduce("", (a, b) -> a + " " + b);

        String college = CollegeNameMapper.mapName(thesis.college());

        return thesis.title() + " "
                + thesis.abstractText() + " "
                + authors + " "
                + keywords + " "
                + college;
    }

    private void showEmptyMessage(String message) {
        Label emptyLabel = new Label(message);
        emptyLabel.setWrapText(true);
        emptyLabel.getStyleClass().add("empty-state");
        keywordVBox.getChildren().add(emptyLabel);
    }

    private void readThesisButtonOnAction(Thesis thesis) {
        this.appState.getSearchState().setSelectedStudy(thesis);
        Router.goTo("views/thesis.fxml");
    }
}