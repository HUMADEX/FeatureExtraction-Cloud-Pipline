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

public class ChatbotServiceSymptomaQuestion{

    //HashMap<String, Object> patients = new HashMap<String, Object>();
    //List<String> globList = new ArrayList<String>();
    Set<String> hashSet = new HashSet<String>();
    HashMap<String, Set<String>> symptomMap =new HashMap<String,Set<String>>();
    public static HashMap<String, HashSet<SymptomaQuestionnaire>> symptomaQuestionsMap =new HashMap<String,HashSet<SymptomaQuestionnaire>>();
    SymptomaQuestionnaire symptomaQuestionnaire;

    public ChatbotServiceSymptomaQuestion() {

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
            String end_of_questions = String.valueOf(mapObj.get("end_of_questions"));
            if(end_of_questions.toLowerCase().equals("yes")){

            }

            JSONObject jsobject = new JSONObject(mapObj);
            JSONObject jsdata = jsobject.getJSONObject("data");
			String country = String.valueOf(jsdata.get("country"));
            String language = String.valueOf(jsdata.get("language"));
            String age = String.valueOf(jsdata.get("age"));
            String sex = String.valueOf(jsdata.get("sex")).toUpperCase();
            String careplan_id = String.valueOf(jsdata.get("careplan_id"));

            symptomaQuestionnaire = new SymptomaQuestionnaire();

            String symptoma_id = String.valueOf(mapObj.get("symptomaId"));
            symptomaQuestionnaire.setSymptomaID(symptoma_id);
            String text = String.valueOf(mapObj.get("text"));
            symptomaQuestionnaire.setText(text);
            String answers = String.valueOf(mapObj.get("answer"));
            symptomaQuestionnaire.setAnswer(answers);
            String titles = String.valueOf(mapObj.get("title"));
            symptomaQuestionnaire.setTitle(titles);
            if(!symptomaQuestionsMap.containsKey(patient_id)){
               symptomaQuestionsMap.put(patient_id, new HashSet<SymptomaQuestionnaire>());
            }  
            HashSet<SymptomaQuestionnaire> h = symptomaQuestionsMap.get(patient_id);
            h.add(symptomaQuestionnaire);
            symptomaQuestionsMap.put(patient_id, h);
            System.out.println("Content of symptomaQuestionsMap: " + symptomaQuestionsMap);

            if(!symptomMap.containsKey(patient_id)){
                symptomMap.put(patient_id, new HashSet<String>());
            }
            Set<String> haSet = symptomMap.get(patient_id);
            haSet.add(symptomaQuestionnaire.getTitle());
            symptomMap.put(patient_id, haSet);
            System.out.println("Content of symptomMap: " + symptomMap);
            
            String joined = String.join(",", symptomMap.get(patient_id));
            mapObj.put("query", joined);
            System.out.println("String content of hashSet: " + joined);
			
			Map<String, Object> mapNullQuestion = new LinkedHashMap<>();
            mapNullQuestion.put("question",null);
            mapNullQuestion.put("total",0);
            mapNullQuestion.put("symptomaId", symptoma_id);
            mapNullQuestion.put("careplan_id", careplan_id);
            mapNullQuestion.put("multimedia_data", "youtube_video_uri");
            //System.out.println(mapNullQuestion);
			
            String json = objectMapper.writeValueAsString(mapObj);
            System.out.println("Final JSON sent from Emoda: " + json);

            mapObjForSymptoma.put("query", joined);
            mapObjForSymptoma.put("answer", answers);
            mapObjForSymptoma.put("text", text);
            mapObjForSymptoma.put("title", titles);
            mapObjForSymptoma.put("symptomaId", symptoma_id);
            mapObjForSymptoma.put("country", country);
            mapObjForSymptoma.put("language", language);
            mapObjForSymptoma.put("age", age);
            mapObjForSymptoma.put("sex", sex.toUpperCase());

            String json_for_symptoma = objectMapper.writeValueAsString(mapObjForSymptoma);
            System.out.println("Final JSON for Symptoma: " + json_for_symptoma);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", "api-key");
            HttpEntity<?> entity = new HttpEntity<>(json_for_symptoma, headers);

            RestTemplate restTemplate = new RestTemplate();
            String authUri = "https://api.domain.com/api/project/v1/question"; // uri to service which you get the token from // USE THIS ONE IN PRODUCTION
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

            int hashSize = symptomMap.get(patient_id).size();
            System.out.println("Number of " + patient_id + " hashSize elements: " + hashSize);

            String youtube_video_uri = "https://www.youtube.com/watch?v=AMnFA12E4cM&list=PLg9ehxVKs-j6D8zSKBCx9akSDYosxgPmv&index=9";

            if(language.toLowerCase().equals("en")){
                youtube_video_uri = "https://www.youtube.com/watch?v=AMnFA12E4cM&list=PLg9ehxVKs-j6D8zSKBCx9akSDYosxgPmv&index=9";
            }
            if(language.toLowerCase().equals("fr")){
                youtube_video_uri = "https://www.youtube.com/watch?v=AMnFA12E4cM&list=PLg9ehxVKs-j6D8zSKBCx9akSDYosxgPmv&index=9";
            }
            if(language.toLowerCase().equals("es")){
                youtube_video_uri = "https://www.youtube.com/watch?v=V_G8XxOLFAc&list=PLg9ehxVKs-j6D8zSKBCx9akSDYosxgPmv&index=12";
            }
            if(language.toLowerCase().equals("sl")){
                youtube_video_uri = "https://www.youtube.com/watch?v=Uf1hDgeNKxQ&list=PLg9ehxVKs-j6D8zSKBCx9akSDYosxgPmv&index=4";
            }
            if(language.toLowerCase().equals("lv")){
                youtube_video_uri = "https://www.youtube.com/watch?v=vnPVcE7jEdE&list=PLg9ehxVKs-j4AQLIWGbQfrBszoCEdfzca&index=6";
            }

            if(mapBody.containsKey("question")){
            //String question = String.valueOf(mapBody.get("question"));
            //System.out.println(question);
                if(mapBody.containsValue(null)){
                    

                String json2 = objectMapper.writeValueAsString(mapObj);
                System.out.println("Final JSON: " + json2);

                HttpHeaders headers2 = new HttpHeaders();
                headers2.setContentType(MediaType.APPLICATION_JSON);
                headers2.set("Authorization", "api-key");
                HttpEntity<?> entity2 = new HttpEntity<>(json2,headers2);

                RestTemplate restTemplate2 = new RestTemplate();
                String authUri2 = "https://api.domain.com/api/project/v1/causes"; // uri to service which you get the token from // USE THIS ONE IN PRODUCTION
                //String authUri = "http://x.x.x.x:5005/webhooks/rest/webhook";
                //String authUri = "http://x.x.x.x:5005/webhooks/rest/webhook"; // uri to service which you get the token from // USE THIS ONE FOR DEV
                //String authUri = "http://x.x.x.x:8080/get-result";
                ResponseEntity<Object> response2 =
                        restTemplate2.exchange(authUri2, HttpMethod.POST, entity2, Object.class); // TO-DO: ADD THE KEY API Authorization: api-key

                //System.out.println("Create Resource: " + response2);

                //String body = response.getBody().toString();
                //System.out.println(body);

                System.out.println("Patient questions history: " + symptomaQuestionsMap.get(patient_id));

                Object body2 = response2.getBody();
                Map<String, Object> mapBody2 = mapObject.convertValue(body2, Map.class);
                System.out.println("Symptoma Causes: "+mapBody2);

                LinkedHashMap<String, Object> mapBody_dcd = new LinkedHashMap<>();
                mapBody_dcd.put("reference_patient", "Patient/"+patient_id);
                mapBody_dcd.put("patient_questions_history", symptomaQuestionsMap.get(patient_id));
                mapBody_dcd.put("symptoma_causes", mapBody2);

                String json_dcd = objectMapper.writeValueAsString(mapBody_dcd);
                System.out.println("json_dcd: "+json_dcd);

                HttpHeaders headers_dcd = new HttpHeaders();
                headers_dcd.setContentType(MediaType.APPLICATION_JSON);
                headers_dcd.set("Authorization", "api-key");
                HttpEntity<?> entity_dcd = new HttpEntity<>(json_dcd,headers_dcd);

                RestTemplate restTemplate_dcd = new RestTemplate();
                String authUri_dcd = "http://x.x.x.x:5000/fhir_composition_dcd"; // uri to service which you get the token from // USE THIS ONE IN PRODUCTION
                //String authUri = "http://x.x.x.x:5005/webhooks/rest/webhook";
                //String authUri = "http://x.x.x.x:5005/webhooks/rest/webhook"; // uri to service which you get the token from // USE THIS ONE FOR DEV
                //String authUri = "http://x.x.x.x:8080/get-result";
                ResponseEntity<Object> response_dcd =
                        restTemplate_dcd.exchange(authUri_dcd, HttpMethod.POST, entity_dcd, Object.class); // TO-DO: ADD THE KEY API Authorization: api-key

                if (response_dcd.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Success: FHIR DCD Composition created successfully.");
                } else {
                    System.out.println("Error: FHIR DCD Composition was not created.");
                }

                mapBody.put("symptomaId", symptoma_id);
                mapBody.put("careplan_id", careplan_id);
                mapBody.put("multimedia_data", youtube_video_uri);

                symptomMap.get(patient_id).clear();
                symptomaQuestionsMap.get(patient_id).clear();

                }
            }

            if(mapBody.containsKey("question")){
                //String question = String.valueOf(mapBody.get("question"));
                //System.out.println(question);
                    if(!mapBody.containsValue(null)){
                        mapBody.put("country", country);
                        mapBody.put("patient_id", patient_id);
                        mapBody.put("age", age);
                        mapBody.put("sex", sex.toUpperCase());
                        mapBody.put("multimedia_data", youtube_video_uri);
                    }
                }

            //exchange.getOut().setBody(response.getBody());
            //mapBody.put("query", joined);
            exchange.getOut().setBody(mapBody);
            if(hashSize==10){
                

                String json2 = objectMapper.writeValueAsString(mapObj);
                System.out.println("Final JSON: " + json2);

                HttpHeaders headers2 = new HttpHeaders();
                headers2.setContentType(MediaType.APPLICATION_JSON);
                headers2.set("Authorization", "api-key");
                HttpEntity<?> entity2 = new HttpEntity<>(json2,headers2);

                RestTemplate restTemplate2 = new RestTemplate();
                String authUri2 = "https://api.domain.com/api/persist/v1/causes"; // uri to service which you get the token from // USE THIS ONE IN PRODUCTION
                //String authUri = "http://x.x.x.x:5005/webhooks/rest/webhook";
                //String authUri = "http://x.x.x.x:5005/webhooks/rest/webhook"; // uri to service which you get the token from // USE THIS ONE FOR DEV
                //String authUri = "http://x.x.x.x:8080/get-result";
                ResponseEntity<Object> response2 =
                        restTemplate2.exchange(authUri2, HttpMethod.POST, entity2, Object.class); // TO-DO: ADD THE KEY API Authorization: api-key

                //System.out.println("Create Resource: " + response2);

                //String body = response.getBody().toString();
                //System.out.println(body);

                System.out.println("Patient questions history: " + symptomaQuestionsMap.get(patient_id));

                Object body2 = response2.getBody();
                Map<String, Object> mapBody2 = mapObject.convertValue(body2, Map.class);
                System.out.println("Symptoma Causes: "+mapBody2);

                LinkedHashMap<String, Object> mapBody_dcd = new LinkedHashMap<>();
                mapBody_dcd.put("reference_patient", "Patient/"+patient_id);
                mapBody_dcd.put("patient_questions_history", symptomaQuestionsMap.get(patient_id));
                mapBody_dcd.put("symptoma_causes", mapBody2);

                String json_dcd = objectMapper.writeValueAsString(mapBody_dcd);
                System.out.println("json_dcd: "+json_dcd);

                HttpHeaders headers_dcd = new HttpHeaders();
                headers_dcd.setContentType(MediaType.APPLICATION_JSON);
                headers_dcd.set("Authorization", "api-key");
                HttpEntity<?> entity_dcd = new HttpEntity<>(json_dcd,headers_dcd);

                RestTemplate restTemplate_dcd = new RestTemplate();
                String authUri_dcd = "http://x.x.x.x:5000/fhir_composition_dcd"; // uri to service which you get the token from // USE THIS ONE IN PRODUCTION
                //String authUri = "http://x.x.x.x:5005/webhooks/rest/webhook";
                //String authUri = "http://x.x.x.x:5005/webhooks/rest/webhook"; // uri to service which you get the token from // USE THIS ONE FOR DEV
                //String authUri = "http://x.x.x.x:8080/get-result";
                ResponseEntity<Object> response_dcd =
                        restTemplate_dcd.exchange(authUri_dcd, HttpMethod.POST, entity_dcd, Object.class); // TO-DO: ADD THE KEY API Authorization: api-key

                if (response_dcd.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Success: FHIR DCD Composition created successfully.");
                } else {
                    System.out.println("Error: FHIR DCD Composition was not created.");
                }

                symptomMap.get(patient_id).clear();
                symptomaQuestionsMap.get(patient_id).clear();

                exchange.getOut().setBody(mapNullQuestion);
            }
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
            //System.out.println(body);

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