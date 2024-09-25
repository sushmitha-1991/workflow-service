/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dsp.workflow.entity.ApplicationAuthority;

/**
 * 
 */
@Repository
public interface ApplicationAuthorityRepository extends JpaRepository<ApplicationAuthority, Long>{

}
