package org.arkibo.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;
import org.arkibo.models.ThesisModels.Thesis;
import org.arkibo.repository.ThesisRepository;
import org.arkibo.repository.UserRepository;
import org.arkibo.router.Router;
import org.arkibo.search.BM25;
import org.arkibo.utils.CollegeNameMapper;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LibraryController implements StatefulController {
    private static final String GUEST_ID = "-1";

    private AppState appState;
    private List<Thesis> savedTheses = Collections.emptyList();

    @Override
    public void setAppState(AppState appState) {
        this.appState = appState;
        populateFields();
    }

    @Override
    public void setUserRepository(UserRepository userRepository) {}

    @Override
    public void setThesisRepository(ThesisRepository thesisRepository) {}

    @FXML
    private BorderPane libraryRoot;

    @FXML
    private Label greetingLabel;

    @FXML
    private Label resultsLabel;

    @FXML
    private TextField searchBarField;

    @FXML
    private ScrollPane savedScrollPane;

    @FXML
    private VBox libraryVBox;

    @FXML
    public void initialize() {
        libraryRoot.getStylesheets().add(
            getClass().getResource("/css/library.css").toExternalForm()
        );
        libraryVBox.getStyleClass().add("results-list");

        searchBarField.textProperty().addListener((obs, oldText, newText) -> searchSaved());
    }

    public void populateFields() {
        libraryVBox.getChildren().clear();
        savedTheses = Collections.emptyList();

        var currentUser = appState.getUserSession().get();

        if (currentUser == null) {
            greetingLabel.setText("Good day!");
            resultsLabel.setText("No saved theses");
            showEmptyMessage("Please sign in to view your saved theses.");
            return;
        }

        boolean isGuest = GUEST_ID.equals(currentUser.id());
        greetingLabel.setText("Good day, " + currentUser.name() + "!");

        if (isGuest) {
            resultsLabel.setText("No saved theses");
            showEmptyMessage("No saved theses available for guest users. Please sign in to save and view your library.");
            return;
        }

        if (currentUser.savedTheses() == null || currentUser.savedTheses().isEmpty()) {
            resultsLabel.setText("No saved theses");
            showEmptyMessage("No saved theses yet.");
            return;
        }

        savedTheses = new ArrayList<>(currentUser.savedTheses());
        renderTheses(savedTheses);
    }

    @FXML
    private void searchSaved() {
        if (savedTheses.isEmpty()) {
            return;
        }

        String query = searchBarField.getText();
        if (query == null || query.isBlank()) {
            renderTheses(savedTheses);
            return;
        }

        renderTheses(rankWithPartialMatch(query));
    }

    private List<Thesis> rankWithPartialMatch(String query) {
        String normalizedQuery = query.toLowerCase().trim();

        List<Thesis> partialMatches = savedTheses.stream()
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

        BM25<Thesis> bm25 = new BM25<>(savedTheses, this::toSearchableText);
        return bm25.rank(query);
    }

    private void renderTheses(List<Thesis> theses) {
        libraryVBox.getChildren().clear();

        if (theses == null || theses.isEmpty()) {
            resultsLabel.setText("No matching saved theses");
            showEmptyMessage("No saved thesis matched your search.");
            return;
        }

        resultsLabel.setText(theses.size() + " saved theses");
        theses.forEach(thesis -> libraryVBox.getChildren().add(buildThesisCard(thesis)));
    }

    private String toSearchableText(Thesis thesis) {
        String authors = thesis.authors().stream()
                .map(a -> a.name())
                .reduce("", (a, b) -> a + " " + b);

        String keywords = thesis.keywords().stream()
                .map(k -> k.word())
                .reduce("", (a, b) -> a + " " + b);
        
        String college = CollegeNameMapper.mapName(thesis.college());

        return thesis.title() + " "
                + thesis.abstractText() + " "
                + authors + " "
                + keywords + " "
                + college;
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
        collegeLabel.getStyleClass().add("thesis-college");

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

    private void showEmptyMessage(String message) {
        Label emptyLabel = new Label(message);
        emptyLabel.setWrapText(true);
        emptyLabel.getStyleClass().add("empty-state");
        libraryVBox.getChildren().add(emptyLabel);
    }

    private void readThesisButtonOnAction(Thesis thesis) {
        this.appState.getSearchState().setSelectedStudy(thesis);
        Router.goTo("views/thesis.fxml");
    }
}
