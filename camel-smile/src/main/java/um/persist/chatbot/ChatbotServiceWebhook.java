package um.persist.chatbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import java.lang.Exception;
import java.net.http.HttpHeaders;

import io.sentry.Sentry;

import um.persist.config.FinalVariables;

/**
 *
 * @author UM FERI
 * @date JAN 2021
 * @description Chatbot service class for REST redirect of Chatbot webhook
 *              endpoint
 *
 */

public class ChatbotServiceWebhook implements FinalVariables {

    public ChatbotServiceWebhook() {

    }

    public void GetResult(Exchange exchange) {

        Message camelMessage = exchange.getIn();

        System.out.println(camelMessage);
        // String dummy_json = "{\"sender\":\"dd\",\"message\":\"hey\"}";

        Map<String, Object> payload = (Map<String, Object>) camelMessage.getBody();
        Map<String, Object> errors = new LinkedHashMap<>();

        String id = "";
        String questionnaire = "";

        if (!payload.containsKey("sender")) {
            errors.put("error","sender is required parameter");
        }else{
            id = payload.get("sender").toString();
        }
        if (!payload.containsKey("message")) {
            errors.put("error","message is required parameter");
        }
        if (!payload.containsKey("language")) {
            errors.put("error","language is required parameter (en, sl)");
        }
        if (!payload.containsKey("chatbot")) {
            errors.put("error","chatbot is required parameter (standard, nonstandard)");
        }else{
            questionnaire = payload.get("chatbot").toString();
        }

        String language = (String) payload.get("language");
        System.out.println("language: "+ language);
        if (!errors.isEmpty()) {language = "en";}
        //if (!language.toLowerCase().equals("en") || !language.toLowerCase().equals("sl")) {
        //    errors.put("error","language code is not correct, languages available: en, sl");
        //}
        String chatbot = (String) payload.get("chatbot");
        String message = (String) payload.get("message");

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> mapErrorKey = new LinkedHashMap<>();
        mapErrorKey.put("error", "invalid_api_key");
        mapErrorKey.put("error_description", "API Key verification failed. Please provide the correct API Key");

        String api_key = String.valueOf(camelMessage.getHeader("Authorization"));
        String date = java.time.Clock.systemUTC().instant().toString();
        System.out.println("[TIME]: " + date);
        System.out.println("API KEY: " + "************");

        if (api_key.equals(rest_api_key)) {

            try {
                payload.remove("chatbot");
                String json = objectMapper.writeValueAsString(payload);
                System.out.println(json);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<?> entity = new HttpEntity<>(json, headers);

                RestTemplate restTemplate = new RestTemplate();

                // Default endpoint
                String authUri = "http://0.0.0.0:5006/webhooks/rest/webhook";

                //if (language.toLowerCase().equals("sl") && chatbot.toLowerCase().equals("nonstandard")) { // && (chatbot.toLowerCase().equals("nonstandard"))
                //    authUri = "http://0.0.0.0:5007/webhooks/rest/webhook"; // uri to service which you get the token
                //                                                           // from // USE THIS ONE IN PRODUCTION
                //}
                if ((language.toLowerCase().equals("en")) && (chatbot.toLowerCase().equals("nonstandard"))) {
                    authUri = "http://0.0.0.0:5006/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                            System.out.println(authUri);
                }
                if ((language.toLowerCase().equals("sl") || language.toLowerCase().equals("si")) && (chatbot.toLowerCase().equals("nonstandard"))) {
                    authUri = "http://0.0.0.0:5007/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if ((language.toLowerCase().equals("sl") || language.toLowerCase().equals("si")) && (chatbot.toLowerCase().equals("standard"))) {
                    authUri = "http://x.x.x.x:5008/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("gad7") || chatbot.toLowerCase().equals("phq9")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.xx.x.x.x:5014/webhooks/rest/webhook"; 
                    //authUri = "http://x.x.x.x:5030/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("diary_en")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5013/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("pswo_c_en")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5028/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("gad7_en")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5029/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("phq9_a_en")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5030/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("phq9_en")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5031/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("wemwbs_en")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5032/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("cyrm12_en")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5033/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("brs_en")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5034/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("ccas_s_en")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5035/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("privacy_en")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5036/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("sus_game_en")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5037/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("sus_comp_en")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5038/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("useq_game_en")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5039/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("useq_comp_en")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5040/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("ueq_s_game_en")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5041/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("ueq_s_comp_en")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5042/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("pswo_c_sl")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5043/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("gad7_sl")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5044/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("phq9_a_sl")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5045/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("phq9_sl")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5046/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("wemwbs_sl")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5047/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("cyrm12_sl")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5048/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("brs_sl")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5049/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("ccas_s_sl")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5058/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("privacy_sl")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5051/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("sus_game_sl")) {
                    //authUri = "http://x.x.x.x:sus_game_sl/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5052/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("sus_comp_sl")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5053/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("useq_game_sl")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5054/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("useq_comp_sl")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5055/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("ueq_s_game_sl")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5056/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("ueq_s_comp_sl")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5057/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("pswo_c_el")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5059/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("gad7_el")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5060/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("phq9_a_el")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5061/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("phq9_el")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5062/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("wemwbs_el")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5063/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("cyrm12_el")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5064/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("brs_el")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5065/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("ccas_s_el")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5066/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("privacy_el")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5067/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("sus_game_el")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5068/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("sus_comp_el")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5069/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("useq_game_el")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5070/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("useq_comp_el")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5071/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("ueq_s_game_el")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5072/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("ueq_s_comp_el")) {
                    //authUri = "http://x.x.x.x:5012/webhooks/rest/webhook"; // uri to service which you get the token
                    authUri = "http://x.x.x.x:5073/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("diary_baseline_en") || chatbot.toLowerCase().equals("diary_week1_en") || chatbot.toLowerCase().equals("diary_week2_en") || chatbot.toLowerCase().equals("diary_week3_en") || chatbot.toLowerCase().equals("diary_week4_1_en") || chatbot.toLowerCase().equals("diary_week4_2_en") || chatbot.toLowerCase().equals("diary_week5_en")) {
                    authUri = "http://x.x.x.x:5021/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("diary_baseline_sl") || chatbot.toLowerCase().equals("diary_week1_sl") || chatbot.toLowerCase().equals("diary_week2_sl") || chatbot.toLowerCase().equals("diary_week3_sl") || chatbot.toLowerCase().equals("diary_week4_1_sl") || chatbot.toLowerCase().equals("diary_week4_2_sl") || chatbot.toLowerCase().equals("diary_week5_sl")) {
                    authUri = "http://x.x.x.x:5022/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("diary_baseline_es") || chatbot.toLowerCase().equals("diary_week1_es") || chatbot.toLowerCase().equals("diary_week2_es") || chatbot.toLowerCase().equals("diary_week3_es") || chatbot.toLowerCase().equals("diary_week4_1_es") || chatbot.toLowerCase().equals("diary_week4_2_es") || chatbot.toLowerCase().equals("diary_week5_es")) {
                    authUri = "http://x.x.x.x:5023/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("diary_baseline_pl") || chatbot.toLowerCase().equals("diary_week1_pl") || chatbot.toLowerCase().equals("diary_week2_pl") || chatbot.toLowerCase().equals("diary_week3_pl") || chatbot.toLowerCase().equals("diary_week4_1_pl") || chatbot.toLowerCase().equals("diary_week4_2_pl") || chatbot.toLowerCase().equals("diary_week5_pl")) {
                    authUri = "http://x.x.x.x:5024/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("athena_intro")) {
                    authUri = "http://0.0.0.0:5013/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("itcl_time")) {
                    authUri = "http://0.0.0.0:5011/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                if (chatbot.toLowerCase().equals("feri_intro")) {
                    authUri = "http://0.0.0.0:5014/webhooks/rest/webhook"; // uri to service which you get the token
                                                                           // from // USE THIS ONE IN PRODUCTION
                                                                           System.out.println(authUri);
                }
                // String authUri = "http://x.x.x.x:5005/webhooks/rest/webhook"; // uri to
                // service which you get the token from // USE THIS ONE IN PRODUCTION
                // String authUri = "http://164.8.66.117:5005/webhooks/rest/webhook";
                ResponseEntity<Object> response = restTemplate.exchange(authUri, HttpMethod.POST, entity, Object.class);

                // System.out.println("Create Resource: " + response2);

                // String body = response.getBody().toString();
                // System.out.println(body);

                if (!errors.isEmpty()) {
                    exchange.getOut().setBody(errors);
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
                } else {
                    Object responseBody = response.getBody();

                    if (responseBody instanceof List) {
                        List<Object> jsonArray = (List<Object>) responseBody;
                        
                        for (Object item : jsonArray) {
                            if (item instanceof Map) {
                                Map<String, Object> jsonObject = (Map<String, Object>) item;
                                Map<String, Object> custom = (Map<String, Object>) jsonObject.get("custom");
                                
                                if (custom != null && custom.containsKey("endofquestionnaire")) {
                                    headers.setContentType(MediaType.APPLICATION_JSON);
                                    headers.add("Authorization", "api-key");
                                    Map<String, Object> fhir_maps = new LinkedHashMap<>();
                                    fhir_maps.put("sender_id", id);
                                    fhir_maps.put("questionary_id", questionnaire);
                                    entity = new HttpEntity<>(fhir_maps, headers);

                                    authUri = "https://x.x.x.x/api/Chatbot/conversationTrackerToFHIR"; // uri to service which you get
                                                                                                                // the token from
                                    // System.out.println("Sending to OHC: " + authUri);
                                    ResponseEntity<?> response2 = restTemplate.exchange(authUri, HttpMethod.POST, entity,
                                            Object.class); // TODO: SWAGER does not show us what is the structure of response

                                    // System.out.println("Create Resource: " + response2);
                                    // String id = response2.getBody().getId();
                                    // return id;

                                    //String response_body = response2.getStatusCode().toString();


                                    Map<String, Object> payloads = new HashMap<>();
                                    payloads.put("fhir_resource", response2.getBody());
                                    jsonArray.add(payloads);
                                    exchange.getOut().setBody(jsonArray);
                                    return;
                                }
                            }
                        }
                        
                        exchange.getOut().setBody(response.getBody());
                    } else {
                        // Handle the case where the response body is not a JSON array
                        exchange.getOut().setBody(responseBody);
                    }
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
                }

                // System.out.println(body);

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            /*
             * try {
             * throw new Exception("This is a test.");
             * } catch (Exception e) {
             * Sentry.captureException(e);
             * }
             */

        } else {
            exchange.getOut().setBody(mapErrorKey);
            exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
        }

    }

}
