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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;

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

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import um.persist.config.*;

@Configuration
@RestController
@RequestMapping("/fhir")
public class DiaryServiceV2D implements FinalVariables, Serializable {

    @PostMapping("/diary-mrast-v2d")
    public void Diary(@RequestHeader("Authorization") String apiKey, Exchange exchange) throws IOException, MessagingException, JSchException, SftpException {

        System.out.println("-------------------------------------------------------");
        System.out.println("---          Video processing is starting...        ---");
        System.out.println("-------------------------------------------------------");
        long startTime = System.currentTimeMillis();
        // Get the current date and time
        LocalDateTime currentTime = LocalDateTime.now();
        // Define the format for the timestamp
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // Format the current time as a string
        String formattedTimestamp = currentTime.format(formatter);
        // Print the timestamp
        System.out.println("Current Timestamp: " + formattedTimestamp);

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

        String api_key = String.valueOf(camelMessage.getHeader("Authorization"));
        System.out.println("API KEY: " + api_key);

        if (api_key.equals(hosmartai_api_key)) {

            @SuppressWarnings("unchecked")
            Map<String, Object> payload = mapObject.convertValue(camelMessage.getBody(), Map.class);
            System.out.println("payload: " + payload);
            Map<String, Object> errors = new LinkedHashMap<>();

            if (!payload.containsKey("video_url")) {
                errors.put("error","video_url is required parameter");
            }
            /*if (!payload.containsKey("language_code")) {
                errors.put("error","video_url is required parameter");
            }*/
            if (!payload.containsKey("user_id")) {
                errors.put("error","video_url is required parameter");
            }

            String document_reference = "";
            String document_reference_extractedPart = "";
            if (payload.containsKey("document_reference")) {
                document_reference = (String) payload.get("document_reference");
                System.out.println("document_reference: " + document_reference);

                // Split the string using "/" as the delimiter
                String[] parts = document_reference.split("/");

                // Take the last part of the array
                if (parts.length > 1) {
                    document_reference_extractedPart = parts[parts.length - 1];
                    System.out.println("DocumentReference ID: " + document_reference_extractedPart);
                } else {
                    System.out.println("No '/' found in document_reference");
                }
            }

            // String reference = (String) camelMessage.getHeader("reference"); //video id
            String reference_patient = (String) payload.get("user_id");

            System.out.println("User ID: " + reference_patient);

            // Make HTTP request to get Patient data
            String patientData = getPatientData(reference_patient);

            // Check and extract "code": "en"
            if (patientData != null) {
                language_code = extractLanguageCode(patientData);
                if (language_code != null) {
                    System.out.println("Found language code: " + language_code);
                } else {
                    System.out.println("Language code 'en' not found.");
                }
            } else {
                System.out.println("Failed to retrieve patient data.");
            }

            //language_code = (String) payload.get("language_code");

            System.out.println("Retrieved body language_code DiaryService: " + language_code);
            if (language_code.toLowerCase().equals("en")) {
                language = "en";
            }
            if (language_code.toLowerCase().equals("it")) {
                language = "it";
            }
            if (language_code.toLowerCase().equals("es")) {
                language = "es";
            }
            if (language_code.toLowerCase().equals("gr")) {
                language = "gr";
            }
            if (language_code.toLowerCase().equals("sl")) {
                language = "sl";
            }
            if (language_code.toLowerCase().equals("de")) {
                language = "de";
            }
            if (language_code.toLowerCase().equals("pl")) {
                language = "pl";
            }

            
            String video_url = (String) payload.get("video_url");
            // "video_url": "https://domain.com/6690f6c9a74214e3c3a55951.mov
            //video_url = "https://domain.com/"+video_url+".mov";

             // Step 1: Download the video from the URL
             System.out.println("Starting to download the video " + document_reference_extractedPart + ".mov");
             String downloadedVideoPath = "/home/user/mrast-smile/" + document_reference_extractedPart + ".mov";
             downloadVideo(video_url, downloadedVideoPath);
 
             // Step 2: Convert the downloaded video to WMV format
             //System.out.println("Starting to converting the video " + reference_patient + ".mov to "+ reference_patient + ".wmv");
             String reference = document_reference_extractedPart;
             String inputFilePath = downloadedVideoPath;
             //String targetFile = "/home/user/mrast-smile/" + reference + ".wmv";
             String targetFile = downloadedVideoPath;
             //convertVideoToWMV(inputFilePath, targetFile);

            //String reference = dh.getName();


            
            // String file_name = dh.getName();
            // whenUploadFileUsingJsch_thenSuccess(file_name);
            // whenUploadVideoFileUsingJsch_thenSuccess("patient_video.wmv");

            LinkedHashMap<String, Object> response = new LinkedHashMap<>();
            response.put("status", "File " + reference + ".mov downloaded from "+ video_url);
            response.put("filename", reference);
            String json = mapObject.writeValueAsString(response);

            // Remove the file extension
            int lastDotIndex = reference.lastIndexOf('.');
            if (lastDotIndex != -1) {
                reference = reference.substring(0, lastDotIndex);
            }

                // Directory containing files with the same name "reference"
                String directoryPath = "/home/user/mrast-smile";

                // Create a reference to the .mov file
                File selectedFile = null;

                // Get the list of files in the directory
                File directory = new File(directoryPath);
                File[] files = directory.listFiles();

                // Iterate over files
                for (File file : files) {
                    // Check if the file's name matches the reference and it is a .mov file
                    String fileName = file.getName();
                    if (fileName.startsWith(reference) && fileName.endsWith(".mov")) {
                        selectedFile = file;
                        break; // Exit loop once the file is found
                    }
                }

                // Ensure the file was found
                if (selectedFile == null) {
                    System.out.println("No matching .mov file found in the directory: " + directoryPath);
                    return;
                }

                // Assuming ObjectMapper object is already created
                ObjectMapper objectMapper = new ObjectMapper();
                System.out.println("Running http://x.x.x.x:5008 flask /features, contacting Matej server with Umut's code");

                String asr_text = "test";

                // Create multipart request with the .mov file
                MultiValueMap<String, Object> bodyfile = new LinkedMultiValueMap<>();
                bodyfile.add("file", new FileSystemResource(selectedFile));
                bodyfile.add("language", language_code);
                bodyfile.add("patient_id", reference_patient);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyfile, headers);

                    // Send POST request to the endpoint
                    RestTemplate restTemplate = new RestTemplate();
                    //ResponseEntity<String> responsezip = restTemplate.postForEntity("http://164.8.66.68:5002/features", requestEntity, String.class);

                    // Get the current date and time
                    LocalDateTime currentTimes = LocalDateTime.now();
                    // Define the format for the timestamp
                    DateTimeFormatter formatters = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    // Format the current time as a string
                    String formattedTimestamps = currentTimes.format(formatters);
                    // Print the timestamp
                    System.out.println("Current Timestamp: " + formattedTimestamps);

                    // End time
                    long endTime = System.currentTimeMillis();
                    // Calculate duration
                    long duration = endTime - startTime;
                    // Print the results
                    System.out.println("-------------------------------------------------------");
                    System.out.println("--- Processing on CAMEL finished for video: " + reference + " ---");
                    System.out.println("-------------------------------------------------------");
                    System.out.println("--- Running time: " + duration / 1000.0 + " seconds ---");
                    System.out.println("-------------------------------------------------------");

                    // Send POST request to the endpoint
                    //ResponseEntity<byte[]> responsezip = restTemplate.postForEntity("http://x.x.x.x:5004/features", requestEntity, byte[].class);
                    ResponseEntity<byte[]> responsezip = restTemplate.postForEntity("http://x.x.x.x:5008/features", requestEntity, byte[].class);


                    // Check if the response status is successful
                    if (responsezip.getStatusCode().is2xxSuccessful()) {
                        // Save the zip file locally
                        saveZipFile(responsezip.getBody(), "/home/user/mrast-smile/"+reference+"-results.zip");
                    } else {
                        // Handle unsuccessful response
                        System.out.println("Error: Failed to retrieve the zip file.");
                    }
                    long startTime_fhir = System.currentTimeMillis();

                    // Print response status and body
                    //System.out.println("Response status code: " + responsezip.getStatusCodeValue());
                    //System.out.println("Response body: " + responsezip.getBody());

                    String zipFilePathresults = "/home/user/mrast-smile/"+reference+"-results.zip";
                    // Directory where you want to extract the files
                    String extractDirPath = "/home/user/mrast-smile/";

                    // Unzip the files
                    try {
                        unzip(zipFilePathresults, extractDirPath);
                        System.out.println("Files extracted successfully to: " + extractDirPath);
                    } catch (IOException e) {
                        System.out.println("Error: Failed to extract files from the zip.");
                        e.printStackTrace();
                    }

                    //Path filePathVisual = Path.of("/home/user/mrast-smile/"+reference+"_visual.json");
                    //String json_mrast_visual_content = Files.readString(filePathVisual);
                    //Path filePathSpeech = Path.of("/home/user/mrast-smile/"+reference+"_speech.json");
                    //String json_mrast_speech_content = Files.readString(filePathSpeech);
                    //Path filePathTranscription = Path.of("/home/user/mrast-smile/analysis.language.SM."+reference+".csv.json");
                    //String json_mrast_transcription_content = Files.readString(filePathTranscription);

                    String json_mrast_transcription_raw_content = asr_text;

                    /*HttpHeaders headers9 = new HttpHeaders();
                    headers9.setContentType(MediaType.APPLICATION_JSON);
                    // headers6.set("Authorization", "test123");

                    LinkedHashMap<String, Object> requq = new LinkedHashMap<>();
                    requq.put("text", asr_text);

                    HttpEntity<?> entity9 = new HttpEntity<>(requq, headers9);
        
                    RestTemplate restTemplate9 = new RestTemplate();
                    //String authUri7 = "http://0.0.0.0:5000/fhir_composition_symptoma_umut";_mrast_visual
                    String authUri9 = "http://x.x.x.x:5101/stanza";
                    ResponseEntity<Object> res9 = restTemplate9.exchange(authUri9, HttpMethod.POST, entity9, Object.class);
                    System.out.println("output: " + res9.getStatusCode());
                    System.out.println("output: " + res9.getBody());

                    Object stanza_results;
                    stanza_results = res9.getBody();*/

                    String json_mrast_symptoms_content = "";

                    try {
                        Path filePathSymptoms = Path.of("/home/user/mrast-smile/"+reference+"_symptoms.json");
                        json_mrast_symptoms_content = Files.readString(filePathSymptoms);
                        System.out.println("Symptoms retrieved successfully.");
                    } catch (IOException e) {
                        System.out.println("Error: Failed to get MRAST symtoms.");
                        e.printStackTrace();
                    }

                    LinkedHashMap<String, Object> asr_response = new LinkedHashMap<>();
                    asr_response.put("patient_transcription", json_mrast_transcription_raw_content);
                    asr_response.put("stanza_results", json_mrast_symptoms_content);
                    asr_response.put("reference_patient", reference_patient);
                    asr_response.put("document_reference", document_reference);

                    
                    Path filePathVisual = Path.of("/home/user/mrast-smile/"+reference+"_visual.json");
                    String json_mrast_visual_content = Files.readString(filePathVisual);
        
                    HttpHeaders headers6 = new HttpHeaders();
                    headers6.setContentType(MediaType.APPLICATION_JSON);
                    // headers6.set("Authorization", "test123");
                    headers6.set("reference", reference);
                    asr_response.put("json_mrast_visual_content", json_mrast_visual_content);
                    HttpEntity<?> entity6 = new HttpEntity<>(asr_response, headers6);
        
                    RestTemplate restTemplate6 = new RestTemplate();
                    String authUri6 = "http://0.0.0.0:5000/fhir_composition_mrast_visual";
                    ResponseEntity<Object> res6 = restTemplate6.exchange(authUri6, HttpMethod.POST, entity6, Object.class);
                    System.out.println("visual output: " + res6.getStatusCode());
                    System.out.println("visual output: " + res6.getBody());


                    Path filePathSpeech = Path.of("/home/user/mrast-smile/"+reference+"_speech.json");
                    String json_mrast_speech_content = Files.readString(filePathSpeech);
        
                    HttpHeaders headers7 = new HttpHeaders();
                    headers7.setContentType(MediaType.APPLICATION_JSON);
                    // headers6.set("Authorization", "test123");
                    headers7.set("reference", reference);
                    asr_response.put("json_mrast_speech_content", json_mrast_speech_content);
                    HttpEntity<?> entity7 = new HttpEntity<>(asr_response, headers7);
        
                    RestTemplate restTemplate7 = new RestTemplate();
                    //String authUri7 = "http://0.0.0.0:5000/fhir_composition_symptoma_umut";_mrast_visual
                    String authUri7 = "http://0.0.0.0:5000/fhir_composition_mrast_speech";
                    ResponseEntity<Object> res7 = restTemplate7.exchange(authUri7, HttpMethod.POST, entity7, Object.class);
                    System.out.println("speech output: " + res7.getStatusCode());
                    System.out.println("speech output: " + res7.getBody());


                    //Path filePathTranscription = Path.of("/home/user/mrast-smile/analysis.language.SM."+reference+".csv.json");
                    Path filePathTranscription = Path.of("/home/user/mrast-smile/"+reference+"_linguistic.json");
                    String json_mrast_transcription_content = Files.readString(filePathTranscription);
                    //String json_mrast_transcription_content = asr_text;
        
                    HttpHeaders headers8 = new HttpHeaders();
                    headers8.setContentType(MediaType.APPLICATION_JSON);
                    // headers6.set("Authorization", "test123");
                    headers8.set("reference", reference);
                    asr_response.put("json_mrast_transcription_content", json_mrast_transcription_content);
                    HttpEntity<?> entity8 = new HttpEntity<>(asr_response, headers8);
        
                    RestTemplate restTemplate8 = new RestTemplate();
                    //String authUri8 = "http://0.0.0.0:5000/fhir_composition_symptoma_umut";_mrast_visual
                    String authUri8 = "http://0.0.0.0:5000/fhir_composition_mrast_transcription";
                    ResponseEntity<Object> res8 = restTemplate8.exchange(authUri8, HttpMethod.POST, entity8, Object.class);
                    System.out.println("transcription output: " + res8.getStatusCode());
                    System.out.println("transcription output: " + res8.getBody());


                    HttpHeaders headers10 = new HttpHeaders();
                    headers10.setContentType(MediaType.APPLICATION_JSON);
                    // headers6.set("Authorization", "test123");
                    headers10.set("reference", reference);
                    HttpEntity<?> entity10 = new HttpEntity<>(asr_response, headers10);
        
                    RestTemplate restTemplate10 = new RestTemplate();
                    //String authUri7 = "http://0.0.0.0:5000/fhir_composition_symptoma_umut";_mrast_visual
                    String authUri10 = "http://0.0.0.0:5000/fhir_composition_mrast_raw_transcription";
                    ResponseEntity<Object> res10 = restTemplate10.exchange(authUri10, HttpMethod.POST, entity10, Object.class);
                    System.out.println("symptoms output: " + res10.getStatusCode());
                    System.out.println("symptoms output: " + res10.getBody());

                    // Parse JSON strings into JsonNode objects
                    JsonNode speechJson = objectMapper.readTree(json_mrast_speech_content);
                    JsonNode visualJson = objectMapper.readTree(json_mrast_visual_content);
                    JsonNode transcriptionJson = objectMapper.readTree(json_mrast_transcription_content);
                    JsonNode symptomsJson = objectMapper.readTree(json_mrast_symptoms_content);

                    System.out.println("Parse JSON strings into JsonNode objects");

                    LinkedHashMap<String, Object> respon = new LinkedHashMap<>();
                    respon.put("fhir_resource_mrast_asr_symptoms", res10.getBody());
                    respon.put("fhir_resource_mrast_visual", res6.getBody());
                    respon.put("fhir_resource_mrast_speech", res7.getBody());
                    respon.put("fhir_resource_mrast_transcription", res8.getBody());
                    respon.put("asr_result", asr_text);
                    respon.put("mrast_symptoms_results", symptomsJson);
                    //respon.put("symptoms_results", stanza_results);
                    respon.put("speech_result", speechJson);
                    respon.put("visual_result", visualJson);
                    respon.put("linguistic_result", transcriptionJson);
                    //System.out.println("respon: " + respon);
                    String json_end = mapObject.writeValueAsString(respon);

                    //System.out.println("json_end: " + json_end);

                    LinkedHashMap<String, Object> respon2 = new LinkedHashMap<>();
                    respon2.put("fhir_resource_mrast_asr_symptoms", res10.getBody());
                    respon2.put("fhir_resource_mrast_visual", res6.getBody());
                    respon2.put("fhir_resource_mrast_speech", res7.getBody());
                    respon2.put("fhir_resource_mrast_transcription", res8.getBody());

                    String wiztoken = "";

                    /*HttpHeaders headers11 = new HttpHeaders();
                    headers11.setContentType(MediaType.APPLICATION_JSON);
                    // headers6.set("Authorization", "test123");
                    //headers11.set("reference", reference);
                    HttpEntity<?> entity11 = new HttpEntity<>(wiztoken, headers11);
        
                    RestTemplate restTemplate11 = new RestTemplate();
                    //String authUri7 = "http://0.0.0.0:5000/fhir_composition_symptoma_umut";_mrast_visual
                    String authUri11 = "https://x.x.x.x/api/auth/generateToken";
                    ResponseEntity<Object> res11 = restTemplate11.exchange(authUri11, HttpMethod.POST, entity11, Object.class);
                    System.out.println("output: " + res11.getStatusCode());
                    System.out.println("SMILE API TOKEN output: " + res11.getBody());

                    Map<String, Object> payloadwiz = mapObject.convertValue(res11.getBody(), Map.class);
                    System.out.println("payload: " + payloadwiz);

                    if (!payloadwiz.containsKey("access_token")) {
                        errors.put("error","video_url is required parameter");
                    }

                    String access_token = (String) payloadwiz.get("access_token");
                    String access_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6IlVvTSIsInN1YiI6IlVvTSIsInJvbGVzIjpbImFkbWluIl0sImlhdCI6MTcyNzI1MjI5NH0.Osd1nVn2IY79Y-ZugdiKkgm-QTfqdhqR0KRUhs-MhNY";
                    System.out.println("access_token: " + access_token);

                    HttpHeaders headers12 = new HttpHeaders();
                    headers12.setContentType(MediaType.APPLICATION_JSON);
                    // headers6.set("Authorization", "test123");
                    headers12.set("Authorization", "Bearer "+access_token);
                    HttpEntity<?> entity12 = new HttpEntity<>(respon2, headers12);
        
                    RestTemplate restTemplate12 = new RestTemplate();
                    //String authUri7 = "http://0.0.0.0:5000/fhir_composition_symptoma_umut";_mrast_visual
                    String authUri12 = "https://x.x.x.x/api/fhir/diary-video-results";
                    ResponseEntity<Object> res12 = restTemplate12.exchange(authUri12, HttpMethod.POST, entity12, Object.class);
                    System.out.println("output: " + res12.getStatusCode());
                    System.out.println("SMILE API UPLOAD DIARY VIDEO RESULTS output: " + res12.getBody());

                    Map<String, Object> payloadwiz2 = mapObject.convertValue(res12.getBody(), Map.class);
                    System.out.println("payload: " + payloadwiz2);*/

                    // End time
                    long endTime_fhir = System.currentTimeMillis();
                    // Calculate duration
                    long duration_fhir = endTime_fhir - startTime_fhir;
                    // Print the results
                    System.out.println("-------------------------------------------------------");
                    System.out.println("--- Storing to FHIR: " + reference + " ---");
                    System.out.println("-------------------------------------------------------");
                    System.out.println("--- Running time: " + duration_fhir / 1000.0 + " seconds ---");
                    System.out.println("-------------------------------------------------------");

                    // Get the current date and time
                    LocalDateTime currentTimes2 = LocalDateTime.now();
                    // Define the format for the timestamp
                    DateTimeFormatter formatters2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    // Format the current time as a string
                    String formattedTimestamps2 = currentTimes.format(formatters2);
                    // Print the timestamp
                    System.out.println("Current Timestamp: " + formattedTimestamps2);

                    // End time
                    long endTimeTotal = System.currentTimeMillis();
                    // Calculate duration
                    long durationTotal = endTimeTotal - startTime;
                    // Print the results
                    System.out.println("-------------------------------------------------------");
                    System.out.println("--- All processing finished for video: " + reference + " ---");
                    System.out.println("-------------------------------------------------------");
                    System.out.println("--- Total running time: " + durationTotal / 1000.0 + " seconds ---");
                    System.out.println("-------------------------------------------------------");

                    exchange.getOut().setBody(respon);
                    exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
                    exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

        }else {
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

    private static void addToZip(File file, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            // Create a new zip entry with the same name as the file
            ZipEntry zipEntry = new ZipEntry(file.getName());

            // Add the zip entry to the zip output stream
            zos.putNextEntry(zipEntry);

            // Write file content to the zip output stream
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            // Close the zip entry
            zos.closeEntry();
        }
    }

    private static void unzip(String zipFilePath, String extractDirPath) throws IOException {
        byte[] buffer = new byte[1024];

        // Create output directory if it doesn't exist
        File extractDir = new File(extractDirPath);
        if (!extractDir.exists()) {
            extractDir.mkdirs();
        }

        // Get the zip file input stream
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            // Get the zip file entry
            ZipEntry zipEntry = zis.getNextEntry();

            // Iterate through each entry in the zip file
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                File newFile = new File(extractDirPath + File.separator + fileName);

                // Create the file's parent directory if it doesn't exist
                new File(newFile.getParent()).mkdirs();

                // Create the file
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    // Read bytes from the zip entry and write to the output file
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                // Move to the next entry in the zip file
                zipEntry = zis.getNextEntry();
            }
            // Close the zip input stream
            zis.closeEntry();
        }
    }

    private static void saveZipFile(byte[] zipBytes, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(zipBytes);
            System.out.println("Zip file saved successfully to: " + filePath);
        } catch (IOException e) {
            System.out.println("Error: Failed to save the zip file.");
            e.printStackTrace();
        }
    }

    private void downloadVideo(String videoUrl, String outputPath) throws IOException {
        URL url = new URL(videoUrl);
        File outputFile = new File(outputPath);
        FileUtils.copyURLToFile(url, outputFile);
        System.out.println("Video downloaded successfully: " + outputPath);
    }

    private void convertVideoToWMV(String inputFilePath, String outputFilePath) {
        String[] cmd = {
            "ffmpeg",
            "-y",
            "-i", inputFilePath,
            "-c:v", "wmv2",
            "-c:a", "wmav2",
            outputFilePath
        };
    
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process process = pb.start();
            process.waitFor();
    
            int exitValue = process.exitValue();
            if (exitValue == 0) {
                System.out.println("Video conversion to WMV successful: " + outputFilePath);
            } else {
                System.out.println("Video conversion to WMV failed");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    private static String getPatientData(String referencePatient) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        String authUri = "http://x.x.x.x:8080/fhir/Patient/" + referencePatient;

        try {
            ResponseEntity<Object> response = restTemplate.exchange(authUri, HttpMethod.GET, entity, Object.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.writeValueAsString(response.getBody());
            } else {
                System.out.println("HTTP request failed with status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String extractLanguageCode(String jsonData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonData);

            JsonNode communicationNode = rootNode.path("communication");
            if (communicationNode.isArray()) {
                for (JsonNode node : communicationNode) {
                    JsonNode languageNode = node.path("language");
                    if (languageNode.isObject()) {
                        JsonNode codingArray = languageNode.path("coding");
                        if (codingArray.isArray()) {
                            for (JsonNode codingNode : codingArray) {
                                //String system = codingNode.path("system").asText();
                                String code = codingNode.path("code").asText();
                                    return code;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
