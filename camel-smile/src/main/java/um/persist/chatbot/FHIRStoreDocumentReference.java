package um.persist.chatbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
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

import org.json.JSONArray;
import org.json.JSONObject;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import um.persist.swagger_examples.fhir.StoreTask.Code.Coding;

import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.model.Enumerations.DocumentReferenceStatus;

@Configuration
@RestController
@RequestMapping("/fhir")
public class FHIRStoreDocumentReference {

    private static final String FHIR_SERVER_URL = "http://x.x.x.x:8080/fhir/DocumentReference/";

    @PostMapping("/update-documentreference")
    public void updateTaskStatus(@RequestHeader("Authorization") String apiKey, Exchange exchange) {

        ObjectMapper objectMapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();

        Message camelMessage = exchange.getIn();

        String api_key = String.valueOf(camelMessage.getHeader("Authorization"));
        System.out.println("API KEY: " + api_key);

        // Validate API key
        if (!api_key.equals("api-key")) {
            Map<String, Object> apiError = new LinkedHashMap<>();
            apiError.put("error", "invalid_api_key");
            apiError.put("error_description", "API Key verification failed. Please provide the correct API Key");
            exchange.getOut().setBody(apiError);
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
            //return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
        }else{

        LocalDateTime datetime = LocalDateTime.ofInstant(java.time.Clock.systemUTC().instant(), ZoneOffset.UTC);
        String date_formatted = DateTimeFormatter.ofPattern("yyyyMMdd-hhmmss").format(datetime);
        System.out.println(date_formatted);
        //date_formatted = "timestamp";

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = objectMapper.convertValue(camelMessage.getBody(), Map.class);
        //Map<String, Object> payload = (Map<String, Object>) camelMessage.getBody();
        Map<String, Object> errors = new LinkedHashMap<>();

        if (!payload.containsKey("UserID")) {
            errors.put("error","UserID is required parameter");
        }
        if (!payload.containsKey("DeviceID")) {
            errors.put("error","DeviceID is required parameter");
        }
        if (!payload.containsKey("TaskID")) {
            errors.put("error","TaskID is required parameter");
        }
        if (!payload.containsKey("TaskIntent")) {
            errors.put("error","TaskIntent is required parameter");
        }
        if (!payload.containsKey("CarePlanID")) {
            errors.put("error","CarePlanID is required parameter");
        }
        if (!payload.containsKey("TaskOutput")) {
            errors.put("error","TaskOutput is required parameter");
        }
        if (!payload.containsKey("Date")) {
            errors.put("error","Date is required parameter");
        }
        if (!payload.containsKey("PathURL")) {
            errors.put("error","PathURL is required parameter");
        }
        if (!payload.containsKey("ContentType")) {
            errors.put("error","ContentType is required parameter");
        }



        String UserID = (String) payload.get("UserID");
        String DeviceID = (String) payload.get("DeviceID");
        String TaskID = (String) payload.get("TaskID");
        String TaskIntent = (String) payload.get("UserID");
        String CareplanID = (String) payload.get("CarePlanID");
        String TaskOutput = (String) payload.get("TaskOutput");
        String Date = (String) payload.get("Date");
        String PathURL = (String) payload.get("PathURL");
        String ContentType = (String) payload.get("ContentType");
        System.out.println("FHIRStoreDocumentReference: " + UserID + " " + ContentType);

        try {
            // Fetch the Task by ID
            //String fetchUrl = FHIR_SERVER_URL + "diary-video-" + UserID +"-"+ date_formatted;
            String fetchUrl = FHIR_SERVER_URL + "diary-video-"+ date_formatted;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            //String authUri = "http://x.x.x.x:8080/fhir/QuestionnaireResponse?patient="+user_id; // uri to service which you get the token from // USE THIS ONE IN PRODUCTION
            String authUri = fetchUrl; // uri to service which you get the token from // USE THIS ONE IN PRODUCTION
			//String authUri = "http://x.x.x.x:5005/conversations/"+user_id+"/tracker";
            //ResponseEntity<Object> response = restTemplate.exchange(authUri, HttpMethod.GET, entity, Object.class);


            /*ResponseEntity<String> taskResponse = restTemplate.exchange(fetchUrl, HttpMethod.GET, null, String.class);

            if (!taskResponse.getStatusCode().is2xxSuccessful() || taskResponse.getBody() == null) {
                errors.put("error", "Failed to fetch Task with ID: " + taskId);
                return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
            }*/

            DocumentReference documentReference = new DocumentReference();

            //String formatedID = "diary-video-" + UserID +"-"+ date_formatted;
            String formatedID = "diary-video-"+ date_formatted;

            documentReference.setId(formatedID);
            documentReference.setStatus(DocumentReferenceStatus.CURRENT);

            // Set Type
            CodeableConcept typeConcept = new CodeableConcept();
            typeConcept.setText("Diary video");
            Coding codingElement = new Coding();
            codingElement.setSystem("http://loinc.org");
            codingElement.setCode("34108-1");
            codingElement.setDisplay("Diary video");
            typeConcept.addCoding(codingElement);
            documentReference.setType(typeConcept);
            
            // Set Subject
            Reference subjectReference = new Reference();
            subjectReference.setReference("Patient/" + UserID);
            documentReference.setSubject(subjectReference);
            
            // Set Date
            documentReference.setDateElement(new InstantType(Date));
            
            // Set Content
            DocumentReference.DocumentReferenceContentComponent contentComponent = new DocumentReference.DocumentReferenceContentComponent();
            Attachment attachment = new Attachment();
            attachment.setContentType(ContentType);
            attachment.setUrl(PathURL);
            attachment.setTitle("Patient's Diary Video");
            contentComponent.setAttachment(attachment);
            documentReference.addContent(contentComponent);
            
            // Print the FHIR DocumentReference as JSON
            FhirContext fhirContext = FhirContext.forR5();
            IParser jsonParser = fhirContext.newJsonParser();
            jsonParser.setPrettyPrint(true);
            String encoded = jsonParser.encodeResourceToString(documentReference);
            System.out.println(encoded);

            // Serialize updated Task back to JSON


            // Deserialize FHIR Task using custom ObjectMapper
            //Task task = objectMapper.readValue(updatedTaskJson, Task.class);
            //task.setStatus(Task.TaskStatus.fromCode(status));
            
            // Serialize updated Task back to JSON
            //String updatedTaskJson2 = objectMapper.writeValueAsString(task);
            //System.out.println("updatedTaskJson2: " + updatedTaskJson);

            // Update the Task
            HttpHeaders headers2 = new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity2 = new HttpEntity<>(encoded, headers2);

            ResponseEntity<Object> updateResponse = restTemplate.exchange(fetchUrl, HttpMethod.PUT, entity2, Object.class);

            if (!updateResponse.getStatusCode().is2xxSuccessful() || updateResponse.getBody() == null) {
                errors.put("error", "Failed to update QuestionnaireResponse with ID: " + formatedID);
                exchange.getOut().setBody(errors);
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
                //return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Map<String, Object> response = new LinkedHashMap();
            response.put("resource_id", formatedID);
            response.put("created_resource", updateResponse.getBody());

            exchange.getOut().setBody(response);
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

            // Return the updated Task
            //return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            errors.put("error", "An error occurred: " + e.getMessage());
            exchange.getOut().setBody(errors);
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
            //return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

