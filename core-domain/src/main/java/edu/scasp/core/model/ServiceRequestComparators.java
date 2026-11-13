package edu.scasp.core.model;

import java.util.Comparator;

/** Alternative orderings for service requests, in addition to the natural priority order. */
public final class ServiceRequestComparators {

    public static final Comparator<ServiceRequest> BY_CREATED =
            Comparator.<ServiceRequest, java.time.Instant>comparing(ServiceRequest::createdAt)
                    .thenComparing(ServiceRequest::requestId);

    public static final Comparator<ServiceRequest> BY_CATEGORY_THEN_CREATED =
            Comparator.<ServiceRequest, RequestCategory>comparing(ServiceRequest::category)
                    .thenComparing(BY_CREATED);

    public static final Comparator<ServiceRequest> BY_STATUS_THEN_PRIORITY =
            Comparator.<ServiceRequest, RequestStatus>comparing(ServiceRequest::status)
                    .thenComparing(Comparator.<ServiceRequest>naturalOrder());

    private ServiceRequestComparators() {
    }
}
