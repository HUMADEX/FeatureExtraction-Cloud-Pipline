package um.persist.chatbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.hl7.fhir.r5.model.Task;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import java.net.http.HttpHeaders;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@RestController
@RequestMapping("/fhir")
public class FHIRStoreQuestionnaireResponse {

    private static final String FHIR_SERVER_URL = "http://x.x.x.x:8080/fhir/QuestionnaireResponse/";

    @PostMapping("/update-questionnaire")
    public ResponseEntity<Object> updateTaskStatus(@RequestHeader("Authorization") String apiKey, Exchange exchange) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        RestTemplate restTemplate = new RestTemplate();

        Message camelMessage = exchange.getIn();

        String api_key = String.valueOf(camelMessage.getHeader("Authorization"));
        System.out.println("API KEY: " + api_key);

        // Validate API key
        if (!api_key.equals("api-key")) {
            Map<String, Object> apiError = new LinkedHashMap<>();
            apiError.put("error", "invalid_api_key");
            apiError.put("error_description", "API Key verification failed. Please provide the correct API Key");
            return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
        }else{

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = objectMapper.convertValue(camelMessage.getBody(), Map.class);
        //Map<String, Object> payload = (Map<String, Object>) camelMessage.getBody();
        Map<String, Object> errors = new LinkedHashMap<>();

        if (!payload.containsKey("id")) {
            errors.put("error","ContentType is required parameter");
        }

        LocalDateTime datetime = LocalDateTime.ofInstant(java.time.Clock.systemUTC().instant(), ZoneOffset.UTC);
        String date_formatted = DateTimeFormatter.ofPattern("yyyyMMdd-hhmmss").format(datetime);
        System.out.println(date_formatted);
        
        String questID = (String) payload.get("id");
        //payload.replace(apiKey, "phq-9-"+date_formatted);
        //questID = (String) payload.get("id");
        System.out.println("FHIRStoreQuestionnaireResponse: " + questID);

        //String status = (String) payload.get("TaskStatus");

        

        try {
            // Fetch the Task by ID
            String fetchUrl = FHIR_SERVER_URL + questID;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            //String authUri = "http://x.x.x.x:8080/fhir/QuestionnaireResponse?patient="+user_id; // uri to service which you get the token from // USE THIS ONE IN PRODUCTION
            String authUri = fetchUrl; // uri to service which you get the token from // USE THIS ONE IN PRODUCTION
			//String authUri = "http://x.x.x.x:5005/conversations/"+user_id+"/tracker";
            //ResponseEntity<Object> response = restTemplate.exchange(authUri, HttpMethod.GET, entity, Object.class);


            String json = objectMapper.writeValueAsString(payload);

            /*ResponseEntity<String> taskResponse = restTemplate.exchange(fetchUrl, HttpMethod.GET, null, String.class);

            if (!taskResponse.getStatusCode().is2xxSuccessful() || taskResponse.getBody() == null) {
                errors.put("error", "Failed to fetch Task with ID: " + taskId);
                return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
            }*/

            // Serialize updated Task back to JSON


            // Deserialize FHIR Task using custom ObjectMapper
            //Task task = objectMapper.readValue(updatedTaskJson, Task.class);
            //task.setStatus(Task.TaskStatus.fromCode(status));
            
            // Serialize updated Task back to JSON
            //String updatedTaskJson2 = objectMapper.writeValueAsString(task);
            System.out.println("receivedjson: " + json);

            // Update the Task
            HttpHeaders headers2 = new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity2 = new HttpEntity<>(json, headers2);

            ResponseEntity<Object> updateResponse = restTemplate.exchange(fetchUrl, HttpMethod.PUT, entity2, Object.class);

            if (!updateResponse.getStatusCode().is2xxSuccessful() || updateResponse.getBody() == null) {
                errors.put("error", "Failed to update QuestionnaireResponse with ID: " + questID);
                return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Return the updated Task
            return new ResponseEntity<>(updateResponse.getBody(), HttpStatus.OK);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            errors.put("error", "Failed to process JSON: " + e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            errors.put("error", "An error occurred: " + e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

