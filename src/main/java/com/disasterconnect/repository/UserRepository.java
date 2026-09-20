package com.disasterconnect.repository;

import com.disasterconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findAllByOrderByIdAsc();

    List<User> findByRoleIgnoreCase(String role);
}