package edu.scasp.core.service;

import edu.scasp.core.repository.Repositories;
import java.time.Clock;

/**
 * Wires the repositories, rules and services together. Later stages obtain their services from here, passing a
 * different set of Repositories (JDBC) without changing any service code.
 */
public final class Platform {

    private final Repositories repositories;
    private final AccessPolicy accessPolicy;
    private final AuditTrail auditTrail;
    private final CourseCatalogueService courses;
    private final RegistrationService registrations;
    private final ServiceDeskService serviceDesk;
    private final AnnouncementService announcements;
    private final ReportService reports;

    private Platform(Repositories repos, Clock clock) {
        IdGenerator ids = new SequentialIdGenerator();
        this.repositories = repos;
        this.accessPolicy = new AccessPolicy(repos.users(), repos.students());
        this.auditTrail = new AuditTrail(repos.auditEvents(), ids, clock);
        SeatPromoter promoter = new SeatPromoter(repos, auditTrail, clock);
        this.courses = new DefaultCourseCatalogueService(repos, accessPolicy, auditTrail, promoter);
        this.registrations = new DefaultRegistrationService(repos, accessPolicy, auditTrail, clock, promoter);
        this.serviceDesk = new DefaultServiceDeskService(repos, accessPolicy, auditTrail, clock, ids);
        this.announcements = new DefaultAnnouncementService(repos, accessPolicy, auditTrail, clock, ids);
        this.reports = new DefaultReportService(repos);
    }

    public static Platform create(Repositories repositories, Clock clock) {
        return new Platform(repositories, clock);
    }

    public Repositories repositories() { return repositories; }
    public AccessPolicy accessPolicy() { return accessPolicy; }
    public AuditTrail auditTrail() { return auditTrail; }
    public CourseCatalogueService courses() { return courses; }
    public RegistrationService registrations() { return registrations; }
    public ServiceDeskService serviceDesk() { return serviceDesk; }
    public AnnouncementService announcements() { return announcements; }
    public ReportService reports() { return reports; }
}
