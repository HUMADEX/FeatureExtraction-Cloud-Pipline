package um.persist.chatbot;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.sentry.Sentry;
import um.persist.chatbot.SymptomaQuestionnaire;
import um.persist.swagger_examples.symptoma.*;

import com.mashape.unirest.http.HttpResponse;
import com.google.common.reflect.TypeToken;

public class PersistGreetingVideo {


    public PersistGreetingVideo() {

    }

    public void GetGreetingVideo(Exchange exchange) {

        Message camelMessage = exchange.getIn();

        System.out.println(camelMessage);
        //String dummy_json = "{\"sender\":\"dd\",\"message\":\"hey\"}";

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
        LocalDateTime now = LocalDateTime.now();  
        System.out.println(dtf.format(now));  

        ObjectMapper mapObject = new ObjectMapper();
        //Map<String, Object> mapObj = mapObject.convertValue(camelMessage.getBody(), Map.class);
		
		Map<String, Object> mapErrorKey = new LinkedHashMap<>();
        mapErrorKey.put("error","invalid_api_key");
        mapErrorKey.put("error_description","API Key verification failed. Please provide the correct API Key");
		
		String api_key = String.valueOf(camelMessage.getHeader("Authorization"));
        System.out.println("OK! API KEY.");

        if(api_key.equals("test123")){

            //File file = new File("/home/matejr/camel/depression.mp4");

            //exchange.getOut().setBody(file);
            //exchange.getOut().setHeader(Exchange.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
            //exchange.getOut().setHeader("Content-Type", "video/mp4");
            //exchange.getOut().setHeader("Transfer-Encoding", "chunked");
            //exchange.getOut().setHeader("Content-Disposition", "attachment; filename=\"" + "depression" + ".mp4\"");
            //exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
            //exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

        //Map<String, Object> response = new LinkedHashMap<String, Object>();
        //List<Map<String, String>> r = new LinkedList<Map<String, String>>();
        
		Map<String, List<String>> val = new LinkedHashMap<>();
        List<String> list_en = new LinkedList<String>();
        list_en.add("https://youtu.be/2Z_agr-OPPk?list=PLg9ehxVKs-j6D8zSKBCx9akSDYosxgPmv?autoplay=1&mute=0&controls=0&modestbranding=1");
        list_en.add("https://youtu.be/2Z_agr-OPPk?list=PLg9ehxVKs-j6D8zSKBCx9akSDYosxgPmv?autoplay=1&mute=0&controls=0&modestbranding=1");
		val.put("en", list_en);
        List<String> list_es = new LinkedList<String>();
        list_es.add("https://youtu.be/ZAfQtUibyfA?list=PLg9ehxVKs-j4AQLIWGbQfrBszoCEdfzca?autoplay=1&mute=0&controls=0&modestbranding=1");
        list_es.add("https://youtu.be/ZAfQtUibyfA?list=PLg9ehxVKs-j4AQLIWGbQfrBszoCEdfzca?autoplay=1&mute=0&controls=0&modestbranding=1");
        val.put("es", list_es);
        List<String> list_fr = new LinkedList<String>();
        list_fr.add("https://youtu.be/l5aOLI20RVM?list=PLg9ehxVKs-j4AQLIWGbQfrBszoCEdfzca?autoplay=1&mute=0&controls=0&modestbranding=1");
        list_fr.add("https://youtu.be/l5aOLI20RVM?list=PLg9ehxVKs-j4AQLIWGbQfrBszoCEdfzca?autoplay=1&mute=0&controls=0&modestbranding=1");
        val.put("fr", list_fr);
        List<String> list_sl = new LinkedList<String>();
        list_sl.add("https://youtu.be/v97DO7XJy2Q?list=PLg9ehxVKs-j6D8zSKBCx9akSDYosxgPmv?autoplay=1&mute=0&controls=0&modestbranding=1");
        list_sl.add("https://youtu.be/v97DO7XJy2Q?list=PLg9ehxVKs-j6D8zSKBCx9akSDYosxgPmv?autoplay=1&mute=0&controls=0&modestbranding=1");
        val.put("sl", list_sl);
        List<String> list_lv = new LinkedList<String>();
        list_lv.add("https://youtu.be/dn6yoqY4j3A?list=PLg9ehxVKs-j4AQLIWGbQfrBszoCEdfzca?autoplay=1&mute=0&controls=0&modestbranding=1");
        list_lv.add("https://youtu.be/dn6yoqY4j3A?list=PLg9ehxVKs-j4AQLIWGbQfrBszoCEdfzca?autoplay=1&mute=0&controls=0&modestbranding=1");
        val.put("lv", list_lv);
        List<String> list_ru = new LinkedList<String>();
        list_ru.add("https://youtu.be/H9-skVX9kFA?list=PLg9ehxVKs-j4AQLIWGbQfrBszoCEdfzca?autoplay=1&mute=0&controls=0&modestbranding=1");
        list_ru.add("https://youtu.be/H9-skVX9kFA?list=PLg9ehxVKs-j4AQLIWGbQfrBszoCEdfzca?autoplay=1&mute=0&controls=0&modestbranding=1");
        val.put("ru", list_ru);
		// response.put("type", "heartRate");
		//r.add(val);
		//response.put("values", r1);

        System.out.println("OK! Sending greeting videos to user as requested.");
        exchange.getOut().setBody(val);
        exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

        }
        else{
            exchange.getOut().setBody(mapErrorKey);
            exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
        } 


    }

    
}
