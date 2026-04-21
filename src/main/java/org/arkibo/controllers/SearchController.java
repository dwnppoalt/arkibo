package org.arkibo.controllers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;
import org.arkibo.models.ThesisModels.College;
import org.arkibo.models.ThesisModels.Thesis;
import org.arkibo.repository.ThesisRepository;
import org.arkibo.repository.UserRepository;
import org.arkibo.router.Router;
import org.arkibo.search.SearchService;
import org.arkibo.utils.CollegeNameMapper;
import org.arkibo.utils.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SearchController implements StatefulController {
    private AppState appState;
    private ThesisRepository thesisRepository;
    private UserRepository userRepository;
    private SearchService searchService;

    private boolean isLoading = true;
    private boolean isGuest;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Override
    public void setAppState(AppState appState) {
        this.appState = appState;
        this.isGuest = appState.getUserSession().get().id() == "-1";
        bindStates();
        search();
    }

    @Override
    public void setThesisRepository(ThesisRepository thesisRepository) {
        this.thesisRepository = thesisRepository;
    }

    @Override
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.searchService = new SearchService(thesisRepository, userRepository);

    }

    @FXML
    private BorderPane searchRoot;

    @FXML
    private Label searchLabel;

    @FXML
    private ChoiceBox<String> sortByChoiceBox;

    @FXML
    private ChoiceBox<String> researchTypeChoiceBox;

    @FXML
    private ChoiceBox<String> collegeChoiceBox;

    @FXML
    private ChoiceBox<String> yearRangeChoiceBox;

    @FXML
    private HBox customYearRangeHBox;

    @FXML
    private TextField customYearRangeFrom;

    @FXML
    private TextField customYearRangeTo;

    @FXML
    private Button applyFilterButton;

    @FXML
    private TextField searchBarField;

    @FXML
    private Label resultsLabel;

    @FXML
    private VBox resultsVBox;

    @FXML
    private Button previousPaginationButton;

    @FXML
    private Button nextPaginationButton;

    @FXML
    public void initialize() {
        searchRoot.getStylesheets()
                .add(getClass().getResource("/css/search.css").toExternalForm());

        searchBarField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                search();
        });
        populateFields();
    }

    private void bindStates() {
        searchBarField.setText(this.appState.getSearchState().getSearchQuery());
        this.appState.getSearchState().setPage(1);

        String initialCollege = collegeChoiceBox.getSelectionModel().getSelectedItem();
        if (initialCollege != null) {
            this.appState.getSearchState().setCollegeChoiceFilter(College.valueOf(initialCollege));
        }
        String initialSortBy = sortByChoiceBox.getSelectionModel().getSelectedItem();
        if (initialSortBy != null) {
            this.appState.getSearchState().setSortByFilter(initialSortBy);
        }
        String initialResearchType = researchTypeChoiceBox.getSelectionModel().getSelectedItem();
        if (initialResearchType != null) {
            this.appState.getSearchState().setResearchTypeFilter(initialResearchType);
        }
        String initialYearRange = yearRangeChoiceBox.getSelectionModel().getSelectedItem();
        if (initialYearRange != null) {
            this.appState.getSearchState().setYearRangeFilter(initialYearRange);
        }

        sortByChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            this.appState.getSearchState().setSortByFilter(newVal);
            this.appState.getSearchState().setPage(1);
        });
        researchTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            this.appState.getSearchState().setResearchTypeFilter(newVal != null ? newVal.toLowerCase() : null);
            this.appState.getSearchState().setPage(1);
        });
        collegeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            this.appState.getSearchState().setCollegeChoiceFilter(College.valueOf(newVal));
            this.appState.getSearchState().setPage(1);
        });
        yearRangeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if ("Custom range".equals(newVal)) {
                customYearRangeHBox.setVisible(true);
            } else {
                customYearRangeHBox.setVisible(false);
            }
            this.appState.getSearchState().setYearRangeFilter(newVal);
            this.appState.getSearchState().setPage(1);
        });

        nextPaginationButton.setOnAction(event -> {
            if (isLoading) return;
            Integer page = this.appState.getSearchState().getPage() + 1;
            Logger.log("SEARCH", "Page: " + page);
            this.appState.getSearchState().setPage(page);
            search();
        });

        previousPaginationButton.setOnAction(event -> {
            if (isLoading) return;
            Integer page = this.appState.getSearchState().getPage() - 1;
            Logger.log("SEARCH", "Page: " + page);
            this.appState.getSearchState().setPage(page);
            search();
        });

        applyFilterButton.setOnAction(event -> search());
    }

    private void populateFields() {
        sortByChoiceBox.setItems(
                FXCollections.observableArrayList("Relevance", "Newest first", "Oldest first"));

        researchTypeChoiceBox.setItems(
                FXCollections.observableArrayList("Any Type", "Qualitative", "Quantitative", "Capstone"));

        collegeChoiceBox.setItems(
                FXCollections.observableArrayList("ALL", "CAG", "CASS", "CBA", "CED", "CEN", "COF", "CHSI", "CVSM",
                        "COS"));

        yearRangeChoiceBox.setItems(
                FXCollections.observableArrayList("Any time", "Last year", "Last 5 years", "Custom range"));

        sortByChoiceBox.getSelectionModel().selectFirst();
        researchTypeChoiceBox.getSelectionModel().selectFirst();
        collegeChoiceBox.getSelectionModel().selectFirst();
        yearRangeChoiceBox.getSelectionModel().selectFirst();

    }

    private void renderResults(List<Thesis> results) {

        resultsVBox.getChildren().clear();
        resultsLabel.setText(results.size() + " results found");

        for (Thesis thesis : results) {

            VBox card = new VBox(8);
            card.getStyleClass().add("thesis-card");

            Label title = new Label(thesis.title());
            title.getStyleClass().add("thesis-title");

            HBox authorsRow = new HBox(5);
            authorsRow.getStyleClass().add("thesis-authors");

            for (var author : thesis.authors()) {
                Label authorLabel = new Label(author.name());
                authorsRow.getChildren().add(authorLabel);
            }

            Label yearLabel = new Label("Published in " + thesis.year());
            yearLabel.getStyleClass().add("thesis-year");

            Label collegeLabel = new Label(CollegeNameMapper.mapName(thesis.college()));
            collegeLabel.getStyleClass().add("thesis-college");

            String preview = thesis.abstractText();
            if (preview.length() > 120) {
                preview = preview.substring(0, 120) + "...";
            }

            Label abstractLabel = new Label(preview);
            abstractLabel.setWrapText(true);
            abstractLabel.getStyleClass().add("thesis-abstract");

            HBox keywordRow = new HBox(6);
            keywordRow.getStyleClass().add("thesis-keywords");

            for (var keyword : thesis.keywords()) {
                Label tag = new Label(keyword.word());
                tag.getStyleClass().add("keyword-badge");
                keywordRow.getChildren().add(tag);
            }

            HBox actionRow = new HBox(10);

            Button readButton = new Button("Read Thesis");
            readButton.getStyleClass().add("read-button");

            readButton.setOnAction(event -> {
                readThesisButtonOnAction(thesis);
            });

            Button savedButton = new Button(this.isGuest ? "Login to save" : isThesisInSaved(thesis.id()) ? "Saved" : "Save");
            
            savedButton.setDisable(this.isGuest);

            savedButton.setOnAction(event -> {
                toggleSaved(thesis, savedButton);
            });

            actionRow.getChildren().addAll(readButton, savedButton);

            card.getChildren().addAll(
                    title,
                    authorsRow,
                    yearLabel,
                    collegeLabel,
                    abstractLabel,
                    keywordRow,
                    actionRow);

            resultsVBox.getChildren().add(card);
        }
    }

    private boolean isThesisInSaved(long thesisId) {
        return appState.getUserSession().get().savedTheses()
                          .stream().anyMatch(t -> t.id() == thesisId);
    }

    private void readThesisButtonOnAction(Thesis thesis) {
        this.appState.getSearchState().setSelectedStudy(thesis);
        Router.goTo("views/thesis.fxml");
    }

    @FXML
    private void search() {
        isLoading = true;

        if (appState.getSearchState().getPage() < 1) {
            appState.getSearchState().setPage(1);
        }

        appState.getSearchState().setSearchQuery(searchBarField.getText());
        resultsVBox.getChildren().clear();
        resultsLabel.setText("Loading...");

        CompletableFuture
                .supplyAsync(() -> this.searchService.search(
                        appState.getSearchState().getSearchQuery(),
                        appState.getSearchState().getYearRangeFilter(),
                        parseYear(customYearRangeFrom.getText()),
                        parseYear(customYearRangeTo.getText()),
                        appState.getSearchState().getResearchTypeFilter(),
                        appState.getSearchState().getCollegeChoiceFilter(),
                        appState.getSearchState().getSortByFilter(),
                        appState.getSearchState().getPage()
                    ),
                        executor)
                .thenAccept(response -> {

                    if (!response.ok()) {
                        throw new RuntimeException("Search failed");
                    }

                    List<Thesis> results = response.data();

                    Platform.runLater(() -> {
                        isLoading = false;
                        int currentPage = appState.getSearchState().getPage();

                        previousPaginationButton.setDisable(currentPage <= 1);
                        nextPaginationButton.setDisable(results.size() < 10);
                        renderResults(results);
                    });

                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        isLoading = false;
                        resultsLabel.setText("Something went wrong. Try searching again.");
                    });
                    return null;
                });
    }
    
    @FXML
    private void toggleSaved(Thesis thesis, Button savedButton) {
        boolean isSaved = isThesisInSaved(thesis.id());

        savedButton.setDisable(true);

        CompletableFuture
                .supplyAsync(() -> isSaved
                        ? this.searchService.removeThesisFromSaved(this.appState.getUserSession().get()
                                .id(), thesis.id())
                        : this.searchService.addThesisToSaved(this.appState.getUserSession().get()
                                .id(), thesis.id()),
                        executor)
                .thenAccept(response -> {
                    if (!response.ok()) {
                        throw new RuntimeException("Saving failed.");
                    }

                    Platform.runLater(() -> {
                        if (isSaved) {
                            this.appState.getUserSession().get().savedTheses().removeIf(t -> t.id() == thesis.id());
                            savedButton.setText("Save");
                        } else {
                            this.appState.getUserSession().get().savedTheses().add(thesis);
                            savedButton.setText("Saved");
                        }
                        savedButton.setDisable(false);
                    });

                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        savedButton.setDisable(false);

                    });
                    return null;
                });
        
    }
    // helpers
    private Integer parseYear(String value) {
        if (value == null || value.isBlank())
            return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
