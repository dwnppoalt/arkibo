package org.arkibo.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;
import org.arkibo.repository.ThesisRepository;
import org.arkibo.repository.UserRepository;
import org.arkibo.services.StorageService;
import org.arkibo.utils.LocalPdfServer;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class ReadController implements StatefulController {

    private AppState appState;
    private ThesisRepository thesisRepository;
    private UserRepository userRepository;

    private final StorageService storageService = new StorageService();

    private WebEngine webEngine;
    private boolean appStateReady = false;

    private LocalPdfServer server;
    private int port;

    @FXML
    private WebView readWebview;

    @FXML
    public void initialize() {
        webEngine = readWebview.getEngine();

        webEngine.setOnError(e -> System.err.println("WebEngine error: " + e.getMessage()));

        webEngine.getLoadWorker().exceptionProperty().addListener((obs, old, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
            }
        });

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                System.out.println("WebView loaded");

                try {
                    Object result = webEngine.executeScript("typeof PDFViewerApplication");
                    System.out.println("PDF.js status: " + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        startServer();
    }

    private void startServer() {
        try {
            server = new LocalPdfServer(0); // random port
            server.start();
            port = server.getListeningPort();

            System.out.println("Server started at port: " + port);

            tryLoadDocument();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setAppState(AppState appState) {
        this.appState = appState;
        this.appStateReady = true;
        tryLoadDocument();
    }

    @Override
    public void setThesisRepository(ThesisRepository thesisRepository) {
        this.thesisRepository = thesisRepository;
    }

    @Override
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private void tryLoadDocument() {
        if (!appStateReady || server == null)
            return;

        try {
            String uuid = Long.toString(
                    appState.getSearchState().getSelectedStudy().id());

            String link = storageService.getAccessLink(uuid);
            System.out.println("PDF link: " + link);

            String encodedSourceUrl = URLEncoder.encode(link, StandardCharsets.UTF_8);
            String proxiedPdfUrl = "http://localhost:" + port + "/proxy-pdf?url=" + encodedSourceUrl;
            String encodedPdfUrl = URLEncoder.encode(proxiedPdfUrl, StandardCharsets.UTF_8);

            String finalUrl = "http://localhost:" + port + "/viewer.html?file=" + encodedPdfUrl;

            System.out.println("Loading: " + finalUrl);

            webEngine.load(finalUrl);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}