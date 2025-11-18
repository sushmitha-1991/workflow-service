package com.dsp.workflow.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.dsp.workflow.dto.ApplicationInstanceProjection;
import com.dsp.workflow.dto.PublishDto;
import com.dsp.workflow.dto.PublishHistoryDTO;
import com.dsp.workflow.dto.PublishHistoryResponseDTO;
import com.dsp.workflow.dto.ResponseDTO;
import com.dsp.workflow.dto.ResponseDocsUuids;
import com.dsp.workflow.dto.ResponseMember;
import com.dsp.workflow.dto.SessionDto;
import com.dsp.workflow.dto.UnPublishRequest;
import com.dsp.workflow.dto.UrlDto;
import com.dsp.workflow.entity.ApplicationInstance;
import com.dsp.workflow.entity.ETransmissionRSUploadTempEntity;
import com.dsp.workflow.entity.ETransmissionUploadTempEntity;
import com.dsp.workflow.entity.MemberPhotoSignTemp;
import com.dsp.workflow.entity.MemberPhotoSignTempRS;
import com.dsp.workflow.entity.PublishDocumentTemp;
import com.dsp.workflow.entity.PublishDocumentTempRS;
import com.dsp.workflow.entity.PublishHistoryEntity;
import com.dsp.workflow.mapper.PublishMapper;
import com.dsp.workflow.repository.ApplicationInstanceRepository;
import com.dsp.workflow.repository.ApplicationRepository;
import com.dsp.workflow.repository.ETransmissionRSRepository;
import com.dsp.workflow.repository.ETransmissionRepository;
import com.dsp.workflow.repository.MemberTempUploadRepository;
import com.dsp.workflow.repository.MemberTempUploadRepositoryRS;
import com.dsp.workflow.repository.PublishDocumentTempRepo;
import com.dsp.workflow.repository.PublishRepository;
import com.dsp.workflow.repository.PublishTempDocsRsRepository;
import com.dsp.workflow.service.Dsp1CacheService;
import com.dsp.workflow.service.PublishService;
import com.dsp.workflow.util.ConstantsUtility;
import com.dsp.workflow.util.ETransmissionDataUpload;
import com.dsp.workflow.util.FolderCreationUtils;
import com.dsp.workflow.util.MemberUploadData;
import com.dsp.workflow.util.Utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
public class PublishServiceImpl implements PublishService {
	@Autowired
	private PublishRepository repository;
	@Autowired
	private PublishTempDocsRsRepository rsDocRepo;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private EntityManager entityManager;
	@Value("${service.url}")
	private String openKmUrl;
	@Autowired
	private ETransmissionRepository eTransmissionRepository;
	@Autowired
	private ETransmissionRSRepository eTransmissionRSRepository;

	// Global set to track already uploaded document URLs
	private final Set<String> processedUrls = new HashSet<>();

	// Regex to detect version suffix like "_v1", "_v12", "_v123" before the
	// extension
	private static final Pattern VERSION_PATTERN = Pattern.compile("_v\\d+(?=\\.[^.]+$)", Pattern.CASE_INSENSITIVE);

	@Autowired
	private ETransmissionDataUpload eTransmissionUtils;
	@Autowired
	private MemberUploadData memberUtils;
	@Autowired
	private FolderCreationUtils folderCreationUtils;
	@Autowired
	private MemberTempUploadRepository memberTempRepo;
	@Autowired
	private MemberTempUploadRepositoryRS memberTempRepoRs;
	@Autowired
	private Utils utils;
	@Autowired
	private PublishDocumentTempRepo tempRepo;
	private static final Logger logger = LoggerFactory.getLogger(PublishServiceImpl.class);
	@Autowired
	private HttpServletRequest httpRequest;
	@Autowired
	private ApplicationInstanceRepository applicationInstanceRepository;
	@Autowired
	private ApplicationRepository appRepository;

	@Autowired
	private Dsp1CacheService dsp1CacheService;

	@Override
	public PublishHistoryDTO save(PublishHistoryDTO dto) {
		PublishHistoryEntity entity = PublishMapper.toEntity(dto);
		PublishHistoryEntity alreadyExists = repository.findByApplicationNo(entity.getApplicationNo());
		if (alreadyExists != null) {
			System.out.println("application no exists  " + alreadyExists.getApplicationNo());
			Field[] fields = entity.getClass().getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true); // Allow access to private fields
				try {
					Object newValue = field.get(entity);
					if (newValue != null) {
						Field entityField = alreadyExists.getClass().getDeclaredField(field.getName());
						entityField.setAccessible(true);
						entityField.set(alreadyExists, newValue);
					}
				} catch (NoSuchFieldException | IllegalAccessException e) {
					throw new RuntimeException("Error updating field: " + field.getName(), e);
				}
			}
			if (StringUtils.isBlank(alreadyExists.getPublishUrl())) {
				alreadyExists.setPublishUrl(openKmUrl + "/dsp1/downloads/" + alreadyExists.getPublishUuid());
			}
			if (StringUtils.isBlank(entity.getHouse())) {
				alreadyExists.setHouse(getHouse());
			}
			entity = repository.save(alreadyExists);
		} else {
			if (StringUtils.isBlank(entity.getPublishUrl())) {
				entity.setPublishUrl(openKmUrl + "/dsp1/downloads/" + entity.getPublishUuid());
			}
			System.out.println("application no  " + entity.getApplicationNo());
			if (StringUtils.isBlank(entity.getHouse())) {
				entity.setHouse(getHouse());
			}
			entity = repository.save(entity);
		}
		return PublishMapper.toDTO(entity);
	}

	private String getHouse() {
		Long houseId = utils.getHouseIdFromAuthentication();
		Optional<String> house = repository.findHouseById(houseId);
		if (house.isEmpty())
			return "NH";
		return house.get();
	}

	@Override
	public List<UrlDto> getAll() {
		// PublishHistoryEntity entity=PublishMapper.toEntity(dto);
		List<PublishHistoryEntity> responseEntitys = repository.findAll();
		List<UrlDto> urlDto = new ArrayList<>();
		// for(PublishHistoryEntity entity:responseEntitys)
		// {
		// UrlDto responseDto = UrlDto.builder()
		// .date(date.toString())
		// .name(entity.getApplicationName())
		// .fileType(entity.getFileType())
		// .url(entity.getPublishUrl())
		// .build();
		// urlDto.add(responseDto);
		// }
		return urlDto;
	}

	@Override
	public List<UrlDto> getAllDocs(LocalDate date, String documentName, String locale) {
		// PublishHistoryEntity entity=PublishMapper.toEntity(dto);

		if (locale == null) {
			locale = "en";
		}

		Specification<PublishHistoryEntity> spec = Specification.where(isPublishStatus(true));
		if (date != null) {
			spec = spec.and(hasForDate(date));
		}
		if (documentName != null) {
			spec = spec.and(hasApplicationName(documentName));
		}
		if (locale != null) {
			spec = spec.and(hasLocale(locale));
		}
		// Sort sort = Sort.by(Sort.Order.desc("forDate"));
		List<PublishHistoryEntity> responseEntitys = repository.findAll(spec);
		List<PublishHistoryEntity> responseEntitysAll = repository.findAll();

		List<UrlDto> urlDto = new ArrayList<>();
		for (PublishHistoryEntity entity : responseEntitys) {

			UrlDto responseDto = UrlDto.builder().date(entity.getForDate().toString()).name(entity.getApplicationName())
					.fileType(entity.getFileType()).url(entity.getPublishUrl()).build();
			urlDto.add(responseDto);
		}
		return urlDto;
	}

	@Override
	public List<UrlDto> getPublishedDocsFromDSP1(LocalDate date, String documentName, String locale, String lsNo,
			String sesNo, String rssNo, String house) {

		List<UrlDto> urlDto = new ArrayList<>();
		// if (locale == null) {
		// locale = "en";
		// }
		Specification<PublishHistoryEntity> spec = Specification.where(isPublishStatus(true));
		if (date != null) {
			spec = spec.and(hasForDate(date));
		}
		if (StringUtils.isNotBlank(documentName)) {
			spec = spec.and(hasApplicationName(documentName));
		}
		if (lsNo != null) {
			spec = spec.and(hasLokSabhaNo(lsNo));
		}
		if (StringUtils.isNotBlank(sesNo)) {
			spec = spec.and(hasLokSabhaSessionNo(sesNo));
		}
		if (rssNo != null) {
			spec = spec.and(hasRajyaSabhaSessionNo(rssNo));
		}
		if (StringUtils.isNotBlank(locale)) {
			spec = spec.and(hasLocale(locale));
		}
		if (StringUtils.isNotBlank(house)) {
			spec = spec.and(hasHouse(house));
		}

		Sort sort = Sort.by(Sort.Order.desc("forDate"), Sort.Order.desc("id"));
		List<PublishHistoryEntity> responseEntitys = repository.findAll(spec, sort);
		// List<PublishHistoryEntity> responseEntitys = repository.findAll();
		// System.out.println("responseEntitys " + responseEntitys);

		List<PublishHistoryDTO> listOfDTOs = responseEntitys.stream().map(PublishMapper::toDTO)
				.collect(Collectors.toList());

		// System.out.println("list of Docs " + listOfDTOs);
		if (responseEntitys != null) {
			for (PublishHistoryEntity entity : responseEntitys) {
				UrlDto responseDto = UrlDto.builder().fileName(entity.getFileTypeName())
						.name(entity.getApplicationName()).fileType((entity.getFileType()).toUpperCase())
						.url("" + entity.getPublishUuid()).build();
				if (entity.getForDate() != null) {
					responseDto.setDate(entity.getForDate().toString());
				}
				urlDto.add(responseDto);
			}
		}
		System.out.println("list of urlDto" + urlDto);

		return urlDto;
	}

	@Override
	public UrlDto getByDateDoc(LocalDate forDate, String applicationName, String locale) {
		// PublishHistoryEntity entity=PublishMapper.toEntity(dto);
		if (locale == null) {
			locale = "en";
		}
		if (Optional.ofNullable(forDate).isEmpty()) {
			Optional<PublishHistoryEntity> responseEntity = repository.findByApplicationName(applicationName);
			if (responseEntity.isPresent()) {
				PublishHistoryEntity entity = responseEntity.get();
				if (entity.getPublishStatus() && locale.equalsIgnoreCase(entity.getLocale())) {
					UrlDto responseDto = UrlDto.builder().date("").name(entity.getApplicationName())
							.fileType(entity.getFileType()).url(entity.getPublishUrl()).build();
					return responseDto;
				}
			}
		} else {
			Optional<PublishHistoryEntity> responseEntity = repository.findByForDateAndApplicationName(forDate,
					applicationName);
			if (responseEntity.isPresent()) {
				PublishHistoryEntity entity = responseEntity.get();
				if (entity.getPublishStatus() && locale.equalsIgnoreCase(entity.getLocale())) {
					UrlDto responseDto = UrlDto.builder().date(forDate.toString()).name(entity.getApplicationName())
							.fileType(entity.getFileType()).url(entity.getPublishUrl()).build();
					return responseDto;
				}
			}
		}

		return UrlDto.builder().build();
	}

	@Override
	public UrlDto getBySession(LocalDate forDate, String lsNo, String sessNo, String rsNo, String applicationName,
			String locale) {
		// PublishHistoryEntity entity=PublishMapper.toEntity(dto);
		if (locale == null) {
			locale = "en";
		}
		if (sessNo == null) {
			Optional<PublishHistoryEntity> responseEntity = repository
					.findByApplicationNameAndRajyaSabhaSessionNo(applicationName, rsNo);
			if (responseEntity.isPresent()) {
				PublishHistoryEntity entity = responseEntity.get();
				if (entity.getPublishStatus() && locale.equalsIgnoreCase(entity.getLocale())) {
					UrlDto responseDto = UrlDto.builder().date(forDate.toString()).name(entity.getApplicationName())
							.fileType(entity.getFileType()).url(entity.getPublishUrl()).build();
					return responseDto;
				}
			}
		} else {
			Optional<PublishHistoryEntity> responseEntity = repository
					.findByApplicationNameAndLokSabhaSessionNo(applicationName, sessNo);
			if (responseEntity.isPresent()) {
				PublishHistoryEntity entity = responseEntity.get();
				if (entity.getPublishStatus() && locale.equalsIgnoreCase(entity.getLocale())) {
					UrlDto responseDto = UrlDto.builder().date("").name(entity.getApplicationName())
							.fileType(entity.getFileType()).url(entity.getPublishUrl()).build();
					return responseDto;
				}
			}
		}

		return UrlDto.builder().build();
	}

	@Override
	public UrlDto getAllUnpublishedDoc(LocalDate forDate, String applicationName, String locale) {
		// PublishHistoryEntity entity=PublishMapper.toEntity(dto);
		if (locale == null) {
			locale = "en";
		}
		Optional<PublishHistoryEntity> responseEntity = repository.findByForDateAndApplicationName(forDate,
				applicationName);
		if (responseEntity.isPresent()) {
			PublishHistoryEntity entity = responseEntity.get();
			if (!entity.getPublishStatus() && locale.equalsIgnoreCase(entity.getLocale())) {
				UrlDto responseDto = UrlDto.builder().date(forDate.toString()).name(entity.getApplicationName())
						.fileType(entity.getFileType()).url(entity.getPublishUrl()).build();

				return responseDto;
			}
		}
		return UrlDto.builder().build();
	}

	@Override
	public PublishHistoryDTO update(Long id, PublishHistoryDTO dto) {

		PublishHistoryEntity existingEntity = repository.findById(id).get();

		PublishHistoryEntity updatedEntity = PublishMapper.toEntity(dto);

		Field[] fields = updatedEntity.getClass().getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true); // Allow access to private fields
			try {
				Object newValue = field.get(updatedEntity);
				if (newValue != null) {
					Field entityField = existingEntity.getClass().getDeclaredField(field.getName());
					entityField.setAccessible(true);
					entityField.set(existingEntity, newValue);
				}
			} catch (NoSuchFieldException | IllegalAccessException e) {
				throw new RuntimeException("Error updating field: " + field.getName(), e);
			}
		}

		existingEntity = repository.save(existingEntity);

		return PublishMapper.toDTO(existingEntity);
	}

	@Override
	@Transactional
	public PublishHistoryDTO updateById(Long id, UnPublishRequest request) {

		PublishHistoryEntity entity = repository.findById(id).get();
		entity.setUnpublishByUser(request.getUnpublishByUser());
		if (request.getUnpublishDate() == null) {
			request.setUnpublishDate(LocalDate.now());
		}
		entity.setUnpublishDate(request.getUnpublishDate());
		entity.setPublishStatus(false);

//		 Cache clearing
		String appNo = entity.getApplicationNo().split("_")[0];
		ApplicationInstance appInstId = applicationInstanceRepository.getByApplicationNo(appNo);
		ApplicationInstanceProjection appDetails = getAppDetails(appInstId.getId());
		dsp1CacheService.clearCacheDsp1(appDetails.getAppNo(), appDetails.getAbbr());

		try {
			// Fetch ID of related application instances
			Long applicationInstId = repository.findApplicationInstanceIdsByAppNo(appDetails.getAppNo());

			// Update publish status in master.application_instance
			repository.updateUnpublishStatus(appDetails.getAppNo());

			// Update publish status in workflow.workflow_history
			repository.updateUnpublishStatusInWorkflowHistory(applicationInstId);
		} catch (Exception e) {
			System.out.println("Error updating unpublish status for application_no={}" + entity.getApplicationNo());
		}

		repository.saveAndFlush(entity);

		return PublishMapper.toDTO(entity);
	}

	/**
	 * @param id
	 * @return
	 */
	private ApplicationInstanceProjection getAppDetails(Long appInstId) {
		ApplicationInstanceProjection details = applicationInstanceRepository.findApplicationDetailsById(appInstId);
		if (details == null) {
			throw new IllegalArgumentException("Application instance not found for ID: " + appInstId);
		}
		return details;
	}

	@Override
	public PublishHistoryDTO getbyAppNo(String applicationNo) {

		PublishHistoryEntity entity = repository.findByApplicationNo(applicationNo);
		return PublishMapper.toDTO(entity);
	}

	@Override
	public List<UrlDto> getAllUnpublished(LocalDate date, String applicationName, String locale) {
		// if (locale == null) {
		// locale = "en";
		// }
		List<UrlDto> urlDto = new ArrayList<>();
		Specification<PublishHistoryEntity> spec = Specification.where(isPublishStatus(false));
		if (date != null) {
			spec = spec.and(hasForDate(date));
		}
		if (StringUtils.isBlank(applicationName)) {
			spec = spec.and(hasApplicationName(applicationName));
		}
		if (StringUtils.isBlank(locale)) {
			spec = spec.and(hasLocale(locale));
		}
		Sort sort = Sort.by(Sort.Order.desc("forDate"));
		List<PublishHistoryEntity> responseEntitys = repository.findAll(spec);

		if (responseEntitys != null) {
			for (PublishHistoryEntity entity : responseEntitys) {
				UrlDto responseDto = UrlDto.builder().date(date.toString()).name(entity.getApplicationName())
						.fileType(entity.getFileType()).url(entity.getPublishUrl()).build();
				urlDto.add(responseDto);
			}
		}
		return urlDto;
	}

	@Override
	public List<String> uploadToDsp2(String applicationName, SessionDto dto) {

		String house = utils.getHouseAbbr();
		System.out.println("house " + house);
		if (ConstantsUtility.LOK_SABHA.equalsIgnoreCase(house)) {
			return uploadToDsp2Ls(applicationName, dto);
		} else {
			return uploadToDsp2RS(applicationName, dto);
		}
	}

	public List<String> uploadToDsp2Ls(String applicationName, SessionDto dto) {
		Specification<PublishDocumentTemp> spec = Specification.where(hasAppName(applicationName));

		spec = spec.and(hasPublishUuid());
		if (StringUtils.isNotBlank(dto.getLsNo())) {
			spec = spec.and(hasLsNo(dto.getLsNo()));
		}
		if (StringUtils.isNotBlank(dto.getSesNo())) {
			spec = spec.and(hasSessNo(dto.getSesNo()));
		}
		if (StringUtils.isNotBlank(dto.getLocale())) {
			spec = spec.and(hasLocaleTemp(dto.getLocale()));
		}
		List<PublishDocumentTemp> urlList = tempRepo.findAll(spec);
		List<String> uploadedUuids = new ArrayList<>();
		Long houseId = utils.getHouseIdFromAuthentication();
		System.out.println("urlList " + urlList.size());
		if (urlList.isEmpty()) {
			return uploadedUuids;
		}
		System.out.println("Url List " + urlList);
		int i = 0;
		try {
			for (PublishDocumentTemp response : urlList) {

				boolean inRange = false;
				if (response.getForDate() == null) {
					inRange = dto.getFromDate() == null && dto.getToDate() == null;
				} else if (response.getForDate() != null) {
					try {
						inRange = betweenDates(dto.getFromDate(), dto.getToDate(),
								LocalDate.parse(response.getForDate()));

					} catch (Exception e) {
						inRange = isBetween(dto.getFromDate(), dto.getToDate(), response.getForDate());
					}
				} else {
					inRange = false;
				}
				if (!inRange) {
					continue;
				}

				logger.info("Processing document: {}", response.getCompleteUrl());
				byte[] fileData = null;
				try {
					fileData = restTemplate.getForObject(response.getCompleteUrl(), byte[].class);
					// System.out.println("fileData " + fileData);
				} catch (Exception e) {
					System.out.println("ERROR While Fetching data from URl " + e.getMessage());
					updateNonUuid(response);
					logger.warn("Incorrect URL or failed to fetch: {}", response.getCompleteUrl(), e);
					continue; // Skip this iteration if download fails
				}
				logger.info("Processing document: {}", response.getCompleteUrl());
				if (fileData == null || fileData.length == 0) {
					updateNonUuid(response);

					System.out.println("iteration inside filedata null " + i++);
					logger.warn("File data is null for URL: {}", response.getCompleteUrl());
					logger.warn("File data is null for URL: {}", response.getCompleteUrl());
					continue;
				}

				// System.out.println("data " + fileData);
				String uuid = uploadAndReturnUuid(response, null, fileData);
				// }
				if (uuid.isEmpty()) {
					updateNonUuid(response);

					logger.warn("Upload failed or UUID is empty for document: {}", response.getId());
					continue; // Skip this iteration and move to the next document
				}
				uploadedUuids.add(uuid);
			}
			return uploadedUuids;
		} catch (Exception e) {
			logger.error("Error downloading or processing file", e);
		}
		return uploadedUuids;
	}

	public List<String> uploadToDsp2RS(String applicationName, SessionDto dto) {
		Specification<PublishDocumentTempRS> spec = Specification.where(hasAppNameRs(applicationName));
		spec = spec.and(hasPublishUuidRs());
		if (StringUtils.isNotBlank(dto.getLocale())) {
			spec = spec.and(hasLocaleTempRs(dto.getLocale()));
		}
		if (StringUtils.isNotBlank(dto.getSesNo())) {
			spec = spec.and(hasRsNo(dto.getSesNo()));
		}
		List<PublishDocumentTempRS> urlList = rsDocRepo.findAll(spec);
		List<String> uploadedUuids = new ArrayList<>();
		Long houseId = utils.getHouseIdFromAuthentication();
		System.out.println("urlList " + urlList.size());
		if (urlList.isEmpty()) {
			return uploadedUuids;
		}
		System.out.println("Url List " + urlList);
		int i = 0;
		try {
			for (PublishDocumentTempRS response : urlList) {
				logger.info("Processing document: {}", response.getCompleteUrl());
				byte[] fileData = null;
				try {
					fileData = restTemplate.getForObject(response.getCompleteUrl(), byte[].class);
					// System.out.println("fileData " + fileData);
				} catch (Exception e) {
					System.out.println("ERROR While Fetching data from URl " + e.getMessage());
					updateNonUuidRs(response);
					logger.warn("Incorrect URL or failed to fetch: {}", response.getCompleteUrl(), e);
					continue; // Skip this iteration if download fails
				}

				logger.info("Processing document: {}", response.getCompleteUrl());
				if (fileData == null || fileData.length == 0) {
					updateNonUuidRs(response);

					System.out.println("iteration inside filedata null " + i++);
					logger.warn("File data is null for URL: {}", response.getCompleteUrl());
					logger.warn("File data is null for URL: {}", response.getCompleteUrl());
					continue;
				}

				System.out.println("data " + fileData);
				System.out.println("res rs " + response);
				String uuid = uploadAndReturnUuid(null, response, fileData);
				// }
				if (uuid.isEmpty()) {
					updateNonUuidRs(response);

					logger.warn("Upload failed or UUID is empty for document: {}", response.getId());
					continue; // Skip this iteration and move to the next document
				}
				uploadedUuids.add(uuid);
			}
			return uploadedUuids;
		} catch (Exception e) {
			logger.error("Error downloading or processing file", e);
		}
		return uploadedUuids;
	}

	@Override

	public List<String> uploadToDsp2Rs(String applicationName, SessionDto dto) {

		List<String> uploadedUuids = new ArrayList<>();
		uploadedUuids.addAll(uploadToDsp2RS(applicationName, dto));
		return uploadedUuids;
	}

	@Override
	public List<ResponseDocsUuids> uploadTransmissionDocs() {
		String house = getHouse();
		if (ConstantsUtility.LOK_SABHA.equalsIgnoreCase(house))
			return uploadTransmissionDocsLs();
		if (ConstantsUtility.RAJYA_SABHA.equalsIgnoreCase(house))
			return uploadTransmissionDocsRs();
		return null;
	}

	// uploadTransmissionDocsLs
	public List<ResponseDocsUuids> uploadTransmissionDocsLs() {
		Long houseId = utils.getHouseIdFromAuthentication();
		Specification<ETransmissionUploadTempEntity> spec = Specification.where(hasHouseId(houseId));
		spec = spec.and(hasUuidTransLs());
		List<ETransmissionUploadTempEntity> urlList = eTransmissionRepository.findAll(spec);

		List<ResponseDocsUuids> uploadedUuids = new ArrayList<>();
		System.out.println("urlList " + urlList.size());
		if (urlList != null && urlList.isEmpty()) {
			return new ArrayList<>();
		}
		// System.out.println("Url List " + urlList);
		int i = 0;
		try {
			List<ETransmissionUploadTempEntity> transmissionData = existsUrlAndUuidCheckLS(urlList);
			System.out.println(transmissionData.size());

			for (ETransmissionUploadTempEntity response : transmissionData) {
				System.out.println("res " + response);
				if (existsUrlAndUuid(response)) {
					continue;
				}
				try {
					uploadedUuids.addAll(fetchAndUploadDocument(response));
				} catch (Exception e) {
					System.out.println("Exception " + e.getMessage());
				}

				System.out.println("uploadedUuids");
				System.out.println(uploadedUuids);
			}
			return uploadedUuids;
		} catch (Exception e) {
			logger.error("Error downloading or processing file", e);
		}
		return uploadedUuids;

	}

	// etransmission_rs_temp
	// uploadTransmissionDocsLs
	public List<ResponseDocsUuids> uploadTransmissionDocsRs() {
		Long houseId = utils.getHouseIdFromAuthentication();
		Specification<ETransmissionRSUploadTempEntity> spec = Specification.where(hasHouseIdRS(houseId));
		List<ETransmissionRSUploadTempEntity> urlList = eTransmissionRSRepository.findAll(spec);
		spec = spec.or(hasUuidTransRs());
		List<ResponseDocsUuids> uploadedUuids = new ArrayList<>();
		System.out.println("urlList " + urlList.size());
		if (urlList != null && urlList.isEmpty()) {
			return new ArrayList<>();
		}
		// System.out.println("Url List " + urlList);
		int i = 0;
		try {
			List<ETransmissionRSUploadTempEntity> transmissionRsData = existsUrlAndUuidCheckRS(urlList);
			for (ETransmissionRSUploadTempEntity response : transmissionRsData) {
				System.out.println("res " + response);
				if (existsUrlAndUuidRS(response)) {
					continue;
				}
				try {
					uploadedUuids.addAll(fetchAndUploadDocumentRS(response));
				} catch (Exception e) {
					System.out.println("Exception " + e.getMessage());
				}

				System.out.println("uploadedUuids");
				System.out.println(uploadedUuids);
			}
			return uploadedUuids;
		} catch (Exception e) {
			logger.error("Error downloading or processing file", e);
		}
		return uploadedUuids;

	}

	private Collection<? extends ResponseDocsUuids> fetchAndUploadDocument(ETransmissionUploadTempEntity response) {
		List<ResponseDocsUuids> uploadedUuids = new ArrayList<>();

		byte[] errataEngData = null;
		byte[] errataHiData = null;
		byte[] fileEngData = null;
		byte[] fileHindiData = null;

		String existE_ENUuid = response.getEngErrataUuid();
		String existE_HIUuid = response.getHindiErrataUuid();
		String existF_ENUuid = response.getList1EngUuid();
		String existF_HIUuid = response.getList1HindiUuid();

		// ----------- ERRATA ENGLISH -----------
		try {
			// if (isUploadAllowed(response.getEngErrataUrl(), response.getEngErrataUuid(),
			// response.getEngErrata())) {
			errataEngData = eTransmissionUtils.getUrlAnReturnFileData(response.getEngErrataUrl(),
					ConstantsUtility.E_EN);

			if (errataEngData != null && errataEngData.length > 0) {
				ResponseDocsUuids res = eTransmissionUtils.processErrata(response, existE_ENUuid, errataEngData,
						ConstantsUtility.E_EN, response.getEngErrataUrl(), uploadedUuids);

				if (res != null) {
					existE_ENUuid = res.getUuid().toString();
					uploadedUuids.add(res);
					// processedUrls.add(response.getEngErrataUrl());
				}
			}
			// }
		} catch (Exception e) {
			System.out.println("ERROR Fetching data from ERRATA EN URL: " + e.getMessage());
			eTransmissionUtils.updateNonUuidInTransmission(response);
		}
		// ----------- ERRATA HINDI -----------
		try {
			// if (isUploadAllowed(response.getHindiErrataUrl(),
			// response.getHindiErrataUuid(),
			// response.getHindiErrata())) {
			errataHiData = eTransmissionUtils.getUrlAnReturnFileData(response.getHindiErrataUrl(),
					ConstantsUtility.E_HI);

			if (errataHiData != null && errataHiData.length > 0) {
				ResponseDocsUuids res = eTransmissionUtils.processErrata(response, existE_HIUuid, errataHiData,
						ConstantsUtility.E_HI, response.getHindiErrataUrl(), uploadedUuids);

				if (res != null) {
					existE_HIUuid = res.getUuid().toString();
					uploadedUuids.add(res);
					// processedUrls.add(response.getHindiErrataUrl());
				}
			}
			// }
		} catch (Exception e) {
			System.out.println("ERROR Fetching data from ERRATA HI URL: " + e.getMessage());
			eTransmissionUtils.updateNonUuidInTransmission(response);
		}

		// ----------- FILE ENGLISH -----------
		try {
			// if (isUploadAllowed(response.getList1EngUrl(), response.getList1EngUuid(),
			// response.getList1Eng())) {
			fileEngData = eTransmissionUtils.getUrlAnReturnFileData(response.getList1EngUrl(), ConstantsUtility.F_EN);

			if (fileEngData != null && fileEngData.length > 0) {
				ResponseDocsUuids res = eTransmissionUtils.processFile(response, existF_ENUuid, fileEngData,
						ConstantsUtility.F_EN, response.getList1EngUrl(), uploadedUuids);

				if (res != null) {
					existF_ENUuid = res.getUuid().toString();
					uploadedUuids.add(res);
					// processedUrls.add(response.getList1EngUrl());
				}
			}
			// }
		} catch (Exception e) {
			System.out.println("ERROR Fetching data from FILE EN URL: " + e.getMessage());
			eTransmissionUtils.updateNonUuidInTransmission(response);
		}

		// ----------- FILE HINDI -----------
		try {
			// if (isUploadAllowed(response.getList1HindiUrl(),
			// response.getList1HindiUuid(), response.getHindiErrata())) {
			fileHindiData = eTransmissionUtils.getUrlAnReturnFileData(response.getList1HindiUrl(),
					ConstantsUtility.F_HI);

			if (fileHindiData != null && fileHindiData.length > 0) {
				ResponseDocsUuids res = eTransmissionUtils.processFile(response, existF_HIUuid, fileHindiData,
						ConstantsUtility.F_HI, response.getList1HindiUrl(), uploadedUuids);

				if (res != null) {
					existF_HIUuid = res.getUuid().toString();
					uploadedUuids.add(res);
					// processedUrls.add(response.getList1HindiUrl());
				}
			}
			// }
		} catch (Exception e) {
			System.out.println("ERROR Fetching data from FILE HI URL: " + e.getMessage());
			eTransmissionUtils.updateNonUuidInTransmission(response);
		}

		// If no valid files were uploaded, update the entity
		if ((fileEngData == null || fileEngData.length == 0) && (fileHindiData == null || fileHindiData.length == 0)) {
			eTransmissionUtils.updateNonUuidInTransmission(response);
		}

		return uploadedUuids;
	}

	private Collection<? extends ResponseDocsUuids> fetchAndUploadDocumentRS(ETransmissionRSUploadTempEntity response) {
		List<ResponseDocsUuids> uploadedUuids = new ArrayList<>();

		byte[] errataEngData = null;
		byte[] errataHiData = null;
		byte[] fileEngData = null;
		byte[] fileHindiData = null;

		String existE_ENUuid = response.getEngErrataUuid();
		String existE_HIUuid = response.getHindiErrataUuid();
		String existF_ENUuid = response.getList1EngUuid();
		String existF_HIUuid = response.getList1HindiUuid();

		// ----------- ERRATA ENGLISH -----------
		try {
			// if (isUploadAllowed(response.getEngErrataUrl(), response.getEngErrataUuid(),
			// response.getEngErrata())) {
			errataEngData = eTransmissionUtils.getUrlAnReturnFileData(response.getEngErrataUrl(),
					ConstantsUtility.E_EN);

			if (errataEngData != null && errataEngData.length > 0) {
				ResponseDocsUuids res = eTransmissionUtils.processErrataRS(response, existE_ENUuid, errataEngData,
						ConstantsUtility.E_EN, response.getEngErrataUrl(), uploadedUuids);

				if (res != null) {
					existE_ENUuid = res.getUuid().toString();
					uploadedUuids.add(res);
					// processedUrls.add(response.getEngErrataUrl());
				}
			}
			// }
		} catch (Exception e) {
			System.out.println("ERROR Fetching data from ERRATA EN URL: " + e.getMessage());
			eTransmissionUtils.updateNonUuidInRSTransmission(response);
		}

		// ----------- ERRATA HINDI -----------
		try {
			// if (isUploadAllowed(response.getHindiErrataUrl(),
			// response.getHindiErrataUuid(),
			// response.getHindiErrata())) {
			errataHiData = eTransmissionUtils.getUrlAnReturnFileData(response.getHindiErrataUrl(),
					ConstantsUtility.E_HI);

			if (errataHiData != null && errataHiData.length > 0) {
				ResponseDocsUuids res = eTransmissionUtils.processErrataRS(response, existE_HIUuid, errataHiData,
						ConstantsUtility.E_HI, response.getHindiErrataUrl(), uploadedUuids);

				if (res != null) {
					existE_HIUuid = res.getUuid().toString();
					uploadedUuids.add(res);
					// processedUrls.add(response.getHindiErrataUrl());
				}
			}
			// }
		} catch (Exception e) {
			System.out.println("ERROR Fetching data from ERRATA HI URL: " + e.getMessage());
			eTransmissionUtils.updateNonUuidInRSTransmission(response);
		}

		// ----------- FILE ENGLISH -----------
		try {
			// if (isUploadAllowed(response.getList1EngUrl(), response.getList1EngUuid(),
			// response.getList1Eng())) {
			fileEngData = eTransmissionUtils.getUrlAnReturnFileData(response.getList1EngUrl(), ConstantsUtility.F_EN);

			if (fileEngData != null && fileEngData.length > 0) {
				ResponseDocsUuids res = eTransmissionUtils.processFileRS(response, existF_ENUuid, fileEngData,
						ConstantsUtility.F_EN, response.getList1EngUrl(), uploadedUuids);

				if (res != null) {
					existF_ENUuid = res.getUuid().toString();
					uploadedUuids.add(res);
					// processedUrls.add(response.getList1EngUrl());
				}
			}
			// }
		} catch (Exception e) {
			System.out.println("ERROR Fetching data from FILE EN URL: " + e.getMessage());
			eTransmissionUtils.updateNonUuidInRSTransmission(response);
		}

		// ----------- FILE HINDI -----------
		try {
			// if (isUploadAllowed(response.getList1HindiUrl(),
			// response.getList1HindiUuid(), response.getHindiErrata())) {
			fileHindiData = eTransmissionUtils.getUrlAnReturnFileData(response.getList1HindiUrl(),
					ConstantsUtility.F_HI);

			if (fileHindiData != null && fileHindiData.length > 0) {
				ResponseDocsUuids res = eTransmissionUtils.processFileRS(response, existF_HIUuid, fileHindiData,
						ConstantsUtility.F_HI, response.getList1HindiUrl(), uploadedUuids);

				if (res != null) {
					existF_HIUuid = res.getUuid().toString();
					uploadedUuids.add(res);
					// processedUrls.add(response.getList1HindiUrl());
				}
			}
			// }
		} catch (Exception e) {
			System.out.println("ERROR Fetching data from FILE HI URL: " + e.getMessage());
			eTransmissionUtils.updateNonUuidInRSTransmission(response);
		}

		// If no valid files were uploaded, update the entity
		if ((fileEngData == null || fileEngData.length == 0) && (fileHindiData == null || fileHindiData.length == 0)) {
			eTransmissionUtils.updateNonUuidInRSTransmission(response);
		}

		return uploadedUuids;
	}

	/**
	 * Checks whether this URL is eligible for upload: - Not null - Has no existing
	 * UUID - Not previously processed - Not a versioned file
	 */
	private boolean isUploadAllowed(String url, String uuid, String fileName) {
		if (url == null || uuid != null || processedUrls.contains(url)) {
			return false;
		}
		// String fileName = getFileNameFromUrl(url);
		if (fileName != null && VERSION_PATTERN.matcher(fileName).find()) {
			System.out.println("Skipping versioned file: " + fileName);
			return false;
		}

		return true;
	}

	private String cleanFileName(String url) {
		if (url == null)
			return null;
		try {
			String fileName = url.substring(url.lastIndexOf('/') + 1);
			Matcher matcher = VERSION_PATTERN.matcher(fileName);
			return matcher.replaceAll(""); // Removes the version suffix
		} catch (Exception e) {
			return url;
		}
	}

	/**
	 * Extract file name from the URL.
	 */
	private String getFileNameFromUrl(String url) {
		if (url == null)
			return null;
		try {
			return url.substring(url.lastIndexOf('/') + 1);
		} catch (Exception e) {
			return null;
		}
	}

	private Boolean existsUrlAndUuid(ETransmissionUploadTempEntity response) {
		Boolean isTrueOrFalse = true;
		if (response.getList1EngUrl() != null && response.getList1EngUuid() == null)
			isTrueOrFalse = false;
		if (response.getList1HindiUrl() != null && response.getList1HindiUuid() == null)
			isTrueOrFalse = false;
		if (response.getEngErrataUrl() != null && response.getEngErrataUuid() == null)
			isTrueOrFalse = false;
		if (response.getHindiErrataUrl() != null && response.getHindiErrataUuid() == null)
			isTrueOrFalse = false;
		System.out.println("isTrueOrFalse " + isTrueOrFalse);
		return isTrueOrFalse;
	}

	private List<ETransmissionUploadTempEntity> existsUrlAndUuidCheckLS(List<ETransmissionUploadTempEntity> response) {
		List<ETransmissionUploadTempEntity> resp = new ArrayList();
		for (ETransmissionUploadTempEntity entity : response) {
			if (!existsUrlAndUuid(entity)) {
				resp.add(entity);
			} else {
				continue;
			}
		}
		return resp;
	}

	private List<ETransmissionRSUploadTempEntity> existsUrlAndUuidCheckRS(
			List<ETransmissionRSUploadTempEntity> response) {

		List<ETransmissionRSUploadTempEntity> resp = new ArrayList();
		for (ETransmissionRSUploadTempEntity entity : response) {
			if (!existsUrlAndUuidRS(entity)) {
				resp.add(entity);
			} else {
				continue;
			}
		}
		System.out.println(resp.size());
		return resp;
	}

	private Boolean existsUrlAndUuidRS(ETransmissionRSUploadTempEntity response) {
		Boolean isTrueOrFalse = true;

		if (response.getList1EngUrl() != null && response.getList1EngUuid() == null)
			isTrueOrFalse = false;
		if (response.getList1HindiUrl() != null && response.getList1HindiUuid() == null)
			isTrueOrFalse = false;
		if (response.getEngErrataUrl() != null && response.getEngErrataUuid() == null)
			isTrueOrFalse = false;
		if (response.getHindiErrataUrl() != null && response.getHindiErrataUuid() == null)
			isTrueOrFalse = false;
		System.out.println("isTrueOrFalse " + isTrueOrFalse);
		return isTrueOrFalse;
	}

	@Override
	public List<ResponseMember> uploadMemberDSP2() {
		String house = getHouse();
		if (ConstantsUtility.LOK_SABHA.equalsIgnoreCase(house)) {
			return uploadMemberDSP2LS(house);

		} else if (ConstantsUtility.RAJYA_SABHA.equalsIgnoreCase(house)) {
			return uploadMemberDSP2RS(house);

		}
		return null;
	}

	public List<ResponseMember> uploadMemberDSP2LS(String house) {
		Specification<MemberPhotoSignTemp> spec = Specification.where(hasHouseInMem(utils.getHouseAbbr()));
		spec = spec.or(hasUuidMemLs());
		List<MemberPhotoSignTemp> urlList = memberTempRepo.findAll(spec);
		List<ResponseMember> uploadedUuids = new ArrayList<>();
		Long houseId = utils.getHouseIdFromAuthentication();
		System.out.println("urlList " + urlList.size());
		if (urlList != null && urlList.isEmpty()) {
			return new ArrayList<>();
		}
		// System.out.println("Url List " + urlList);
		int i = 0;
		try {
			for (MemberPhotoSignTemp response : urlList) {
				// logger.info("Processing document: {}", response.getPhotoUrl());
				byte[] filePhotoData = null;
				byte[] fileSignatureData = null;
				UUID existingPhUuid = response.getPhotoUuid();
				UUID existingSgUuid = response.getSignatureUuid();
				if (existingPhUuid != null && existingSgUuid != null) {
					// System.out.println("skip");
					continue;
				} else {
					if (existingPhUuid == null && StringUtils.isNotBlank(response.getPhotoUrl())) {
						try {
							System.out.println("file " + response.getPhotoUrl());

							filePhotoData = memberUtils.getUrlAnReturnFileData(response.getPhotoUrl(),
									ConstantsUtility.PHOTO);
							System.out.println("fileData " + filePhotoData);
							ResponseMember memPhoto = memberUtils.processPhoto(response, existingPhUuid, filePhotoData,
									uploadedUuids, house);
							existingPhUuid = memPhoto.getPhotoUuid();
							uploadedUuids.add(memPhoto);
							// processedUrls.add(response.getPhotoUrl());

						} catch (Exception e) {
							System.out.println("ERROR While Fetching data from photo URl " + e.getMessage());
							memberUtils.updateNonUuidInMemPhoto(response);
						}
					}
					if (existingSgUuid == null && StringUtils.isNotBlank(response.getSignatureUrl())) {
						try {
							System.out.println("file " + response.getSignatureUrl());
							fileSignatureData = memberUtils.getUrlAnReturnFileData(response.getSignatureUrl(),
									ConstantsUtility.SIGN);
							System.out.println("fileData " + fileSignatureData);
							ResponseMember memSign = memberUtils.processSignature(response, existingSgUuid,
									fileSignatureData, uploadedUuids, house);
							existingSgUuid = memSign.getSignUuid();
							uploadedUuids.add(memSign);
							// processedUrls.add(response.getSignatureUrl());
						} catch (Exception e) {
							System.out.println("ERROR While Fetching data from sign URl " + e.getMessage());
							memberUtils.updateNonUuidInMemSignature(response);
						}
					}
				}
				System.out.println("uploadedUuids");
				System.out.println(uploadedUuids);
			}
			return uploadedUuids;
		} catch (Exception e) {
			logger.error("Error downloading or processing file", e);
		}
		return uploadedUuids;
	}

	private Specification<MemberPhotoSignTempRS> hasHouseInMemRs(String house) {
		return (root, query, criteriaBuilder) -> {
			if (house != null) {
				return criteriaBuilder.equal(root.get("house"), house);
			}
			return criteriaBuilder.conjunction(); // Returns a valid Predicate that does not filter anything
		};
	}

	public List<ResponseMember> uploadMemberDSP2RS(String house) {
		Specification<MemberPhotoSignTempRS> spec = Specification.where(hasHouseInMemRs(utils.getHouseAbbr()));
		spec = spec.and(hasUuidMemRs());
		List<MemberPhotoSignTempRS> urlList = memberTempRepoRs.findAll(spec);
		List<ResponseMember> uploadedUuids = new ArrayList<>();
		Long houseId = utils.getHouseIdFromAuthentication();
		System.out.println("urlList " + urlList.size());
		if (urlList != null && urlList.isEmpty()) {
			return new ArrayList<>();
		}
		// System.out.println("Url List " + urlList);
		int i = 0;
		try {
			for (MemberPhotoSignTempRS response : urlList) {
				// logger.info("Processing document: {}", response.getPhotoUrl());
				byte[] filePhotoData = null;
				byte[] fileSignatureData = null;
				UUID existingPhUuid = response.getPhotoUuid();
				UUID existingSgUuid = response.getSignatureUuid();
				if (existingPhUuid != null && existingSgUuid != null) {
					System.out.println("skip");
					continue;
				} else {
					if (existingPhUuid == null && StringUtils.isNotBlank(response.getPhotoUrl())) {
						try {
							System.out.println("file " + response.getPhotoUrl());

							filePhotoData = memberUtils.getUrlAnReturnFileData(response.getPhotoUrl(),
									ConstantsUtility.PHOTO);
							System.out.println("fileData " + filePhotoData);
							ResponseMember memPhoto = memberUtils.processPhotoRs(response, existingPhUuid,
									filePhotoData, uploadedUuids, house);
							existingPhUuid = memPhoto.getPhotoUuid();
							uploadedUuids.add(memPhoto);
							// processedUrls.add(response.getPhotoUrl());

						} catch (Exception e) {
							System.out.println("ERROR While Fetching data from photo URl " + e.getMessage());
							memberUtils.updateNonUuidInMemPhotoRs(response);
						}
					}
					if (existingSgUuid == null && StringUtils.isNotBlank(response.getSignatureUrl())) {
						try {
							System.out.println("file " + response.getSignatureUrl());
							fileSignatureData = memberUtils.getUrlAnReturnFileData(response.getSignatureUrl(),
									ConstantsUtility.SIGN);
							System.out.println("fileData " + fileSignatureData);
							ResponseMember memSign = memberUtils.processSignatureRs(response, existingSgUuid,
									fileSignatureData, uploadedUuids, house);
							existingSgUuid = memSign.getSignUuid();
							uploadedUuids.add(memSign);
							// processedUrls.add(response.getSignatureUrl());
						} catch (Exception e) {
							System.out.println("ERROR While Fetching data from sign URl " + e.getMessage());
							memberUtils.updateNonUuidInMemSignatureRs(response);
						}
					}
				}
				System.out.println("uploadedUuids");
				System.out.println(uploadedUuids);
			}
			return uploadedUuids;
		} catch (Exception e) {
			logger.error("Error downloading or processing file", e);
		}
		return uploadedUuids;
	}

	private boolean betweenDates(LocalDate fromDate, LocalDate toDate, LocalDate targetDate) {
		if (targetDate == null && fromDate == null && toDate == null) {
			return true;
		} else if (targetDate == null && (fromDate != null || toDate != null)) {
			return false;
		}

		if (fromDate != null && targetDate.isBefore(fromDate)) {
			return false;
		}

		if (toDate != null && targetDate.isAfter(toDate)) {
			return false;
		}

		return true;
	}

	private boolean isBetween(LocalDate fromDate, LocalDate toDate, String targetDateStr) {
		if (StringUtils.isBlank(targetDateStr)) {
			return fromDate == null && toDate == null;
		}
		try {

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
			LocalDate targetDate = LocalDate.parse(targetDateStr.trim(), formatter);
			return betweenDates(fromDate, toDate, targetDate);
		} catch (DateTimeParseException e) {
			System.out.println("Invalid date format: " + targetDateStr);
			LocalDate targetDate = LocalDate.parse(targetDateStr.trim());
			return betweenDates(fromDate, toDate, targetDate);
		}
	}

	private Specification<PublishDocumentTemp> hasPublishUuid() {
		return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("publishUuid"));
	}

	private Specification<MemberPhotoSignTemp> hasUuidMemLs() {
		return (root, query, cb) -> cb.or(cb.and(cb.isNotNull(root.get("photo")), cb.isNull(root.get("photoUuid"))),
				cb.and(cb.isNotNull(root.get("signature")), cb.isNull(root.get("signatureUuid"))));
	}

	private Specification<MemberPhotoSignTempRS> hasUuidMemRs() {
		return (root, query, cb) -> cb.or(cb.and(cb.isNotNull(root.get("photo")), cb.isNull(root.get("photoUuid"))),
				cb.and(cb.isNotNull(root.get("signature")), cb.isNull(root.get("signatureUuid"))));
	}

	private Specification<ETransmissionUploadTempEntity> hasUuidTransLs() {
		return (root, query, cb) -> cb.or(
				cb.and(cb.isNotNull(root.get("list1Eng")), cb.isNull(root.get("list1EngUuid"))),
				cb.and(cb.isNotNull(root.get("list1Hindi")), cb.isNull(root.get("list1HindiUuid"))),
				cb.and(cb.isNotNull(root.get("hindiErrata")), cb.isNull(root.get("hindiErrataUuid"))),
				cb.and(cb.isNotNull(root.get("list1Hindi")), cb.isNull(root.get("list1HindiUuid"))));
	}

	private Specification<ETransmissionRSUploadTempEntity> hasUuidTransRs() {
		return (root, query, cb) -> cb.or(
				cb.and(cb.isNotNull(root.get("list1Eng")), cb.isNull(root.get("list1EngUuid"))),
				cb.and(cb.isNotNull(root.get("list1Hindi")), cb.isNull(root.get("list1HindiUuid"))),
				cb.and(cb.isNotNull(root.get("hindiErrata")), cb.isNull(root.get("hindiErrataUuid"))),
				cb.and(cb.isNotNull(root.get("list1Hindi")), cb.isNull(root.get("list1HindiUuid"))));
	}

	private Specification<PublishDocumentTempRS> hasPublishUuidRs() {
		return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("publishUuid"));
	}

	public static Specification<PublishDocumentTempRS> hasRsNo(String rsNo) {
		return (root, query, criteriaBuilder) -> {
			if (rsNo != null) {
				return criteriaBuilder.equal(root.get("sesNo"), rsNo);
			}
			return criteriaBuilder.conjunction();
		};
	}

	private Specification<ETransmissionUploadTempEntity> hasHouseId(Long house) {
		return (root, query, criteriaBuilder) -> {
			if (house != null) {
				return criteriaBuilder.equal(root.get("houseId"), house);
			}
			return criteriaBuilder.conjunction(); // Returns a valid Predicate that does not filter anything
		};
	}

	private Specification<ETransmissionRSUploadTempEntity> hasHouseIdRS(Long house) {
		return (root, query, criteriaBuilder) -> {
			if (house != null) {
				return criteriaBuilder.equal(root.get("houseId"), house);
			}
			return criteriaBuilder.conjunction(); // Returns a valid Predicate that does not filter anything
		};
	}

	private Specification<MemberPhotoSignTemp> hasHouseInMem(String house) {
		return (root, query, criteriaBuilder) -> {
			if (house != null) {
				return criteriaBuilder.equal(root.get("house"), house);
			}
			return criteriaBuilder.conjunction(); // Returns a valid Predicate that does not filter anything
		};
	}

	public String uploadAndReturnUuid(PublishDocumentTemp response, PublishDocumentTempRS responseRs, byte[] fileData) {
		try {
			String houseAbbr = utils.getHouseAbbr();
			System.out.println("response " + response);
			System.out.println("response Rs " + responseRs);

			String uploadUrl = openKmUrl + "/document/upload";
			if (fileData == null) {
				if (ConstantsUtility.LOK_SABHA.equalsIgnoreCase(houseAbbr))
					updateNonUuid(response);
				else
					updateNonUuidRs(responseRs);
				return "file data null";
			}
			// // Set Authorization Token
			String token = getTokenFromRequest();
			if (token == null || token.isEmpty()) {
				logger.error("Authorization token is missing. Cannot upload file.");
				return "token null";
			}

			String appAbbr = getAppAbbr(response, responseRs, houseAbbr);
			System.out.println("appAbbr " + appAbbr);
			List<String> folders = new ArrayList();
			folders.add(houseAbbr);
			System.out.println("house " + houseAbbr);
			if (ConstantsUtility.LOK_SABHA.equals(houseAbbr)) {
				if (StringUtils.isNotBlank(appAbbr) && appAbbr.equalsIgnoreCase("PMB")
						|| appAbbr.equalsIgnoreCase("B2")) {
					folders.add(getYear(response.getForDate()));
					folders.add(getMonth(response.getForDate()));
				} else {
					if (response.getSesNo() != null) {
						folders.add(response.getLsNo());
						if (!isRoman(folderCreationUtils.trimStr(response.getSesNo()))) {
							response.setSesNo(toRoman(Integer.parseInt(response.getSesNo())));
						}
						folders.add(response.getSesNo());
					}
				}
			} else {
				if (StringUtils.isNotBlank(appAbbr) && appAbbr.equalsIgnoreCase("PMB")
						|| appAbbr.equalsIgnoreCase("B2")) {
					if (responseRs.getForDate() != null) {
						// LocalDateTime
						// dateTime=getLocalDateTimeFromTimeStramp(responseRs.getForDate())
						folders.add(getYearFromTime(responseRs.getForDate()));
						folders.add(getMonthFromTime(responseRs.getForDate()));
					}
				} else {
					if (StringUtils.isNotBlank(responseRs.getSesNo())) {
						folders.add(responseRs.getSesNo());

					}
				}
			}

			folders.add(appAbbr);

			System.out.println("folders " + folders);
			String rootPath = ConstantsUtility.OKM_ROOT;
			String folderPath = createDmsFolderStructure(rootPath, folders);
			System.out.println("folder path " + folderPath);
			HttpHeaders headers = new HttpHeaders();
			System.out.println("uploadUrl  " + uploadUrl);
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			headers.set("Authorization", "Bearer " + token);
			// String originalFileName = response.getUrl();
			// int lastDotIndex = originalFileName.lastIndexOf(".");
			// String fileNameWithoutExt = (lastDotIndex == -1) ? originalFileName
			// : originalFileName.substring(0, lastDotIndex);
			String fileNameWithoutExt = "";
			System.out.println("LS " + response);
			System.out.println("RS " + responseRs);

			if (ConstantsUtility.LOK_SABHA.equalsIgnoreCase(houseAbbr))
				fileNameWithoutExt = getFileNameWithOutExtension(response.getUrl());
			else
				fileNameWithoutExt = getFileNameWithOutExtension(responseRs.getUrl());
			System.out.println("file Name before slash and space update" + fileNameWithoutExt);

			if (fileNameWithoutExt.contains("/") || fileNameWithoutExt.contains(" ")) {
				fileNameWithoutExt = fileNameWithoutExt.replace(" ", "-").replace("/", "-");
			}
			System.out.println("file Name after slash and space update" + fileNameWithoutExt);
			String updatedFileName = fileNameWithoutExt + ".pdf";
			// Create a temporary file

			File tempFile = File.createTempFile(updatedFileName, ".pdf");
			try (FileOutputStream fos = new FileOutputStream(tempFile)) {
				fos.write(fileData);
			}
			// Create file resource
			ByteArrayResource resource = new ByteArrayResource(fileData) {
				@Override
				public String getFilename() {
					return updatedFileName;
				}
			};

			// Prepare request body
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", resource);
			body.add("folderPath", folderPath);
			body.add("documentName", updatedFileName);
			body.add("masterId", 231);

			body.add("applicationNo", getApplicationIdHouse(response, responseRs, houseAbbr));
			body.add("referenceId", 1);
			body.add("referenceType", "ApplicationInstance");
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
			System.out.println("uploading file with");
			System.out.println(requestEntity);
			System.out.println(uploadUrl);
			ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(uploadUrl, HttpMethod.POST,
					requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
					});
			logger.info("Response from OpenKM: {}", responseEntity);
			// Check response status
			if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
				Map<String, Object> responseBody = responseEntity.getBody();
				logger.info("Response Body: {}", responseBody);
				// Extract "data"
				Object dataObject = responseBody.get("data");
				if (dataObject instanceof Map) {
					Map<String, Object> dataMap = (Map<String, Object>) dataObject;
					// Extract "documentUuid"
					Object generatedUuid = dataMap.get("documentUuid");
					logger.info("Extracted documentUuid: {}", generatedUuid);
					if (generatedUuid != null && !generatedUuid.toString().trim().isEmpty()) {
						String uuidString = generatedUuid.toString().trim();
						try {
							UUID uuid = UUID.fromString(uuidString);
							if (ConstantsUtility.LOK_SABHA.equalsIgnoreCase(houseAbbr))
								updatePublishTempTable(response, "" + uuid);
							else
								updatePublishTempTableRs(responseRs, "" + uuid);

							logger.info("entity after saving");
							return uuidString;
						} catch (IllegalArgumentException ex) {
							logger.error("Invalid UUID format received from OpenKM: {}", uuidString, ex);
						}
					} else {
						logger.warn("Received empty or null UUID from OpenKM: {}", dataMap);
					}
				} else {
					logger.warn("Response does not contain 'data' field: {}", responseBody);
				}
			} else {
				logger.error("Failed to upload document. HTTP Status: {}", responseEntity.getStatusCode());
			}
		} catch (IOException e) {
			logger.error("Error writing temp file: ", e);
		} catch (Exception e) {
			logger.error("Error uploading file: ", e.getMessage());
			e.printStackTrace();
		}
		return "";
	}

	private Object getApplicationIdHouse(PublishDocumentTemp response, PublishDocumentTempRS responseRs,
			String houseAbbr) {
		// TODO Auto-generated method stub
		if (ConstantsUtility.LOK_SABHA.equalsIgnoreCase(houseAbbr))
			return response.getApplicationId();
		else
			return responseRs.getApplicationId();
	}

	public String getAppAbbr(PublishDocumentTemp ls, PublishDocumentTempRS rs, String house) {
		// TODO Auto-generated method stub
		try {
			if (ConstantsUtility.LOK_SABHA.equalsIgnoreCase(house))
				return repository.getAbbrByApplicationId(ls.getApplicationId());
			else
				return repository.getAbbrByApplicationId(rs.getApplicationId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public LocalDateTime getLocalDateTimeFromTimeStramp(String date) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		// Convert to LocalDateTime
		return LocalDateTime.parse(date, formatter);
	}

	public String getFileNameWithOutExtension(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex > 0) {
			return fileName.substring(0, dotIndex);
		} else {
			return fileName;
		}
	}

	private String getMonthFromTime(String forDate) {
		// TODO Auto-generated method stub
		LocalDateTime date = getLocalDateTimeFromTimeStramp(forDate);
		return "" + date.getMonthValue();
	}

	private String getYearFromTime(String forDate) {
		// TODO Auto-generated method stub
		LocalDateTime date = getLocalDateTimeFromTimeStramp(forDate);
		return "" + date.getYear();
	}

	private String getYear(String date) {
		// Assuming format mm/dd/yyyy or m/d/yyyy
		if (date == null || !date.contains("/"))
			return "";
		String[] parts = date.split("/");
		return ((parts.length == 3) ? parts[2] : "");
	}

	private String getMonth(String date) {
		// Assuming format mm/dd/yyyy or m/d/yyyy
		if (date == null || !date.contains("/"))
			return "";
		String[] parts = date.split("/");
		return ((parts.length >= 1) ? parts[0] : "");
	}

	private void updatePublishTempTable(PublishDocumentTemp response, String uuid) {
		Optional<PublishDocumentTemp> entity = tempRepo.findById(response.getId());
		if (entity.isPresent()) {
			PublishDocumentTemp entitys = entity.get();
			entitys.setPublishUuid(stringToUuid(uuid));
			entitys.setIsUpdatedUuid(true);
			entitys = tempRepo.save(entitys);
			System.out.println("entity " + entity);
		}
	}

	private void updatePublishTempTableRs(PublishDocumentTempRS response, String uuid) {
		Optional<PublishDocumentTempRS> entity = rsDocRepo.findById(response.getId());
		if (entity.isPresent()) {
			PublishDocumentTempRS entitys = entity.get();
			entitys.setPublishUuid(stringToUuid(uuid));
			entitys.setPublishUrl(uuidToUrl(uuid));
			entitys.setIsUpdatedUuid(true);
			entitys = rsDocRepo.save(entitys);
			System.out.println("entity " + entity);
		}
	}

	public String uuidToUrl(String uuid) {
		return openKmUrl + "/dsp1/downloads/" + uuid;
	}

	private void updateNonUuid(PublishDocumentTemp response) {
		Optional<PublishDocumentTemp> entity = tempRepo.findById(response.getId());
		if (entity.isPresent()) {
			PublishDocumentTemp entitys = entity.get();
			entitys.setIsUpdatedUuid(false);
			entitys = tempRepo.save(entitys);
			System.out.println("entity " + entity);
		}
	}

	private void updateNonUuidRs(PublishDocumentTempRS response) {
		Optional<PublishDocumentTempRS> entity = rsDocRepo.findById(response.getId());
		if (entity.isPresent()) {
			PublishDocumentTempRS entitys = entity.get();
			entitys.setIsUpdatedUuid(false);
			entitys = rsDocRepo.save(entitys);

			System.out.println("entity " + entity);
		}
	}

	private UUID stringToUuid(String uuid) {
		return UUID.fromString(uuid);
	}

	@Override
	public String createDmsFolderStructure(String root, List<String> folders) {
		return folderCreationUtils.createDmsFolderStructure(root, folders);
	}

	public String getTokenFromRequest() {

		String authorizationHeader = httpRequest.getHeader("Authorization");
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			return authorizationHeader.substring(7);
		}
		return null;
	}

	// Specifications
	public static Specification<PublishDocumentTemp> hasAppName(String appName) {
		return (root, query, criteriaBuilder) -> {
			if (appName != null) {
				return criteriaBuilder.equal(root.get("applicationName"), appName);
			}
			return criteriaBuilder.conjunction(); // Returns a valid Predicate that does not filter anything
		};
	}

	public static Specification<PublishDocumentTemp> hasLocaleTemp(String locale) {
		return (root, query, criteriaBuilder) -> {
			if (locale != null) {
				return criteriaBuilder.equal(root.get("locale"), locale);
			}
			return criteriaBuilder.conjunction(); // Returns a valid Predicate that does not filter anything
		};
	}

	public static Specification<PublishDocumentTempRS> hasLocaleTempRs(String locale) {
		return (root, query, criteriaBuilder) -> {
			if (locale != null) {
				return criteriaBuilder.equal(root.get("locale"), locale);
			}
			return criteriaBuilder.conjunction(); // Returns a valid Predicate that does not filter anything
		};
	}

	public static Specification<PublishDocumentTempRS> hasAppNameRs(String appName) {
		return (root, query, criteriaBuilder) -> {
			if (appName != null) {
				return criteriaBuilder.equal(root.get("applicationName"), appName);
			}
			return criteriaBuilder.conjunction(); // Returns a valid Predicate that does not filter anything
		};
	}

	public static Specification<PublishDocumentTemp> hasLsNo(String lsNo) {
		return (root, query, criteriaBuilder) -> {
			if (lsNo != null) {
				return criteriaBuilder.equal(root.get("lsNo"), lsNo);
			}
			return criteriaBuilder.conjunction(); // Returns a valid Predicate that does not filter anything
		};
	}

	public static Specification<PublishHistoryEntity> hasHouse(String house) {
		return (root, query, criteriaBuilder) -> {
			if (house != null) {
				return criteriaBuilder.equal(root.get("house"), house);
			}
			return criteriaBuilder.conjunction(); // Returns a valid Predicate that does not filter anything
		};
	}

	public static Specification<PublishDocumentTemp> hasSessNo(String sesNo) {
		return (root, query, criteriaBuilder) -> {
			if (sesNo != null) {
				return criteriaBuilder.equal(root.get("sesNo"), sesNo);
			}
			return criteriaBuilder.conjunction(); // Returns a valid Predicate that does not filter anything
		};
	}

	/*
	 * 
	 */
	// Specifications publish history
	public static Specification<PublishHistoryEntity> hasForDate(LocalDate date) {
		return (root, query, criteriaBuilder) -> {
			if (date != null) {
				return criteriaBuilder.equal(root.get("forDate"), date);
			}
			return criteriaBuilder.conjunction(); // Returns a valid Predicate that does not filter anything
		};
	}

	public static Specification<PublishHistoryEntity> isPublishStatus(Boolean publishStatus) {
		return (root, query, criteriaBuilder) -> {
			if (publishStatus != null) {
				return criteriaBuilder.equal(root.get("publishStatus"), publishStatus);
			}
			return criteriaBuilder.conjunction(); // No filtering if null
		};
	}

	public static Specification<PublishHistoryEntity> hasApplicationName(String applicationName) {
		return (root, query, criteriaBuilder) -> {
			if (applicationName != null) {
				return criteriaBuilder.equal(root.get("applicationName"), applicationName);
			}
			return criteriaBuilder.conjunction();
		};
	}

	public static Specification<PublishHistoryEntity> hasLokSabhaNo(String lsNo) {
		return (root, query, criteriaBuilder) -> {
			if (lsNo != null) {
				return criteriaBuilder.equal(root.get("lokSabhaNo"), lsNo);
			}
			return criteriaBuilder.conjunction();
		};
	}

	public static Specification<PublishHistoryEntity> hasLokSabhaSessionNo(String sessNo) {
		String sess = "";
		if (!isRoman(sessNo)) {
			System.out.println("is Roman false " + sessNo);
			return (root, query, criteriaBuilder) -> {
				if (sessNo != null) {
					return criteriaBuilder.equal(root.get("lokSabhaSessionNo"), toRoman(Integer.parseInt(sessNo)));
				}
				return criteriaBuilder.conjunction();
			};
		} else {
			System.out.println("is Roman true " + sessNo);

			return (root, query, criteriaBuilder) -> {
				if (sessNo != null) {
					return criteriaBuilder.equal(root.get("lokSabhaSessionNo"), sessNo);
				}
				return criteriaBuilder.conjunction();
			};
		}
	}

	public static Specification<PublishHistoryEntity> hasRajyaSabhaSessionNo(String rsNo) {
		return (root, query, criteriaBuilder) -> {
			if (rsNo != null) {
				return criteriaBuilder.equal(root.get("rajyaSabhaSessionNo"), rsNo);
			}
			return criteriaBuilder.conjunction();
		};
	}

	public static boolean isRoman(String input) {
		String ROMAN_REGEX = "^(?i)(M{0,3})(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$";

		return input.matches(ROMAN_REGEX);
	}

	public static String toRoman(Integer number) {
		if (number <= 0 || number > 50) {
			throw new IllegalArgumentException("Number must be between 1 and 50");
		}
		int[] values = { 50, 40, 10, 9, 5, 4, 1 };
		String[] symbols = { "L", "XL", "X", "IX", "V", "IV", "I" };
		StringBuilder roman = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			while (number >= values[i]) {
				number -= values[i];
				roman.append(symbols[i]);
			}
		}
		return roman.toString();
	}

	public static Specification<PublishHistoryEntity> hasLocale(String locale) {
		return (root, query, criteriaBuilder) -> {
			if (locale != null) {
				return criteriaBuilder.equal(criteriaBuilder.lower(root.get("locale")), locale.toLowerCase());
			}
			return criteriaBuilder.conjunction();
		};
	}
	// old
	// @Override
	// public List<PublishHistoryDTO> getPublishedDocs(LocalDate date, String
	// applicationName, String locale) {
	// // if (locale == null) {
	// // locale = "en";
	// // }
	// Long userId = utils.getUserIdFromAuthentication();
	// System.out.println("user " + userId);
	// Specification<PublishHistoryEntity> spec =
	// Specification.where(isPublishStatus(true));
	// if (date != null) {
	// spec = spec.and(hasForDate(date));
	// }
	// if (!(StringUtils.isBlank(applicationName))) {
	// spec = spec.and(hasApplicationName(applicationName));
	// }
	// if (StringUtils.isBlank(locale)) {
	// spec = spec.and(hasLocale(locale));
	// }
	// Sort sort = Sort.by(Sort.Order.desc("forDate"));
	// List<PublishHistoryEntity> responseEntitys = repository.findAll(spec);
	// // List<PublishHistoryEntity> responseEntitys = repository.findAll();
	// List<PublishHistoryDTO> listOfDTOs = new ArrayList<>();
	// Long houseId = utils.getHouseIdFromAuthentication();
	// for (PublishHistoryEntity entity : responseEntitys) {
	// System.out.println("entity " + entity);
	// System.out.println("userId " + userId);
	//
	// Boolean exists = repository.findOfficeIdByUserId(userId,
	// entity.getApplicationId());
	// if (exists != null && exists) {
	// if (houseId == 1 && entity.getRajyaSabhaSessionNo() != null) {
	//
	// listOfDTOs.add(PublishMapper.toDTO(entity));
	// } else if (houseId == 2 && entity.getLokSabhaSessionNo() != null) {
	// listOfDTOs.add(PublishMapper.toDTO(entity));
	// }
	// }
	// }
	//
	// return listOfDTOs;
	// }

	// changes for the branch name
	@Override
	public List<PublishHistoryDTO> getPublishedDocs(LocalDate date, String applicationName, String locale) {
		Long userId = utils.getUserIdFromAuthentication();
		Long houseId = utils.getHouseIdFromAuthentication();

		Specification<PublishHistoryEntity> spec = Specification.where(isPublishStatus(true));

		if (date != null) {
			spec = spec.and(hasForDate(date));
		}

		if (!StringUtils.isBlank(applicationName)) {
			spec = spec.and(hasApplicationName(applicationName));
		}

		if (!StringUtils.isBlank(locale)) {
			spec = spec.and(hasLocale(locale));
		}

		Sort sort = Sort.by(Sort.Order.desc("forDate"));
		List<PublishHistoryEntity> responseEntities = repository.findAll(spec, sort);

		List<PublishHistoryDTO> listOfDTOs = new ArrayList<>();

		for (PublishHistoryEntity entity : responseEntities) {
			Boolean exists = repository.findOfficeIdByUserId(userId, entity.getApplicationId());

			if (exists != null && exists) {
				boolean shouldAdd = false;

				if (houseId == 1 && entity.getRajyaSabhaSessionNo() != null) {
					shouldAdd = true;
				} else if (houseId == 2 && entity.getLokSabhaSessionNo() != null) {
					shouldAdd = true;
				}

				if (shouldAdd) {
					String branch = repository.getPrimaryOfficeNameByApplicationId(entity.getApplicationId());
					listOfDTOs.add(PublishMapper.toDTO(entity, branch));
				}
			}
		}

		return listOfDTOs;
	}

	@Override
	public List<ResponseDTO> getDocsByAppId(Long appId, String abbr, String lsNo, String sesNo, LocalDate fromDate,
			LocalDate toDate) {
		List<ResponseDTO> responseList = new ArrayList<>();
		List<PublishHistoryEntity> responseEntitysAll = new ArrayList<>();
		Long houseId = utils.getHouseIdFromAuthentication();
		if (houseId == 1) {
			if (sesNo == null) {
				sesNo = repository.findLatestRS();
			}
			responseEntitysAll = repository.findByApplicationIdAndRajyaSabhaSessionNo(appId, sesNo);

		} else {
			if (sesNo == null) {
				Map<String, Object> obj = repository.findLatestLS();
				lsNo = obj.get("lsNo").toString();
				sesNo = obj.get("sesNo").toString();
			}

			else {
				// Convert numeric sesNo from request to Roman for DB query
				try {
					int numericSesNo = Integer.parseInt(sesNo);
					sesNo = convertNumberToRoman(numericSesNo);
				} catch (NumberFormatException e) {
					// If it's already a Roman numeral, leave it
				}
			}

			responseEntitysAll = repository.findByApplicationIdAndLokSabhaNoAndLokSabhaSessionNo(appId, lsNo, sesNo);
		}
		if (responseEntitysAll.isEmpty()) {
			return responseList;
		}
		for (PublishHistoryEntity entity : responseEntitysAll) {
			Boolean addRes = true;
			if (fromDate != null || toDate != null) {
				LocalDate forDate = entity.getForDate();
				System.out.println("(fromDate!=null && (forDate.isEqual(fromDate)||forDate.isAfter(fromDate)))");
				System.out.println((fromDate != null && (forDate.isEqual(fromDate) || forDate.isAfter(fromDate))));
				if (!(fromDate != null && (forDate.isEqual(fromDate) || forDate.isAfter(fromDate)))) {
					addRes = false;
					continue;
				}
				System.out.println("(toDate!=null && (forDate.isEqual(toDate)||forDate.isBefore(toDate)))");
				System.out.println((toDate != null && (forDate.isEqual(toDate) || forDate.isBefore(toDate))));
				if (!(toDate != null && (forDate.isEqual(toDate) || forDate.isBefore(toDate)))) {
					addRes = false;
					continue;
				}
			}
			System.out.println("add to dto " + addRes);
			if (addRes) {
				String text = "";
				Map<String, Object> applicationInstance = applicationInstanceRepository
						.getByApplicationIdAndApplicationNo(entity.getApplicationId(), entity.getApplicationNo());
				System.out.println("instance " + applicationInstance);
				if (applicationInstance != null || !applicationInstance.isEmpty()) {
					text += applicationInstance.get("enContent") + "<br/> " + applicationInstance.get("hiContent");
				}
				System.out.println("text " + text);
				responseList.add(ResponseDTO.builder().appId(entity.getApplicationId())
						.applicationName(entity.getApplicationName()).lsNo(entity.getLokSabhaNo()).sessNo(sesNo)
						.url(entity.getPublishUrl()).text(removeExtraTags(text)).refNo(entity.getApplicationNo())
						.docDate(entity.getForDate())
						.name(appRepository.findApplicationDisplayName(entity.getApplicationId())).build());
			}
		}
		System.out.println("responseList");
		System.out.println(responseList);

		return responseList;
	}

	/**
	 * Converts a number to its Roman numeral representation.
	 */
	private String convertNumberToRoman(int number) {
		String[] romans = { "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV",
				"XV", "XVI", "XVII", "XVIII", "XIX", "XX", "XXI", "XXII", "XXIII", "XXIV", "XXV", "XXVI", "XXVII",
				"XXVIII", "XXIX", "XXX" };
		if (number >= 1 && number <= 30) {
			return romans[number];
		}
		throw new IllegalArgumentException("Session number must be between 1 and 30");
	}

	private static String removeExtraTags(String input) {
		String cleaned = input.replace("\n", "").replace("\t", "").replace("\r", "").replace("\\\"", "\"").replace("\\",
				"");
		// System.out.println("Cleaned: " + cleaned);
		return cleaned;
	}

	@Override
	public List<PublishHistoryResponseDTO> getFilteredPublishHistory(Long applicationId, String lokSabhaNo,
			String lokSabhaSessionNo, String rajyaSabhaSessionNo, Long officeId, LocalDateTime publishDateFrom,
			LocalDateTime publishDateTo, LocalDate documentDateFrom, LocalDate documentDateTo) {

		StringBuilder sql = new StringBuilder("""
				    SELECT
						wf.id                         AS id,
						wf.lok_sabha_no               AS lokSabhaNo,
						wf.lok_sabha_session_no       AS lokSabhaSessionNo,
						wf.rajya_sabha_session_no     AS rajyaSabhaSessionNo,
						wf.created_at                 AS publishDate,
						wf.publish_by_user            AS publishByUser,
						wf.publish_uuid               AS publishUuid,
						wf.publish_status             AS publishStatus,
						wf.unpublish_date             AS unpublishDate,
						wf.unpublish_by_user          AS unpublishByUser,
						wf.for_date					  AS documentDate,
						wf.office_id                  AS officeId,
						wf.application_id             AS applicationId,
						 wf.application_no             AS applicationNo,
						o.office_name                 AS officeName,
						COALESCE(a.name, wf.application_name) AS documentName
				    FROM workflow.publish_history wf
				    JOIN master.offices o ON wf.office_id = o.id
				    LEFT JOIN master.applications a ON wf.application_id = a.id
				    WHERE 1=1
				""");

		Map<String, Object> params = new HashMap<>();

		String house = utils.getHouseAbbr();
		if (house != null && !house.isBlank()) {
			sql.append(" AND wf.house = :house");
			params.put("house", house);
		}
		if (applicationId != null) {
			sql.append(" AND wf.application_id = :applicationId");
			params.put("applicationId", applicationId);
		}
		if (lokSabhaNo != null && !lokSabhaNo.isBlank()) {
			sql.append(" AND wf.lok_sabha_no = :lokSabhaNo");
			params.put("lokSabhaNo", lokSabhaNo);
		}
		if (lokSabhaSessionNo != null && !lokSabhaSessionNo.isBlank()) {
			sql.append(" AND wf.lok_sabha_session_no = :lokSabhaSessionNo");
			params.put("lokSabhaSessionNo", lokSabhaSessionNo);
		}
		if (rajyaSabhaSessionNo != null && !rajyaSabhaSessionNo.isBlank()) {
			sql.append(" AND wf.rajya_sabha_session_no = :rajyaSabhaSessionNo");
			params.put("rajyaSabhaSessionNo", rajyaSabhaSessionNo);
		}
		if (officeId != null) {
			sql.append(" AND wf.office_id = :officeId");
			params.put("officeId", officeId);
		}
		if (publishDateFrom != null) {
			sql.append(" AND wf.created_at >= :publishDateFrom");
			params.put("publishDateFrom", publishDateFrom);
		}
		if (publishDateTo != null) {
			sql.append(" AND wf.created_at <= :publishDateTo");
			params.put("publishDateTo", publishDateTo);
		}
		if (documentDateFrom != null) {
			sql.append(" AND wf.for_date >= :documentDateFrom");
			params.put("documentDateFrom", documentDateFrom);
		}
		if (documentDateTo != null) {
			sql.append(" AND wf.for_date <= :documentDateTo");
			params.put("documentDateTo", documentDateTo);
		}

		sql.append(" ORDER BY wf.publish_date DESC");

		Query query = entityManager.createNativeQuery(sql.toString());
		params.forEach(query::setParameter);

		@SuppressWarnings("unchecked")
		List<Object[]> rows = query.getResultList();

		return rows.stream()
				.map(row -> PublishHistoryResponseDTO.builder()
						.id(row[0] != null ? ((Number) row[0]).longValue() : null)
						.lokSabhaNo(row[1] != null ? row[1].toString() : "")
						.lokSabhaSessionNo(row[2] != null ? row[2].toString() : "")
						.rajyaSabhaSessionNo(row[3] != null ? row[3].toString() : "")
						.publishDate(row[4] != null ? ((java.sql.Timestamp) row[4]).toLocalDateTime() : null)
						.publishByUser(row[5] != null ? row[5].toString() : "")
						.publishUuid(row[6] != null
								? (row[6] instanceof UUID ? (UUID) row[6] : UUID.fromString(row[6].toString()))
								: null)
						.publishStatus(row[7] != null && (Boolean) row[7])
						.unpublishDate(row[8] != null ? ((java.sql.Timestamp) row[8]).toLocalDateTime() : null)
						.unpublishByUser(row[9] != null ? row[9].toString() : "")
						.documentDate(row[10] != null ? ((java.sql.Date) row[10]).toLocalDate() : null)
						.officeId(row[11] != null ? ((Number) row[11]).longValue() : null)
						.applicationId(row[12] != null ? ((Number) row[12]).longValue() : null)
						.applicationNo(row[13] != null ? row[13].toString() : "")
						.branch(row[14] != null ? row[14].toString() : "")
						.applicationName(row[15] != null ? row[15].toString() : "").build())
				.toList();
	}

	@Override
	public String overridePublish(PublishDto dto) {

		String dsp1DocName = repository.getDsp1DocName(dto.getApplicationId());
		LocalDate forDate = LocalDate.parse(dto.getForDate());

		StringBuilder message = new StringBuilder();

		// If English document exists ---
		if (dto.getDocUuidEn() != null) {

			String refEn = generateRef(dto.getLokSabhaSessionNo());

			PublishHistoryEntity enEntity = PublishHistoryEntity.builder().lokSabhaNo(dto.getLokSabhaNo())
					.lokSabhaSessionNo(dto.getLokSabhaSessionNo()).rajyaSabhaSessionNo(dto.getRajyaSabhaSession())
					.applicationId(dto.getApplicationId()).applicationName(dsp1DocName).applicationNo(refEn)
					.publishUuid(dto.getDocUuidEn()).year(forDate.getYear()).month(forDate.getMonthValue())
					.fileType("pdf").publishUrl("").title("Override Publish").forDate(forDate).publishStatus(true)
					.publishByUser(utils.getUserFullNameFromAuthentication()).publishDate(LocalDate.now())
					.officeId(utils.getOfficeIdFromAuthentication()).locale("en").fileTypeName("pdf").house("LS")
					.build();

			repository.save(enEntity);
			message.append("English document published successfully. Reference: ").append(refEn).append(". ");
		}

		// If Hindi document exists ---
		if (dto.getDocUuidHi() != null) {

			String refHi = generateRef(dto.getLokSabhaSessionNo());

			PublishHistoryEntity hiEntity = PublishHistoryEntity.builder().lokSabhaNo(dto.getLokSabhaNo())
					.lokSabhaSessionNo(dto.getLokSabhaSessionNo()).rajyaSabhaSessionNo(dto.getRajyaSabhaSession())
					.applicationId(dto.getApplicationId()).applicationName(dsp1DocName).applicationNo(refHi)
					.publishUuid(dto.getDocUuidHi()).year(forDate.getYear()).month(forDate.getMonthValue())
					.fileType("pdf").publishUrl("").title("Override Publish").forDate(forDate).publishStatus(true)
					.publishByUser(utils.getUserFullNameFromAuthentication()).publishDate(LocalDate.now())
					.officeId(utils.getOfficeIdFromAuthentication()).locale("hi").fileTypeName("pdf").house("LS")
					.build();

			repository.save(hiEntity);
			message.append("Hindi document published successfully. Reference: ").append(refHi).append(". ");
		}

		return message.toString().trim();
	}

	private String generateRef(String sessionNo) {
		return "OVR-" + sessionNo + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-"
				+ (new Random().nextInt(90000) + 10000);
	}

}
