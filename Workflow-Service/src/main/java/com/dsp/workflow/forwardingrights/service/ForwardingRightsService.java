/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.forwardingrights.service;

import java.util.List;
import java.util.Map;

import com.dsp.workflow.forwardingrights.dto.ForwardingRightsDTO;
import com.dsp.workflow.response.GenericResponse;

/**
 * 
 */
public interface ForwardingRightsService {
	GenericResponse<ForwardingRightsDTO> createForwardingRight(ForwardingRightsDTO dto);

	GenericResponse<ForwardingRightsDTO> updateForwardingRight(Long id, ForwardingRightsDTO dto);

	GenericResponse<ForwardingRightsDTO> getForwardingRightById(Long id);

	GenericResponse<List<ForwardingRightsDTO>> getAllForwardingRights();

	GenericResponse<String> deleteForwardingRight(Long id);

	/**
	 * @param applicationId
	 * @param senderRoleId
	 * @return
	 */
	List<Map<String, Object>> getUserDetails(Long applicationId, Long senderRoleId);

}