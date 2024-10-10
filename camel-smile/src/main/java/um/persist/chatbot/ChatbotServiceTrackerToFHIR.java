package um.persist.chatbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONArray;
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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.time.Instant;
import java.time.LocalDateTime;  
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter; 

/**
 *
 * @author UM FERI
 * @date JAN 2021
 * @description Chatbot service class for REST redirect of Chatbot tracker
 *              endpoint
 *
 */

public class ChatbotServiceTrackerToFHIR {

    public ChatbotServiceTrackerToFHIR() {

    }

    private String ResourceAddRequestQuestionnaireResponse(String question_answer, String id, String questionnaire, String date_formatted) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "api-key");
        HttpEntity<?> entity = new HttpEntity<>(question_answer, headers);

        RestTemplate restTemplate = new RestTemplate();
        //String authUri = "http://smile.feri.um.si:8080/fhir/QuestionnaireResponse/" + id +"-"+questionnaire+"-"+date_formatted; // uri to service which you get
        String authUri = "http://x.x.x.x:8080/fhir/QuestionnaireResponse/" + questionnaire+"-"+date_formatted; // uri to service which you get
                                                                                      // the token from
        // System.out.println("Sending to OHC: " + authUri);
        ResponseEntity<?> response2 = restTemplate.exchange(authUri, HttpMethod.PUT, entity,
                Object.class); // TODO: SWAGER does not show us what is the structure of response

        // System.out.println("Create Resource: " + response2);
        // String id = response2.getBody().getId();
        // return id;

        String response_body = response2.getStatusCode().toString();
        headers.clear();
        entity = null;
        response2 = null;

        return response_body;
    }

    private String ResourceAddRequestComposition(String question_answer, String id, String questionnaire, String date_formatted) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "api-key");
        HttpEntity<?> entity = new HttpEntity<>(question_answer, headers);

        RestTemplate restTemplate = new RestTemplate();
        //String authUri = "http://x.x.x.x:8080/fhir/Composition/" + id + "-" + questionnaire; // uri to service which you get
        String authUri = "http://x.x.x.x:8080/fhir/Composition/" + questionnaire +"-"+ date_formatted; // uri to service which you get
                                                                                      // the token from
        // System.out.println("Sending to OHC: " + authUri);
        ResponseEntity<?> response2 = restTemplate.exchange(authUri, HttpMethod.PUT, entity,
                Object.class); // TODO: SWAGER does not show us what is the structure of response

        // System.out.println("Create Resource: " + response2);
        // String id = response2.getBody().getId();
        // return id;

        String response_body = response2.getStatusCode().toString();
        headers.clear();
        entity = null;
        response2 = null;

        return response_body;
    }

    public void GetResult(Exchange exchange) {

        Message camelMessage = exchange.getIn();

        System.out.println(camelMessage);
        // String dummy_json = "{\"sender\":\"dd\",\"message\":\"hey\"}";

        Map<String, Object> payload = (Map<String, Object>) camelMessage.getBody();
        // List<String> errors = new LinkedList<String>();

        //{"sender": "user-id","message": "nasa_male, Umut","language": "en","chatbot": "standard"}

        LocalDateTime datetime = LocalDateTime.ofInstant(java.time.Clock.systemUTC().instant(), ZoneOffset.UTC);
        String date_formatted = DateTimeFormatter.ofPattern("yyyyMMdd-hhmmss").format(datetime);
        System.out.println(date_formatted);
        //date_formatted = "timestamp";

        String user_id = (String) payload.get("sender_id");
        System.out.println("Camel user id: " + user_id);
        String questionnaire_id = (String) payload.get("questionary_id");
        System.out.println("Camel questionnaire id: " + questionnaire_id);

        String chatbot = "standard";

        String questionnaire = "none";
        if (questionnaire_id.equals("nasa_male") || questionnaire_id.equals("nasa_female")) {
            questionnaire = "NASA-TLX";
        }
        if (questionnaire_id.equals("nep_male") || questionnaire_id.equals("nep_female")) {
            questionnaire = "NEP";
        }
        if (questionnaire_id.equals("ngse_male") || questionnaire_id.equals("ngse_female")) {
            questionnaire = "NGSE";
        }
        if (questionnaire_id.equals("phe_male") || questionnaire_id.equals("phe_female")) {
            questionnaire = "PHE";
        }
        if (questionnaire_id.equals("pqmc_male") || questionnaire_id.equals("pqmc_female")) {
            questionnaire = "PQMC";
        }
        if (questionnaire_id.equals("sus_male") || questionnaire_id.equals("sus_female")) {
            questionnaire = "SUS";
        }
        if (questionnaire_id.equals("ueq_male") || questionnaire_id.equals("ueq_female")) {
            questionnaire = "UEQ";
        }
        if (questionnaire_id.equals("utaut_male") || questionnaire_id.equals("utaut_female")) {
            questionnaire = "UTAUT";
        }

        if (questionnaire_id.equals("info_si")) {
            questionnaire = "UKCM-Part-A";
        }
        if (questionnaire_id.equals("hospital_si")) {
            questionnaire = "UKCM-Part-B";
        }
        if (questionnaire_id.equals("form_si_male") || questionnaire_id.equals("form_si_female")) {
            questionnaire = "UKCM-Part-C";
        }
        if (questionnaire_id.equals("plevel_si_male") || questionnaire_id.equals("plevel_si_female")) {
            questionnaire = "UKCM-Part-D";
        }

        if (questionnaire_id.equals("info")) {
            questionnaire = "UKCM-Part-A-EN";
        }
        if (questionnaire_id.equals("hospital")) {
            questionnaire = "UKCM-Part-B-EN";
        }
        if (questionnaire_id.equals("form") || questionnaire_id.equals("form")) {
            questionnaire = "UKCM-Part-C-EN";
        }
        if (questionnaire_id.equals("plevel") || questionnaire_id.equals("plevel")) {
            questionnaire = "UKCM-Part-D-EN";
        }

        if (questionnaire_id.equals("gad7")) {
            questionnaire = "gad7";
        }
        if (questionnaire_id.equals("phq9")) {
            questionnaire = "phq9";
        }

        if (questionnaire_id.equals("diary_en")) {
            questionnaire = "diary-en";
        }

        String port = "none";
        if (questionnaire_id.equals("info_si") || questionnaire_id.equals("hospital_si")
                || questionnaire_id.equals("form_si_male") || questionnaire_id.equals("form_si_female")
                || questionnaire_id.equals("plevel_si_male")) {
            port = "5007";
            chatbot = "nonstandard";
        }
        if (questionnaire_id.equals("info") || questionnaire_id.equals("hospital") || questionnaire_id.equals("form")
                || questionnaire_id.equals("plevel")) {
            port = "5006";
            chatbot = "nonstandard";
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
            chatbot = "standard";
        }
        if (questionnaire_id.equals("gad7") || questionnaire_id.equals("phq9")) {
            port = "5014";
            //port = "5030";
            chatbot = "standard";
        }

        if (questionnaire_id.equals("diary_en")) {
            port = "5013";
            chatbot = "standard";
        }
        //English diary
        if (questionnaire_id.equals("diary_baseline_en")) {
            port = "5021";
            chatbot = "standard";
            questionnaire = "diary-baseline-en";
        }
        if (questionnaire_id.equals("diary_week1_en")) {
            port = "5021";
            chatbot = "standard";
            questionnaire = "diary-week1-en";
        }
        if (questionnaire_id.equals("diary_week2_en")) {
            port = "5021";
            chatbot = "standard";
            questionnaire = "diary-week2-en";
        }
        if (questionnaire_id.equals("diary_week3_en")) {
            port = "5021";
            chatbot = "standard";
            questionnaire = "diary-week3-en";
        }
        if (questionnaire_id.equals("diary_week4_1_en")) {
            port = "5021";
            chatbot = "standard";
            questionnaire = "diary-week4-1-en";
        }
        if (questionnaire_id.equals("diary_week4_2_en")) {
            port = "5021";
            chatbot = "standard";
            questionnaire = "diary-week4-2-en";
        }
        if (questionnaire_id.equals("diary_week5_en")) {
            port = "5021";
            chatbot = "standard";
            questionnaire = "diary-week5-en";
        }
        //Slovenian diary
        if (questionnaire_id.equals("diary_baseline_sl")) {
            port = "5022";
            chatbot = "standard";
            questionnaire = "diary-baseline-sl";
        }
        if (questionnaire_id.equals("diary_week1_sl")) {
            port = "5022";
            chatbot = "standard";
            questionnaire = "diary-week1-sl";
        }
        if (questionnaire_id.equals("diary_week2_sl")) {
            port = "5022";
            chatbot = "standard";
            questionnaire = "diary-week2-sl";
        }
        if (questionnaire_id.equals("diary_week3_sl")) {
            port = "5022";
            chatbot = "standard";
            questionnaire = "diary-week3-sl";
        }
        if (questionnaire_id.equals("diary_week4_1_sl")) {
            port = "5022";
            chatbot = "standard";
            questionnaire = "diary-week4-1-sl";
        }
        if (questionnaire_id.equals("diary_week4_2_sl")) {
            port = "5022";
            chatbot = "standard";
            questionnaire = "diary-week4-2-sl";
        }
        if (questionnaire_id.equals("diary_week5_sl")) {
            port = "5022";
            chatbot = "standard";
            questionnaire = "diary-week5-sl";
        }
        //Spanish diary
        if (questionnaire_id.equals("diary_baseline_es")) {
            port = "5023";
            chatbot = "standard";
            questionnaire = "diary-baseline-es";
        }
        if (questionnaire_id.equals("diary_week1_es")) {
            port = "5023";
            chatbot = "standard";
            questionnaire = "diary-week1-es";
        }
        if (questionnaire_id.equals("diary_week2_es")) {
            port = "5023";
            chatbot = "standard";
            questionnaire = "diary-week2-es";
        }
        if (questionnaire_id.equals("diary_week3_es")) {
            port = "5023";
            chatbot = "standard";
            questionnaire = "diary-week3-es";
        }
        if (questionnaire_id.equals("diary_week4_1_es")) {
            port = "5023";
            chatbot = "standard";
            questionnaire = "diary-week4-1-es";
        }
        if (questionnaire_id.equals("diary_week4_2_es")) {
            port = "5023";
            chatbot = "standard";
            questionnaire = "diary-week4-2-es";
        }
        if (questionnaire_id.equals("diary_week5_es")) {
            port = "5023";
            chatbot = "standard";
            questionnaire = "diary-week5-es";
        }
        //Polish diary
        if (questionnaire_id.equals("diary_baseline_pl")) {
            port = "5024";
            chatbot = "standard";
            questionnaire = "diary-baseline-pl";
        }
        if (questionnaire_id.equals("diary_week1_pl")) {
            port = "5024";
            chatbot = "standard";
            questionnaire = "diary-week1-pl";
        }
        if (questionnaire_id.equals("diary_week2_pl")) {
            port = "5024";
            chatbot = "standard";
            questionnaire = "diary-week2-pl";
        }
        if (questionnaire_id.equals("diary_week3_pl")) {
            port = "5024";
            chatbot = "standard";
            questionnaire = "diary-week3-pl";
        }
        if (questionnaire_id.equals("diary_week4_1_pl")) {
            port = "5024";
            chatbot = "standard";
            questionnaire = "diary-week4-1-pl";
        }
        if (questionnaire_id.equals("diary_week4_2_pl")) {
            port = "5024";
            chatbot = "standard";
            questionnaire = "diary-week4-2-pl";
        }
        if (questionnaire_id.equals("diary_week5_pl")) {
            port = "5024";
            chatbot = "standard";
            questionnaire = "diary-week5-pl";
        }

        //English questionnaires
        if (questionnaire_id.equals("pswo_c_en")) {
            port = "5028";
            chatbot = "standard";
            questionnaire = "pswo-c-en";
        }
        if (questionnaire_id.equals("gad7_en")) {
            port = "5029";
            chatbot = "standard";
            questionnaire = "gad7-en";
        }
        if (questionnaire_id.equals("phq9_A_en")) {
            port = "5030";
            chatbot = "standard";
            questionnaire = "phq9-A-en";
        }
        if (questionnaire_id.equals("phq9_en")) {
            port = "5031";
            chatbot = "standard";
            questionnaire = "phq9-en";
        }
        if (questionnaire_id.equals("wemwbs_en")) {
            port = "5032";
            chatbot = "standard";
            questionnaire = "wemwbs-en";
        }
        if (questionnaire_id.equals("cyrm12_en")) {
            port = "5033";
            chatbot = "standard";
            questionnaire = "cyrm12-en";
        }
        if (questionnaire_id.equals("brs_en")) {
            port = "5034";
            chatbot = "standard";
            questionnaire = "brs-en";
        }
        if (questionnaire_id.equals("ccas_s_en")) {
            port = "5035";
            chatbot = "standard";
            questionnaire = "ccas-s-en";
        }
        if (questionnaire_id.equals("privacy_en")) {
            port = "5036";
            chatbot = "standard";
            questionnaire = "privacy-en";
        }
        if (questionnaire_id.equals("sus_game_en")) {
            port = "5037";
            chatbot = "standard";
            questionnaire = "sus-game-en";
        }
        if (questionnaire_id.equals("sus_comp_en")) {
            port = "5038";
            chatbot = "standard";
            questionnaire = "sus-comp-en";
        }
        if (questionnaire_id.equals("useq_game_en")) {
            port = "5039";
            chatbot = "standard";
            questionnaire = "useq-game-en";
        }
        if (questionnaire_id.equals("useq_comp_en")) {
            port = "5040";
            chatbot = "standard";
            questionnaire = "useq-comp-en";
        }
        if (questionnaire_id.equals("ueq_s_game_en")) {
            port = "5041";
            chatbot = "standard";
            questionnaire = "ueq-s-game-en";
        }
        if (questionnaire_id.equals("ueq_s_comp_en")) {
            port = "5042";
            chatbot = "standard";
            questionnaire = "ueq-s-comp-en";
        }
        //Slovenian questionnaires
        if (questionnaire_id.equals("pswo_c_sl")) {
            port = "5043";
            chatbot = "standard";
            questionnaire = "pswo-c-sl";
        }
        if (questionnaire_id.equals("gad7_sl")) {
            port = "5044";
            chatbot = "standard";
            questionnaire = "gad7-sl";
        }
        if (questionnaire_id.equals("phq9_A_sl")) {
            port = "5045";
            chatbot = "standard";
            questionnaire = "phq9-A-sl";
        }
        if (questionnaire_id.equals("phq9_sl")) {
            port = "5046";
            chatbot = "standard";
            questionnaire = "phq9-sl";
        }
        if (questionnaire_id.equals("wemwbs_sl")) {
            port = "5047";
            chatbot = "standard";
            questionnaire = "wemwbs-sl";
        }
        if (questionnaire_id.equals("cyrm12_sl")) {
            port = "5048";
            chatbot = "standard";
            questionnaire = "cyrm12-sl";
        }
        if (questionnaire_id.equals("brs_sl")) {
            port = "5049";
            chatbot = "standard";
            questionnaire = "brs-sl";
        }
        if (questionnaire_id.equals("ccas_s_sl")) {
            port = "5058";
            chatbot = "standard";
            questionnaire = "ccas-s-sl";
        }
        if (questionnaire_id.equals("privacy_sl")) {
            port = "5051";
            chatbot = "standard";
            questionnaire = "privacy-sl";
        }
        if (questionnaire_id.equals("sus_game_sl")) {
            port = "5052";
            chatbot = "standard";
            questionnaire = "sus-game-sl";
        }
        if (questionnaire_id.equals("sus_comp_sl")) {
            port = "5053";
            chatbot = "standard";
            questionnaire = "sus-comp-sl";
        }
        if (questionnaire_id.equals("useq_game_sl")) {
            port = "5054";
            chatbot = "standard";
            questionnaire = "useq-game-sl";
        }
        if (questionnaire_id.equals("useq_comp_sl")) {
            port = "5055";
            chatbot = "standard";
            questionnaire = "useq-comp-sl";
        }
        if (questionnaire_id.equals("ueq_s_game_sl")) {
            port = "5056";
            chatbot = "standard";
            questionnaire = "ueq-s-game-sl";
        }
        if (questionnaire_id.equals("ueq_s_comp_sl")) {
            port = "5057";
            chatbot = "standard";
            questionnaire = "ueq-s-comp-sl";
        }
        //Greek questionnaires
        if (questionnaire_id.equals("pswo_c_el")) {
            port = "5059";
            chatbot = "standard";
            questionnaire = "pswo-c-el";
        }
        if (questionnaire_id.equals("gad7_el")) {
            port = "5060";
            chatbot = "standard";
            questionnaire = "gad7-el";
        }
        if (questionnaire_id.equals("phq9_A_el")) {
            port = "5061";
            chatbot = "standard";
            questionnaire = "phq9-A-el";
        }
        if (questionnaire_id.equals("phq9_el")) {
            port = "5062";
            chatbot = "standard";
            questionnaire = "phq9-el";
        }
        if (questionnaire_id.equals("wemwbs_el")) {
            port = "5063";
            chatbot = "standard";
            questionnaire = "wemwbs-el";
        }
        if (questionnaire_id.equals("cyrm12_el")) {
            port = "5064";
            chatbot = "standard";
            questionnaire = "cyrm12-el";
        }
        if (questionnaire_id.equals("brs_el")) {
            port = "5065";
            chatbot = "standard";
            questionnaire = "brs-el";
        }
        if (questionnaire_id.equals("ccas_s_el")) {
            port = "5066";
            chatbot = "standard";
            questionnaire = "ccas-s-el";
        }
        if (questionnaire_id.equals("privacy_el")) {
            port = "5067";
            chatbot = "standard";
            questionnaire = "privacy-el";
        }
        if (questionnaire_id.equals("sus_game_el")) {
            port = "5068";
            chatbot = "standard";
            questionnaire = "sus-game-el";
        }
        if (questionnaire_id.equals("sus_comp_el")) {
            port = "5069";
            chatbot = "standard";
            questionnaire = "sus-comp-el";
        }
        if (questionnaire_id.equals("useq_game_el")) {
            port = "5070";
            chatbot = "standard";
            questionnaire = "useq-game-el";
        }
        if (questionnaire_id.equals("useq_comp_el")) {
            port = "5071";
            chatbot = "standard";
            questionnaire = "useq-comp-el";
        }
        if (questionnaire_id.equals("ueq_s_game_el")) {
            port = "5072";
            chatbot = "standard";
            questionnaire = "ueq-s-game-el";
        }
        if (questionnaire_id.equals("ueq_s_comp_el")) {
            port = "5073";
            chatbot = "standard";
            questionnaire = "ueq-s-comp-el";
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
            // "http://x.x.x.x:5005/conversations/"+user_id+"/tracker";
            ResponseEntity<Object> response = restTemplate.exchange(authUri, HttpMethod.GET, entity, Object.class);

            // System.out.println("Create Resource: " + response2);

            // String body = response.getBody().toString();

            Map<String, Object> inpayload = (Map<String, Object>) response.getBody();
            List<String> errors = new LinkedList<String>();
            if (!inpayload.containsKey("sender_id")) {
                errors.add("sender_id parameter is not found");
            }

            if (chatbot.equals("standard")) {

                Map<String, Object> maps = new LinkedHashMap<>();
                Map<String, Object> question_answer = new LinkedHashMap<>();
                String sender_id = (String) inpayload.get("sender_id");
                //sender_id = "user-id";
                maps.put("sender_id", sender_id);

                Map<String, Object> fhir_maps = new LinkedHashMap<>();
                fhir_maps.put("resourceType", "QuestionnaireResponse");
                fhir_maps.put("id", questionnaire+"-"+date_formatted);
                //fhir_maps.put("id", sender_id +"-"+ questionnaire+"-"+date_formatted);
                Map<String, Object> questionnaire_url_map = new LinkedHashMap<>();
                LinkedList<Object> questionnaire_url_extenstion_list = new LinkedList<>();
                Map<String, Object> questionnaire_url_extenstion_map = new LinkedHashMap<>();
                questionnaire_url_extenstion_map.put("url",
                        "http://x.x.x.x:8080/fhir/Questionnaire/" + questionnaire);
                questionnaire_url_extenstion_map.put("valueString", questionnaire);
                questionnaire_url_extenstion_list.add(questionnaire_url_extenstion_map);
                questionnaire_url_map.put("extension", questionnaire_url_extenstion_list);
                fhir_maps.put("_questionnaire", questionnaire_url_map);

                fhir_maps.put("status", "completed");
                Map<String, Object> fhir_maps_subject = new LinkedHashMap<>();
                fhir_maps_subject.put("reference", "Patient/" + sender_id);
                fhir_maps.put("subject", fhir_maps_subject);
                LinkedList<Object> fhir_maps_list = new LinkedList<>();

                JSONObject json = new JSONObject(inpayload);
                // JSONObject events = json.getJSONObject("events");
                String bot_text = "questionnaire_start";
                JSONArray events = json.getJSONArray("events");
                int question_counter = -1;
                String username = "none";
                LinkedList<Object> last_bot_buttons = new LinkedList<>(); // To hold last bot event buttons

                for (int i = 1; i < events.length(); ++i) {
                    JSONObject inside = events.getJSONObject(i);
                    String event = inside.getString("event");
                    System.out.println(event);
                    if (event.equals("bot")) {
                        if (inside.getJSONObject("data") != null) {
                            JSONObject data = inside.getJSONObject("data");
                            if (!data.isNull("custom")) {
                                JSONObject custom = data.getJSONObject("custom");
                                if (!custom.isNull("text")) {
                                    bot_text = custom.getString("text");
                                }
                                if (!custom.isNull("buttons")) {
                                    last_bot_buttons = new LinkedList<>(custom.getJSONArray("buttons").toList());
                                }
                                
                            }
                        }
                    }
                    if (event.equals("user")) {
                        if (inside.getString("text") != null) {
                            String answer = inside.getString("text");
                            Integer value = null;
                            question_answer.put(bot_text, answer);

                            // Check if user answer matches any button title from the last bot event
                            for (Object btnObj : last_bot_buttons) {
                                Map<String, Object> button = (Map<String, Object>) btnObj;
                                String title = (String) button.get("title");
                                if (title.equals(answer)) {
                                    System.out.println("Button value for '" + answer + "': " + button.get("value"));
                                    value = (Integer) button.get("value");
                                }
                            }

                            question_counter += 1;
                            Map<String, Object> fhir_maps_item = new LinkedHashMap<>();
                            fhir_maps_item.put("text", bot_text);
                            fhir_maps_item.put("linkId", Integer.toString(question_counter));
                            LinkedList<Object> fhir_maps_list_answer = new LinkedList<>();
                            Map<String, Object> fhir_maps_item_answer_value = new LinkedHashMap<>();
                            fhir_maps_item_answer_value.put("valueString", answer);
                            fhir_maps_list_answer.add(fhir_maps_item_answer_value);
                            Map<String, Object> fhir_maps_item_answer_value2 = new LinkedHashMap<>();
                            fhir_maps_item_answer_value2.put("valueInteger", value);
                            fhir_maps_list_answer.add(fhir_maps_item_answer_value2);
                            fhir_maps_item.put("answer", fhir_maps_list_answer);
                            if (bot_text.equals("questionnaire_start")) {
                                username = answer.substring(answer.lastIndexOf(",") + 1);
                            }
                            fhir_maps_list.add(fhir_maps_item);

                        }
                    }
                }
                fhir_maps_subject.put("display", username);

                System.out.println(maps);
                maps.put("question_answer", question_answer);

                fhir_maps.put("item", fhir_maps_list);
                // System.out.println(fhir_maps);

                try {
                    String jsonmap = objectMapper.writeValueAsString(fhir_maps);
                    System.out.println(jsonmap);
                    ResourceAddRequestQuestionnaireResponse(jsonmap, sender_id, questionnaire, date_formatted);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                exchange.getOut().setBody(fhir_maps);
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

            } else { //NONSTANDARD

                Map<String, Object> maps = new LinkedHashMap<>();
                Map<String, Object> question_answer = new LinkedHashMap<>();

                Map<String, Object> type = new LinkedHashMap<>();
                String sender_id = (String) inpayload.get("sender_id");
                //sender_id = "user-id";
                maps.put("sender_id", sender_id);

                Map<String, Object> fhir_maps = new LinkedHashMap<>();
                fhir_maps.put("resourceType", "Composition");
                fhir_maps.put("id", sender_id);
                fhir_maps.put("status", "final");
                fhir_maps.put("title", "Nonstandard Questionnaires");
                /*String date = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
                fhir_maps.put("date", date);*/
                String date = java.time.Clock.systemUTC().instant().toString();
                fhir_maps.put("date", date);

                LinkedList<Object> section_list = new LinkedList<>();
                //Map<String, Object> section_map = new LinkedHashMap<>();

                JSONObject json = new JSONObject(inpayload);
                // JSONObject events = json.getJSONObject("events");
                String bot_text = "questionnaire_start";
                JSONArray events = json.getJSONArray("events");
                int question_counter = -1;
                String username = "none";
                for (int i = 1; i < events.length(); ++i) {
                    JSONObject inside = events.getJSONObject(i);
                    String event = inside.getString("event");
                    System.out.println(event);
                    if (event.equals("bot")) {
                        if (inside.getJSONObject("data") != null) {
                            JSONObject data = inside.getJSONObject("data");
                            if (!data.isNull("custom")) {
                                JSONObject custom = data.getJSONObject("custom");
                                if (!custom.isNull("text")) {
                                    bot_text = custom.getString("text");
                                }
                            }
                        }
                    }
                    if (event.equals("user")) {
                        if (inside.getString("text") != null) {
                            String answer = inside.getString("text");
                            question_answer.put(bot_text, answer);

                            question_counter += 1;

                            Map<String, Object> section_map = new LinkedHashMap<>();
                            section_map.put("title", "Question-Answer pairs");

                            Map<String, Object> text_map = new LinkedHashMap<>();
                            text_map.put("status", "generated");
                            text_map.put("div", answer);
                            section_map.put("text", text_map);

                            Map<String, Object> code_map = new LinkedHashMap<>();
                            LinkedList<Object> section_coding_list = new LinkedList<>();
                            Map<String, Object> section_coding_map = new LinkedHashMap<>();
                            section_coding_map.put("system", "UM-QUESTIONNAIRE");
                            section_coding_map.put("code", "UM-QUESTION-"+Integer.toString(question_counter));
                            section_coding_map.put("display", bot_text);
                            section_coding_list.add(section_coding_map);
                            code_map.put("coding", section_coding_list);
                            section_map.put("code", code_map);

                            section_list.add(section_map);

                            if (bot_text.equals("questionnaire_start")) {
                                username = answer.substring(answer.lastIndexOf(",") + 1);
                            }

                        }
                    }
                }

                fhir_maps.put("section", section_list);
                
                
                LinkedList<Object> subject_list = new LinkedList<>();
                Map<String, Object> subject_map = new LinkedHashMap<>();
                subject_map.put("reference", "Patient/" + sender_id);
                subject_map.put("display", username);
                subject_list.add(subject_map);
                fhir_maps.put("subject", subject_list);

                Map<String, Object> type_map = new LinkedHashMap<>();
                LinkedList<Object> coding_list = new LinkedList<>();
                Map<String, Object> coding_map = new LinkedHashMap<>();
                coding_map.put("system", "http://loinc.org");
                coding_map.put("code", "11488-4");
                coding_map.put("display", "Consult note");
                coding_list.add(coding_map);
                type_map.put("coding", coding_list);
                fhir_maps.put("type", type_map);
               
                System.out.println(fhir_maps);
                // System.out.println(fhir_maps);

                try {
                    String jsonmap = objectMapper.writeValueAsString(fhir_maps);
                    System.out.println(jsonmap);
                    ResourceAddRequestComposition(jsonmap, sender_id, questionnaire, date_formatted);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                Map<String, Object> exit_map = new LinkedHashMap<>();
                //exit_map.put("fhir_resource", "http://x.x.x.x:8080/fhir/QuestionnaireResponse/"+sender_id+"-"+questionnaire);
                //exit_map.put("fhir_resource_id", sender_id+"-"+questionnaire);
                exit_map.put("fhir_resource", "http://x.x.x.x:8080/fhir/QuestionnaireResponse/"+questionnaire);
                exit_map.put("fhir_resource_id", questionnaire);


                exchange.getOut().setBody(exit_map);
                //exchange.getOut().setBody(fhir_maps);
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

            }

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
