package um.persist.chatbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
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

/**
 *
 * @author UM FERI
 * @date JAN 2021
 * @description Chatbot service class for REST redirect of Chatbot story endpoint
 *
 */

public class ChatbotServicePredict {

    public ChatbotServicePredict() {

    }

    public void GetResult(Exchange exchange) {

        Message camelMessage = exchange.getIn();

        System.out.println(camelMessage);
        //String dummy_json = "{\"sender\":\"dd\",\"message\":\"hey\"}";

        //Map<String, Object> payload = (Map<String, Object>) camelMessage.getBody();
        //List<String> errors = new LinkedList<String>();

        String user_id = (String) camelMessage.getHeader("id");
        System.out.println("Camel path user id: "+user_id);
        String questionnaire_id = (String) camelMessage.getHeader("questionnaire_id");
        System.out.println("Camel questionnaire id: "+questionnaire_id);

        String port = "none";
        if(questionnaire_id.equals("info_si") || questionnaire_id.equals("hospital_si") || questionnaire_id.equals("form_si_male") || questionnaire_id.equals("form_si_female") || questionnaire_id.equals("plevel_si_male")){
            port = "5007";
        }
        if(questionnaire_id.equals("info") || questionnaire_id.equals("hospital") || questionnaire_id.equals("form") || questionnaire_id.equals("plevel")){
            port = "5006";
        }
        if(questionnaire_id.equals("nasa_male") || questionnaire_id.equals("nasa_female") || questionnaire_id.equals("nep_male") || questionnaire_id.equals("nep_female") || questionnaire_id.equals("ngse_male") || questionnaire_id.equals("ngse_female")
            || questionnaire_id.equals("phe_male") || questionnaire_id.equals("phe_female") || questionnaire_id.equals("pqmc_male") || questionnaire_id.equals("pqmc_female") || questionnaire_id.equals("sus_male") || questionnaire_id.equals("sus_female")
            || questionnaire_id.equals("ueq_male") || questionnaire_id.equals("ueq_female") || questionnaire_id.equals("utaut_male") || questionnaire_id.equals("utaut_female")){
            port = "5008";
        }
        Map<String, Object> error = new LinkedHashMap();
        error.put("error", "wrong questionnaire_id. check that you use the correct one");

        //if(!payload.containsKey("user_id"))
        //{errors.add("user_id is required parameter"); }
        //String user_id = (String) payload.get("user_id");

        ObjectMapper objectMapper = new ObjectMapper();

        //try {
            //String json = objectMapper.writeValueAsString(payload);
            //System.out.println(json);

            if(!port.equals("none")){
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            String authUri = "http://0.0.0.0:"+port+"/conversations/"+user_id+"/predict"; // uri to service which you get the token from // USE THIS ONE IN PRODUCTION
			//String authUri = "http://164.8.66.117:5005/conversations/"+user_id+"/story";
            ResponseEntity<String> response =
                    restTemplate.exchange(authUri, HttpMethod.POST, entity, String.class);

            //System.out.println("Create Resource: " + response2);

            //String body = response.getBody().toString();

            exchange.getOut().setBody(response.getBody());
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
        }else{
            exchange.getOut().setBody(error);
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
        }
            //System.out.println(body);

        //} catch (JsonProcessingException e) {
        //    e.printStackTrace();
        //}

    }


}

