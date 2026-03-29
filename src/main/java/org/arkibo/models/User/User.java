package org.arkibo.models.User;

import org.arkibo.models.ThesisModels.Thesis;

import java.util.ArrayList;
import java.util.List;

public record User(
        String id,
        String name,
        String email,
        String imageUrl,
        List<Thesis> savedTheses
) {
        public User {
        savedTheses = (savedTheses == null)
                ? new ArrayList<>()
                : new ArrayList<>(savedTheses);
    }
}
