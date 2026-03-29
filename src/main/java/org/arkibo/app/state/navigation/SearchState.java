package org.arkibo.app.state.navigation;

import org.arkibo.models.ThesisModels.College;
import org.arkibo.models.ThesisModels.Thesis;

public class SearchState {
    private String searchQuery;
    private String sortByFilter;
    private String researchTypeFilter;
    private College collegeChoiceFilter;
    private String yearRangeFilter;
    private Integer page;
    
    private Thesis selectedStudy;

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query;
    }

    public Thesis getSelectedStudy() {
        return selectedStudy;
    }

    public void setSelectedStudy(Thesis thesis) {
        this.selectedStudy = thesis;
    }

    public String getSortByFilter() {
        return sortByFilter;
    }

    public void setSortByFilter(String sortByFilter) {
        this.sortByFilter = sortByFilter;
    }

    public String getResearchTypeFilter() {
        return researchTypeFilter;
    }

    public void setResearchTypeFilter(String researchTypeFilter) {
        this.researchTypeFilter = researchTypeFilter;
    }

    public College getCollegeChoiceFilter() {
        return collegeChoiceFilter;
    }

    public void setCollegeChoiceFilter(College collegeChoiceFilter) {
        this.collegeChoiceFilter = collegeChoiceFilter;
    }

    public String getYearRangeFilter() {
        return yearRangeFilter;
    }

    public void setYearRangeFilter(String yearRangeFilter) {
        this.yearRangeFilter = yearRangeFilter;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPage() {
        return this.page;
    }

    public void clear() {
        this.searchQuery = null;
        this.selectedStudy = null;
    }

}
