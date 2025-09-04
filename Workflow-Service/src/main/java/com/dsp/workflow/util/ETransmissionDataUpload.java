package com.dsp.workflow.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.dsp.workflow.dto.ResponseDocsUuids;
import com.dsp.workflow.dto.ResponseMember;
import com.dsp.workflow.entity.ETransmissionUploadTempEntity;
import com.dsp.workflow.entity.MemberPhotoSignTemp;
import com.dsp.workflow.repository.ETransmissionRepository;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ETransmissionDataUpload {
    @Autowired
    private ETransmissionRepository eTransmissionRepository;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FolderCreationUtils folderCreationUtils;
    @Value("${service.url}")
    private String openKmUrl;

    @Autowired
    private Utils utils;
    private static final Logger logger = LoggerFactory.getLogger(ETransmissionDataUpload.class);

    @Autowired
    private HttpServletRequest httpRequest;

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

    public void updateNonUuidInTransmission(ETransmissionUploadTempEntity response) {
        Optional<ETransmissionUploadTempEntity> entity = eTransmissionRepository
                .findById(response.getId());
        if (entity.isPresent()) {
            ETransmissionUploadTempEntity entitys = entity.get();
            entitys.setIsUpdatedAllUuid(false);
            entitys = eTransmissionRepository.save(entitys);
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

    public ResponseDocsUuids uploadAndReturnUuidResp(ETransmissionUploadTempEntity response,
            byte[] fileData, String fileType, String url) {
        try {
            System.out.println("response " + response);
            String uploadUrl = openKmUrl + "/document/upload";
            if (fileData == null) {
                updateNonUuidInTransmission(response);
                return null;
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
            folders.add("E_Papers");
            folders.add(getYear(response.getDateOfCirculation()));
            folders.add(fileType);
            System.out.println("folders " + folders);
            String rootPath = OKM_ROOT;
            String folderPath = folderCreationUtils.createDmsFolderStructure(rootPath, folders);
            System.out.println("folder path " + folderPath);
            HttpHeaders headers = new HttpHeaders();
            System.out.println("uploadUrl  " + uploadUrl);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + token);
            String fileNameWithoutExt = "";
            if (ConstantsUtility.F_EN.equalsIgnoreCase(fileType))
                fileNameWithoutExt = response.getList1Eng();
            if (ConstantsUtility.F_HI.equalsIgnoreCase(fileType))
                fileNameWithoutExt = response.getList1Hindi();
            if (ConstantsUtility.E_EN.equalsIgnoreCase(fileType))
                fileNameWithoutExt = response.getEngErrata();
            if (ConstantsUtility.E_HI.equalsIgnoreCase(fileType))
                fileNameWithoutExt = response.getHindiErrata();
            System.out.println("file Name before slash and space update" + fileNameWithoutExt);

            if (fileNameWithoutExt.contains("/") || fileNameWithoutExt.contains(" ")) {
                fileNameWithoutExt = fileNameWithoutExt
                        .replace(" ", "-")
                        .replace("/", "-");
            }
            System.out.println("file Name after slash and space update" + fileNameWithoutExt);
            String updatedFileName = fileNameWithoutExt;
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
                            ResponseDocsUuids res = updateTransmTableUuid(uuid, fileType, response, url, houseAbbr);
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

    private String getYear(LocalDate dateOfCirculation) {
        // TODO Auto-generated method stub
        if(dateOfCirculation!=null)
        {
            return ""+dateOfCirculation.getYear();
        }
        return ""+LocalDate.now().getYear();
    }

    public void processErrata(ETransmissionUploadTempEntity response, String existingUuid,
            byte[] data, String type, String url,
            List<ResponseDocsUuids> uploadedUuids) {
        if (StringUtils.isBlank(existingUuid) && data != null && data.length > 0) {
            uploadedUuids.add(uploadAndReturnUuidResp(response, data, type, url));
        }
    }

    public void processFile(ETransmissionUploadTempEntity response, String existingUuid,
            byte[] data, String type, String url,
            List<ResponseDocsUuids> uploadedUuids) {
        if (StringUtils.isBlank(existingUuid)) {
            ResponseDocsUuids respFile = uploadAndReturnUuidResp(response, data, type, url);
            uploadedUuids.add(respFile);
        }
    }

    public ResponseDocsUuids updateTransmTableUuid(UUID uuid, String fileType,
            ETransmissionUploadTempEntity response, String url, String house) {
        // TODO Auto-generated method stub
        Optional<ETransmissionUploadTempEntity> entity = eTransmissionRepository
                .findById(response.getId());
        if (entity.isPresent()) {
            ETransmissionUploadTempEntity entitys = entity.get();
            entitys.setIsUpdatedAllUuid(true);
            if (ConstantsUtility.E_EN.equalsIgnoreCase(fileType))
                entitys.setEngErrataUuid(uuid.toString());
            if (ConstantsUtility.E_HI.equalsIgnoreCase(fileType))
                entitys.setHindiErrataUuid(uuid.toString());
            if (ConstantsUtility.F_EN.equalsIgnoreCase(fileType))
                entitys.setList1EngUuid(uuid.toString());
            if (ConstantsUtility.F_HI.equalsIgnoreCase(fileType))
                entitys.setList1HindiUuid(uuid.toString());
            entitys = eTransmissionRepository.save(entitys);
            System.out.println("entity " + entity);
            ResponseDocsUuids res = new ResponseDocsUuids().builder().house(house)
                    .fileType(fileType)
                    .url(url)
                    .uuid(uuid).build();
            return res;

        }
        return null;
    }
}
