package edu.scasp.core.repository;

/** The full set of repositories the services need. Stage 5 supplies a JDBC-backed set. */
public record Repositories(UserRepository users, StudentRepository students, CourseRepository courses,
                           RegistrationRepository registrations, WaitlistRepository waitlist,
                           ServiceRequestRepository requests, AnnouncementRepository announcements,
                           AuditEventRepository auditEvents, CampusServiceRepository campusServices) {
}
