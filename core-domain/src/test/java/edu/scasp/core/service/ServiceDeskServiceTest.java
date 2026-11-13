package edu.scasp.core.service;

import static edu.scasp.core.support.Fixture.ADMIN;
import static edu.scasp.core.support.Fixture.STAFF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.scasp.core.exception.EntityNotFoundException;
import edu.scasp.core.exception.InvalidOperationException;
import edu.scasp.core.exception.UnauthorizedOperationException;
import edu.scasp.core.model.RequestCategory;
import edu.scasp.core.model.RequestPriority;
import edu.scasp.core.model.RequestStatus;
import edu.scasp.core.model.ServiceRequest;
import edu.scasp.core.model.ServiceRequestComparators;
import edu.scasp.core.support.Fixture;
import java.util.List;
import org.junit.jupiter.api.Test;

class ServiceDeskServiceTest {

    private static ServiceRequest submit(Fixture f, String student, RequestPriority priority) {
        return f.platform.serviceDesk().submit("U" + student.substring(1), student, RequestCategory.TRANSCRIPT, priority);
    }

    private static List<String> ids(List<ServiceRequest> list) {
        return list.stream().map(ServiceRequest::requestId).toList();
    }

    @Test
    void studentCanSubmitForThemselves_andStaffForAnyStudent() {
        Fixture f = new Fixture(2);
        ServiceRequest own = submit(f, "S1", RequestPriority.NORMAL);
        ServiceRequest byStaff = f.platform.serviceDesk()
                .submit(STAFF, "S2", RequestCategory.CLEARANCE, RequestPriority.LOW);
        assertEquals(RequestStatus.SUBMITTED, own.status());
        assertEquals("S2", byStaff.studentId());
        assertEquals("REQ-0001", own.requestId());
        assertEquals("REQ-0002", byStaff.requestId());
    }

    @Test
    void studentCannotSubmitForAnotherStudent_orForAnUnknownOne() {
        Fixture f = new Fixture(2);
        assertThrows(UnauthorizedOperationException.class, () -> f.platform.serviceDesk()
                .submit("U1", "S2", RequestCategory.OTHER, RequestPriority.LOW));
        assertThrows(EntityNotFoundException.class, () -> f.platform.serviceDesk()
                .submit(STAFF, "S99", RequestCategory.OTHER, RequestPriority.LOW));
        assertTrue(f.platform.serviceDesk().listRequests().isEmpty());
    }

    @Test
    void pendingQueue_isMostUrgentFirstThenOldest() {
        Fixture f = new Fixture(3);
        ServiceRequest normal = submit(f, "S1", RequestPriority.NORMAL);
        ServiceRequest urgentLate = submit(f, "S2", RequestPriority.URGENT);
        ServiceRequest high = submit(f, "S3", RequestPriority.HIGH);
        ServiceRequest urgentLater = submit(f, "S1", RequestPriority.URGENT);
        assertEquals(List.of(urgentLate.requestId(), urgentLater.requestId(), high.requestId(), normal.requestId()),
                ids(f.platform.serviceDesk().pendingQueue()));
    }

    @Test
    void assignNext_takesRequestsInPriorityOrder_thenReturnsEmpty() {
        Fixture f = new Fixture(3);
        submit(f, "S1", RequestPriority.LOW);
        ServiceRequest urgent = submit(f, "S2", RequestPriority.URGENT);
        ServiceRequest high = submit(f, "S3", RequestPriority.HIGH);

        ServiceRequest first = f.platform.serviceDesk().assignNext(STAFF, STAFF).orElseThrow();
        ServiceRequest second = f.platform.serviceDesk().assignNext(STAFF, ADMIN).orElseThrow();
        assertEquals(urgent.requestId(), first.requestId());
        assertEquals(RequestStatus.ASSIGNED, first.status());
        assertEquals(STAFF, first.assignedTo());
        assertEquals(high.requestId(), second.requestId());
        assertEquals(ADMIN, second.assignedTo());
        f.platform.serviceDesk().assignNext(STAFF, STAFF).orElseThrow();
        assertTrue(f.platform.serviceDesk().assignNext(STAFF, STAFF).isEmpty());
    }

    @Test
    void assign_requiresStaff_andAnActiveStaffAssignee() {
        Fixture f = new Fixture(2);
        ServiceRequest request = submit(f, "S1", RequestPriority.NORMAL);
        assertThrows(UnauthorizedOperationException.class,
                () -> f.platform.serviceDesk().assign("U1", request.requestId(), STAFF));
        assertThrows(InvalidOperationException.class,
                () -> f.platform.serviceDesk().assign(STAFF, request.requestId(), "U2"));
        assertThrows(EntityNotFoundException.class,
                () -> f.platform.serviceDesk().assign(STAFF, request.requestId(), "GHOST"));
        assertEquals(RequestStatus.SUBMITTED, f.platform.serviceDesk().findRequest(request.requestId()).orElseThrow().status());
    }

    @Test
    void assign_removesTheRequestFromThePendingQueue() {
        Fixture f = new Fixture(2);
        ServiceRequest a = submit(f, "S1", RequestPriority.HIGH);
        ServiceRequest b = submit(f, "S2", RequestPriority.LOW);
        f.platform.serviceDesk().assign(STAFF, a.requestId(), STAFF);
        assertEquals(List.of(b.requestId()), ids(f.platform.serviceDesk().pendingQueue()));
    }

    @Test
    void assign_aRequestThatIsNotSubmitted_isRejected() {
        Fixture f = new Fixture(1);
        ServiceRequest request = submit(f, "S1", RequestPriority.NORMAL);
        f.platform.serviceDesk().assign(STAFF, request.requestId(), STAFF);
        assertThrows(InvalidOperationException.class,
                () -> f.platform.serviceDesk().assign(STAFF, request.requestId(), ADMIN));
    }

    @Test
    void lifecycle_movesThroughTheAllowedStatuses() {
        Fixture f = new Fixture(1);
        ServiceRequest request = submit(f, "S1", RequestPriority.NORMAL);
        String id = request.requestId();
        f.platform.serviceDesk().assign(STAFF, id, STAFF);
        f.platform.serviceDesk().changeStatus(STAFF, id, RequestStatus.IN_PROGRESS);
        f.platform.serviceDesk().changeStatus(STAFF, id, RequestStatus.RESOLVED);
        ServiceRequest closed = f.platform.serviceDesk().changeStatus(STAFF, id, RequestStatus.CLOSED);
        assertEquals(RequestStatus.CLOSED, closed.status());
        assertEquals(List.of("SUBMIT_REQUEST", "ASSIGN_REQUEST", "CHANGE_REQUEST_STATUS", "CHANGE_REQUEST_STATUS",
                "CHANGE_REQUEST_STATUS"),
                f.platform.auditTrail().eventsFor("ServiceRequest", id).stream().map(e -> e.action()).toList());
    }

    @Test
    void invalidStatusChanges_areRejected_andLeaveTheRequestUnchanged() {
        Fixture f = new Fixture(1);
        ServiceRequest request = submit(f, "S1", RequestPriority.NORMAL);
        String id = request.requestId();
        assertThrows(InvalidOperationException.class,
                () -> f.platform.serviceDesk().changeStatus(STAFF, id, RequestStatus.RESOLVED));
        assertThrows(InvalidOperationException.class,
                () -> f.platform.serviceDesk().changeStatus(STAFF, id, RequestStatus.ASSIGNED));
        assertThrows(UnauthorizedOperationException.class,
                () -> f.platform.serviceDesk().changeStatus("U1", id, RequestStatus.REJECTED));
        assertEquals(RequestStatus.SUBMITTED, f.platform.serviceDesk().findRequest(id).orElseThrow().status());
        assertThrows(EntityNotFoundException.class,
                () -> f.platform.serviceDesk().changeStatus(STAFF, "REQ-9999", RequestStatus.REJECTED));
    }

    @Test
    void rejectingASubmittedRequest_removesItFromTheQueue() {
        Fixture f = new Fixture(2);
        ServiceRequest urgent = submit(f, "S1", RequestPriority.URGENT);
        ServiceRequest low = submit(f, "S2", RequestPriority.LOW);
        f.platform.serviceDesk().changeStatus(STAFF, urgent.requestId(), RequestStatus.REJECTED);
        ServiceRequest next = f.platform.serviceDesk().assignNext(STAFF, STAFF).orElseThrow();
        assertEquals(low.requestId(), next.requestId());
    }

    @Test
    void listings_filterAndSort() {
        Fixture f = new Fixture(2);
        ServiceRequest a = submit(f, "S1", RequestPriority.LOW);
        ServiceRequest b = submit(f, "S2", RequestPriority.URGENT);
        ServiceRequest c = submit(f, "S1", RequestPriority.HIGH);
        f.platform.serviceDesk().assign(STAFF, b.requestId(), ADMIN);

        assertEquals(List.of(b.requestId(), c.requestId(), a.requestId()), ids(f.platform.serviceDesk().listRequests()));
        assertEquals(List.of(a.requestId(), b.requestId(), c.requestId()),
                ids(f.platform.serviceDesk().listRequests(ServiceRequestComparators.BY_CREATED)));
        assertEquals(List.of(c.requestId(), a.requestId()),
                ids(f.platform.serviceDesk().listByStatus(RequestStatus.SUBMITTED)));
        assertEquals(List.of(b.requestId()), ids(f.platform.serviceDesk().listByAssignee(ADMIN)));
        assertEquals(List.of(c.requestId(), a.requestId()), ids(f.platform.serviceDesk().listForStudent("S1")));
    }

    @Test
    void listResults_areUnmodifiable() {
        Fixture f = new Fixture(1);
        submit(f, "S1", RequestPriority.LOW);
        assertThrows(UnsupportedOperationException.class, () -> f.platform.serviceDesk().listRequests().clear());
        assertThrows(UnsupportedOperationException.class, () -> f.platform.serviceDesk().pendingQueue().clear());
        assertEquals(1, f.platform.serviceDesk().pendingQueue().size());
    }

    @Test
    void aNewServiceOverTheSameRepositories_rebuildsThePendingQueue() {
        Fixture f = new Fixture(2);
        ServiceRequest urgent = submit(f, "S1", RequestPriority.URGENT);
        submit(f, "S2", RequestPriority.LOW);
        Platform restarted = Platform.create(f.repos, f.clock);
        assertEquals(2, restarted.serviceDesk().pendingQueue().size());
        assertEquals(urgent.requestId(), restarted.serviceDesk().pendingQueue().get(0).requestId());
    }
}
