package edu.scasp.core.repository.memory;

import edu.scasp.core.model.CampusService;
import edu.scasp.core.repository.CampusServiceRepository;
import java.util.LinkedHashMap;

public class InMemoryCampusServiceRepository extends InMemoryRepository<String, CampusService> implements CampusServiceRepository {

    public InMemoryCampusServiceRepository() {
        super(new LinkedHashMap<>(), CampusService::serviceId);
    }
}
