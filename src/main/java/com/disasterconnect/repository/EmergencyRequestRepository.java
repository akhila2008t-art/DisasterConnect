package com.disasterconnect.repository;

import com.disasterconnect.entity.EmergencyRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyRequestRepository
        extends JpaRepository<EmergencyRequest, Long> {
                long countByStatus(String status);

long countByStatusIn(java.util.List<String> statuses);
}