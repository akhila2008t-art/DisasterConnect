package com.disasterconnect.repository;

import com.disasterconnect.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository
        extends JpaRepository<Resource, Long> {

    List<Resource> findByCategoryIgnoreCase(String category);

    List<Resource> findByStatusIgnoreCase(String status);

    List<Resource> findByLocationContainingIgnoreCase(
            String location);
}
