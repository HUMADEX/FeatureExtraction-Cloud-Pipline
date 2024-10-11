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

public class DiaryService implements FinalVariables, Serializable {

    public void Diary(Exchange exchange) throws IOException, MessagingException, JSchException, SftpException {

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

            System.out
                    .println("Retrieved body language_code DiaryService: " + camelMessage.getHeader("language_code"));
            language_code = (String) camelMessage.getHeader("language_code");
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

            // String reference = (String) camelMessage.getHeader("reference"); //video id
            String reference_patient = (String) camelMessage.getHeader("user_id"); // patient id

            System.out.println("User ID: " + reference_patient);

            InputStream is = exchange.getIn().getBody(InputStream.class);
            // byte[] bytes = exchange.getIn().getBody(byte[].class);
            MimeBodyPart mimeMessage = new MimeBodyPart(is);
            DataHandler dh = mimeMessage.getDataHandler();
            exchange.getIn().setBody(dh.getInputStream());
            exchange.getIn().setHeader(Exchange.FILE_NAME, dh.getName());

            String reference = dh.getName();

            // File targetFile = new File("/home/user/hosmartai-api/asraudio.raw");
            // File targetFile = new File("/home/user/persist/" + dh.getName());
            File targetFile = new File("/home/user/mrast-smile/" + reference);

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
            int lastDotIndex = reference.lastIndexOf('.');
            if (lastDotIndex != -1) {
                reference = reference.substring(0, lastDotIndex);
            }

            String inputFilePath = "/home/user/mrast-smile/" + reference + ".wmv";
            String outputFilePath = "/home/user/mrast-smile/" + reference + ".wav";

            // Create the command to extract audio
            String[] cmd = {
                    "ffmpeg",
                    "-i",
                    inputFilePath,
                    "-vn", // Disable video
                    "-acodec",
                    "pcm_s16le", // Set audio codec to PCM 16-bit little-endian
                    "-ar",
                    "44100", // Set sample rate to 44100 Hz
                    "-ac",
                    "2", // Set number of audio channels to 2 (stereo)
                    "-y", // Overwrite output file if it exists
                    outputFilePath
            };

            try {
                // Execute the FFmpeg command
                ProcessBuilder pb = new ProcessBuilder(cmd);
                Process process = pb.start();

                // Wait for the process to finish
                process.waitFor();

                // Check the process exit status
                int exitValue = process.exitValue();
                if (exitValue == 0) {
                    System.out.println("Audio extraction from diary video successful");
                } else {
                    System.out.println("Audio extraction from diary video failed");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            HttpHeaders headers31 = new HttpHeaders();
            headers31.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers31.set("Authorization", "api-key");
            byte[] bytes = Files.readAllBytes(Paths.get("/home/user/mrast-smile/" + reference + ".wav"));

            // Content-Disposition entry with metadata "name" and "filename"
            MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
            ContentDisposition contentDisposition = ContentDisposition
                    .builder("form-data")
                    .name("file")
                    .filename(reference)
                    .build();
            fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            HttpEntity<byte[]> fileEntity = new HttpEntity<>(bytes, fileMap);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileEntity);

            HttpEntity<?> entity31 = new HttpEntity<>(body, headers31);
            // HttpEntity<?> entity31 = new HttpEntity<>(json3, headers31);

            RestTemplate restTemplate31 = new RestTemplate();
            //String authUri31 = "http://domain.com:8000/api/ASR/asr/" + language;
            String authUri31 = "http://domain.com:8000/api/ASR/asr_v2/" + language;
            ResponseEntity<Object> res31 = restTemplate31.exchange(authUri31, HttpMethod.POST, entity31, Object.class);
            System.out.println("output: " + res31.getStatusCode());
            System.out.println("output: " + res31.getBody());

            String asr_text = "NULL";
            Map<String, Object> mapObj_asr = mapObject.convertValue(res31.getBody(), Map.class);
            asr_text = mapObj_asr.get("asr_text_result").toString();

            LinkedHashMap<String, Object> response3 = new LinkedHashMap<>();
            response3.put("patient_id", reference_patient);
            response3.put("text", asr_text);
            response3.put("country", language);
            response3.put("language", language);
            // response3.put("careplan_id", "test-123");
            String json3 = mapObject.writeValueAsString(response3);

            String transcriptionCsvfilePath = "/home/user/mrast-smile/" + reference + ".csv";

            // Write the string to a CSV file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(transcriptionCsvfilePath))) {
                writer.write(asr_text);
                System.out.println("CSV file created successfully at: " + transcriptionCsvfilePath);
            } catch (IOException e) {
                System.out.println("CSV file error while creating it.");
                e.printStackTrace();
            }

                // Directory containing files with the same name "reference"
                String directoryPath = "/home/user/mrast-smile";

                // Output zip file path
                String zipFilePath = directoryPath + "/" + reference + ".zip";

                // Create a ZipOutputStream to write to the zip file
                try (FileOutputStream fos = new FileOutputStream(zipFilePath);
                        ZipOutputStream zos = new ZipOutputStream(fos)) {

                    // Get list of files in the directory
                    File directory = new File(directoryPath);
                    File[] files = directory.listFiles();

                    // Iterate over files
                    for (File file : files) {
                        // Check if the file's name matches the reference and it is a CSV, WAV, or WMV
                        // file
                        String fileName = file.getName();
                        if (fileName.startsWith(reference) && (fileName.endsWith(".csv") || fileName.endsWith(".wav")
                                || fileName.endsWith(".wmv"))) {
                            // Add file to zip
                            addToZip(file, zos);
                        }
                    }

                    System.out.println("Files zipped successfully: " + zipFilePath);
                } catch (IOException e) {
                    System.out.println("Files zipp error!" );
                    e.printStackTrace();
                }

                if (res31.getStatusCode().is2xxSuccessful()) {
                    System.out.println(
                            "Running http://x.x.x.x:5003 flask /features, contacting matej server with umut's code");

                    // Check if the zip file exists
                    File zipFile = new File(zipFilePath);
                    if (!zipFile.exists()) {
                        System.out.println("ZIP file does not exist: " + zipFilePath);
                        return;
                    }

                    // Create multipart request with the zip file
                    MultiValueMap<String, Object> bodyzip = new LinkedMultiValueMap<>();
                    bodyzip.add("file", new FileSystemResource(zipFile));
                    bodyzip.add("asr_text_result", asr_text);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyzip, headers);

                    // Send POST request to the endpoint
                    RestTemplate restTemplate = new RestTemplate();
                    //ResponseEntity<String> responsezip = restTemplate.postForEntity("http://x.x.x.x:5002/features", requestEntity, String.class);

                    // Send POST request to the endpoint
                    ResponseEntity<byte[]> responsezip = restTemplate.postForEntity("http://x.x.x.x:5003/features", requestEntity, byte[].class);

                    // Check if the response status is successful
                    if (responsezip.getStatusCode().is2xxSuccessful()) {
                        // Save the zip file locally
                        saveZipFile(responsezip.getBody(), "/home/user/mrast-smile/"+reference+"-results.zip");
                    } else {
                        // Handle unsuccessful response
                        System.out.println("Error: Failed to retrieve the zip file.");
                    }

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
                    //Path filePathTranscription = Path.of("/home/user/mrast-smile/"+reference+"_features.json");
                    //String json_mrast_transcription_content = Files.readString(filePathTranscription);

                    String json_mrast_transcription_raw_content = asr_text;

                    /*HttpHeaders headers9 = new HttpHeaders();
                    headers9.setContentType(MediaType.APPLICATION_JSON);
                    // headers6.set("Authorization", "api-key");

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
                    asr_response.put("document_reference", "DocumentReference/luka-vovk-test");

                    
                    Path filePathVisual = Path.of("/home/user/mrast-smile/"+reference+"_visual.json");
                    String json_mrast_visual_content = Files.readString(filePathVisual);
        
                    HttpHeaders headers6 = new HttpHeaders();
                    headers6.setContentType(MediaType.APPLICATION_JSON);
                    // headers6.set("Authorization", "api-key");
                    headers6.set("reference", reference);
                    asr_response.put("json_mrast_visual_content", json_mrast_visual_content);
                    HttpEntity<?> entity6 = new HttpEntity<>(asr_response, headers6);
        
                    RestTemplate restTemplate6 = new RestTemplate();
                    String authUri6 = "http://0.0.0.0:5000/fhir_composition_mrast_visual";
                    ResponseEntity<Object> res6 = restTemplate6.exchange(authUri6, HttpMethod.POST, entity6, Object.class);
                    System.out.println("output: " + res6.getStatusCode());
                    System.out.println("output: " + res6.getBody());


                    Path filePathSpeech = Path.of("/home/user/mrast-smile/"+reference+"_speech.json");
                    String json_mrast_speech_content = Files.readString(filePathSpeech);
        
                    HttpHeaders headers7 = new HttpHeaders();
                    headers7.setContentType(MediaType.APPLICATION_JSON);
                    // headers6.set("Authorization", "api-key");
                    headers7.set("reference", reference);
                    asr_response.put("json_mrast_speech_content", json_mrast_speech_content);
                    HttpEntity<?> entity7 = new HttpEntity<>(asr_response, headers7);
        
                    RestTemplate restTemplate7 = new RestTemplate();
                    //String authUri7 = "http://0.0.0.0:5000/fhir_composition_symptoma_umut";_mrast_visual
                    String authUri7 = "http://0.0.0.0:5000/fhir_composition_mrast_speech";
                    ResponseEntity<Object> res7 = restTemplate7.exchange(authUri7, HttpMethod.POST, entity7, Object.class);
                    System.out.println("output: " + res7.getStatusCode());
                    System.out.println("output: " + res7.getBody());


                    //Path filePathTranscription = Path.of("/home/user/mrast-smile/analysis.language.SM."+reference+".csv.json");
                    //Path filePathTranscription = Path.of("/home/user/mrast-smile/data.language.SM.CS.json");
                    Path filePathTranscription = Path.of("/home/user/mrast-smile/"+reference+"_linguistic.json");
                    String json_mrast_transcription_content = Files.readString(filePathTranscription);
                    //String json_mrast_transcription_content = asr_text;
        
                    HttpHeaders headers8 = new HttpHeaders();
                    headers8.setContentType(MediaType.APPLICATION_JSON);
                    // headers6.set("Authorization", "api-key");
                    headers8.set("reference", reference);
                    asr_response.put("json_mrast_transcription_content", json_mrast_transcription_content);
                    HttpEntity<?> entity8 = new HttpEntity<>(asr_response, headers8);
        
                    RestTemplate restTemplate8 = new RestTemplate();
                    //String authUri8 = "http://0.0.0.0:5000/fhir_composition_symptoma_umut";_mrast_visual
                    String authUri8 = "http://0.0.0.0:5000/fhir_composition_mrast_transcription";
                    ResponseEntity<Object> res8 = restTemplate8.exchange(authUri8, HttpMethod.POST, entity8, Object.class);
                    System.out.println("output: " + res8.getStatusCode());
                    System.out.println("output: " + res8.getBody());


                    HttpHeaders headers10 = new HttpHeaders();
                    headers10.setContentType(MediaType.APPLICATION_JSON);
                    // headers6.set("Authorization", "api-key");
                    headers10.set("reference", reference);
                    HttpEntity<?> entity10 = new HttpEntity<>(asr_response, headers10);
        
                    RestTemplate restTemplate10 = new RestTemplate();
                    //String authUri7 = "http://0.0.0.0:5000/fhir_composition_symptoma_umut";_mrast_visual
                    String authUri10 = "http://0.0.0.0:5000/fhir_composition_mrast_raw_transcription";
                    ResponseEntity<Object> res10 = restTemplate10.exchange(authUri10, HttpMethod.POST, entity10, Object.class);
                    System.out.println("output: " + res10.getStatusCode());
                    System.out.println("output: " + res10.getBody());

                   
                
                    // Assuming ObjectMapper object is already created
                    ObjectMapper objectMapper = new ObjectMapper();

                    // Parse JSON strings into JsonNode objects
                    JsonNode speechJson = objectMapper.readTree(json_mrast_speech_content);
                    JsonNode visualJson = objectMapper.readTree(json_mrast_visual_content);
                    JsonNode transcriptionJson = objectMapper.readTree(json_mrast_transcription_content);
                    JsonNode symptomsJson = objectMapper.readTree(json_mrast_symptoms_content);

                    LinkedHashMap<String, Object> respon = new LinkedHashMap<>();
                    respon.put("fhir_resource_mrast_asr_symptoms", res10.getBody());
                    respon.put("fhir_resource_mrast_visual", res6.getBody());
                    respon.put("fhir_resource_mrast_speech", res7.getBody());
                    respon.put("fhir_resource_mrast_transcription", res8.getBody());
                    respon.put("asr_result", res31.getBody());
                    respon.put("mrast_symptoms_results", symptomsJson);
                    respon.put("speech_result", speechJson);
                    respon.put("visual_result", visualJson);
                    respon.put("linguistic_result", transcriptionJson);
                    String json_end = mapObject.writeValueAsString(respon);

                    exchange.getOut().setBody(json_end);
                    exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
                    exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

                }
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

}
