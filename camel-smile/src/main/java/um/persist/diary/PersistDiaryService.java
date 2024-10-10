package um.persist.diary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.bouncycastle.pqc.jcajce.provider.qtesla.SignatureSpi.qTESLA;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import java.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.sentry.Sentry;
import um.persist.chatbot.SymptomaQuestionnaire;
import um.persist.swagger_examples.symptoma.*;

import com.mashape.unirest.http.HttpResponse;
import com.google.common.reflect.TypeToken;

public class PersistDiaryService {

    public String OHC_endpoint_diary = "https://domain.com/v2downloadFile/";

    public PersistDiaryService() {

    }

    public String PERSISTOHCLogin() throws Exception {
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post("https://domain.com/auth/realms/persist/protocol/openid-connect/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .field("grant_type", "password")
                .field("username", "username")
                .field("password", "password")
                .field("client_id", "id")
                .asString();

        String body = response.getBody();

        Map<String, Object> retMap = new Gson().fromJson(
                body, new TypeToken<HashMap<String, Object>>() {
                }.getType()
        );

        String token = retMap.get("access_token").toString();
		//Unirest.shutdown();
        return token;
    }

    public void SetRestResponse(boolean sucess, Map<String, Object> Message, Exchange exchange) {
        exchange.getIn().setBody(Message);
        exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");

        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, (sucess) ? 200 : 400);
    }

    private void ResourceAddReuest(String token, String video_id) {
        HttpHeaders headers = new HttpHeaders();
        //headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        headers.add("Authorization", "Bearer " + token);
        headers.add("Accept", "*/*");
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        String Uri = this.OHC_endpoint_diary + video_id; // uri to service which you get the token from
        System.out.println("Sending to OHC: GET " + Uri);
        ResponseEntity<byte[]> response2 =
                restTemplate.exchange(Uri, HttpMethod.GET, entity,
                byte[].class); //TODO: SWAGER does not show us what is the structure of response
    
        try {
            Files.write(Paths.get("/home/user/camel/patient_videos/"+video_id+".mp4"), response2.getBody());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //return response2;
    }

    public void DownloadDiaryVideo(Exchange exchange) {

        Message camelMessage = exchange.getIn();
        String video_id = (String) camelMessage.getHeader("id");

        System.out.println(camelMessage);
        //String dummy_json = "{\"sender\":\"dd\",\"message\":\"hey\"}";

        ObjectMapper mapObject = new ObjectMapper();
        //Map<String, Object> mapObj = mapObject.convertValue(camelMessage.getBody(), Map.class);
		
		Map<String, Object> mapErrorKey = new LinkedHashMap<>();
        mapErrorKey.put("error","invalid_api_key");
        mapErrorKey.put("error_description","API Key verification failed. Please provide the correct API Key");
		
		String api_key = String.valueOf(camelMessage.getHeader("Authorization"));
        System.out.println("API KEY: " + api_key);

        if(api_key.equals("api-key")){
			
			List<String> errors = new LinkedList<String>();
            String accesstoken = "";
            try {
                accesstoken = PERSISTOHCLogin();
            } catch (Exception e) {
                errors.add("could not retrieve token {" + e.getMessage() + "}");
            }
            if (errors.size() > 0) {
                Map<String, Object> r = new LinkedHashMap<String, Object>();
                r.put("error", String.join(";", errors));
                this.SetRestResponse(false, r, exchange);
                return;
            }

            //String video_id = String.valueOf(mapObj.get("video_id"));

            this.ResourceAddReuest(accesstoken, video_id);
            File file = new File("/home/user/camel/patient_videos/"+video_id+".mp4");

            // return File
            exchange.getOut().setBody(file);
            exchange.getOut().setHeader(Exchange.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
            exchange.getOut().setHeader("Content-Type", "video/mp4");
            exchange.getOut().setHeader("Transfer-Encoding", "chunked");
            exchange.getOut().setHeader("Content-Disposition", "attachment; filename=\"" + video_id + ".mp4\"");
            //exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

        } 


    }
    
}
