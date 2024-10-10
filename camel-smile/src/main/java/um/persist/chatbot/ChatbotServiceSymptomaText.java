package um.persist.chatbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.bouncycastle.pqc.jcajce.provider.qtesla.SignatureSpi.qTESLA;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;
import java.util.*;
import java.io.IOException;
import java.lang.Exception;
import io.sentry.Sentry;

import um.persist.swagger_examples.symptoma.*;

/**
 *
 * @author UM FERI
 * @date JAN 2021
 * @description Chatbot service class for REST redirect of Chatbot webhook endpoint
 *
 */

public class ChatbotServiceSymptomaText{

    //HashMap<String, Object> patients = new HashMap<String, Object>();
    //List<String> globList = new ArrayList<String>();
    Set<String> hashSet = new HashSet<String>();
    HashMap<String, Set<String>> symptomMap =new HashMap<String,Set<String>>();
    public static HashMap<String, HashSet<SymptomaQuestionnaire>> symptomaQuestionsMap =new HashMap<String,HashSet<SymptomaQuestionnaire>>();
    SymptomaQuestionnaire symptomaQuestionnaire;

    public ChatbotServiceSymptomaText() {

    }

    public void GetResult(Exchange exchange) {

        Message camelMessage = exchange.getIn();

        System.out.println(camelMessage);
        //String dummy_json = "{\"sender\":\"dd\",\"message\":\"hey\"}";

        ObjectMapper mapObject = new ObjectMapper();
        Map<String, Object> mapObj = mapObject.convertValue(camelMessage.getBody(), Map.class);
        Map<String, Object> mapObjForSymptoma = new HashMap<>();
        List<String> errors = new LinkedList<String>();
		
		Map<String, Object> mapErrorKey = new LinkedHashMap<>();
        mapErrorKey.put("error","invalid_api_key");
        mapErrorKey.put("error_description","API Key verification failed. Please provide the correct API Key");
		
		String api_key = String.valueOf(camelMessage.getHeader("Authorization"));
        System.out.println("API KEY: " + api_key);

        if(api_key.equals("api-key")){
			
			ObjectMapper objectMapper = new ObjectMapper();

        try {
            String patient_id = String.valueOf(mapObj.get("patient_id"));

            JSONObject jsobject = new JSONObject(mapObj);
			String country = String.valueOf(jsobject.get("country"));
            String language = String.valueOf(jsobject.get("language"));
            String text = String.valueOf(jsobject.get("text"));
            String careplan_id = String.valueOf(jsobject.get("careplan_id"));

            mapObjForSymptoma.put("text", text);
            mapObjForSymptoma.put("country", country);
            mapObjForSymptoma.put("language", language);

            String json_for_symptoma = objectMapper.writeValueAsString(mapObjForSymptoma);
            System.out.println("Final JSON for Symptoma: " + json_for_symptoma);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", "api-key");
            HttpEntity<?> entity = new HttpEntity<>(json_for_symptoma, headers);

            RestTemplate restTemplate = new RestTemplate();
            String authUri = "https://api.domain.com/api/persist/v1/extracted"; // uri to service which you get the token from // USE THIS ONE IN PRODUCTION
			//String authUri = "http://x.x.x.x:5005/webhooks/rest/webhook";
			//String authUri = "http://x.x.x.x:5005/webhooks/rest/webhook"; // uri to service which you get the token from // USE THIS ONE FOR DEV
            //String authUri = "http://x.x.x.x:8080/get-result";
            ResponseEntity<Object> response =
                    restTemplate.exchange(authUri, HttpMethod.POST, entity, Object.class); // TO-DO: ADD THE KEY API Authorization: api-key

            //System.out.println("Create Resource: " + response2);

            //String body = response.getBody().toString();
			//System.out.println(body);

            Object body = response.getBody();
            Map<String, Object> mapBody = mapObject.convertValue(body, Map.class);
            //mapBody.put("query", query);
            //System.out.println(mapBody);

            //exchange.getOut().setBody(response.getBody());
            //mapBody.put("query", joined);
            exchange.getOut().setBody(mapBody);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
			
        }else{
            exchange.getOut().setBody(mapErrorKey);
            exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
        }

    }


}