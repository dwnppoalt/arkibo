package org.arkibo.models.ThesisModels;

import java.util.List;

public record Thesis(
        long id,
        String title,
        String abstractText,
        List<Author> authors,
        List<Keyword> keywords,
        int year,
        ResearchType researchType,
        String college
){}