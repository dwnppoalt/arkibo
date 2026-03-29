package org.arkibo.dto.thesis;

import java.util.List;

import org.arkibo.models.ThesisModels.College;

public record ThesisCreateRequest(
        String title,
        List<AuthorCreateRequest> authors,
        String abstractText,
        List<KeywordCreateRequest> keywords,
        int year,
        String researchType,
        College college
) {}
