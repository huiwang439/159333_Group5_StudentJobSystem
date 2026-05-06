package com.group5.jobboard.repository;

import com.group5.jobboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findByRole(String role);
    long countByRole(String role);

    List<User> findByAccountStatus(String accountStatus);

    List<User> findByRoleAndAccountStatus(String role, String accountStatus);

    List<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String fullName, String email);

    List<User> findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
            String role1,
            String fullName,
            String role2,
            String email
    );
}