package edu.scasp.core.repository;

import edu.scasp.core.model.Course;

/** findAll() returns courses in natural order (by course code). */
public interface CourseRepository extends Repository<String, Course> {
}
