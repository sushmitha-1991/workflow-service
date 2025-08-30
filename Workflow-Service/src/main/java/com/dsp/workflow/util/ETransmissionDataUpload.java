package com.dsp.workflow.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.dsp.workflow.dto.ResponseMember;
import com.dsp.workflow.entity.MemberPhotoSignTemp;
import com.dsp.workflow.repository.ApplicationInstanceRepository;
import com.dsp.workflow.repository.ApplicationRepository;
import com.dsp.workflow.repository.MemberTempUploadRepository;
import com.dsp.workflow.repository.PublishDocumentTempRepo;
import com.dsp.workflow.repository.PublishRepository;
import com.dsp.workflow.service.PublishService;
import com.dsp.workflow.service.impl.PublishServiceImpl;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;

public class ETransmissionDataUpload {
@Autowired
    private PublishRepository repository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private EntityManager entityManager;
    public static final String PHOTO = "PHOTO";
    public static final String SIGN = "SIGNATURE";

    @Value("${service.url}")
    private String openKmUrl;
    @Autowired
    private MemberUploadData memberUtils;
    @Autowired 
    private PublishService publishService;

    @Autowired
    private MemberTempUploadRepository memberTempRepo;

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
    @Autowired
    private ApplicationRepository appRepository;

    private final static String OKM_ROOT = "/okm:root";

    public byte[] getUrlAnReturnFileData(String url, String typeOfDoc) {
        // TODO Auto-generated method stub
        try {
            byte[] fileData = restTemplate.getForObject(url, byte[].class);
            if (fileData == null || fileData.length == 0) {
                throw new RuntimeException("Empty file retrieved for " + typeOfDoc + " from URL: " + url);
            }
            return fileData;        
        } catch (Exception e) {
            throw new RuntimeException("file not found!!");
        }
    }


    
    public void updateNonUuidInMemPhoto(MemberPhotoSignTemp response) {
        Optional<MemberPhotoSignTemp> entity = memberTempRepo
                .findById(response.getId());
        if (entity.isPresent()) {
            MemberPhotoSignTemp entitys = entity.get();
            // entitys.setIsUpdatedUuid(false);
            entitys = memberTempRepo.save(entitys);
            System.out.println("entity " + entity);
        }
    }

    public void updateNonUuidInMemSignature(MemberPhotoSignTemp response) {
        Optional<MemberPhotoSignTemp> entity = memberTempRepo
                .findById(response.getId());
        if (entity.isPresent()) {
            MemberPhotoSignTemp entitys = entity.get();
            // entitys.setIsUpdatedUuid(false);
            entitys = memberTempRepo.save(entitys);
            System.out.println("entity " + entity);
        }
    }
    public String getTokenFromRequest() {

        String authorizationHeader = httpRequest.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
    public ResponseMember uploadAndReturnUuidInMem(MemberPhotoSignTemp response, byte[] fileData, String fileType) {
        try {
            System.out.println("response " + response);
            String uploadUrl = openKmUrl + "/document/upload";
            if (fileData == null) {
                if (PHOTO.equalsIgnoreCase(fileType))
                    updateNonUuidInMemPhoto(response);
                if (SIGN.equalsIgnoreCase(fileType))
                    updateNonUuidInMemSignature(response);
            }
            // // Set Authorization Token
            String token = getTokenFromRequest();
            if (token == null || token.isEmpty()) {
                logger.error("Authorization token is missing. Cannot upload file.");
                return null;
            }
            List<String> folders = new ArrayList();
            String houseAbbr = utils.getHouseAbbr();
            folders.add(houseAbbr);
            folders.add("MEMBER");
            folders.add(response.getMpcode().toString());
            folders.add(fileType);
            System.out.println("folders " + folders);
            String rootPath = OKM_ROOT;
            String folderPath = publishService.createDmsFolderStructure(rootPath, folders);
            System.out.println("folder path " + folderPath);
            HttpHeaders headers = new HttpHeaders();
            System.out.println("uploadUrl  " + uploadUrl);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + token);
            String fileNameWithoutExt = "";
            if (PHOTO.equalsIgnoreCase(fileType))
                fileNameWithoutExt = response.getPhoto();
            if (SIGN.equalsIgnoreCase(fileType))
                fileNameWithoutExt = response.getSignature();
            System.out.println("file Name before slash and space update" + fileNameWithoutExt);

            if (fileNameWithoutExt.contains("/") || fileNameWithoutExt.contains(" ")) {
                fileNameWithoutExt = fileNameWithoutExt
                        .replace(" ", "-")
                        .replace("/", "-");
            }
            System.out.println("file Name after slash and space update" + fileNameWithoutExt);
            String updatedFileName = fileNameWithoutExt;
            // Create a temporary file

            File tempFile = File.createTempFile(fileNameWithoutExt, ".jpg");

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
            body.add("applicationNo", fileType + "_" + updatedFileName);
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
                            ResponseMember res = updateMemTableUuid(uuid, fileType, response);
                            return res;
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
        return null;
    }

    private ResponseMember updateMemTableUuid(UUID uuid, String fileType, MemberPhotoSignTemp response) {
        // TODO Auto-generated method stub
        Optional<MemberPhotoSignTemp> entity = memberTempRepo
                .findById(response.getId());
        if (entity.isPresent()) {
            MemberPhotoSignTemp entitys = entity.get();
            if (PHOTO.equalsIgnoreCase(fileType))
                entitys.setPhotoUuid(uuid);
            if (SIGN.equalsIgnoreCase(fileType))
                entitys.setSignatureUuid(uuid);
            entitys = memberTempRepo.save(entitys);
            System.out.println("entity " + entity);
            return mapMemberDataToRes(entitys);
        }
        return null;
    }

    private ResponseMember mapMemberDataToRes(MemberPhotoSignTemp entitys) {
        // TODO Auto-generated method stub
        ResponseMember res = new ResponseMember();
        res.setHouse(entitys.getHouse());
        res.setId(entitys.getId());
        res.setMemberCode(entitys.getMpcode().toString());
        res.setPhotoUrl(entitys.getPhotoUrl());
        res.setPhotoUuid(entitys.getPhotoUuid());
        res.setSignUrl(entitys.getSignatureUrl());
        res.setSignUuid(entitys.getSignatureUuid());
        return res;
    }
}
