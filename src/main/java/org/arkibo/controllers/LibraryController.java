package org.arkibo.controllers;

import org.arkibo.app.state.AppState;
import org.arkibo.app.state.StatefulController;
import org.arkibo.repository.ThesisRepository;
import org.arkibo.repository.UserRepository;

public class LibraryController implements StatefulController {
    private AppState appState;
    private AppController appController;

    @Override
    public void setAppState(AppState appState) {
        this.appState = appState;
    }

    @Override
    public void setUserRepository(UserRepository userRepository) {
    }

    @Override
    public void setThesisRepository(ThesisRepository thesisRepository) {
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }
}
