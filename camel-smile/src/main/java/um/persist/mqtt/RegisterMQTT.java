package um.persist.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author UM FERI
 * @date JAN 2021
 * @description Class for user validation
 *
 */

@Service("registerMQTT")

public class RegisterMQTT {

    public void GetResult(Exchange exchange) {

        Message camelMessage = exchange.getIn();

        System.out.println(camelMessage);
        //String dummy_json = "{\"sender\":\"dd\",\"message\":\"hey\"}";

        Map<String, Object> payload = (Map<String, Object>) camelMessage.getBody();
        List<String> errors = new LinkedList<String>();

        //String user_id = (String) camelMessage.getHeader("id");
        //System.out.println("Camel path user id: "+user_id);

        if(!payload.containsKey("user_id")) {
            errors.add("user_id is required parameter");
        }
        String user_id = (String) payload.get("user_id");

        if(!payload.containsKey("user_token")) {
            errors.add("user_token is required parameter");
        }
        String user_token= (String) payload.get("user_token");

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String json = objectMapper.writeValueAsString(payload);
            System.out.println(json);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + user_token);
            HttpEntity<?> entity = new HttpEntity<>(json,headers);

            RestTemplate restTemplate = new RestTemplate();
            //String authUri = "http://164.8.66.78:5005/conversations/"+user_id+"/tracker/events"; // uri to service which you get the token from
            //String authUri = "http://164.8.66.89:5005/conversations/"+user_id+"/tracker/events"; // uri to service which you get the token from
            //String authUri = "http://164.8.66.72:8080/get-result";
            String authUri = "https://domain.com/auth/realms/persist/protocol/openid-connect/userinfo";
            try{
			ResponseEntity<Object> response =
                    restTemplate.exchange(authUri, HttpMethod.GET, entity, Object.class);

                //System.out.println("Create Resource: " + response2);

                //String body = response.getBody().toString();

                exchange.getOut().setBody(response.getBody());
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
                //System.out.println(body);
            } catch (HttpClientErrorException e) {
			  System.out.println(e.getStatusCode());
			  System.out.println(e.getResponseBodyAsString());
			  Gson g = new Gson();
                Object json_error = g.fromJson(e.getResponseBodyAsString(), Object.class);
                exchange.getOut().setBody(json_error);
			  exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
              exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
			}
			

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

}