package um.persist.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.msgpack.io.Input;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import um.persist.config.FinalVariables;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import um.persist.config.*;

public class UploadSMILEVideo implements FinalVariables, Serializable {

    public void UploadSMILEVideo(Exchange exchange)
            throws IOException, MessagingException, JSchException, SftpException {

        Message camelMessage = exchange.getIn();

        ObjectMapper mapObject = new ObjectMapper();
        String language_code = "NULL";
        String language = "en";
        // Map<String, Object> mapObj = mapObject.convertValue(camelMessage.getBody(),
        // Map.class);

        Map<String, Object> mapErrorKey = new LinkedHashMap<>();
        mapErrorKey.put("error", "invalid_api_key");
        mapErrorKey.put("error_description", "API Key verification failed. Please provide the correct API Key");
        String error_msg = mapObject.writeValueAsString(mapErrorKey);

        String hosmartai_api_key = "api-key"; // 
        String hosmartai_api_key2 = "api-key"; // 

        String api_key = String.valueOf(camelMessage.getHeader("Authorization"));
        System.out.println("API KEY: " + api_key);

        if (api_key.equals(hosmartai_api_key) || api_key.equals(hosmartai_api_key2)) {

            InputStream is = exchange.getIn().getBody(InputStream.class);
            // byte[] bytes = exchange.getIn().getBody(byte[].class);
            MimeBodyPart mimeMessage = new MimeBodyPart(is);
            DataHandler dh = mimeMessage.getDataHandler();
            exchange.getIn().setBody(dh.getInputStream());
            exchange.getIn().setHeader(Exchange.FILE_NAME, dh.getName());

            String reference = dh.getName();

            // File targetFile = new File("/home/matejr/hosmartai-api/asraudio.raw");
            // File targetFile = new File("/home/matejr/persist/" + dh.getName());
            File targetFile = new File("/home/user/smile-app-videos/" + reference);

            FileUtils.copyInputStreamToFile(dh.getInputStream(), targetFile);

            System.out.println("File " + dh.getName() + " uploaded!");
            // String file_name = dh.getName();
            // whenUploadFileUsingJsch_thenSuccess(file_name);
            // whenUploadVideoFileUsingJsch_thenSuccess("patient_video.wmv");

            LinkedHashMap<String, Object> response = new LinkedHashMap<>();
            response.put("status", "File " + dh.getName() + " uploaded!");
            response.put("filename", dh.getName());
            String json = mapObject.writeValueAsString(response);

            // Remove the file extension
            /*int lastDotIndex = reference.lastIndexOf('.');
            if (lastDotIndex != -1) {
                reference = reference.substring(0, lastDotIndex);
            }*/

            String inputFilePath = "/home/user/smile-app-videos/" + reference;

            HttpHeaders headers31 = new HttpHeaders();
            headers31.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers31.set("API-Key",
                    "api-key");
            byte[] bytes = Files.readAllBytes(Paths.get("/home/user/smile-app-videos/" + reference ));

            // Content-Disposition entry with metadata "name" and "filename"
            MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
            ContentDisposition contentDisposition = ContentDisposition
                    .builder("form-data")
                    .name("files")
                    .filename(reference)
                    .build();
            fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            HttpEntity<byte[]> fileEntity = new HttpEntity<>(bytes, fileMap);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("files", fileEntity);

            HttpEntity<?> entity31 = new HttpEntity<>(body, headers31);
            // HttpEntity<?> entity31 = new HttpEntity<>(json3, headers31);

            RestTemplate restTemplate31 = new RestTemplate();
            // String authUri31 = "http://x.x.x.x:8000/api/ASR/asr/" + language;
            String authUri31 = "http://x.x.x.x:5000/upload_smile";
            ResponseEntity<Object> res31 = restTemplate31.exchange(authUri31, HttpMethod.POST, entity31, Object.class);
            System.out.println("output: " + res31.getStatusCode());
            System.out.println("output: " + res31.getBody());

            // Assume the response is in a form like this: "{files=[user_video.mov], status=success}"
            // Convert it into a LinkedHashMap to map the response correctly
            //LinkedHashMap<String, Object> responseMap = new LinkedHashMap<>();
            //responseMap.put("files", new String[]{"user_video.mov"}); // Manually parse the files part
            //responseMap.put("status", "success");

            // Convert the LinkedHashMap to JSON string
            //String jsonResponse = mapObject.writeValueAsString(responseMap);
            String jsonResponse = mapObject.writeValueAsString(res31.getBody());
            System.out.println("JSON Response: " + jsonResponse);

            // Send JSON response
            exchange.getOut().setBody(jsonResponse);

        } else {
            exchange.getOut().setBody(error_msg);
            exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
        }
    }

    public Exchange jsonError(String error, String error_description, Exchange exchange)
            throws JsonProcessingException {
        ObjectMapper mapObject = new ObjectMapper();
        Map<String, Object> mapErrorKey = new LinkedHashMap<>();
        mapErrorKey.put("error", error);
        mapErrorKey.put("error_description", error_description);
        String json = mapObject.writeValueAsString(mapErrorKey);
        exchange.getOut().setBody(json);
        exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
        return exchange;
    }

   

}
