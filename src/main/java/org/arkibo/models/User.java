package org.arkibo.models;

import org.arkibo.models.ThesisModels.Thesis;

import java.util.Optional;
import java.util.List;

public record User(
        long id,
        String name,
        String email,
        Optional<List<Thesis>> savedTheses
) {}
