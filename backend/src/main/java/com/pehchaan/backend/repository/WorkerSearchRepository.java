package com.pehchaan.backend.repository;

import com.pehchaan.backend.entity.User;
import com.pehchaan.backend.entity.Role; 
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.locationtech.jts.geom.Point; 
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class WorkerSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<User> searchWorkersBySkill(String skill) {
        String jpql = "SELECT u FROM User u JOIN u.skills s WHERE u.role = :role AND UPPER(s) = UPPER(:skill)";
        TypedQuery<User> query = entityManager.createQuery(jpql, User.class);
        query.setParameter("role", Role.ROLE_LABOR); 
        query.setParameter("skill", skill);
        return query.getResultList();
    }

    /**
     * ✅ FIXED: Switched to ordinal parameters ?1 and ?2.
     * ?1 maps to the first method argument (consumerLocation).
     * ?2 maps to the second method argument (skill).
     */
    @SuppressWarnings("unchecked")
    public List<User> findNearbyAvailableWorkers(Point consumerLocation, String skill) {
        String sql = "SELECT * FROM users u " +
                     "WHERE u.role = 'ROLE_LABOR' " +
                     "AND u.status = 'AVAILABLE' " +
                     "AND EXISTS (SELECT 1 FROM user_skills s WHERE s.user_id = u.id AND UPPER(s.skill) = UPPER(?2)) " +
                     "AND ST_DWithin(u.current_location::geography, ?1::geography, 5000) " +
                     "ORDER BY ST_Distance(u.current_location::geography, ?1::geography) " +
                     "LIMIT 10"; 

        Query query = entityManager.createNativeQuery(sql, User.class);
        query.setParameter(1, consumerLocation); // ✅ ?1
        query.setParameter(2, skill); // ✅ ?2
        
        return query.getResultList();
    }
}