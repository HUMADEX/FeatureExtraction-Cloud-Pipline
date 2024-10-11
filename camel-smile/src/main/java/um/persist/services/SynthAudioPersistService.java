package um.persist.services;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
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

public class SynthAudioPersistService implements FinalVariables, Serializable {

    public void SynthAudioPersist(Exchange exchange) throws IOException {

        Message camelMessage = exchange.getIn();

        ObjectMapper mapObject = new ObjectMapper();
        Map<String, Object> mapObj = mapObject.convertValue(camelMessage.getBody(), Map.class);

        String api_key = String.valueOf(camelMessage.getHeader("Authorization"));
        System.out.println("API KEY: " + api_key);

        if (api_key.equals(rest_api_key)) {

            String language = mapObj.get("language").toString();

            if (language.toLowerCase().equals("en") || language.toLowerCase().equals("sl")
                    || language.toLowerCase().equals("ru") || language.toLowerCase().equals("es")
                    || language.toLowerCase().equals("fr") || language.toLowerCase().equals("lv")) {

                String gender = mapObj.get("gender").toString();

                if (gender.toLowerCase().equals("male") || gender.toLowerCase().equals("female")) {

                    String kafka_message = mapObj.get("text").toString();
                    String topic_name_producer = text_topic + language.toLowerCase();
                    String topic_name_consumer = audio_topic + language.toLowerCase();
                    String response = "no response (null)";

                    if (gender.toLowerCase().equals("male") && language.toLowerCase().equals("sl")) {
                        topic_name_producer = text_topic + language.toLowerCase() + "_MOSKI";
                        topic_name_consumer = audio_topic + language.toLowerCase()+ "_MOSKI";
                    }

                    ObjectMapper objectMapper = new ObjectMapper();

                    LinkedHashMap map = new LinkedHashMap<>();
                    map.put("text", kafka_message);
                    map.put("userID", "persist_en_10");
                    map.put("timeStamp", "timestamp");
                    map.put("date", "dt_object");
                    map.put("topic_name_producer", topic_name_producer);
                    map.put("topic_name_consumer", topic_name_consumer);
                    map.put("gender", gender);
                    map.put("language", language.toUpperCase());

                    String jsons = objectMapper.writeValueAsString(map);
                    System.out.println("Final JSON for Kafka: " + jsons);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    // headers.set("Authorization", "api-key");
                    HttpEntity<?> entity = new HttpEntity<>(jsons, headers);

                    RestTemplate restTemplate = new RestTemplate();
                    String authUri = local_tts_flask_endpoint;
                    ResponseEntity<Object> res = restTemplate.exchange(authUri, HttpMethod.POST, entity, Object.class);

                    Object body = res.getBody();
                    Map<String, Object> mapBody = mapObject.convertValue(body, Map.class);
                    // System.out.println(mapBody);

                    response = mapBody.toString();

                    //try {
                        //whenUploadFileUsingJsch_thenSuccess();

                        byte[] bytes = Files.readAllBytes(Paths.get("/home/user/hosmartaiaudio/sample.wav"));

                        File file = new File("/home/user/hosmartaiaudio/bytes.wav");
                        FileOutputStream os = new FileOutputStream(file, false);
                        os.write(bytes);
                        os.close();

                        Map<String, Object> audio_string = new HashMap<>();
                        audio_string.put("audio", bytes);

                        LinkedHashMap<String, Map<String, Object>> out = new LinkedHashMap<>();
                        out.put("sent_content", mapObj);
                        out.put("received_content", audio_string);

                        exchange.getOut().setBody(file);
                        exchange.getOut().setHeader(Exchange.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
                        exchange.getOut().setHeader("Content-Disposition",
                                "attachment; filename=\"" + kafka_message + ".wav\"");
                        //exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
                        exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

                    /*} catch (JSchException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (SftpException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }*/

                } else {
                    jsonError("invalid_gender", "Please enter valid gender. Valid gender choices are: male, female",
                            exchange);
                }

            } else {
                jsonError("invalid_language_code",
                        "Please enter valid language code. These are valid codes: en, sl, ru, es, fr, lv", exchange);
            }

        } else {
            jsonError("invalid_api_key", "API Key verification failed. Please provide the correct API Key", exchange);
        }

    }


    public Exchange jsonError(String error, String error_description, Exchange exchange) {
        Map<String, Object> mapErrorKey = new LinkedHashMap<>();
        mapErrorKey.put("error", error);
        mapErrorKey.put("error_description", error_description);
        exchange.getOut().setBody(mapErrorKey);
        exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
        return exchange;
    }

}
