package edu.scasp.core.repository.memory;

import edu.scasp.core.repository.Repositories;

/** Builds a complete, empty set of in-memory repositories. */
public final class InMemoryRepositories {

    private InMemoryRepositories() {
    }

    public static Repositories create() {
        return new Repositories(
                new InMemoryUserRepository(),
                new InMemoryStudentRepository(),
                new InMemoryCourseRepository(),
                new InMemoryRegistrationRepository(),
                new InMemoryWaitlistRepository(),
                new InMemoryServiceRequestRepository(),
                new InMemoryAnnouncementRepository(),
                new InMemoryAuditEventRepository(),
                new InMemoryCampusServiceRepository());
    }
}
