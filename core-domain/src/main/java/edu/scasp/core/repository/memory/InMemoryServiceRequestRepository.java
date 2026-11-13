package edu.scasp.core.repository.memory;

import edu.scasp.core.model.ServiceRequest;
import edu.scasp.core.repository.ServiceRequestRepository;
import java.util.LinkedHashMap;

public class InMemoryServiceRequestRepository extends InMemoryRepository<String, ServiceRequest> implements ServiceRequestRepository {

    public InMemoryServiceRequestRepository() {
        super(new LinkedHashMap<>(), ServiceRequest::requestId);
    }
}
