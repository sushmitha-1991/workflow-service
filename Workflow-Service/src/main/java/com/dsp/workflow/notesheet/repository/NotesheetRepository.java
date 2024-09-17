/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.notesheet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dsp.workflow.notesheet.entity.Notesheet;

public interface NotesheetRepository extends JpaRepository<Notesheet, Long> {
}
