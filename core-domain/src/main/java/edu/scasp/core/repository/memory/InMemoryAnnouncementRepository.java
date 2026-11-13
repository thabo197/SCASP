package edu.scasp.core.repository.memory;

import edu.scasp.core.model.Announcement;
import edu.scasp.core.repository.AnnouncementRepository;
import java.util.LinkedHashMap;

public class InMemoryAnnouncementRepository extends InMemoryRepository<String, Announcement> implements AnnouncementRepository {

    public InMemoryAnnouncementRepository() {
        super(new LinkedHashMap<>(), Announcement::announcementId);
    }
}
