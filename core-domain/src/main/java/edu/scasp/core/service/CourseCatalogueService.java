package edu.scasp.core.service;

import edu.scasp.core.model.Course;
import edu.scasp.core.model.CourseStatus;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public interface CourseCatalogueService {

    /** Staff only. The new course is OPEN. */
    Course createCourse(String actorId, String courseCode, String title, int capacity);

    /** Staff only. Capacity cannot drop below the number of registered students; an increase fills seats from the waiting list. */
    Course updateCourse(String actorId, String courseCode, String title, int capacity);

    /** Staff only. Only allowed status transitions are accepted. */
    Course changeStatus(String actorId, String courseCode, CourseStatus newStatus);

    Optional<Course> findCourse(String courseCode);

    /** All courses in natural order (by course code). */
    List<Course> listCourses();

    List<Course> listCourses(Comparator<? super Course> order);

    /** Case-insensitive match on code or title. A blank keyword returns every course. */
    List<Course> search(String keyword);

    List<Course> findByStatus(CourseStatus status);
}
