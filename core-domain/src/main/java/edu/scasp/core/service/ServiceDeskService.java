package edu.scasp.core.service;

import edu.scasp.core.model.RequestCategory;
import edu.scasp.core.model.RequestPriority;
import edu.scasp.core.model.RequestStatus;
import edu.scasp.core.model.ServiceRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public interface ServiceDeskService {

    /** A student may submit for themselves; staff may submit for any student. The request starts as SUBMITTED. */
    ServiceRequest submit(String actorId, String studentId, RequestCategory category, RequestPriority priority);

    /** Staff only. Assigns a SUBMITTED request to an active staff member. */
    ServiceRequest assign(String actorId, String requestId, String staffUserId);

    /** Staff only. Assigns the most urgent, oldest SUBMITTED request (taken from the priority queue). */
    Optional<ServiceRequest> assignNext(String actorId, String staffUserId);

    /** Staff only. Allowed transitions only; use assign to move a request to ASSIGNED. */
    ServiceRequest changeStatus(String actorId, String requestId, RequestStatus newStatus);

    Optional<ServiceRequest> findRequest(String requestId);

    /** All requests in natural order: highest priority first, then oldest. */
    List<ServiceRequest> listRequests();

    List<ServiceRequest> listRequests(Comparator<? super ServiceRequest> order);

    List<ServiceRequest> listByStatus(RequestStatus status);

    List<ServiceRequest> listByAssignee(String staffUserId);

    List<ServiceRequest> listForStudent(String studentId);

    /** A sorted snapshot of the SUBMITTED requests waiting to be assigned, most urgent first. */
    List<ServiceRequest> pendingQueue();
}
