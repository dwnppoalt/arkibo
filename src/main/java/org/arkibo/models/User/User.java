package org.arkibo.models.User;

import org.arkibo.models.ThesisModels.Thesis;

import java.util.List;

public record User(
        String id,
        String name,
        String email,
        String imageUrl,
        List<Thesis> savedTheses
) {}
