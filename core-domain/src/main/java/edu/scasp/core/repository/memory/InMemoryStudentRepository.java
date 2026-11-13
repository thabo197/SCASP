package edu.scasp.core.repository.memory;

import edu.scasp.core.model.Student;
import edu.scasp.core.repository.StudentRepository;
import java.util.LinkedHashMap;

public class InMemoryStudentRepository extends InMemoryRepository<String, Student> implements StudentRepository {

    public InMemoryStudentRepository() {
        super(new LinkedHashMap<>(), Student::studentId);
    }
}
