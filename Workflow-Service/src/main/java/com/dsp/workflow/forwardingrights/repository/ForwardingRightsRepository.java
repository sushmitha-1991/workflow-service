/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.forwardingrights.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dsp.workflow.forwardingrights.entity.ForwardingRights;

/**
 * 
 */
@Repository
public interface ForwardingRightsRepository extends JpaRepository<ForwardingRights, Long> {
    Optional<ForwardingRights> findByApplicationIdAndSenderRoleId(Long applicationId, Long senderRoleId);
    
    /**
     * Native query to fetch user details based on the role_id from user_roles and users tables.
     * 
     * @param roleId the roleId to filter users
     * @return List of user details (firstName, middleName, lastName, email, officeId)
     */
    @Query(value = "SELECT u.first_name, u.middle_name, u.last_name, u.email, ur.office_id " +
                   "FROM authentication.base_user u " +
                   "JOIN authentication.user_roles ur ON u.id = ur.user_id " +
                   "WHERE ur.role_id = :roleId AND ur.is_active = true AND ur.is_deleted = false", 
           nativeQuery = true)
    List<Object[]> findUserDetailsByRoleId(@Param("roleId") Integer roleId);

}
