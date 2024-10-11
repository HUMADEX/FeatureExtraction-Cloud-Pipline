package um.persist.chatbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 *
 * @author UM FERI
 * @date JAN 2021
 * @description Chatbot service class for REST redirect of Chatbot tracker
 *              endpoint
 *
 */

public class ChatbotServiceTracker {

    public ChatbotServiceTracker() {

    }

    public void GetResult(Exchange exchange) {

        Message camelMessage = exchange.getIn();

        System.out.println(camelMessage);
        // String dummy_json = "{\"sender\":\"dd\",\"message\":\"hey\"}";

        // Map<String, Object> payload = (Map<String, Object>) camelMessage.getBody();
        // List<String> errors = new LinkedList<String>();

        String user_id = (String) camelMessage.getHeader("id");
        System.out.println("Camel path user id: " + user_id);
        String questionnaire_id = (String) camelMessage.getHeader("questionnaire_id");
        System.out.println("Camel questionnaire id: " + questionnaire_id);

        String port = "none";
        if (questionnaire_id.equals("info_si") || questionnaire_id.equals("hospital_si")
                || questionnaire_id.equals("form_si_male") || questionnaire_id.equals("form_si_female")
                || questionnaire_id.equals("plevel_si_male")) {
            port = "5007";
        }
        if (questionnaire_id.equals("gad7") || questionnaire_id.equals("phq9")) {
            // port = "5014";
            port = "5030";
        }
        if (questionnaire_id.equals("diary_en")) {
            port = "5013";
        }
        if (questionnaire_id.equals("info") || questionnaire_id.equals("hospital") || questionnaire_id.equals("form")
                || questionnaire_id.equals("plevel")) {
            port = "5006";
        }
        if (questionnaire_id.equals("nasa_male") || questionnaire_id.equals("nasa_female")
                || questionnaire_id.equals("nep_male") || questionnaire_id.equals("nep_female")
                || questionnaire_id.equals("ngse_male") || questionnaire_id.equals("ngse_female")
                || questionnaire_id.equals("phe_male") || questionnaire_id.equals("phe_female")
                || questionnaire_id.equals("pqmc_male") || questionnaire_id.equals("pqmc_female")
                || questionnaire_id.equals("sus_male") || questionnaire_id.equals("sus_female")
                || questionnaire_id.equals("ueq_male") || questionnaire_id.equals("ueq_female")
                || questionnaire_id.equals("utaut_male") || questionnaire_id.equals("utaut_female")) {
            port = "5008";
        }

        // English diary
        if (questionnaire_id.equals("diary_baseline_en")) {
            port = "5021";
        }
        if (questionnaire_id.equals("diary_week1_en")) {
            port = "5021";
        }
        if (questionnaire_id.equals("diary_week2_en")) {
            port = "5021";
        }
        if (questionnaire_id.equals("diary_week3_en")) {
            port = "5021";
        }
        if (questionnaire_id.equals("diary_week4_1_en")) {
            port = "5021";
        }
        if (questionnaire_id.equals("diary_week4_2_en")) {
            port = "5021";
        }
        if (questionnaire_id.equals("diary_week5_en")) {
            port = "5021";
        }

        // Slovenian diary
        if (questionnaire_id.equals("diary_baseline_sl")) {
            port = "5022";
        }
        if (questionnaire_id.equals("diary_week1_sl")) {
            port = "5022";
        }
        if (questionnaire_id.equals("diary_week2_sl")) {
            port = "5022";
        }
        if (questionnaire_id.equals("diary_week3_sl")) {
            port = "5022";
        }
        if (questionnaire_id.equals("diary_week4_1_sl")) {
            port = "5022";
        }
        if (questionnaire_id.equals("diary_week4_2_sl")) {
            port = "5022";
        }
        if (questionnaire_id.equals("diary_week5_sl")) {
            port = "5022";
        }

        // Spanish diary
        if (questionnaire_id.equals("diary_baseline_es")) {
            port = "5023";
        }
        if (questionnaire_id.equals("diary_week1_es")) {
            port = "5023";
        }
        if (questionnaire_id.equals("diary_week2_es")) {
            port = "5023";
        }
        if (questionnaire_id.equals("diary_week3_es")) {
            port = "5023";
        }
        if (questionnaire_id.equals("diary_week4_1_es")) {
            port = "5023";
        }
        if (questionnaire_id.equals("diary_week4_2_es")) {
            port = "5023";
        }
        if (questionnaire_id.equals("diary_week5_es")) {
            port = "5023";
        }

        // Polish diary
        if (questionnaire_id.equals("diary_baseline_pl")) {
            port = "5024";
        }
        if (questionnaire_id.equals("diary_week1_pl")) {
            port = "5024";
        }
        if (questionnaire_id.equals("diary_week2_pl")) {
            port = "5024";
        }
        if (questionnaire_id.equals("diary_week3_pl")) {
            port = "5024";
        }
        if (questionnaire_id.equals("diary_week4_1_pl")) {
            port = "5024";
        }
        if (questionnaire_id.equals("diary_week4_2_pl")) {
            port = "5024";
        }
        if (questionnaire_id.equals("diary_week5_pl")) {
            port = "5024";
        }

        // English questionnaires
        if (questionnaire_id.equals("pswo_c_en")) {
            port = "5028";
        }
        if (questionnaire_id.equals("gad7_en")) {
            port = "5029";
        }
        if (questionnaire_id.equals("phq9_A_en")) {
            port = "5030";
        }
        if (questionnaire_id.equals("phq9_en")) {
            port = "5031";
        }
        if (questionnaire_id.equals("wemwbs_en")) {
            port = "5032";
        }
        if (questionnaire_id.equals("cyrm12_en")) {
            port = "5033";
        }
        if (questionnaire_id.equals("brs_en")) {
            port = "5034";
        }
        if (questionnaire_id.equals("ccas_s_en")) {
            port = "5035";
        }
        if (questionnaire_id.equals("privacy_en")) {
            port = "5036";
        }
        if (questionnaire_id.equals("sus_game_en")) {
            port = "5037";
        }
        if (questionnaire_id.equals("sus_comp_en")) {
            port = "5038";
        }
        if (questionnaire_id.equals("useq_game_en")) {
            port = "5039";
        }
        if (questionnaire_id.equals("useq_comp_en")) {
            port = "5040";
        }
        if (questionnaire_id.equals("ueq_s_game_en")) {
            port = "5041";
        }
        if (questionnaire_id.equals("ueq_s_comp_en")) {
            port = "5042";
        }

        // Slovenian questionnaires
        if (questionnaire_id.equals("pswo_c_sl")) {
            port = "5043";
        }
        if (questionnaire_id.equals("gad7_sl")) {
            port = "5044";
        }
        if (questionnaire_id.equals("phq9_A_sl")) {
            port = "5045";
        }
        if (questionnaire_id.equals("phq9_sl")) {
            port = "5046";
        }
        if (questionnaire_id.equals("wemwbs_sl")) {
            port = "5047";
        }
        if (questionnaire_id.equals("cyrm12_sl")) {
            port = "5048";
        }
        if (questionnaire_id.equals("brs_sl")) {
            port = "5049";
        }
        if (questionnaire_id.equals("ccas_s_sl")) {
            port = "5058";
        }
        if (questionnaire_id.equals("privacy_sl")) {
            port = "5051";
        }
        if (questionnaire_id.equals("sus_game_sl")) {
            port = "5052";
        }
        if (questionnaire_id.equals("sus_comp_sl")) {
            port = "5053";
        }
        if (questionnaire_id.equals("useq_game_sl")) {
            port = "5054";
        }
        if (questionnaire_id.equals("useq_comp_sl")) {
            port = "5055";
        }
        if (questionnaire_id.equals("ueq_s_game_sl")) {
            port = "5056";
        }
        if (questionnaire_id.equals("ueq_s_comp_sl")) {
            port = "5057";
        }

        // Greek questionnaires
        if (questionnaire_id.equals("pswo_c_el")) {
            port = "5059";
        }
        if (questionnaire_id.equals("gad7_el")) {
            port = "5060";
        }
        if (questionnaire_id.equals("phq9_A_el")) {
            port = "5061";
        }
        if (questionnaire_id.equals("phq9_el")) {
            port = "5062";
        }
        if (questionnaire_id.equals("wemwbs_el")) {
            port = "5063";
        }
        if (questionnaire_id.equals("cyrm12_el")) {
            port = "5064";
        }
        if (questionnaire_id.equals("brs_el")) {
            port = "5065";
        }
        if (questionnaire_id.equals("ccas_s_el")) {
            port = "5066";
        }
        if (questionnaire_id.equals("privacy_el")) {
            port = "5067";
        }
        if (questionnaire_id.equals("sus_game_el")) {
            port = "5068";
        }
        if (questionnaire_id.equals("sus_comp_el")) {
            port = "5069";
        }
        if (questionnaire_id.equals("useq_game_el")) {
            port = "5070";
        }
        if (questionnaire_id.equals("useq_comp_el")) {
            port = "5071";
        }
        if (questionnaire_id.equals("ueq_s_game_el")) {
            port = "5072";
        }
        if (questionnaire_id.equals("ueq_s_comp_el")) {
            port = "5073";
        }

        Map<String, Object> error = new LinkedHashMap();
        error.put("error", "wrong questionnaire_id. check that you use the correct one");

        // if(!payload.containsKey("user_id"))
        // {errors.add("user_id is required parameter"); }
        // String user_id = (String) payload.get("user_id");

        ObjectMapper objectMapper = new ObjectMapper();

        // try {
        // String json = objectMapper.writeValueAsString(payload);
        // System.out.println(json);

        if (!port.equals("none")) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            String authUri = "http://x.x.x.x:" + port + "/conversations/" + user_id + "/tracker"; // uri to service
                                                                                                       // which you get
                                                                                                       // the token from
                                                                                                       // // USE THIS
                                                                                                       // ONE IN
                                                                                                       // PRODUCTION
            // String authUri =
            // "http://0.0.0.0:5005/conversations/"+user_id+"/tracker";
            ResponseEntity<Object> response = restTemplate.exchange(authUri, HttpMethod.GET, entity, Object.class);

            // System.out.println("Create Resource: " + response2);

            // String body = response.getBody().toString();

            exchange.getOut().setBody(response.getBody());
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
        } else {
            exchange.getOut().setBody(error);
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
        }

        // System.out.println(body);

        // } catch (JsonProcessingException e) {
        // e.printStackTrace();
        // }

    }

}
