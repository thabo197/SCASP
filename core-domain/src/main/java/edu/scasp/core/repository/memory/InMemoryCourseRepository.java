package edu.scasp.core.repository.memory;

import edu.scasp.core.model.Course;
import edu.scasp.core.repository.CourseRepository;
import java.util.TreeMap;

public class InMemoryCourseRepository extends InMemoryRepository<String, Course> implements CourseRepository {

    public InMemoryCourseRepository() {
        super(new TreeMap<>(), Course::courseCode);
    }
}
