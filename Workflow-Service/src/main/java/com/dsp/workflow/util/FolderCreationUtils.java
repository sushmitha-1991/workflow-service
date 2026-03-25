package com.dsp.workflow.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.dsp.workflow.dto.FolderCreateDTO;
import com.dsp.workflow.response.GenericResponse;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class FolderCreationUtils {
    @Autowired
    private RestTemplate restTemplate;
    @Value("${service.url}")
    private String openKmUrl;
    private static final Logger logger = LoggerFactory.getLogger(FolderCreationUtils.class);
    @Autowired
    private HttpServletRequest httpRequest;
    private final static String OKM_ROOT = "/okm:root";
    public String getTokenFromRequest() {

        String authorizationHeader = httpRequest.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
     public String trimStr(String str)
    {
        if(StringUtils.isBlank(str) || StringUtils.isEmpty(str))
        return "";
        return str.trim();
    }
    public String createDmsFolderStructure(String root, List<String> folders) {

        String path = (StringUtils.isEmpty(root)) ? OKM_ROOT : root;

        String token = getTokenFromRequest();
        if (StringUtils.isBlank(token)) {
            logger.error("Authorization token is missing. Cannot upload file.");
            return "";
        }
//        System.out.println("folder " + folders);
        String folderName = "";
        for (int i = 0; i < folders.size(); i++) {
            int size = folders.size();
            if (i > 0) {
                folderName += "/";
            }
            folderName += trimStr(folders.get(i));
            try {
                createFolderIfNotExists(path, folderName, token);

            } catch (Exception e) {

//                System.out.println(e.getLocalizedMessage());
            }
//            System.out.println("path " + path);
//            System.out.println("folderName " + folderName);

        }

//        System.out.println("complete path " + path + " createdolders " + folderName);
        if (path.endsWith("/"))
            return path + "" + folderName;
        else
            return path + "/" + folderName;
    }

    public String createFolderIfNotExists(String parentPath, String folderName, String token) {

//        System.out.println("parent path " + parentPath);
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
//                System.out.println("status " + responseBody.getStatusCode());
                if (responseBody.getStatusCode() == 200)
                // responseBody.isSuccess())
                {
                    return responseBody.getData();
                } else if (responseBody.getStatusCode() == 409) {
                    return folderPath;
                } else {
//                    System.out.println("Unexpected response: " + responseBody.getMessage());
                    return null;
                }
            } else {
//                System.out.println("Empty response received from folder creation API");
                return null;
            }

        } catch (HttpClientErrorException.Conflict e) {
//            System.out.println("status already exist conflict exception ");
            // e.printStackTrace();
            return folderPath;
        } catch (HttpClientErrorException e) {
//            System.out.println("status already exist exception");
            // e.printStackTrace();
            return folderPath;
        } catch (Exception e) {
            // e.printStackTrace();
//            System.out.println("Error occurred while creating folder: ");
            throw e;
            // return folderPath;
        }
    }


}
