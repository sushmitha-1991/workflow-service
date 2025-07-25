/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dsp.workflow.entity;


import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "input_workflow_history", schema = "workflow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InputWorkflowHistory extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "application_id", nullable = false)
	private Long applicationId;

	@Column(name = "application_record_id", nullable = false)
	private Long applicationRecordId;

	@Column(name = "action_taken")
	private String actionTaken;

	@Column(name = "status")
	private String status;

	@Column(name = "action_taken_by")
	private Long actionTakenBy;

	@Column(name = "action_taken_by_name")
	private String actionTakenByName;

	@Column(name = "action_taken_on")
	private Timestamp actionTakenOn;

	@Column(name = "pending_with")
	private Long pendingWith;

	@Column(name = "pending_with_name")
	private String pendingWithName;

	@Column(name = "comment_for_display")
	private String commentForDisplay;

	@Column(name = "note", columnDefinition = "TEXT")
	private String note;
	
	@Column(name = "ip_address") 
	private String ipAddress;
	
	@Column(name = "input_instance_id") 
	private Long inputInstanceId;
}
