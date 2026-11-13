package edu.scasp.core.report;

import edu.scasp.core.model.RequestCategory;
import edu.scasp.core.model.RequestStatus;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Counts of service requests. All maps are unmodifiable copies. */
public record RequestWorkloadReport(Map<RequestStatus, Long> byStatus, Map<RequestCategory, Long> byCategory,
                                    Map<String, Long> openByAssignee, long unassigned) {

    public RequestWorkloadReport {
        byStatus = Collections.unmodifiableMap(new LinkedHashMap<>(byStatus));
        byCategory = Collections.unmodifiableMap(new LinkedHashMap<>(byCategory));
        openByAssignee = Collections.unmodifiableMap(new LinkedHashMap<>(openByAssignee));
    }
}
