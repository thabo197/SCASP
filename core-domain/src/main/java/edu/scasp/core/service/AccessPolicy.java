package edu.scasp.core.service;

import edu.scasp.core.exception.EntityNotFoundException;
import edu.scasp.core.exception.UnauthorizedOperationException;
import edu.scasp.core.model.Role;
import edu.scasp.core.model.Student;
import edu.scasp.core.model.User;
import edu.scasp.core.model.Validate;
import edu.scasp.core.repository.StudentRepository;
import edu.scasp.core.repository.UserRepository;

/** The single place where "who may do what" is decided (invariant I-08). */
public final class AccessPolicy {

    private final UserRepository users;
    private final StudentRepository students;

    public AccessPolicy(UserRepository users, StudentRepository students) {
        this.users = users;
        this.students = students;
    }

    /** The actor must exist and be active. */
    public User requireActiveActor(String actorId) {
        String id = Validate.notBlank("actorId", actorId);
        User user = users.findById(id)
                .orElseThrow(() -> new UnauthorizedOperationException("Unknown actor: " + id));
        if (!user.isActive()) {
            throw new UnauthorizedOperationException("Actor is not active: " + id);
        }
        return user;
    }

    public User requireStaff(String actorId) {
        User user = requireActiveActor(actorId);
        if (!user.role().isStaffOrAdmin()) {
            throw new UnauthorizedOperationException("Staff access required for " + actorId);
        }
        return user;
    }

    /** Staff and admins may act for any student; a student may act only for themselves. */
    public User requireSelfOrStaff(String actorId, String studentId) {
        User user = requireActiveActor(actorId);
        if (user.role().isStaffOrAdmin()) {
            return user;
        }
        Student student = students.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Unknown student: " + studentId));
        if (user.role() == Role.STUDENT && student.userId().equals(user.userId())) {
            return user;
        }
        throw new UnauthorizedOperationException(actorId + " may not act for student " + studentId);
    }
}
