package com.dsp.workflow.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.dsp.workflow.entity.BaseUser;
import com.dsp.workflow.repository.MyUserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@Component
public class Utils {

    @Autowired
    private MyUserRepository myUserRepository;

    @Autowired
    private EntityManager entityManager; // For native query execution

    /**
     * Fetches the user ID of the currently authenticated user
     * @return userId
     */
    public Long getUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        String username = authentication.getName();
        BaseUser baseUser = myUserRepository.findByUsername(username);
        if (baseUser == null) {
            throw new UsernameNotFoundException("Authenticated user not found in the database");
        }

        return baseUser.getId();
    }

    /**
     * Fetches the role ID of the authenticated user by joining user_roles and users table
     * @return roleId
     */
    public Long getRoleIdByUserId() {
        Long userId = getUserIdFromAuthentication(); // Get the authenticated user's ID

        String nativeQuery = "SELECT ur.role_id FROM authentication.user_roles ur " +
                             "JOIN authentication.base_user u ON ur.user_id = u.id " +
                             "WHERE u.id = :userId AND ur.is_active = true AND ur.is_deleted = false";
        
        Query query = entityManager.createNativeQuery(nativeQuery);
        query.setParameter("userId", userId);

        try {
            return (Long) query.getSingleResult();
        } catch (Exception e) {
            throw new IllegalStateException("No active role found for the user with ID: " + userId);
        }
    }
}
