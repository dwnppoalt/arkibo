package org.arkibo.search;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.arkibo.utils.Logger;

public class BM25<T> {

    private final List<T> documents;
    private final List<Map<String, Integer>> termFrequencies;
    private final Map<String, Integer> documentFrequencies;
    private final double avgDocLength;
    private final Function<T, String> textExtractor;

    private final double k1 = 1.5;
    private final double b = 0.75;

    public BM25(List<T> documents, Function<T, String> textExtractor) {
        this.documents = documents;
        this.textExtractor = textExtractor;
        this.termFrequencies = new ArrayList<>();
        this.documentFrequencies = new HashMap<>();

        int totalLength = 0;

        for (T doc : documents) {
            String text = preprocess(textExtractor.apply(doc));
            String[] tokens = text.split("\\s+");

            totalLength += tokens.length;

            Map<String, Integer> tf = new HashMap<>();
            for (String token : tokens) {
                tf.put(token, tf.getOrDefault(token, 0) + 1);
            }

            termFrequencies.add(tf);

            for (String term : tf.keySet()) {
                documentFrequencies.put(term,
                        documentFrequencies.getOrDefault(term, 0) + 1);
            }
        }

        this.avgDocLength = documents.isEmpty()
                ? 0
                : (double) totalLength / documents.size();
    }

    public List<T> rank(String query) {
        String[] queryTerms = preprocess(query).split("\\s+");

        List<Scored<T>> scored = new ArrayList<>();

        for (int i = 0; i < documents.size(); i++) {
            double score = score(i, queryTerms);
            if (score > 0) {
                scored.add(new Scored<>(documents.get(i), score));
            }
        }

        List<Scored<T>> sorted = scored.stream()
                .sorted(Comparator.comparingDouble(Scored<T>::score).reversed())
                .collect(Collectors.toList());

        Logger.log("BM25", "Query:" + query);
        for (Scored<T> s : sorted) {
            String title = textExtractor.apply(s.doc());
            if (title.length() > 60) {
                title = title.substring(0, 57) + "...";
            }
            System.out.printf("[BM25]   %.4f - %s%n", s.score(), title);
        }

        return sorted.stream()
                .map(Scored::doc)
                .collect(Collectors.toList());
    }

    private double score(int docIndex, String[] queryTerms) {
        Map<String, Integer> tf = termFrequencies.get(docIndex);
        int docLength = tf.values().stream().mapToInt(Integer::intValue).sum();
        int N = documents.size();

        double score = 0.0;

        for (String term : queryTerms) {
            if (!tf.containsKey(term))
                continue;

            int freq = tf.get(term);
            int df = documentFrequencies.getOrDefault(term, 0);

            double idf = Math.log((N - df + 0.5) / (df + 0.5) + 1);

            double numerator = freq * (k1 + 1);
            double denominator = freq + k1 * (1 - b + b * docLength / avgDocLength);

            score += idf * (numerator / denominator);
        }

        return score;
    }

    private String preprocess(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ");
    }

    private record Scored<T>(T doc, double score) {}
}