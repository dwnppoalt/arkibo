package org.arkibo.search;

import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.arkibo.dto.Response;
import org.arkibo.models.ThesisModels.College;
import org.arkibo.models.ThesisModels.Thesis;
import org.arkibo.repository.ThesisRepository;
import org.arkibo.repository.UserRepository;

public class SearchService {

    private final ThesisRepository thesisRepository;
    private final UserRepository userRepository;
    private static final int PAGE_SIZE = 10;

    public SearchService(ThesisRepository thesisRepository, UserRepository userRepository) {
        this.thesisRepository = thesisRepository;
        this.userRepository = userRepository;
    }

    public Response<List<Thesis>> search(
            String query,
            String yearPreset,
            Integer beforeYear,
            Integer afterYear,
            String researchType,
            College college,
            String sortBy,
            Integer page) {

        int safePage = (page == null || page < 1) ? 1 : page;

        String normalizedType = normalizeResearchType(researchType);

        YearRange yearRange = resolveYearRange(yearPreset, beforeYear, afterYear);

        List<Thesis> results = new ArrayList<>(
                thesisRepository.search(
                        query,
                        yearRange.before(),
                        yearRange.after(),
                        normalizedType,
                        college).data());

        results = applySorting(results, query, sortBy);

        List<Thesis> paged = paginate(results, safePage);

        return Response.success("Search and ranking successful.", paged);
    }


    private List<Thesis> applySorting(List<Thesis> results, String query, String sortBy) {

        if ("Relevance".equals(sortBy) && query != null && !query.isBlank()) {
            BM25<Thesis> bm25 = new BM25<>(results, this::toSearchableText);
            return bm25.rank(query);
        }

        if ("Newest first".equals(sortBy)) {
            results.sort(Comparator.comparingInt(Thesis::year).reversed());
        } else if ("Oldest first".equals(sortBy)) {
            results.sort(Comparator.comparingInt(Thesis::year));
        }

        return results;
    }

    private List<Thesis> paginate(List<Thesis> results, int page) {

        int total = results.size();
        int fromIndex = (page - 1) * PAGE_SIZE;

        if (fromIndex >= total) {
            return List.of();
        }

        int toIndex = Math.min(fromIndex + PAGE_SIZE, total);

        return results.subList(fromIndex, toIndex);
    }

    private String normalizeResearchType(String researchType) {
        if (researchType == null || researchType.equalsIgnoreCase("Any Type")) {
            return null;
        }
        return researchType.toLowerCase();
    }

    private String toSearchableText(Thesis thesis) {

        String authors = thesis.authors().stream()
                .map(a -> a.name())
                .reduce("", (a, b) -> a + " " + b);

        String keywords = thesis.keywords().stream()
                .map(k -> k.word())
                .reduce("", (a, b) -> a + " " + b);

        return thesis.title() + " "
                + thesis.abstractText() + " "
                + authors + " "
                + keywords;
    }

    private YearRange resolveYearRange(
            String preset,
            Integer customBefore,
            Integer customAfter) {

        int currentYear = Year.now().getValue();

        if (customBefore != null || customAfter != null) {

            Integer before = customBefore;
            Integer after = customAfter;

            if (before != null && after != null && after > before) {
                int temp = before;
                before = after;
                after = temp;
            }

            return new YearRange(before, after);
        }

        return switch (preset) {
            case "Last year" ->
                new YearRange(currentYear, currentYear - 1);

            case "Last 5 years" ->
                new YearRange(currentYear, currentYear - 5);

            default ->
                new YearRange(null, null);
        };
    }

    public Response<Void> addThesisToSaved(String userId, long thesisId) {
        try {
            this.userRepository.addThesisToSaved(userId, thesisId);
            return Response.success("Added to saved successfully", null);
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }
    
    public Response<Void> removeThesisFromSaved(String userId, long thesisId) {
        try {
            this.userRepository.removeThesisFromSaved(userId, thesisId);
            return Response.success("Removed from saved successfully", null);
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }

    private record YearRange(Integer before, Integer after) {
    }
}