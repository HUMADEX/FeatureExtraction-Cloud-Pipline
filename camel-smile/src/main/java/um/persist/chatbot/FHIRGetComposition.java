package um.persist.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

public class FHIRGetComposition {

    public FHIRGetComposition() {}

    public void GetResult(Exchange exchange) {

        Message camelMessage = exchange.getIn();

        String patient_id = (String) camelMessage.getHeader("patient_id");
        String section = (String) camelMessage.getHeader("section");
        String entry = (String) camelMessage.getHeader("entry");

        if (patient_id == null || patient_id.isEmpty()) {
            exchange.getOut().setBody("Missing required parameter: patient_id");
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
            return;
        }

        String api_key = String.valueOf(camelMessage.getHeader("Authorization"));
        String rest_api_key = "api-key";

        if (!api_key.equals(rest_api_key)) {
            Map<String, Object> mapErrorKey = new LinkedHashMap<>();
            mapErrorKey.put("error", "invalid_api_key");
            mapErrorKey.put("error_description", "API Key verification failed. Please provide the correct API Key");

            exchange.getOut().setBody(mapErrorKey);
            exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
            return;
        }

        String authUri = "http://x.x.x.x:8080/fhir/Composition?subject=" + patient_id;
        if (section != null && !section.isEmpty()) {
            authUri += "&section=" + section;
        }
        if (entry != null && !entry.isEmpty()) {
            authUri += "&entry=" + entry;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accept", "application/fhir+json");
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode allCompositions = objectMapper.createArrayNode();

        try {
            String nextPageUrl = authUri;

            while (nextPageUrl != null) {
                ResponseEntity<JsonNode> response = restTemplate.exchange(nextPageUrl, HttpMethod.GET, entity, JsonNode.class);
                JsonNode bundle = response.getBody();

                if (bundle == null || !bundle.has("entry")) {
                    break; // No entries in the bundle, stop processing
                }

                // Add current page entries to the result
                JsonNode entries = bundle.get("entry");
                ((ArrayNode) allCompositions).add(entries);

                // Check if there is a next page
                nextPageUrl = null;
                if (bundle.has("link")) {
                    for (JsonNode link : bundle.get("link")) {
                        if (link.get("relation").asText().equals("next")) {
                            nextPageUrl = link.get("url").asText();
                            break;
                        }
                    }
                }
            }

            exchange.getOut().setBody(allCompositions);
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

        } catch (Exception e) {
            exchange.getOut().setBody("Error fetching compositions: " + e.getMessage());
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
            e.printStackTrace();
        }
    }
}