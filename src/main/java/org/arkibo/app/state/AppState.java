package org.arkibo.app.state;

import org.arkibo.app.state.navigation.SearchState;

public class AppState {
    private final UserSession userSession = new UserSession();
    private final SearchState searchState = new SearchState();

    public UserSession getUserSession() {
        return userSession;
    }

    public SearchState getSearchState() {
        return searchState;
    }
}
