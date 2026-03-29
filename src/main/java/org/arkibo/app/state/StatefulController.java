package org.arkibo.app.state;

import org.arkibo.repository.ThesisRepository;
import org.arkibo.repository.UserRepository;

public interface StatefulController {
    void setAppState(AppState appState);
    void setThesisRepository(ThesisRepository thesisRepository);
    void setUserRepository(UserRepository userRepository);
}
