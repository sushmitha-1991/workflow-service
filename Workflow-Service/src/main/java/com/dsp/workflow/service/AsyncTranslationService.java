/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.dsp.workflow.dto.CorrectedDebatedDTO;
import com.dsp.workflow.dto.DebateTaggingDTO;
import com.dsp.workflow.dto.SectionBulletin2DTO;
import com.dsp.workflow.dto.TranslationInputDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Service
@Slf4j
public class AsyncTranslationService {

	private final RestTemplate asyncRestTemplate;

	@Value("${pdf.upload.url}")
	private String pdfUploadUrl;

	@Value("${translation.input.url}")
	private String translationInputUrl;

	@Value("${corrected.debate.url}")
	private String correctedDebateUrl;

	@Value("${debate.tagging.url}")
	private String debateTaggingUrl;

	@Value("${section.b2.rs.url}")
	private String sectionB2Url;

	@Autowired
	private HttpServletRequest httpRequest;

	public AsyncTranslationService(@Qualifier("asyncRestTemplate") RestTemplate asyncRestTemplate) {
		this.asyncRestTemplate = asyncRestTemplate;
	}

	@Async
	public CompletableFuture<Void> callTranslationAPIAsync(TranslationInputDTO translationInputDTO, String token) {
		try {
			callTranslationAPI(translationInputDTO, token);
			return CompletableFuture.completedFuture(null);
		} catch (Exception e) {
			log.error("Translation API call failed asynchronously", e);
			return CompletableFuture.failedFuture(e);
		}
	}

	// Keep your existing synchronous callTranslationAPI method unchanged
	public void callTranslationAPI(TranslationInputDTO translationInputDTO, String token) {
		try {

			// Upload to OKM is HTML. If OnlyOffice, then no need to upload
			System.out.println("translationInputDTO in callTranslationAPI: " + translationInputDTO);

			if (translationInputDTO.getContentUUID() == null) {

				// Step 1: Call upload-html-english API
				HttpHeaders formHeaders = new HttpHeaders();
				formHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				formHeaders.setBearerAuth(token);

				// Check if startingPageNo is null, if so set to "1"
				String startingPageNo = (translationInputDTO.getStartingPageNo() != null)
						? translationInputDTO.getStartingPageNo().toString()
						: "1";

				MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
				formParams.add("htmlContentEn", translationInputDTO.getInputContent());
				formParams.add("startingPageNo", startingPageNo);
				formParams.add("applicationId", translationInputDTO.getDocumentTypeId().toString());
				formParams.add("locale", "en");

				System.out.println("PARAMS: " + formParams);

				HttpEntity<MultiValueMap<String, String>> formRequest = new HttpEntity<>(formParams, formHeaders);

				System.out.println(">>> upload failing - ");
				ResponseEntity<String> uploadResponse = asyncRestTemplate.postForEntity(pdfUploadUrl, formRequest,
						String.class);

				System.out.println("### upload completed");
				// Parse documentUuid from response
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode responseJson = objectMapper.readTree(uploadResponse.getBody());

				if (!responseJson.has("documentUuid") || responseJson.get("documentUuid").isNull()) {
					throw new RuntimeException("documentUuid not found in upload-html-english response");
				}

				UUID documentUuid = UUID.fromString(responseJson.get("documentUuid").asText());

				System.out.println("### doc - " + documentUuid);

				translationInputDTO.setContentUUID(documentUuid); // directly set it
			}

			// Step 2: Call translation API
			HttpHeaders jsonHeaders = new HttpHeaders();
			jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
			jsonHeaders.setBearerAuth(token);

			HttpEntity<TranslationInputDTO> jsonRequest = new HttpEntity<>(translationInputDTO, jsonHeaders);
			ResponseEntity<String> translationResponse = asyncRestTemplate.postForEntity(translationInputUrl,
					jsonRequest, String.class);

			System.out.println("### Translation API Response: " + translationResponse.getBody());

		} catch (HttpClientErrorException e) {
			System.err.println("HTTP error during Translation API call: " + e.getResponseBodyAsString());
			throw new RuntimeException("Error calling Translation API: " + e.getStatusCode(), e);
		} catch (Exception e) {
			System.err.println("Unexpected error during translation process.");
			e.printStackTrace();
			throw new RuntimeException("Translation API call failed.", e);
		}
	}

	// ------------------- Corrected Debates API -------------------
	@Async
	public CompletableFuture<Void> callCorrectedDebatesAPIAsync(CorrectedDebatedDTO correctedDebatedDTO, String token) {
		try {
			callCorrectedDebatesAPI(correctedDebatedDTO, token);
			return CompletableFuture.completedFuture(null);
		} catch (Exception e) {
			log.error("Corrected Debates API call failed asynchronously", e);
			return CompletableFuture.failedFuture(e);
		}
	}

	public void callCorrectedDebatesAPI(CorrectedDebatedDTO correctedDebatedDTO, String token) {
		try {
			HttpHeaders formHeaders = new HttpHeaders();
			formHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			formHeaders.setBearerAuth(token);

			String startingPageNo = "1";

			MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
			formParams.add("htmlContentEn", correctedDebatedDTO.getInputContent());
			formParams.add("startingPageNo", startingPageNo);
			formParams.add("applicationId", correctedDebatedDTO.getDocumentTypeId().toString());
			formParams.add("locale", "en");
			formParams.add("applicationAbbr", "CORRECTED_DEBATES_ABBR");

			HttpEntity<MultiValueMap<String, String>> formRequest = new HttpEntity<>(formParams, formHeaders);
			ResponseEntity<String> uploadResponse = asyncRestTemplate.postForEntity(pdfUploadUrl, formRequest,
					String.class);

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode responseJson = objectMapper.readTree(uploadResponse.getBody());
			if (!responseJson.has("documentUuid") || responseJson.get("documentUuid").isNull()) {
				throw new RuntimeException("documentUuid not found in upload-html-english response");
			}

			correctedDebatedDTO.setContentUUID(responseJson.get("documentUuid").asText());

			// Step 2: Call corrected debates API
			HttpHeaders jsonHeaders = new HttpHeaders();
			jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
			jsonHeaders.setBearerAuth(token);

			HttpEntity<CorrectedDebatedDTO> jsonRequest = new HttpEntity<>(correctedDebatedDTO, jsonHeaders);
			ResponseEntity<String> correctedResponse = asyncRestTemplate.postForEntity(correctedDebateUrl, jsonRequest,
					String.class);

			System.out.println("### Corrected Debates API Response: " + correctedResponse.getBody());

		} catch (Exception e) {
			log.error("Corrected Debates API call failed.", e);
			throw new RuntimeException("Corrected Debates API call failed.", e);
		}
	}

	@Async
	public CompletableFuture<Void> callDebateTaggingAsync(DebateTaggingDTO debateTaggingDTO, String token) {
		try {
			callDebateTagging(debateTaggingDTO, token);
			return CompletableFuture.completedFuture(null);
		} catch (Exception e) {
			log.error("Debate Tagging API call failed asynchronously", e);
			return CompletableFuture.failedFuture(e);
		}
	}

	private void callDebateTagging(DebateTaggingDTO debateTaggingDTO, String token) {
		try {

			// Step 2: Call translation API
			HttpHeaders jsonHeaders = new HttpHeaders();
			jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
			jsonHeaders.setBearerAuth(token);
			HttpEntity<DebateTaggingDTO> jsonRequest = new HttpEntity<>(debateTaggingDTO, jsonHeaders);
			ResponseEntity<String> response = asyncRestTemplate.postForEntity(debateTaggingUrl, jsonRequest,
					String.class);

			System.out.println("### Debate tagging API Response: " + response.getBody());

		} catch (HttpClientErrorException e) {
			System.err.println("HTTP error during Debates Tagging API call: " + e.getResponseBodyAsString());
			throw new RuntimeException("Error calling Debates Tagging API: " + e.getStatusCode(), e);
		} catch (Exception e) {
			System.err.println("Unexpected error during debates tagging process.");
			e.printStackTrace();
			throw new RuntimeException("Debates Tagging API call failed.", e);
		}
	}

	@Async
	public CompletableFuture<Void> callSectionB2RSAsync(List<SectionBulletin2DTO> sectionBulletin2DTOs, String token) {
		try {
			callSectionB2RS(sectionBulletin2DTOs, token);
			return CompletableFuture.completedFuture(null);
		} catch (Exception e) {
			log.error("Debate Tagging API call failed asynchronously", e);
			return CompletableFuture.failedFuture(e);
		}
	}

	private void callSectionB2RS(List<SectionBulletin2DTO> sectionBulletin2DTOs, String token) {
		try {

			HttpHeaders jsonHeaders = new HttpHeaders();
			jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
			jsonHeaders.setBearerAuth(token);

			HttpEntity<List<SectionBulletin2DTO>> jsonRequest = new HttpEntity<>(sectionBulletin2DTOs, jsonHeaders);

			ResponseEntity<String> response = asyncRestTemplate.postForEntity(sectionB2Url, jsonRequest, String.class);

			System.out.println("### Section B2 API Response: " + response.getBody());

		} catch (HttpClientErrorException e) {
			System.err.println("HTTP error during Section B2 API call: " + e.getResponseBodyAsString());
			throw new RuntimeException("Error calling Section B2 API: " + e.getStatusCode(), e);
		} catch (Exception e) {
			System.err.println("Unexpected error during Section B2 process.");
			e.printStackTrace();
			throw new RuntimeException("Section B2 API call failed.", e);
		}
	}
}
