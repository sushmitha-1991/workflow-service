package com.dsp.workflow.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dsp.workflow.dto.DocumentMetadataDTO;
import com.dsp.workflow.entity.DraftDocument;
import com.dsp.workflow.entity.InputWorkflowHistory;
import com.dsp.workflow.entity.WorkflowHistory;
import com.dsp.workflow.mapper.WorkflowHistoryMapper;
import com.dsp.workflow.repository.DraftDocumentRepository;
import com.dsp.workflow.repository.InputWorkflowRepository;
import com.dsp.workflow.service.InputWorkflowService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InputWorkflowServiceImpl implements InputWorkflowService {

	@Autowired
	private InputWorkflowRepository inputHistoryRepository;

	private final WorkflowHistoryMapper wfHistoryMapper;

	@Autowired
	private DraftDocumentRepository draftDocumentRepository;

	@Override
	public List<WorkflowHistory> getInputHistoryByInputInstanceId(Long inputInstanceId) {

		List<InputWorkflowHistory> historyList = inputHistoryRepository.findByInputInstanceId(inputInstanceId);

		return historyList.stream().map(wfHistoryMapper::toWorkflowHistory).collect(Collectors.toList());
	}

	@Override
	public List<DocumentMetadataDTO> getDocuments(Long inputInstanceId, Long draftId) {

		// case 1 → inputInstanceId available
		if (inputInstanceId != null) {
			InputWorkflowHistory history = inputHistoryRepository
					.findTop1ByInputInstanceIdOrderByCreatedAtDesc(inputInstanceId);

			if (history == null)
				return Collections.emptyList();

			List<Map<String, Object>> docList = history.getReferenceFiles();

			if (docList == null)
				return Collections.emptyList();

			return docList.stream().map(wfHistoryMapper::toDocumentDTO).collect(Collectors.toList());
		}

		// case 2 → draftId available
		if (draftId != null) {
			Optional<DraftDocument> draftOpt = draftDocumentRepository.findById(draftId);
			if (draftOpt.isEmpty())
				return Collections.emptyList();

			DraftDocument draft = draftOpt.get();
			JsonNode draftData = draft.getDraftData();
			if (draftData == null || !draftData.has("attachments"))
				return Collections.emptyList();

			ArrayNode attachments = (ArrayNode) draftData.get("attachments");

			return StreamSupport.stream(attachments.spliterator(), false).map(node -> {
				String url = node.get("url").asText();
				String documentName = node.has("documentName") ? node.get("documentName").asText()
						: extractFileName(url);

				// Prefer documentUuid field, fallback to parsing from URL
				UUID uuid = null;
				if (node.has("documentUuid")) {
					uuid = UUID.fromString(node.get("documentUuid").asText());
				} else {
					uuid = extractUuid(url); // fallback
				}

				return DocumentMetadataDTO.builder().id(null).documentId(documentName).documentMasterId(null)
						.fileSize(null).mimeType(null).uuid(uuid).version(null).filePath(null).refId(null).refType(null)
						.applicationNo(null).build();
			}).collect(Collectors.toList());
		}

		return Collections.emptyList();
	}

	private String extractFileName(String url) {
		try {
			return url.substring(url.lastIndexOf('/') + 1);
		} catch (Exception e) {
			return null;
		}
	}

	private UUID extractUuid(String url) {
		try {
			// last part of URL (uuid before file name)
			String[] parts = url.split("/");
			String candidate = parts[parts.length - 2]; // second last part
			return UUID.fromString(candidate);
		} catch (Exception e) {
			return null;
		}
	}

}
