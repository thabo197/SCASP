package edu.scasp.core.service;

import edu.scasp.core.model.Registration;
import edu.scasp.core.model.RequestCategory;
import edu.scasp.core.model.RequestStatus;
import edu.scasp.core.model.ServiceRequest;
import edu.scasp.core.model.Student;
import edu.scasp.core.model.WaitlistEntry;
import edu.scasp.core.report.CourseCapacityRow;
import edu.scasp.core.report.RequestWorkloadReport;
import edu.scasp.core.report.StudentRegistrationRow;
import edu.scasp.core.report.WaitlistRow;
import edu.scasp.core.repository.Repositories;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Reports built with streams and collectors (groupingBy, counting, mapping, summarizingInt). */
public final class DefaultReportService implements ReportService {

    private final Repositories repos;

    DefaultReportService(Repositories repos) {
        this.repos = repos;
    }

    @Override
    public List<CourseCapacityRow> capacityReport() {
        Map<String, Long> registered = repos.registrations().findAll().stream()
                .collect(Collectors.groupingBy(Registration::courseCode, Collectors.counting()));
        Map<String, Long> waiting = repos.waitlist().findAll().stream()
                .collect(Collectors.groupingBy(WaitlistEntry::courseCode, Collectors.counting()));
        return repos.courses().findAll().stream()
                .sorted()
                .map(course -> {
                    int reg = registered.getOrDefault(course.courseCode(), 0L).intValue();
                    int wait = waiting.getOrDefault(course.courseCode(), 0L).intValue();
                    return new CourseCapacityRow(course.courseCode(), course.title(), course.status(),
                            course.capacity(), reg, Math.max(0, course.capacity() - reg), wait);
                })
                .toList();
    }

    @Override
    public List<WaitlistRow> waitingListReport() {
        return repos.courses().findAll().stream()
                .map(course -> new WaitlistRow(course.courseCode(), course.title(),
                        repos.waitlist().entries(course.courseCode()).stream()
                                .map(WaitlistEntry::studentId).toList()))
                .filter(row -> !row.studentIds().isEmpty())
                .sorted(Comparator.<WaitlistRow>comparingInt(row -> row.studentIds().size()).reversed()
                        .thenComparing(WaitlistRow::courseCode))
                .toList();
    }

    @Override
    public RequestWorkloadReport requestWorkloadReport() {
        List<ServiceRequest> all = repos.requests().findAll();

        Map<RequestStatus, Long> byStatus = new EnumMap<>(RequestStatus.class);
        for (RequestStatus status : RequestStatus.values()) {
            byStatus.put(status, 0L);
        }
        all.stream().collect(Collectors.groupingBy(ServiceRequest::status, Collectors.counting()))
                .forEach(byStatus::put);

        Map<RequestCategory, Long> byCategory = new EnumMap<>(RequestCategory.class);
        for (RequestCategory category : RequestCategory.values()) {
            byCategory.put(category, 0L);
        }
        all.stream().collect(Collectors.groupingBy(ServiceRequest::category, Collectors.counting()))
                .forEach(byCategory::put);

        Map<String, Long> openByAssignee = all.stream()
                .filter(r -> r.status().isBeingWorked() && r.assignee().isPresent())
                .collect(Collectors.groupingBy(r -> r.assignee().get(), TreeMap::new, Collectors.counting()));

        return new RequestWorkloadReport(byStatus, byCategory, openByAssignee,
                byStatus.get(RequestStatus.SUBMITTED));
    }

    @Override
    public List<StudentRegistrationRow> studentRegistrationReport() {
        Map<String, List<String>> courses = repos.registrations().findAll().stream()
                .collect(Collectors.groupingBy(Registration::studentId, TreeMap::new,
                        Collectors.mapping(Registration::courseCode, Collectors.toList())));
        Map<String, Long> waiting = repos.waitlist().findAll().stream()
                .collect(Collectors.groupingBy(WaitlistEntry::studentId, Collectors.counting()));
        return repos.students().findAll().stream()
                .sorted()
                .map(student -> new StudentRegistrationRow(student.studentId(), nameOf(student),
                        courses.getOrDefault(student.studentId(), List.of()).stream().sorted().toList(),
                        waiting.getOrDefault(student.studentId(), 0L).intValue()))
                .toList();
    }

    @Override
    public IntSummaryStatistics registrationsPerCourseStatistics() {
        return capacityReport().stream().collect(Collectors.summarizingInt(CourseCapacityRow::registered));
    }

    private String nameOf(Student student) {
        return repos.users().findById(student.userId()).map(u -> u.name()).orElse(student.studentId());
    }
}
