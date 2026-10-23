package com.example.demo.repository;

import com.example.demo.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Integer>, JpaSpecificationExecutor<AppUser> {
    Optional<AppUser> findByUsername(String username);
}
