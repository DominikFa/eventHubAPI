// File: eventHubAPI/src/main/java/com/example/eventhubapi/location/LocationRepository.java
package com.example.eventhubapi.location;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository

public interface LocationRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location> {


}
