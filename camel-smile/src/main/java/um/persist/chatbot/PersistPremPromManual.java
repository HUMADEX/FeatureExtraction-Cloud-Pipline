package um.persist.chatbot;

//import javafx.util;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * @author UM FERI
 * @date JAN 2021
 * @description Chatbot PREM/PROM non-loop setters and getters
 *
 */

@Service("persistPremPromManual")

public class PersistPremPromManual extends PersistPremPromAPI {
	
	public PersistPremPromManual()  { }
	
	
    private String language_code ="";
	private String persist_oid = "";
	private String chatbot_id = "";
	private String questionary_title = "";
	private String questionary_type = "";
	
	 public String getLanguageCode() 
	 {
	        return language_code;
	 }

	public void setLanguageCode(String language_code) 
	{
	        this.language_code = language_code;
	}
	
	public String getPersistOid() 
    {
	        return persist_oid;
	}

	public void setPersistOid(String persist_oid) 
	{
	        this.persist_oid = persist_oid;
	}
	
	public void setQuestionaryType(String questionary_type) 
	{
	        this.questionary_type = questionary_type;
	}
	
	public String getChatbotId() 
    {
	        return chatbot_id;
	}
	
	public String getQuestionaryType() 
	{
	        return questionary_type;
	}

	public void setChatbotId(String chatbot_id) 
	{
	        this.chatbot_id = chatbot_id;
	}
	
	public void setQuestionaryTitle(String questionary_title) 
	{
	        this.questionary_title = questionary_title;
	}
	
	public String getQuestionaryTitle() 
    {
	        return questionary_title;
	}
}