package com.dsp.workflow.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.dsp.workflow.dto.FolderCreateDTO;
import com.dsp.workflow.dto.PublishHistoryDTO;
import com.dsp.workflow.dto.SessionDto;
import com.dsp.workflow.dto.ResponseDTO;
import com.dsp.workflow.dto.SerialNumberRequest;
import com.dsp.workflow.dto.SerialNumberResponse;
import com.dsp.workflow.dto.UnPublishRequest;
import com.dsp.workflow.dto.UrlDto;
import com.dsp.workflow.entity.PublishDocumentTemp;
import com.dsp.workflow.entity.PublishDocumentTempRS;
import com.dsp.workflow.entity.PublishHistoryEntity;
import com.dsp.workflow.mapper.PublishMapper;
import com.dsp.workflow.repository.ApplicationInstanceRepository;
import com.dsp.workflow.repository.PublishDocumentTempRepo;
import com.dsp.workflow.repository.PublishRepository;
import com.dsp.workflow.response.GenericResponse;
import com.dsp.workflow.service.PublishService;
import com.dsp.workflow.util.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.criteria.Expression;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class PublishServiceImpl implements PublishService {

    @Autowired
    private PublishRepository repository;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${service.url}")
    private String openKmUrl;

    // private String openKmUrl="http://localhost:8889/dms/openkm";
    @Autowired
    private Utils utils;
    @Autowired
    private PublishDocumentTempRepo tempRepo;
    private static final Logger logger = LoggerFactory.getLogger(PublishServiceImpl.class);

    @Autowired
    private HttpServletRequest httpRequest;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;

    private final static String OKM_ROOT = "/okm:root";

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
        Long house = utils.getHouseIdFromAuthentication();
        if (house == 1)
            return "RS";
        else if (house == 2)
            return "LS";
        else
            return "NH";
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
            String sesNo,
            String rssNo, String house) {

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

        Sort sort = Sort.by(
                Sort.Order.desc("forDate"),
                Sort.Order.desc("id"));
        List<PublishHistoryEntity> responseEntitys = repository.findAll(spec, sort);
        // List<PublishHistoryEntity> responseEntitys = repository.findAll();
        // System.out.println("responseEntitys " + responseEntitys);

        List<PublishHistoryDTO> listOfDTOs = responseEntitys.stream().map(PublishMapper::toDTO)
                .collect(Collectors.toList());

        // System.out.println("list of Docs " + listOfDTOs);
        if (responseEntitys != null) {
            for (PublishHistoryEntity entity : responseEntitys) {
                UrlDto responseDto = UrlDto.builder().fileName(entity.getFileTypeName())
                        .name(entity.getApplicationName()).fileType(entity.getFileType()).url(entity.getPublishUrl())
                        .build();
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
    public PublishHistoryDTO updateById(Long id, UnPublishRequest request) {

        PublishHistoryEntity entity = repository.findById(id).get();
        entity.setUnpublishByUser(request.getUnpublishByUser());
        if (request.getUnpublishDate() == null) {
            request.setUnpublishDate(LocalDate.now());
        }
        entity.setUnpublishDate(request.getUnpublishDate());
        entity.setPublishStatus(false);
        entity = repository.save(entity);

        return PublishMapper.toDTO(entity);
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
                if (!isBetween(dto.getFromDate(), dto.getToDate(), response.getForDate())) {
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
                String uuid = uploadAndReturnUuid(response, fileData);
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

    private boolean isBetween(LocalDate fromDate, LocalDate toDate, String targetDateStr) {
        if (StringUtils.isBlank(targetDateStr) && fromDate == null && toDate == null)
         {  
             return true;
        }
        else if (StringUtils.isBlank(targetDateStr) && (fromDate != null || toDate != null)) {
            return false;
        }
        try {
            // Adjust this format to match the format stored in your DB
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
            LocalDate targetDate = LocalDate.parse(targetDateStr.trim(), formatter);
            System.out.println("date  in entry " + targetDate);
            if (fromDate != null && targetDate.isBefore(fromDate)) {
                return false;
            }

            if (toDate != null && targetDate.isAfter(toDate)) {
                return false;
            }

            return true;
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format: " + targetDateStr);
            return false;
        }
    }

    private Specification<PublishDocumentTemp> hasPublishUuid() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("publishUuid"));
    }

    public String uploadAndReturnUuid(PublishDocumentTemp response, byte[] fileData) {
        try {
            System.out.println("response " + response);
            String uploadUrl = openKmUrl + "/document/upload";
            if (fileData == null) {
                updateNonUuid(response);
            }
            // // Set Authorization Token
            String token = getTokenFromRequest();
            if (token == null || token.isEmpty()) {
                logger.error("Authorization token is missing. Cannot upload file.");
                return "";
            }
            String appAbbr = repository.getAbbrByApplicationId(response.getApplicationId());
            System.out.println("appAbbr " + appAbbr);
            List<String> folders = new ArrayList();
            String houseAbbr = utils.getHouseAbbr();
            folders.add(houseAbbr);
            if (appAbbr.equalsIgnoreCase("PMB") || appAbbr.equalsIgnoreCase("B2")) {
                folders.add(getYear(response.getForDate()));
                folders.add(getMonth(response.getForDate()));
            } else {
                if ("LS".equals(houseAbbr)) {
                    folders.add(response.getLsNo());
                }
                if (StringUtils.isNotBlank(response.getSesNo())) {
                    folders.add(response.getSesNo());
                }
            }

            folders.add(appAbbr);
            System.out.println("folders " + folders);
            String rootPath = OKM_ROOT;

            String folderPath = createDmsFolderStructure(rootPath, folders);
            System.out.println("folder path " + folderPath);
            HttpHeaders headers = new HttpHeaders();
            System.out.println("uploadUrl  " + uploadUrl);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + token);
            String originalFileName = response.getUrl();
            int lastDotIndex = originalFileName.lastIndexOf(".");
            String fileNameWithoutExt = (lastDotIndex == -1) ? originalFileName
                    : originalFileName.substring(0, lastDotIndex);
            System.out.println("file Name before slash and space update" + fileNameWithoutExt);

            if (fileNameWithoutExt.contains("/") || fileNameWithoutExt.contains(" ")) {
                fileNameWithoutExt = fileNameWithoutExt
                        .replace(" ", "-")
                        .replace("/", "-")
                        .replace(".", "-");
            }
            System.out.println("file Name after slash and space update" + fileNameWithoutExt);
            String updatedFileName = fileNameWithoutExt + ".pdf";
            // Create a temporary file

            File tempFile = File.createTempFile(fileNameWithoutExt, ".pdf");
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
            body.add("applicationNo", response.getApplicationId());
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
                            updatePublishTempTable(response, "" + uuid);
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
        }
        return "";
    }

    private String getYear(String date) {
        // Assuming format mm/dd/yyyy or m/d/yyyy
        if (date == null || !date.contains("/"))
            return "";
        String[] parts = date.split("/");
        return (parts.length == 3) ? parts[2] : "";
    }

    private String getMonth(String date) {
        // Assuming format mm/dd/yyyy or m/d/yyyy
        if (date == null || !date.contains("/"))
            return "";
        String[] parts = date.split("/");
        return (parts.length >= 1) ? parts[0] : "";
    }

    private void updatePublishTempTable(PublishDocumentTemp response, String uuid) {
        Optional<PublishDocumentTemp> entity = tempRepo
                .findById(response.getId());
        if (entity.isPresent()) {
            PublishDocumentTemp entitys = entity.get();
            entitys.setPublishUuid(stringToUuid(uuid));
            entitys.setIsUpdatedUuid(true);
            entitys = tempRepo.save(entitys);
            System.out.println("entity " + entity);
        }
    }

    private void updateNonUuid(PublishDocumentTemp response) {
        Optional<PublishDocumentTemp> entity = tempRepo
                .findById(response.getId());
        if (entity.isPresent()) {
            PublishDocumentTemp entitys = entity.get();
            entitys.setIsUpdatedUuid(false);
            entitys = tempRepo.save(entitys);
            System.out.println("entity " + entity);
        }
    }

    private UUID stringToUuid(String uuid) {
        return UUID.fromString(uuid);
    }

    @Override
    public String createDmsFolderStructure(String root, List<String> folders) {

        String path = (StringUtils.isEmpty(root)) ? OKM_ROOT : root;

        String token = getTokenFromRequest();
        if (StringUtils.isBlank(token)) {
            logger.error("Authorization token is missing. Cannot upload file.");
            return "";
        }
        System.out.println("folder " + folders);
        String folderName = "";
        for (int i = 0; i < folders.size(); i++) {
            int size = folders.size();
            if (i > 0) {
                folderName += "/";
            }
            folderName += folders.get(i);
            try {
                createFolderIfNotExists(path, folderName, token);

            } catch (Exception e) {

                System.out.println(e.getLocalizedMessage());
            }
            System.out.println("path " + path);
            System.out.println("folderName " + folderName);

        }

        System.out.println("complete path " + path + " createdolders " + folderName);
        if (path.endsWith("/"))
            return path + "" + folderName;
        else
            return path + "/" + folderName;
    }

    private String createFolderIfNotExists(String parentPath, String folderName, String token) {

        System.out.println("parent path " + parentPath);
        // System.out.println("folder name "+folderName);
        if (StringUtils.isBlank(parentPath))
            parentPath = "/okm:root";
        String url = openKmUrl + "/folders/create?parentPath=" + parentPath;
        // System.out.println("Hardcoded Final URL: " + url);
        // System.out.println("folderName: " + folderName);
        // System.out.println("token: " + token);
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Authorization token is missing");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // System.out.println("headers " + headers);
        FolderCreateDTO dto = FolderCreateDTO.builder()
                .folderName(folderName)
                .build();
        // System.out.println("dto " + dto);
        HttpEntity<FolderCreateDTO> entity = new HttpEntity<>(dto, headers);
        String folderPath = parentPath + "/" + folderName;
        // System.out.println("path " + folderPath);
        // System.out.println("create api entity " + entity);
        try {
            ResponseEntity<GenericResponse<String>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<GenericResponse<String>>() {
                    });
            // System.out.println("response of folder creation "+response);
            GenericResponse<String> responseBody = response.getBody();
            // System.out.println("status " + responseBody);
            if (responseBody != null) {
                System.out.println("status " + responseBody.getStatusCode());
                if (responseBody.getStatusCode() == 200)
                // responseBody.isSuccess())
                {
                    return responseBody.getData();
                } else if (responseBody.getStatusCode() == 409) {
                    return folderPath;
                } else {
                    System.out.println("Unexpected response: " + responseBody.getMessage());
                    return null;
                }
            } else {
                System.out.println("Empty response received from folder creation API");
                return null;
            }

        } catch (HttpClientErrorException.Conflict e) {
            System.out.println("status already exist conflict exception ");
            // e.printStackTrace();
            return folderPath;
        } catch (HttpClientErrorException e) {
            System.out.println("status already exist exception");
            // e.printStackTrace();
            return folderPath;
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println("Error occurred while creating folder: ");
            throw e;
            // return folderPath;
        }
    }

    public String uploadAndReturnUuidRs(PublishDocumentTemp response, byte[] fileData) {
        try {
            String folderPath = "/okm:root/";
            String folderCheckUrl = openKmUrl + "/folder/getPath?path=" + folderPath;
            // + URLEncoder.encode(folderPath, StandardCharsets.UTF_8);
            System.out.println("folder   " + folderPath);
            System.out.println("folder url " + folderCheckUrl);
            System.out.println("response   " + response);
            System.out.println("fileData  " + fileData);
            String uploadUrl = openKmUrl + "/document/upload";
            HttpHeaders headers = new HttpHeaders();
            System.out.println("uploadUrl  " + uploadUrl);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            // Set Authorization Token
            String token = getTokenFromRequest();
            if (token == null || token.isEmpty()) {
                logger.error("Authorization token is missing. Cannot upload file.");
                return "";
            }
            headers.set("Authorization", "Bearer " + token);
            // Extract filename and ensure .pdf extension

            // adding specific folders
            try {
                HttpEntity<String> checkEntity = new HttpEntity<>(headers);
                restTemplate.exchange(folderCheckUrl, HttpMethod.GET, checkEntity, String.class);
                logger.info("Folder exists: {}", folderPath);
            } catch (HttpClientErrorException.NotFound ex) {
                // Folder does not exist - create it
                logger.info("Folder not found, creating: {}", folderPath);
                String createFolderUrl = openKmUrl + "/folder/createSimple";
                MultiValueMap<String, String> createFolderBody = new LinkedMultiValueMap<>();
                createFolderBody.add("parentPath", "/okm:root");
                createFolderBody.add("name", "PC");
                HttpEntity<MultiValueMap<String, String>> createFolderEntity = new HttpEntity<>(createFolderBody,
                        headers);
                ResponseEntity<String> createFolderResponse = restTemplate.postForEntity(createFolderUrl,
                        createFolderEntity, String.class);
                if (createFolderResponse.getStatusCode().is2xxSuccessful()) {
                    logger.info("Folder created successfully: {}", folderPath);
                } else {
                    logger.error("Failed to create folder: {}", createFolderResponse.getStatusCode());
                    return "";
                }
            } catch (Exception e) {
                logger.error("Error while checking or creating folder: ", e);
                return "";
            }
            //
            //
            String originalFileName = response.getUrl();
            int lastDotIndex = originalFileName.lastIndexOf(".");
            String fileNameWithoutExt = (lastDotIndex == -1) ? originalFileName
                    : originalFileName.substring(0, lastDotIndex);
            String updatedFileName = fileNameWithoutExt + ".pdf";
            // Create a temporary file
            File tempFile = File.createTempFile(fileNameWithoutExt, ".pdf");
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
            body.add("applicationNo", response.getApplicationId());
            body.add("referenceId", 1);
            body.add("referenceType", "ApplicationInstance");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
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
                            Optional<PublishDocumentTemp> entity = tempRepo.findById(response.getId());
                            if (entity.isPresent()) {
                                PublishDocumentTemp temp = tempRepo.save(entity.get());
                            }
                            logger.info("entity after saving");
                            // repository.saveUuidById(uuid, response.getId());
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
            logger.error("Error uploading file: ", e);
        }
        return "";
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

    public static Specification<PublishDocumentTempRS> hasRSNo(String rsNo) {
        return (root, query, criteriaBuilder) -> {
            if (rsNo != null) {
                return criteriaBuilder.equal(root.get("rsesNo"), rsNo);
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
                return criteriaBuilder.equal(root.get("applicationName"),
                        applicationName);
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
    public List<ResponseDTO> getDocsByAppId(Long appId, String abbr, String lsNo,
            String sesNo, LocalDate fromDate, LocalDate toDate) {
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
                responseList.add(
                        ResponseDTO.builder()
                                .appId(entity.getApplicationId())
                                .applicationName(entity.getApplicationName())
                                .lsNo(entity.getLokSabhaNo())
                                .sessNo(sesNo)
                                .url(entity.getPublishUrl())
                                .text(removeExtraTags(text))
                                .build());
            }
        }
        System.out.println("responseList");
        System.out.println(responseList);

        return responseList;
    }

    private static String removeExtraTags(String input) {
        String cleaned = input
                .replace("\n", "")
                .replace("\t", "")
                .replace("\r", "")
                .replace("\\\"", "\"")
                .replace("\\", "");
        // System.out.println("Cleaned: " + cleaned);
        return cleaned;
    }

}
