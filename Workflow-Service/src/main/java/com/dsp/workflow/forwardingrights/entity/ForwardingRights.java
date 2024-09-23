/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.forwardingrights.entity;

import com.dsp.workflow.entity.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = false)
@Table(name = "forwarding_rights", schema = "workflow")
public class ForwardingRights extends Auditable {

	@Id
	@SequenceGenerator(name = "forwarding_rights_seq", sequenceName = "workflow.forwarding_rights_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forwarding_rights_seq")
	private Long id;

	@Column(name = "application_id", nullable = false)
	private Long applicationId;

	@Column(name = "sender_role_id", nullable = false)
	private int senderRoleId;

	@Column(name = "receiver_role_id", nullable = false)
	private int receiverRoleId;

	@Column(name = "level", nullable = false, length = 10)
	private String level;

}