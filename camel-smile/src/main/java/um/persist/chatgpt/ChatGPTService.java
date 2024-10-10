package um.persist.chatgpt;

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
import io.sentry.Sentry;

import um.persist.config.FinalVariables;

/**
 *
 * @author UM FERI
 * @date JAN 2021
 * @description Chatbot service class for REST redirect of Chatbot webhook
 *              endpoint
 *
 */

public class ChatGPTService implements FinalVariables {

    public ChatGPTService() {

    }

    public void GetResult(Exchange exchange) {

        Message camelMessage = exchange.getIn();

        System.out.println(camelMessage);
        // String dummy_json = "{\"sender\":\"dd\",\"message\":\"hey\"}";

        ObjectMapper mapObject = new ObjectMapper();
        Map<String, Object> payload = mapObject.convertValue(camelMessage.getBody(), Map.class);
        //Map<String, Object> payload = (Map<String, Object>) camelMessage.getBody();
        Map<String, Object> errors = new LinkedHashMap<>();

        if (!payload.containsKey("user_id")) {
            errors.put("error","user_id is required parameter");
        }
        if (!payload.containsKey("text")) {
            errors.put("error","text is required parameter");
        }

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> mapErrorKey = new LinkedHashMap<>();
        mapErrorKey.put("error", "invalid_api_key");
        mapErrorKey.put("error_description", "API Key verification failed. Please provide the correct API Key");

        String api_key = String.valueOf(camelMessage.getHeader("Authorization"));
        System.out.println("API KEY: " + api_key);

        if (api_key.equals(rest_api_key)) {

            try {
                String json = objectMapper.writeValueAsString(payload);
                System.out.println(json);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<?> entity = new HttpEntity<>(json, headers);

                RestTemplate restTemplate = new RestTemplate();

                // Default endpoint
                String authUri = "http://0.0.0.0:5009/chatgpt";
                
                ResponseEntity<Object> response = restTemplate.exchange(authUri, HttpMethod.POST, entity, Object.class);

                // System.out.println("Create Resource: " + response2);

                // String body = response.getBody().toString();
                // System.out.println(body);

                if (!errors.isEmpty()) {
                    exchange.getOut().setBody(errors);
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
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
