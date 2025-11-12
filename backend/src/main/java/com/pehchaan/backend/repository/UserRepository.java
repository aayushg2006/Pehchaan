package com.pehchaan.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.pehchaan.backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their phone number.
     * Spring Data JPA automatically creates the query for this method.
     *
     * @param phone The phone number to search for.
     * @return An Optional containing the User if found, or an empty Optional if not.
     */
    Optional<User> findByPhone(String phone);
}