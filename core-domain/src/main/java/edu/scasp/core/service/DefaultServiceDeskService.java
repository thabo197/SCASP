package edu.scasp.core.service;

import edu.scasp.core.exception.EntityNotFoundException;
import edu.scasp.core.exception.InvalidOperationException;
import edu.scasp.core.model.RequestCategory;
import edu.scasp.core.model.RequestPriority;
import edu.scasp.core.model.RequestStatus;
import edu.scasp.core.model.ServiceRequest;
import edu.scasp.core.model.Student;
import edu.scasp.core.model.User;
import edu.scasp.core.repository.Repositories;
import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

/**
 * Service request workflow. Every request is stored in the repository (a Map, for lookup by id). A PriorityQueue
 * holds the SUBMITTED ones so the most urgent request can be taken next; it is rebuilt from the repository when
 * the service starts.
 */
public final class DefaultServiceDeskService implements ServiceDeskService {

    private final Repositories repos;
    private final AccessPolicy access;
    private final AuditTrail audit;
    private final Clock clock;
    private final IdGenerator ids;
    private final PriorityQueue<ServiceRequest> pending = new PriorityQueue<>();

    DefaultServiceDeskService(Repositories repos, AccessPolicy access, AuditTrail audit, Clock clock,
                              IdGenerator ids) {
        this.repos = repos;
        this.access = access;
        this.audit = audit;
        this.clock = clock;
        this.ids = ids;
        repos.requests().findAll().stream()
                .filter(r -> r.status() == RequestStatus.SUBMITTED)
                .forEach(pending::add);
    }

    @Override
    public ServiceRequest submit(String actorId, String studentId, RequestCategory category,
                                 RequestPriority priority) {
        access.requireSelfOrStaff(actorId, studentId);
        Student student = repos.students().findById(studentId == null ? "" : studentId.trim())
                .orElseThrow(() -> new EntityNotFoundException("Unknown student: " + studentId));
        ServiceRequest request = ServiceRequest.submitted(ids.next("REQ"), student.studentId(), category,
                priority, clock.instant());
        repos.requests().save(request);
        pending.add(request);
        audit.record(actorId, "SUBMIT_REQUEST", "ServiceRequest", request.requestId());
        return request;
    }

    @Override
    public ServiceRequest assign(String actorId, String requestId, String staffUserId) {
        access.requireStaff(actorId);
        User assignee = requireAssignee(staffUserId);
        ServiceRequest request = require(requestId);
        ServiceRequest assigned = request.assignTo(assignee.userId(), clock.instant());
        pending.remove(request);
        repos.requests().save(assigned);
        audit.record(actorId, "ASSIGN_REQUEST", "ServiceRequest", assigned.requestId());
        return assigned;
    }

    @Override
    public Optional<ServiceRequest> assignNext(String actorId, String staffUserId) {
        access.requireStaff(actorId);
        User assignee = requireAssignee(staffUserId);
        ServiceRequest next = pending.poll();
        if (next == null) {
            return Optional.empty();
        }
        ServiceRequest assigned = next.assignTo(assignee.userId(), clock.instant());
        repos.requests().save(assigned);
        audit.record(actorId, "ASSIGN_REQUEST", "ServiceRequest", assigned.requestId());
        return Optional.of(assigned);
    }

    @Override
    public ServiceRequest changeStatus(String actorId, String requestId, RequestStatus newStatus) {
        access.requireStaff(actorId);
        ServiceRequest request = require(requestId);
        ServiceRequest updated = request.withStatus(newStatus, clock.instant());
        if (request.status() == RequestStatus.SUBMITTED) {
            pending.remove(request);
        }
        repos.requests().save(updated);
        audit.record(actorId, "CHANGE_REQUEST_STATUS", "ServiceRequest", updated.requestId());
        return updated;
    }

    @Override
    public Optional<ServiceRequest> findRequest(String requestId) {
        return repos.requests().findById(requestId);
    }

    @Override
    public List<ServiceRequest> listRequests() {
        return repos.requests().findAll().stream().sorted().toList();
    }

    @Override
    public List<ServiceRequest> listRequests(Comparator<? super ServiceRequest> order) {
        return repos.requests().findAll().stream().sorted(order).toList();
    }

    @Override
    public List<ServiceRequest> listByStatus(RequestStatus status) {
        return listRequests().stream().filter(r -> r.status() == status).toList();
    }

    @Override
    public List<ServiceRequest> listByAssignee(String staffUserId) {
        return listRequests().stream().filter(r -> r.assignee().filter(a -> a.equals(staffUserId)).isPresent())
                .toList();
    }

    @Override
    public List<ServiceRequest> listForStudent(String studentId) {
        return listRequests().stream().filter(r -> r.studentId().equals(studentId)).toList();
    }

    @Override
    public List<ServiceRequest> pendingQueue() {
        return pending.stream().sorted().toList();
    }

    private ServiceRequest require(String requestId) {
        return repos.requests().findById(requestId == null ? "" : requestId.trim())
                .orElseThrow(() -> new EntityNotFoundException("Unknown request: " + requestId));
    }

    private User requireAssignee(String staffUserId) {
        User user = repos.users().findById(staffUserId == null ? "" : staffUserId.trim())
                .orElseThrow(() -> new EntityNotFoundException("Unknown staff member: " + staffUserId));
        if (!user.isActive() || !user.role().isStaffOrAdmin()) {
            throw new InvalidOperationException("User " + user.userId() + " cannot be assigned requests");
        }
        return user;
    }
}
