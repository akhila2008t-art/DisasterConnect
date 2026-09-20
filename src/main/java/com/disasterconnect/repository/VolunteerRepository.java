package com.disasterconnect.repository;

import com.disasterconnect.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VolunteerRepository
        extends JpaRepository<Volunteer, Long> {
long countByStatus(String status);
    Optional<Volunteer> findByEmail(String email);

    List<Volunteer> findByStatus(String status);

    List<Volunteer> findByAvailability(String availability);

    List<Volunteer> findByLocationContainingIgnoreCase(String location);
}
