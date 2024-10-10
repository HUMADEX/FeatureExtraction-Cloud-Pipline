package um.persist.chatbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import java.lang.Exception;
import java.net.http.HttpHeaders;

import io.sentry.Sentry;

/**
 *
 * @author UM FERI
 * @date JAN 2021
 * @description Chatbot service class for REST redirect of Chatbot webhook
 *              endpoint
 *
 */

public class FHIRStoreRobotResource {

    public FHIRStoreRobotResource() {

    }

    public void GetResult(Exchange exchange) {

        Message camelMessage = exchange.getIn();

        System.out.println(camelMessage);
        // String dummy_json = "{\"sender\":\"dd\",\"message\":\"hey\"}";

        Map<String, Object> payload = (Map<String, Object>) camelMessage.getBody();
        Map<String, Object> errors = new LinkedHashMap<>();

        if (!payload.containsKey("user_id")) {
            errors.put("error","sender is required parameter");
        }
        if (!payload.containsKey("message")) {
            errors.put("error","message is required parameter");
        }
        if (!payload.containsKey("language")) {
            errors.put("error","language is required parameter");
        }

        String language = (String) payload.get("language");
        System.out.println("language: "+ language);
        if (!errors.isEmpty()) {language = "en";}
        //if (!language.toLowerCase().equals("en") || !language.toLowerCase().equals("sl")) {
        //    errors.put("error","language code is not correct, languages available: en, sl");
        //}

        ObjectMapper objectMapper = new ObjectMapper();
        

        Map<String, Object> mapErrorKey = new LinkedHashMap<>();
        mapErrorKey.put("error", "invalid_api_key");
        mapErrorKey.put("error_description", "API Key verification failed. Please provide the correct API Key");

        String api_key = String.valueOf(camelMessage.getHeader("Authorization"));
        System.out.println("API KEY: " + api_key);

        if (api_key.equals("um1234")) {

            try {
                String json = objectMapper.writeValueAsString(payload);
                System.out.println(json);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<?> entity = new HttpEntity<>(json, headers);

                RestTemplate restTemplate = new RestTemplate();

                // Default endpoint
                String authUri = "http://x.x.x.x:8080/fhir/Composition/";
                // String authUri = "http://x.x.x.x:5005/webhooks/rest/webhook"; // uri to
                // service which you get the token from // USE THIS ONE IN PRODUCTION
                // String authUri = "http://x.x.x.x:5005/webhooks/rest/webhook";
                ResponseEntity<Object> response = restTemplate.exchange(authUri, HttpMethod.POST, entity, Object.class);

                // System.out.println("Create Resource: " + response2);

                // String body = response.getBody().toString();
                // System.out.println(body);

                if (!errors.isEmpty()) {
                    exchange.getOut().setBody(errors);
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
                } else {
                    exchange.getOut().setBody(response.getBody());
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
                }

                // System.out.println(body);

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            /*
             * try {
             * throw new Exception("This is a test.");
             * } catch (Exception e) {
             * Sentry.captureException(e);
             * }
             */

        } else {
            exchange.getOut().setBody(mapErrorKey);
            exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
        }

    }

}
