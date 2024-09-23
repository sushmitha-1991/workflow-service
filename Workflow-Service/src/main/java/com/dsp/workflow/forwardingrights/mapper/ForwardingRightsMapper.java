/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.forwardingrights.mapper;

import org.springframework.stereotype.Component;

import com.dsp.workflow.forwardingrights.dto.ForwardingRightsDTO;
import com.dsp.workflow.forwardingrights.entity.ForwardingRights;

/**
 * 
 */
@Component
public class ForwardingRightsMapper {

	public ForwardingRights mapToEntity(ForwardingRightsDTO dto) {
		return ForwardingRights.builder().applicationId(dto.getApplicationId()).senderRoleId(dto.getSenderRoleId())
				.receiverRoleId(dto.getReceiverRoleId()).level(dto.getLevel()).build();
	}

	public ForwardingRightsDTO mapToDTO(ForwardingRights entity) {
		return ForwardingRightsDTO.builder().id(entity.getId()).applicationId(entity.getApplicationId())
				.senderRoleId(entity.getSenderRoleId()).receiverRoleId(entity.getReceiverRoleId())
				.level(entity.getLevel()).build();
	}

}
