package um.persist.chatbot;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.*;
//import javafx.util;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * @author UM FERI
 * @date JAN 2021
 * @description Chatbot service class for REST Chatbot language endpoint
 *
 */

@Service("chatServiceLanguage")

public class ChatbotServiceLanguage {
	public ChatbotServiceLanguage() {

	}

	public Map<String, Object> SendMessage(Map<String, Object> payload) {

		if (!payload.containsKey("sender")) {
			Map<String, Object> error = new LinkedHashMap<String, Object>();
			error.put("error", "sender not supplied");

			return error;
		}
		if (!payload.containsKey("message")) {
			Map<String, Object> error = new LinkedHashMap<String, Object>();
			error.put("error", "message not supplied");

			return error;
		}
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("message_sent", true);

		return response;
	}
	
	
	public Map<String, Object> ListQuestionaries(Map<String, Object> payload) 
	{
		if (!payload.containsKey("language")) 
		{
			Map<String, Object> error = new LinkedHashMap<String, Object>();
			error.put("error", "language must be supplied");
			return error;
		}
		
		PersistPremPromAPI pq = new PersistPremPromAPI();
		
		String langcode = (String)payload.get("language");
		
		List<String> codes = Arrays.asList(langcode.toLowerCase().split(","));
		
		Map<String, Object> Questionaries = new LinkedHashMap<String, Object>();
		
		// EN, SI, LT, ES, FR, RU
		
		if(codes.contains("en")){
			Questionaries.put("en", pq.LangEnglish());
		}
		else{
			Questionaries.put("en", pq.LangEnglish());
		}
		
		/*if(langcode.toLowerCase().equals("en"))
			Questionaries.put("en", pq.LangEnglish());*/
			
		/*if(codes.contains("sl"))
			Questionaries.put("sl", pq.LangSlovenian());
			
		if(codes.contains("lv"))
			Questionaries.put("lv", pq.LangLatvian());
		
		if(codes.contains("es"))
			Questionaries.put("es", pq.LangSpanish());
			
		if(codes.contains("fr"))
			Questionaries.put("fr", pq.LangFrench());
		
		if(codes.contains("ru"))
			Questionaries.put("ru", pq.LangRussian());*/
			
		
		return Questionaries;
		
		
	}
	

}
