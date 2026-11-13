package edu.scasp.core.repository.memory;

import edu.scasp.core.model.User;
import edu.scasp.core.repository.UserRepository;
import java.util.LinkedHashMap;

public class InMemoryUserRepository extends InMemoryRepository<String, User> implements UserRepository {

    public InMemoryUserRepository() {
        super(new LinkedHashMap<>(), User::userId);
    }
}
