package edu.scasp.core.model;

import java.util.Objects;

/** A campus location (library, registry, clinic). Sample locations are fictional. */
public record CampusService(String serviceId, String name, String description, double latitude,
                            double longitude) {

    public CampusService {
        serviceId = Validate.notBlank("serviceId", serviceId);
        name = Validate.notBlank("name", name);
        description = Validate.notBlank("description", description);
        Validate.inRange("latitude", latitude, -90, 90);
        Validate.inRange("longitude", longitude, -180, 180);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CampusService other && serviceId.equals(other.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId);
    }
}
