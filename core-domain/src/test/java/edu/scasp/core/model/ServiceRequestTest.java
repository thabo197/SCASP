package edu.scasp.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.scasp.core.exception.InvalidOperationException;
import edu.scasp.core.exception.ValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import org.junit.jupiter.api.Test;

class ServiceRequestTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    private static ServiceRequest request(String id, RequestPriority priority, long secondsAfterT0) {
        return ServiceRequest.submitted(id, "S1", RequestCategory.TRANSCRIPT, priority, T0.plusSeconds(secondsAfterT0));
    }

    @Test
    void newRequest_isSubmittedAndUnassigned() {
        ServiceRequest r = request("R1", RequestPriority.NORMAL, 0);
        assertEquals(RequestStatus.SUBMITTED, r.status());
        assertNull(r.assignedTo());
        assertTrue(r.assignee().isEmpty());
    }

    @Test
    void naturalOrder_isHighestPriorityThenOldestThenId() {
        List<ServiceRequest> list = new ArrayList<>(List.of(
                request("R1", RequestPriority.NORMAL, 0),
                request("R2", RequestPriority.URGENT, 20),
                request("R3", RequestPriority.URGENT, 10),
                request("R4", RequestPriority.HIGH, 5),
                request("R5", RequestPriority.URGENT, 10)));
        Collections.sort(list);
        assertEquals(List.of("R3", "R5", "R2", "R4", "R1"), list.stream().map(ServiceRequest::requestId).toList());
    }

    @Test
    void priorityQueue_pollsMostUrgentFirst() {
        PriorityQueue<ServiceRequest> queue = new PriorityQueue<>();
        queue.add(request("R1", RequestPriority.LOW, 0));
        queue.add(request("R2", RequestPriority.URGENT, 5));
        queue.add(request("R3", RequestPriority.HIGH, 1));
        assertEquals("R2", queue.poll().requestId());
        assertEquals("R3", queue.poll().requestId());
        assertEquals("R1", queue.poll().requestId());
    }

    @Test
    void equalsAndHashCode_useTheRequestId() {
        ServiceRequest a = request("R1", RequestPriority.LOW, 0);
        ServiceRequest b = a.assignTo("U-STAFF", T0.plusSeconds(1));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, request("R2", RequestPriority.LOW, 0));
    }

    @Test
    void assignTo_movesSubmittedToAssignedAndRecordsTheAssignee() {
        ServiceRequest assigned = request("R1", RequestPriority.LOW, 0).assignTo("U-STAFF", T0.plusSeconds(5));
        assertEquals(RequestStatus.ASSIGNED, assigned.status());
        assertEquals("U-STAFF", assigned.assignedTo());
        assertEquals(T0.plusSeconds(5), assigned.updatedAt());
    }

    @Test
    void fullLifecycle_followsTheAllowedTransitions() {
        ServiceRequest r = request("R1", RequestPriority.LOW, 0).assignTo("U-STAFF", T0.plusSeconds(1));
        r = r.withStatus(RequestStatus.IN_PROGRESS, T0.plusSeconds(2));
        r = r.withStatus(RequestStatus.RESOLVED, T0.plusSeconds(3));
        r = r.withStatus(RequestStatus.CLOSED, T0.plusSeconds(4));
        assertEquals(RequestStatus.CLOSED, r.status());
    }

    @Test
    void illegalTransitions_areRejected() {
        ServiceRequest submitted = request("R1", RequestPriority.LOW, 0);
        assertThrows(InvalidOperationException.class,
                () -> submitted.withStatus(RequestStatus.RESOLVED, T0.plusSeconds(1)));
        assertThrows(InvalidOperationException.class,
                () -> submitted.withStatus(RequestStatus.ASSIGNED, T0.plusSeconds(1)));

        ServiceRequest inProgress = submitted.assignTo("U-STAFF", T0.plusSeconds(1))
                .withStatus(RequestStatus.IN_PROGRESS, T0.plusSeconds(2));
        assertThrows(InvalidOperationException.class, () -> inProgress.assignTo("U-OTHER", T0.plusSeconds(3)));
    }

    @Test
    void rejectedAndClosed_areFinal() {
        ServiceRequest rejected = request("R1", RequestPriority.LOW, 0)
                .withStatus(RequestStatus.REJECTED, T0.plusSeconds(1));
        assertThrows(InvalidOperationException.class,
                () -> rejected.withStatus(RequestStatus.CLOSED, T0.plusSeconds(2)));
    }

    @Test
    void updatedBeforeCreated_isRejected() {
        assertThrows(ValidationException.class, () -> new ServiceRequest("R1", "S1", RequestCategory.OTHER,
                RequestPriority.LOW, RequestStatus.SUBMITTED, null, T0, T0.minusSeconds(1)));
    }

    @Test
    void comparators_orderByCreatedCategoryAndStatus() {
        ServiceRequest a = new ServiceRequest("R1", "S1", RequestCategory.OTHER, RequestPriority.LOW,
                RequestStatus.SUBMITTED, null, T0.plusSeconds(3), T0.plusSeconds(3));
        ServiceRequest b = new ServiceRequest("R2", "S1", RequestCategory.TRANSCRIPT, RequestPriority.URGENT,
                RequestStatus.ASSIGNED, "U", T0.plusSeconds(2), T0.plusSeconds(2));
        ServiceRequest c = new ServiceRequest("R3", "S1", RequestCategory.CLEARANCE, RequestPriority.HIGH,
                RequestStatus.SUBMITTED, null, T0.plusSeconds(1), T0.plusSeconds(1));
        List<ServiceRequest> all = List.of(a, b, c);
        assertEquals(List.of("R3", "R2", "R1"),
                all.stream().sorted(ServiceRequestComparators.BY_CREATED).map(ServiceRequest::requestId).toList());
        assertEquals(List.of("R2", "R3", "R1"),
                all.stream().sorted(ServiceRequestComparators.BY_CATEGORY_THEN_CREATED)
                        .map(ServiceRequest::requestId).toList());
        assertEquals(List.of("R3", "R1", "R2"),
                all.stream().sorted(ServiceRequestComparators.BY_STATUS_THEN_PRIORITY)
                        .map(ServiceRequest::requestId).toList());
    }
}
