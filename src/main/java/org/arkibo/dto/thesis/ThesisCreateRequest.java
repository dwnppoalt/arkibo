package org.arkibo.dto.thesis;

import java.util.List;

public record ThesisCreateRequest(
        String title,
        List<AuthorCreateReqeust> authors,
        String abstractText,
        List<KeywordCreateRequest> keywords,
        int year,
        String researchType,
        String college
) {}
