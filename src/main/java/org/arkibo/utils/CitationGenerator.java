package org.arkibo.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.arkibo.models.ThesisModels.Thesis;

public class CitationGenerator {

    public static String generateCitation(Thesis thesis, String format) {
        if (thesis == null)
            return "";

        List<String> authorNames = thesis.authors()
                .stream()
                .map(a -> a.name())
                .collect(Collectors.toList());

        switch (format.toLowerCase()) {
            case "apa":
                return generateAPA(thesis, authorNames);
            case "mla":
                return generateMLA(thesis, authorNames);
            case "chicago":
                return generateChicago(thesis, authorNames);
            case "harvard":
                return generateHarvard(thesis, authorNames);
            default:
                return "";
        }
    }

    private static String generateAPA(Thesis thesis, List<String> authors) {
        List<String> apaAuthors = authors.stream().map(name -> {
            String[] parts = name.split(" ");
            String lastName = parts[parts.length - 1];

            StringBuilder initials = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                initials.append(parts[i].charAt(0)).append(". ");
            }

            return lastName + ", " + initials.toString().trim();
        }).collect(Collectors.toList());

        String authorString;
        if (apaAuthors.size() > 1) {
            String last = apaAuthors.remove(apaAuthors.size() - 1);
            authorString = String.join(", ", apaAuthors) + ", & " + last;
        } else {
            authorString = apaAuthors.get(0);
        }

        return String.format("%s (%d). %s [Undergraduate thesis]. Central Luzon State University.",
                authorString,
                thesis.year(),
                thesis.title());
    }

    private static String generateMLA(Thesis thesis, List<String> authors) {
        String[] parts = authors.get(0).split(" ");
        String lastName = parts[parts.length - 1];
        String firstName = String.join(" ", java.util.Arrays.copyOf(parts, parts.length - 1));

        String mlaAuthor = lastName + ", " + firstName;

        return String.format("%s%s. %s. %d. Central Luzon State University, Undergraduate thesis.",
                mlaAuthor,
                authors.size() > 1 ? ", et al" : "",
                thesis.title(),
                thesis.year());
    }

    private static String generateChicago(Thesis thesis, List<String> authors) {
        return String.format("%s. %d. %s. Central Luzon State University. Undergraduate thesis.",
                String.join(", ", authors),
                thesis.year(),
                thesis.title());
    }

    private static String generateHarvard(Thesis thesis, List<String> authors) {
        List<String> formatted = authors.stream().map(name -> {
            String[] parts = name.split(" ");
            String lastName = parts[parts.length - 1];

            StringBuilder initials = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                initials.append(parts[i].charAt(0)).append(".");
            }

            return lastName + ", " + initials;
        }).collect(Collectors.toList());

        return String.format("%s, %d. %s. Undergraduate thesis. Central Luzon State University.",
                String.join(", ", formatted),
                thesis.year(),
                thesis.title());
    }

}