package um.persist.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import um.persist.config.FinalVariables;

public class ASRService implements FinalVariables, Serializable {

    String language_code = "NULL";
    String language = "sl";

    public void ASR(Exchange exchange) throws IOException, MessagingException {

        Message camelMessage = exchange.getIn();

        ObjectMapper mapObject = new ObjectMapper();
        //Map<String, Object> mapObj = mapObject.convertValue(camelMessage.getBody(), Map.class);

        String api_key = String.valueOf(camelMessage.getHeader("Authorization"));
        System.out.println("API KEY: " + api_key);

        if (api_key.equals(rest_api_key)) {

            System.out.println("Retrieved body language_code ASRService: " + camelMessage.getHeader("language_code"));
            language_code = (String) camelMessage.getHeader("language_code");
            if(language_code.toLowerCase().equals("en")){
                language = "en";
            }
            if(language_code.toLowerCase().equals("lv")){
                language = "lv";
            }
            if(language_code.toLowerCase().equals("es")){
                language = "es";
            }
            if(language_code.toLowerCase().equals("fr")){
                language = "fr";
            }
            if(language_code.toLowerCase().equals("sl")){
                language = "sl";
            }
            if(language_code.toLowerCase().equals("ru")){
                language = "ru";
            }

            InputStream is = exchange.getIn().getBody(InputStream.class);
            //byte[] bytes = exchange.getIn().getBody(byte[].class);
            MimeBodyPart mimeMessage = new MimeBodyPart(is);
            DataHandler dh = mimeMessage.getDataHandler();
            exchange.getIn().setBody(dh.getInputStream());
            exchange.getIn().setHeader(Exchange.FILE_NAME, dh.getName());

            //File targetFile = new File("/home/matejr/hosmartai-api/asraudio.raw");
            //File targetFile = new File("/home/dsplab/hosmartai-api/" + dh.getName());
            File targetFile = new File("/home/user/hosmartai-api/asraudio.wav");

            FileUtils.copyInputStreamToFile(dh.getInputStream(), targetFile);

            System.out.println("File " + dh.getName() + " uploaded!");

            LinkedHashMap<String, Object> response = new LinkedHashMap<>();
            response.put("status", "File " + dh.getName() + " uploaded!");
            response.put("filename", "asraudio.wav");
            response.put("language", language);
            String json = mapObject.writeValueAsString(response);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // headers.set("Authorization", "api-key");
            HttpEntity<?> entity = new HttpEntity<>(json, headers);

            RestTemplate restTemplate = new RestTemplate();
            String authUri = local_asr_flask_endpoint;
            //String authUri = "http://0.0.0.0:5000/asr";
            //String authUri = "http://0.0.0.0:5000/asr_chunk";
            ResponseEntity<Object> res = restTemplate.exchange(authUri, HttpMethod.POST, entity, Object.class);

            Map<String, Object> response2 = new HashMap<>();
            String json2 = "no response";
            if(res.getStatusCodeValue()==200){
                response.put("status", "File " + dh.getName() + " was uploaded, processed and sent to the ASR engine.");
                response.put("asr_text_result", res.getBody());
                json2 = mapObject.writeValueAsString(response);
            }else{
                response.put("status", "Error. Problem is related to the ASR endpoints.");
                json2 = mapObject.writeValueAsString(response);
            }

            exchange.getOut().setBody(json2);
            //exchange.getOut().setHeader(Exchange.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
            //exchange.getOut().setHeader("Content-Disposition",
            //        "attachment; filename=\"" + "asraudio" + ".wav\"");
            //exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

        } else {
            jsonError("invalid_api_key", "API Key verification failed. Please provide the correct API Key", exchange);
        }

    }

    public Exchange jsonError(String error, String error_description, Exchange exchange) throws JsonProcessingException {
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
