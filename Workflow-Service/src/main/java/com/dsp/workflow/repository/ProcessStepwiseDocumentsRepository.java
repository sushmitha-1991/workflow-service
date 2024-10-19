/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dsp.workflow.entity.NoteStepwiseDocuments;

@Repository
public interface ProcessStepwiseDocumentsRepository extends JpaRepository<NoteStepwiseDocuments, Long> {
}
