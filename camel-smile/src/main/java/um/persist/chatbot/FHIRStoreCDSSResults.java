package um.persist.chatbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Quantity;
import org.hl7.fhir.r5.model.Reference;
import org.hl7.fhir.r5.model.Enumerations.ObservationStatus;
import org.hl7.fhir.r5.model.Enumerations.ObservationStatusEnumFactory;
import org.hl7.fhir.r5.model.Annotation;

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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.model.Enumerations.DocumentReferenceStatus;

@Configuration
@RestController
@RequestMapping("/fhir")
public class FHIRStoreCDSSResults {

    private static final String FHIR_SERVER_URL = "http://x.x.x.x:8080/fhir/Observation/";

    @PostMapping("/update-cdss-results")
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
        //date_formatted = "timestamp3";

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = objectMapper.convertValue(camelMessage.getBody(), Map.class);
        //Map<String, Object> payload = (Map<String, Object>) camelMessage.getBody();
        System.out.println("payload for store cdss: " + payload);
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



        try {

                // Extract payload
                String userID = (String) payload.get("UserID");
                String deviceID = (String) payload.get("DeviceID");
                String taskID = (String) payload.get("TaskID");
                String taskIntent = (String) payload.get("TaskIntent");
                String carePlanID = (String) payload.get("CarePlanID");
                String taskOutput = (String) payload.get("TaskOutput");

                // Extract User_Features
                List<Map<String, Object>> userFeatures = (List<Map<String, Object>>) payload.get("User_Features");
                String featureName = (String) userFeatures.get(0).get("Feature");
                String featureValue = (String) userFeatures.get(0).get("Value");
                String featureTimestamp = (String) userFeatures.get(0).get("Timestamp");
                String featureValue2 = (String) userFeatures.get(1).get("Value");
                String featureSource = (String) userFeatures.get(1).get("Source");

                // Create FHIR Observation resource
                Observation observation = new Observation();
                //String resourceID = "cdss-" + taskID + "-" + date_formatted;
                String resourceID = "cdss-result-"+ date_formatted;
                observation.setId(resourceID);
                observation.setStatus(ObservationStatus.FINAL);

                // Set category
                Coding categoryCoding = new Coding();
                categoryCoding.setSystem("http://terminology.hl7.org/CodeSystem/observation-category");
                categoryCoding.setCode("questionnaire");
                categoryCoding.setDisplay(taskOutput);
                CodeableConcept category = new CodeableConcept();
                category.addCoding(categoryCoding);
                observation.addCategory(category);

                // Set code
                Coding codeCoding = new Coding();
                codeCoding.setSystem("http://loinc.org");
                codeCoding.setCode("85354-9");
                codeCoding.setDisplay("CDS results for questionnaire " + taskOutput);
                CodeableConcept code = new CodeableConcept();
                code.addCoding(codeCoding);
                code.setText("CDS results for questionnaire " + taskOutput);
                observation.setCode(code);

                // Set subject (Patient reference)
                observation.setSubject(new Reference("Patient/" + userID));

                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                // Parse the featureTimestamp using the input formatter
                LocalDateTime parsedDateTime = LocalDateTime.parse(featureTimestamp, inputFormatter);
                // Convert to ISO 8601 format with a specific time zone
                ZonedDateTime zonedDateTime = parsedDateTime.atZone(ZoneOffset.UTC);
                String isoDateTime = zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                // Set effective date/time
                observation.setEffective(new org.hl7.fhir.r5.model.DateTimeType(isoDateTime));

                // Set value quantity (assuming the featureValue is numeric)
                Quantity quantity = new Quantity();
                quantity.setValue(Double.parseDouble(featureValue));
                quantity.setUnit(featureSource);
                quantity.setSystem("http://unitsofmeasure.org");
                quantity.setCode(featureTimestamp);
                observation.setValue(quantity);

                // Set interpretation (e.g., "Normal")
                Coding interpretationCoding = new Coding();
                interpretationCoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation");
                interpretationCoding.setCode(featureSource);
                interpretationCoding.setDisplay(featureValue2);
                CodeableConcept interpretation = new CodeableConcept();
                interpretation.addCoding(interpretationCoding);
                observation.addInterpretation(interpretation);

                // Add a note
                Annotation note = new Annotation();
                note.setText("No additional note needed.");
                observation.addNote(note);

                // Serialize the Observation resource to JSON
                FhirContext fhirContext = FhirContext.forR5();
                IParser parser = fhirContext.newJsonParser();
                String observationJson = parser.encodeResourceToString(observation);

                Map<String, Object> responses = new LinkedHashMap();
                responses.put("resource_id", resourceID);
                

                // Send the PUT request to FHIR server
                String putUrl = FHIR_SERVER_URL + resourceID;

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(observationJson, headers);

                ResponseEntity<String> response = restTemplate.exchange(putUrl, HttpMethod.PUT, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Observation stored successfully.");
                    exchange.getOut().setBody(Collections.singletonMap("observation_id", resourceID));
                } else {
                    System.out.println("Failed to store Observation.");
                    exchange.getOut().setBody(Collections.singletonMap("status", "failed"));
                }
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

            } catch (Exception e) {
                e.printStackTrace();
                errors.put("error", "An error occurred: " + e.getMessage());
                exchange.getOut().setBody(errors);
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
            }
    }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

