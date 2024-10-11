package um.persist.chatbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.hl7.fhir.r5.model.Task;
import org.json.JSONArray;
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
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@RestController
@RequestMapping("/fhir")
public class FHIRUpdateTask {

    private static final String FHIR_SERVER_URL_TASK = "http://x.x.x.x:8080/fhir/Task/";
    private static final String FHIR_SERVER_URL_MEDIA = "http://x.x.x.x:8080/fhir/DocumentReference";
    private static final String CAMEL_API_URL_MRAST = "https://x.x.x.x/api/Diary/mrast";
    private static final String TRIGGER_CDS_API_URL = "https://x.x.x.x/api/fhir/trigger-cdss";

    @PostMapping("/update-task-status")
    public ResponseEntity<Object> updateTaskStatus(@RequestHeader("Authorization") String apiKey, Exchange exchange) {
        Map<String, Object> errors = new LinkedHashMap<>();

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
            return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.convertValue(camelMessage.getBody(), Map.class);
            System.out.println("FHIRUpdateTask - payload from rdiup: " + payload);

            String taskId = (String) payload.get("TaskID");
            String status = (String) payload.get("TaskStatus");
            String taskOutput = (String) payload.get("TaskOutput");
            String taskIntent = (String) payload.get("TaskIntent");
            String userID = (String) payload.get("UserID");
            String deviceID = (String) payload.get("DeviceID");
            String carePlanID = (String) payload.get("CarePlanID");
            String ResourceID = null;
            if (payload.containsKey("ResourceID")) {
                ResourceID = (String) payload.get("ResourceID");
                System.out.println("ResourceID: " + ResourceID);
            }

            try {
                // Fetch the Task by ID
                String fetchUrl = FHIR_SERVER_URL_TASK + taskId;

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<?> entity = new HttpEntity<>(headers);

                ResponseEntity<Object> response = restTemplate.exchange(fetchUrl, HttpMethod.GET, entity, Object.class);

                // Serialize updated Task back to JSON
                System.out.println("response.getBody(): " + response.getBody());

                // Parse the Task JSON
                String taskJsonStr = objectMapper.writeValueAsString(response.getBody());
                JSONObject taskJson = new JSONObject(taskJsonStr);

                // Update the status
                taskJson.put("status", status);

                // Update the patient reference using UserID (for field)
                if (userID != null) {
                    JSONObject patientReference = new JSONObject();
                    patientReference.put("reference", "Patient/" + userID);
                    taskJson.put("for", patientReference);  // Update "for" field

                    // Also update the owner reference
                    JSONObject ownerReference = new JSONObject();
                    ownerReference.put("reference", "Patient/" + userID);
                    taskJson.put("owner", ownerReference);  // Update "owner" field
                }

                // Get existing output array, if any
                JSONArray outputArray;
                boolean containsResourceID = false;
                if (taskJson.has("output")) {
                    outputArray = taskJson.getJSONArray("output");

                    // Check if the output array already contains the ResourceID
                    for (int i = 0; i < outputArray.length(); i++) {
                        JSONObject output = outputArray.getJSONObject(i);
                        if (output.has("valueReference")) {
                            JSONObject valueReference = output.getJSONObject("valueReference");
                            if (valueReference.getString("reference").equals(ResourceID)) {
                                containsResourceID = true;
                                break;
                            }
                        }
                    }
                } else {
                    outputArray = new JSONArray();
                }

                // Add output section if ResourceID is present
                if (ResourceID != null && !containsResourceID) {
                    JSONObject outputType = new JSONObject();
                    JSONArray codingArray = new JSONArray();
                    JSONObject coding = new JSONObject();
                    coding.put("system", "http://terminology.hl7.org/CodeSystem/task-output-type");
                    coding.put("code", "documentation");
                    coding.put("display", "Documentation");
                    codingArray.put(coding);
                    outputType.put("coding", codingArray);

                    JSONObject valueReference = new JSONObject();
                    if(taskIntent.equals("Questionnaire")){

                        if (ResourceID.contains("Observation/")) {
                            System.out.println("The resourceID contains 'Observation/'");
                            valueReference.put("reference", ResourceID);
                            System.out.println("ResourceID: "+ResourceID);
                        } else {
                            System.out.println("The resourceID does not contain 'Observation/'");
                            valueReference.put("reference", "QuestionnaireResponse/"+ResourceID);
                            System.out.println("ResourceID: QuestionnaireResponse/"+ResourceID);

                            //Trigger the CDS fore questionnaire evaulation
                            String wiztoken = "";

                            /*HttpHeaders headers11 = new HttpHeaders();
                            headers11.setContentType(MediaType.APPLICATION_JSON);
                            // headers6.set("Authorization", "test123");
                            //headers11.set("reference", reference);
                            HttpEntity<?> entity11 = new HttpEntity<>(wiztoken, headers11);
                
                            RestTemplate restTemplate11 = new RestTemplate();
                            String authUri11 = "https://api.domain.eu/api/auth/generateToken";
                            ResponseEntity<Object> res11 = restTemplate11.exchange(authUri11, HttpMethod.POST, entity11, Object.class);
                            System.out.println("output: " + res11.getStatusCode());
                            System.out.println("SMILE API TOKEN output: " + res11.getBody());

                            Map<String, Object> payloadw = objectMapper.convertValue(res11.getBody(), Map.class);
                            System.out.println("payload: " + payloadw);

                            if (!payloadw.containsKey("access_token")) {
                                errors.put("error","access_token is not retreived from wiz SMILE API");
                            }

                            String access_token = (String) payloadw.get("access_token");*/
                            String access_token = "api-key";
                            System.out.println("access_token: " + access_token);

                            Map<String, Object> apiPayload = new LinkedHashMap<>();
                            apiPayload.put("UserID", userID);
                            apiPayload.put("DeviceID", deviceID);
                            apiPayload.put("TaskID", taskId);
                            apiPayload.put("TaskIntent", taskIntent);
                            apiPayload.put("CarePlanID", carePlanID);
                            apiPayload.put("TaskOutput", taskOutput);
                            apiPayload.put("ResourceID", "QuestionnaireResponse/"+ResourceID);

                            String fetchUrl2 = TRIGGER_CDS_API_URL;
                            String updatedapiPayload = apiPayload.toString();

                            System.out.println("updatedTaskJson: " + updatedapiPayload);

                            HttpHeaders headers21 = new HttpHeaders();
                            headers21.setContentType(MediaType.APPLICATION_JSON);
                            headers21.set("Authorization", "Bearer "+access_token);
                            HttpEntity<Object> entity21 = new HttpEntity<>(apiPayload, headers21);

                            ResponseEntity<Object> triggerCDSS = restTemplate.exchange(fetchUrl2, HttpMethod.POST, entity21, Object.class);

                            if (!triggerCDSS.getStatusCode().is2xxSuccessful() || triggerCDSS.getBody() == null) {
                                errors.put("error", "Failed to trigger CDSS with ID: " + taskId);
                                return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
                            }
                            System.out.println("===================================");
                            System.out.println("Automatic trigger of CDSS: success");
                            System.out.println("===================================");
                        }
                        
                    }else if(taskIntent.equals("Diary")){

                        valueReference.put("reference", "DocumentReference/"+ResourceID);
                    }

                    JSONObject output = new JSONObject();
                    output.put("type", outputType);
                    output.put("valueReference", valueReference);

                    outputArray.put(output);

                    taskJson.put("output", outputArray);
                }

                // Update the Task
                String updatedTaskJson = taskJson.toString();

                System.out.println("updatedTaskJson: " + updatedTaskJson);

                HttpHeaders headers2 = new HttpHeaders();
                headers2.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity2 = new HttpEntity<>(updatedTaskJson, headers2);

                ResponseEntity<Object> updateResponse = restTemplate.exchange(fetchUrl, HttpMethod.PUT, entity2, Object.class);

                if (!updateResponse.getStatusCode().is2xxSuccessful() || updateResponse.getBody() == null) {
                    errors.put("error", "Failed to update Task with ID: " + taskId);
                    return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
                }

                // Add output section if ResourceID is present
                if (ResourceID != null && !containsResourceID && taskOutput == "diary") {
                    String[] parts = ResourceID.split("/");

                    // The first part will be "DocumentReference"
                    String documentReference = parts[0];
                    System.out.println("documentReference: " + documentReference);
                    String documentReference2 = parts[1];
                    String documentReferenceID = documentReference2;
                    System.out.println("documentReference2: " + documentReference2);

                    // Now you can compare it
                    if (documentReference.equals("DocumentReference")) {
                        System.out.println("Add the DocumentReference for media to the Task.");
                    }


                    // Retreive the video URL from DocumentReference
                    // Fetch the Task by ID
                    // Retrieve the video URL from DocumentReference
                    String fetchUrldc = FHIR_SERVER_URL_MEDIA + documentReferenceID;

                    Map<String, Object> apiPayload = new LinkedHashMap<>();

                    HttpHeaders headersdc = new HttpHeaders();
                    headersdc.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<?> entitydc = new HttpEntity<>(headersdc);

                    ResponseEntity<Object> responsedc = restTemplate.exchange(fetchUrldc, HttpMethod.GET, entitydc, Object.class);

                    // Check if the response is successful and has a body
                    if (responsedc.getStatusCode().is2xxSuccessful() && responsedc.getBody() != null) {
                        // Serialize the DocumentReference response to JSON string
                        String documentReferenceJsonStr = objectMapper.writeValueAsString(responsedc.getBody());
                        JSONObject documentReferenceJson = new JSONObject(documentReferenceJsonStr);

                        // Extract the video URL from the content array
                        if (documentReferenceJson.has("content")) {
                            JSONArray contentArray = documentReferenceJson.getJSONArray("content");
                            if (contentArray.length() > 0) {
                                JSONObject firstContent = contentArray.getJSONObject(0);
                                if (firstContent.has("attachment")) {
                                    JSONObject attachment = firstContent.getJSONObject("attachment");
                                    if (attachment.has("url")) {
                                        String videoUrl = attachment.getString("url");

                                        // Forward the video URL in the apiPayload
                                        apiPayload.put("video_url", videoUrl);
                                        System.out.println("Video URL: " + videoUrl);
                                        apiPayload.put("document_reference",documentReferenceID);
                                        System.out.println("Video URL: " + documentReferenceID);
                                        apiPayload.put("user_id", userID);
                                    } else {
                                        System.out.println("Error: 'url' field is missing in the attachment.");
                                    }
                                } else {
                                    System.out.println("Error: 'attachment' field is missing in the content.");
                                }
                            } else {
                                System.out.println("Error: 'content' array is empty.");
                            }
                        } else {
                            System.out.println("Error: 'content' field is missing in the DocumentReference.");
                        }
                    } else {
                        System.out.println("Error: Failed to retrieve the DocumentReference or response body is empty.");
                    }

                    

                    // Call MRAST processing for diary
                    String fetchUrl2 = CAMEL_API_URL_MRAST;
                    String updatedapiPayload = apiPayload.toString();

                    System.out.println("updatedTaskJson: " + updatedapiPayload);

                    HttpHeaders headers21 = new HttpHeaders();
                    headers21.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<String> entity21 = new HttpEntity<>(updatedapiPayload, headers21);

                    ResponseEntity<Object> updateResponseMRAST = restTemplate.exchange(fetchUrl2, HttpMethod.PUT, entity21, Object.class);

                    if (!updateResponseMRAST.getStatusCode().is2xxSuccessful() || updateResponseMRAST.getBody() == null) {
                        errors.put("error", "Failed to update Task with ID: " + taskId);
                        return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
                    }


                    // When MRAST processing finishes update the task as completed
                    // Update the status
                    taskJson.put("status", "completed");

                    // Get existing output array, if any
                    JSONArray outputArrayUpdated;
                    boolean containsResourceIDUpdated = false;
                    if (taskJson.has("output")) {
                        outputArrayUpdated = taskJson.getJSONArray("output");

                        // Check if the output array already contains the ResourceID
                        for (int i = 0; i < outputArrayUpdated.length(); i++) {
                            JSONObject output = outputArrayUpdated.getJSONObject(i);
                            if (output.has("valueReference")) {
                                JSONObject valueReference = output.getJSONObject("valueReference");
                                if (valueReference.getString("reference").equals(ResourceID)) {
                                    containsResourceIDUpdated = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        outputArrayUpdated = new JSONArray();
                    }

                    // Add output section if ResourceID is present
                    if (ResourceID != null && !containsResourceIDUpdated) {
                        JSONObject outputType = new JSONObject();
                        JSONArray codingArray = new JSONArray();
                        JSONObject coding = new JSONObject();
                        coding.put("system", "http://terminology.hl7.org/CodeSystem/task-output-type");
                        coding.put("code", "documentation");
                        coding.put("display", "Documentation");
                        codingArray.put(coding);
                        outputType.put("coding", codingArray);

                        JSONObject valueReference = new JSONObject();
                        valueReference.put("reference", ResourceID);

                        JSONObject output = new JSONObject();
                        output.put("type", outputType);
                        output.put("valueReference", valueReference);

                        outputArray.put(output);

                        taskJson.put("output", outputArray);
                    }

                    // Update the Task
                    String updatedTaskJson2 = taskJson.toString();

                    System.out.println("updatedTaskJson: " + updatedTaskJson2);

                    HttpHeaders headers3 = new HttpHeaders();
                    headers3.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<String> entity3 = new HttpEntity<>(updatedTaskJson2, headers3);

                    ResponseEntity<Object> updateResponse2 = restTemplate.exchange(fetchUrl, HttpMethod.PUT, entity3, Object.class);

                    if (!updateResponse2.getStatusCode().is2xxSuccessful() || updateResponse2.getBody() == null) {
                        errors.put("error", "Failed to update Task with ID: " + taskId);
                        return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                    
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

