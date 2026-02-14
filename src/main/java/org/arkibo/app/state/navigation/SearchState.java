package org.arkibo.app.state.navigation;

public class SearchState {
    private String searchQuery;
    private int selectedStudyId = -1;

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query;
    }

    public int getSelectedStudyId() {
        return selectedStudyId;
    }

    public void setSelectedStudyId(int id) {
        this.selectedStudyId = id;
    }

    public void clear() {
        this.searchQuery = null;
        this.selectedStudyId = -1;
    }

}
