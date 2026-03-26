package com.dsp.workflow.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dsp.workflow.dto.PublishDto;
import com.dsp.workflow.dto.PublishHistoryBranchViewResponseDTO;
import com.dsp.workflow.dto.PublishHistoryDTO;
import com.dsp.workflow.dto.PublishHistoryResponseDTO;
import com.dsp.workflow.dto.ResponseDTO;
import com.dsp.workflow.dto.ResponseDocsUuids;
import com.dsp.workflow.dto.ResponseMember;
import com.dsp.workflow.dto.SessionDto;
import com.dsp.workflow.dto.UnPublishRequest;
import com.dsp.workflow.dto.UrlDto;
import com.dsp.workflow.response.GenericResponse;
import com.dsp.workflow.service.PublishService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/publish-history")
public class PublishController {

	@Autowired
	private PublishService publishService;

	@Autowired
	private HttpServletRequest httpRequest;

	@PostMapping("/create")
	public PublishHistoryDTO save(@RequestBody PublishHistoryDTO dto) {
		if (dto == null) {
			throw new RuntimeException("DTO is NULL. JSON payload might be incorrect.");
		}
		PublishHistoryDTO responseDto = publishService.save(dto);
		return responseDto;
	}

	@GetMapping("/get-published-docs")
	public ResponseEntity<GenericResponse<List<PublishHistoryResponseDTO>>> getPublishedDocs(
			@RequestParam(required = false) Long applicationId, 
			@RequestParam(required = false) String lokSabhaNo,
			@RequestParam(required = false) String lokSabhaSessionNo,
			@RequestParam(required = false) String rajyaSabhaSessionNo, 
			@RequestParam(required = false) Long officeId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime publishDateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime publishDateTo,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate documentDateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate documentDateTo,
			@RequestParam(required = false) String applicationName) {
		try {
			List<PublishHistoryResponseDTO> result = publishService.getFilteredPublishHistory(applicationId, lokSabhaNo,
					lokSabhaSessionNo, rajyaSabhaSessionNo, officeId, publishDateFrom, publishDateTo, documentDateFrom,
					documentDateTo, applicationName);

			return ResponseEntity
					.ok(new GenericResponse<>(true, "Records retrieved successfully", result, HttpStatus.OK.value()));

		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericResponse<>(false,
					"An error occurred: " + ex.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR.value()));
		}
	}

	@GetMapping("/get-docs")
	public ResponseEntity<GenericResponse<List<ResponseDTO>>> getPublishedDocsByAppId(
			@RequestParam(required = false) Long appId, @RequestParam(required = false) String abbr,
			@RequestParam(required = false) String lsNo, @RequestParam(required = false) String sesNo,
			@RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) LocalDate toDate) {
		List<ResponseDTO> responseDto = publishService.getDocsByAppId(appId, abbr, lsNo, sesNo, fromDate, toDate);

		if (responseDto.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		GenericResponse<List<ResponseDTO>> response = new GenericResponse<>(true, "Record found", responseDto,
				HttpStatus.OK.value());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/getAll/publishedDocs")
	public ResponseEntity<List<UrlDto>> getPublishedDocsFromDSP1(@RequestParam(required = false) LocalDate date,
			@RequestParam(required = false) String applicationName, @RequestParam(required = false) String locale,
			@RequestParam(required = false) String lsNo, @RequestParam(required = false) String sessNo,
			@RequestParam(required = false) String rssNo, @RequestParam(required = false) String house,
			@RequestParam(required = false) Integer month,@RequestParam(required = false) Integer year) {
		List<UrlDto> responseDto = publishService.getPublishedDocsFromDSP1(date, applicationName, locale, lsNo, sessNo,
				rssNo, house,month,year);
		return ResponseEntity.ok(responseDto);
	}

	@GetMapping("/getAll/dates")
	public ResponseEntity<List<String>> getPublishedDocsFromDSP1Dates(@RequestParam(required = false) LocalDate date,
			@RequestParam(required = false) String applicationName, @RequestParam(required = false) String locale,
			@RequestParam(required = false) String lsNo, @RequestParam(required = false) String sessNo,
			@RequestParam(required = false) String rssNo, @RequestParam(required = false) String house,
			@RequestParam(required = false) Integer month,@RequestParam(required = false) Integer year) {
		List<String> responseDto = publishService.getPublishedDocsFromDSP1Dates(date, applicationName, locale, lsNo, sessNo,
				rssNo, house,month,year);
		return ResponseEntity.ok(responseDto);
	}

	@GetMapping("/getAll/publishedDocs")
	public ResponseEntity<List<UrlDto>> getPublishedDocsFromDSP1V2(@RequestParam(required = false) LocalDate date,
			@RequestParam(required = false) String applicationName, @RequestParam(required = false) String locale,
			@RequestParam(required = false) String lsNo, @RequestParam(required = false) String sessNo,
			@RequestParam(required = false) String rssNo, @RequestParam(required = false) String house,
			@RequestParam(required=false) Integer month,Integer year) {
		List<UrlDto> responseDto = publishService.getPublishedDocsFromDSP1(date, applicationName, locale, lsNo, sessNo,
				rssNo, house,month,year);
		return ResponseEntity.ok(responseDto);
	}

	@PostMapping("/getAll/unpublishedDocs")
	public ResponseEntity<List<UrlDto>> getAllUnpublished(@RequestParam(required = false) LocalDate date,
			@RequestParam(required = false) String applicationName, @RequestParam(required = false) String locale) {
		List<UrlDto> responseDto = publishService.getAllUnpublished(date, applicationName, locale);
		return ResponseEntity.ok(responseDto); // Returns 200 OK with the list
	}

	@GetMapping("/getAll/unpublishedDocs")
	public ResponseEntity<List<UrlDto>> getAllUnpublishedV2(@RequestParam(required = false) LocalDate date,
			@RequestParam(required = false) String applicationName, @RequestParam(required = false) String locale) {
		List<UrlDto> responseDto = publishService.getAllUnpublished(date, applicationName, locale);
		// if (responseDto.isEmpty()) {
		// return ResponseEntity.noContent().build(); // Returns 204 No Content if the
		// list is empty
		// }
		return ResponseEntity.ok(responseDto); // Returns 200 OK with the list
	}

	@PostMapping("/getAll/publishedDoc")
	public UrlDto getByDateDoc(@RequestParam(required = false) LocalDate date,
			@RequestParam(required = false) String applicationName, @RequestParam(required = false) String locale,
			@RequestParam(required = false) String lsNo, @RequestParam(required = false) String sesNo,
			@RequestParam(required = false) String rsNo

	) {
		UrlDto responseDto = publishService.getByDateDoc(date, applicationName, locale);
		// if (responseDto.isEmpty()) {
		// return ResponseEntity.noContent().build();
		// }
		return responseDto;
	}

	@GetMapping("/getAll/publishedDoc")
	public UrlDto getByDateDocV2(@RequestParam(required = false) LocalDate date,
			@RequestParam(required = false) String applicationName, @RequestParam(required = false) String locale,
			@RequestParam(required = false) String lsNo, @RequestParam(required = false) String sesNo,
			@RequestParam(required = false) String rsNo

	) {
		UrlDto responseDto = publishService.getByDateDoc(date, applicationName, locale);
		// if (responseDto.isEmpty()) {
		// return ResponseEntity.noContent().build();
		// }
		return responseDto;
	}

	@PostMapping("/getAll/docBySession")
	public UrlDto getBySession(@RequestParam(required = false) LocalDate date,
			@RequestParam(required = false) String applicationName, @RequestParam(required = false) String lsNo,
			@RequestParam(required = false) String sessNo, @RequestParam(required = false) String rsNo,
			@RequestParam(required = false) String locale) {
		UrlDto responseDto = publishService.getBySession(date, lsNo, sessNo, rsNo, applicationName, locale);
		// if (responseDto.isEmpty()) {
		// return ResponseEntity.noContent().build();
		// }
		return responseDto;
	}

	@PostMapping("/getAll/unpublishedDoc")
	public UrlDto getAllUnpublishedDoc(@RequestParam(required = false) LocalDate date,
			@RequestParam(required = false) String applicationName, @RequestParam(required = false) String locale) {
		UrlDto responseDto = publishService.getAllUnpublishedDoc(date, applicationName, locale);
		// if (responseDto.isEmpty()) {
		// return ResponseEntity.noContent().build(); // Returns 204 No Content if the
		// list is empty
		// }
		return responseDto; // Returns 200 OK with the list
	}

	@GetMapping("/getAll/docBySession")
	public UrlDto getBySessionV2(@RequestParam(required = false) LocalDate date,
			@RequestParam(required = false) String applicationName, @RequestParam(required = false) String lsNo,
			@RequestParam(required = false) String sessNo, @RequestParam(required = false) String rsNo,
			@RequestParam(required = false) String locale) {
		UrlDto responseDto = publishService.getBySession(date, lsNo, sessNo, rsNo, applicationName, locale);
		// if (responseDto.isEmpty()) {
		// return ResponseEntity.noContent().build();
		// }
		return responseDto;
	}

	@GetMapping("/getAll/unpublishedDoc")
	public UrlDto getAllUnpublishedDocV2(@RequestParam(required = false) LocalDate date,
			@RequestParam(required = false) String applicationName, @RequestParam(required = false) String locale) {
		UrlDto responseDto = publishService.getAllUnpublishedDoc(date, applicationName, locale);
		// if (responseDto.isEmpty()) {
		// return ResponseEntity.noContent().build(); // Returns 204 No Content if the
		// list is empty
		// }
		return responseDto; // Returns 200 OK with the list
	}

	@PutMapping("/update/{id}")
	public PublishHistoryDTO update(@PathVariable Long id, @RequestBody PublishHistoryDTO dto) {
		if (id == null) {
			throw new RuntimeException("id is NULL");
		}
		PublishHistoryDTO responseDto = publishService.update(id, dto);
		return responseDto;
	}

	@PutMapping("/unpublish/{id}")
	public ResponseEntity<GenericResponse<PublishHistoryDTO>> unpublishById(@PathVariable Long id,
			@RequestBody UnPublishRequest request) {
		if (id == null) {
			throw new RuntimeException("id is NULL");
		}
		PublishHistoryDTO responseDto = publishService.updateById(id, request);
		if (responseDto == null) {
			return ResponseEntity.noContent().build();
		}
		GenericResponse<PublishHistoryDTO> response = new GenericResponse<>(true, "Record unpublished", responseDto,
				HttpStatus.OK.value());

		return ResponseEntity.ok(response);
	}

	@PostMapping("/update/{id}")
	public PublishHistoryDTO updateV2(@PathVariable Long id, @RequestBody PublishHistoryDTO dto) {
		if (id == null) {
			throw new RuntimeException("id is NULL");
		}
		PublishHistoryDTO responseDto = publishService.update(id, dto);
		return responseDto;
	}

	@PostMapping("/unpublish/{id}")
	public ResponseEntity<GenericResponse<PublishHistoryDTO>> unpublishByIdV2(@PathVariable Long id,
			@RequestBody UnPublishRequest request) {
		if (id == null) {
			throw new RuntimeException("id is NULL");
		}
		PublishHistoryDTO responseDto = publishService.updateById(id, request);
		if (responseDto == null) {
			return ResponseEntity.noContent().build();
		}
		GenericResponse<PublishHistoryDTO> response = new GenericResponse<>(true, "Record unpublished", responseDto,
				HttpStatus.OK.value());

		return ResponseEntity.ok(response);
		// return responseDto;
	}

	@GetMapping("/getByApplicationNo/{applicationNo}")
	public PublishHistoryDTO getbyAppNo(@PathVariable String applicationNo) {
		if (applicationNo == null) {
			throw new RuntimeException("ApplicationNo is NULL.");
		}
		PublishHistoryDTO response = publishService.getbyAppNo(applicationNo);
		return response;
	}

	@PostMapping("/getAll/docs")
	public List<UrlDto> getAllDocs(@RequestParam(required = false) LocalDate date,
			@RequestParam(required = false) String applicationName, @RequestParam(required = false) String locale) {
		List<UrlDto> responseDto = publishService.getAllDocs(date, applicationName, locale);
		return responseDto;
	}

	@PostMapping("/uploadDocDSP2/{appName}")
	public ResponseEntity<List<String>> uploadToDsp2(@PathVariable String appName, @RequestBody SessionDto dto) {
		List<String> uuidString = publishService.uploadToDsp2(appName, dto);
		return ResponseEntity.ok(uuidString);
	}

	@PostMapping("/uploadDocDSP2/rs/{appName}")
	public ResponseEntity<List<String>> uploadToDsp2Rs(@PathVariable String appName, @RequestBody SessionDto dto) {
		List<String> uuidString = publishService.uploadToDsp2Rs(appName, dto);
		return ResponseEntity.ok(uuidString);
	}

	@GetMapping("/uploadMemberDSP2")
	public ResponseEntity<List<ResponseMember>> uploadMembersToDsp2() {
		List<ResponseMember> res = publishService.uploadMemberDSP2();
		return ResponseEntity.ok(res);
	}

	@GetMapping("/uploadTransmissionDsp2")
	public ResponseEntity<List<ResponseDocsUuids>> uploadTransmissionDocs() {
		List<ResponseDocsUuids> res = publishService.uploadTransmissionDocs();
		return ResponseEntity.ok(res);
	}

	@GetMapping("/create-folder-structure")
	public ResponseEntity<String> folderStructure(@RequestParam(required = false) String houseType,
			@RequestParam(required = false) String lsNo, @RequestParam(required = false) String sessionNo,
			@RequestParam(required = false) String appName) {

		List<String> folders = new ArrayList<String>();
		if (!StringUtils.isEmpty(houseType)) {
			folders.add(houseType);
		}
		if (!StringUtils.isEmpty(lsNo)) {
			folders.add(lsNo);
		}
		if (!StringUtils.isEmpty(sessionNo)) {
			folders.add(sessionNo);
		}
		folders.add(appName);
		String folder = publishService.createDmsFolderStructure("/okm:root", folders);

		return ResponseEntity.ok(folder);
	}

	public String getTokenFromRequest() {

		String authorizationHeader = httpRequest.getHeader("Authorization");
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			return authorizationHeader.substring(7);
		}
		return null;
	}

	@PostMapping("/override-publish")
	public ResponseEntity<GenericResponse<String>> overridePublish(@RequestBody PublishDto dto) {

		String message = publishService.overridePublish(dto);

		GenericResponse<String> response = new GenericResponse<>(true, message, null, HttpStatus.OK.value());

		return ResponseEntity.ok(response);
	}

	 @GetMapping("/search")
	    public ResponseEntity<GenericResponse<Page<PublishHistoryBranchViewResponseDTO>>> search(
	            @RequestParam(required = false) String lokSabhaNo,
	            @RequestParam(required = false) String sessionNo,
	            @RequestParam(required = false)
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	            LocalDate fromDate,
	            @RequestParam(required = false)
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	            LocalDate toDate,
	            @RequestParam(required = false) Long applicationId,
	            @RequestParam(required = false) String locale,
	            @RequestParam(defaultValue = "0") int page,
	            @RequestParam(defaultValue = "10") int size,
	            @RequestParam(required = false)String keyword
	    ) {

			Page<PublishHistoryBranchViewResponseDTO> response = publishService.searchPublishHistory(lokSabhaNo,
					sessionNo, fromDate,toDate, applicationId,locale, page, size,keyword);

	        return (!response.isEmpty())? ResponseEntity.ok(
	                new GenericResponse<>(true, "Fetched publish history successfully", response, HttpStatus.OK.value())
	        ):ResponseEntity.ok(
	                new GenericResponse<>(true, "Data Not Found", response, HttpStatus.NO_CONTENT.value())
	        );
	    }
	 
		@GetMapping("/getDocuments")
		public ResponseEntity<GenericResponse<Page<PublishHistoryBranchViewResponseDTO>>> getDocumentsPublishHistory(
				@RequestParam(required = false) String lokSabhaNo, @RequestParam(required = false) String sessionNo,
				@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
				@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
				@RequestParam(required = false) String applicationAbbr,@RequestParam(required = false) String locale,
				@RequestParam(defaultValue = "0") int page,
				@RequestParam(defaultValue = "100") int size, @RequestParam(required = false) String keyword) {

			Page<PublishHistoryBranchViewResponseDTO> response = publishService.getDocumentsPublishHistory(lokSabhaNo,
					sessionNo, fromDate, toDate, applicationAbbr,locale, page, size, keyword);

			return (!response.isEmpty())
					? ResponseEntity.ok(new GenericResponse<>(true, "Fetched publish history successfully", response,
							HttpStatus.OK.value()))
					: ResponseEntity
							.ok(new GenericResponse<>(true, "Data Not Found", response, HttpStatus.NO_CONTENT.value()));
		}
}
