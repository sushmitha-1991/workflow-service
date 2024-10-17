/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dsp.workflow.entity.ApplicationInstance;

/**
 * 
 */
@Repository
public interface ApplicationInstanceRepository extends JpaRepository<ApplicationInstance, Long> {

}
