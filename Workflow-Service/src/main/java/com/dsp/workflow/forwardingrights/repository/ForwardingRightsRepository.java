/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.forwardingrights.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dsp.workflow.forwardingrights.entity.ForwardingRights;

/**
 * 
 */
@Repository
public interface ForwardingRightsRepository extends JpaRepository<ForwardingRights, Long> {
    Optional<ForwardingRights> findByApplicationIdAndSenderRoleId(Long applicationId, Long senderRoleId);

}
