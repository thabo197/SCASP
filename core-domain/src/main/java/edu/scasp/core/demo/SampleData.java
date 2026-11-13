package edu.scasp.core.demo;

import edu.scasp.core.model.CampusService;
import edu.scasp.core.model.Course;
import edu.scasp.core.model.CourseStatus;
import edu.scasp.core.model.Role;
import edu.scasp.core.model.Student;
import edu.scasp.core.model.User;
import edu.scasp.core.model.UserStatus;
import edu.scasp.core.repository.Repositories;

/** Fictional demo data: no real people, courses or places. Loaded straight into the repositories. */
public final class SampleData {

    public static final String STAFF_ID = "U-STAFF1";
    public static final String ADMIN_ID = "U-ADMIN1";

    private SampleData() {
    }

    public static void load(Repositories repos) {
        repos.users().save(new User(STAFF_ID, "Sam Staff", Role.STAFF, UserStatus.ACTIVE));
        repos.users().save(new User(ADMIN_ID, "Alex Admin", Role.ADMIN, UserStatus.ACTIVE));

        String[] names = {"Ana Demo", "Ben Demo", "Chipo Demo", "Dan Demo", "Esi Demo", "Farai Demo"};
        String[] programmes = {"BSc Information Systems", "BSc Computer Science"};
        for (int i = 0; i < names.length; i++) {
            String number = String.format("%03d", i + 1);
            repos.users().save(new User("U" + number, names[i], Role.STUDENT, UserStatus.ACTIVE));
            repos.students().save(new Student("S" + number, "U" + number, programmes[i % 2], 1 + i % 3));
        }

        repos.courses().save(new Course("BCS101", "Introduction to Programming", 3, CourseStatus.OPEN));
        repos.courses().save(new Course("BCS102", "Data Structures", 2, CourseStatus.OPEN));
        repos.courses().save(new Course("BCS201", "Databases", 2, CourseStatus.OPEN));
        repos.courses().save(new Course("BCS301", "Advanced Java", 3, CourseStatus.OPEN));
        repos.courses().save(new Course("BCS999", "Legacy Systems Seminar", 1, CourseStatus.CLOSED));

        repos.campusServices().save(new CampusService("CS-01", "Library", "Study space and borrowing desk",
                -15.4000, 28.3000));
        repos.campusServices().save(new CampusService("CS-02", "Registry Office",
                "Transcripts, clearance and enquiries", -15.4010, 28.3010));
        repos.campusServices().save(new CampusService("CS-03", "Student Health Clinic",
                "Basic health services", -15.4020, 28.2990));
    }
}
