///**
// * 
// * @author Mrinal Lakhanpal
// * @version 1.0
// */
//package com.dsp.workflow.entity;
//
//import java.util.Date;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
///**
// * 
// */
//@Entity
//@Table(name = "tasks")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class TaskEntity {
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Long id;
//
//	private String camundaTaskId;
//	private String name;
//	private String assignee;
//	private Date created;
//
//	private Long senderRoleId;
//}
