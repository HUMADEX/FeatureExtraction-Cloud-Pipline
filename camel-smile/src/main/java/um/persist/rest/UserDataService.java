package um.persist.rest;

import um.persist.*;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
//import javafx.util;

import org.springframework.stereotype.Service;
import org.apache.camel.model.dataformat.JsonLibrary;

//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;

/**
 *
 * @author UM FERI
 * @date NOV 2020
 * @description Class that contains dummy endpoints subclasses for Emoda
 *
 */

@Service("userDataService")

public class UserDataService {
	public UserDataService() {

	}

	private Random ran = new Random();
	//private Gson gson =  new Gson();

	public Map<String, Object> TestAPI(Map<String, Object> payload) {
		System.out.println("abcd:" + String.valueOf(payload.containsKey("userid")));

		Map<String, Object> resonse = new LinkedHashMap<String, Object>();

		resonse.put("steps", Integer.toString(ran.nextInt(25000)));
		resonse.put("userid", (payload.containsKey("userid") ? (String) payload.get("userid") : ""));

		return resonse;
	}

	public Map<String, Object> GetTodaysStepsSoFar(Map<String, Object> payload) {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("steps", Integer.toString(ran.nextInt(30000)));
		return response;

	}

	public Map<String, Object> GetDailyStepGoal(Map<String, Object> payload) {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("steps", 10000);

		return response;

	}

	public Map<String, Object> GetStepsByHour(Map<String, Object> payload) {

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z", Locale.getDefault());
		Date date = new Date();

		Map<String, Object> response = new LinkedHashMap<String, Object>();
		List<Integer> values = new LinkedList<Integer>();
		for (int i = 0; i < 24; i++) {
			values.add(ran.nextInt(100));
		}
		response.put("values",values);
		response.put("now",formatter.format(date));

		return response;

	}

	public Map<String, Object> GetCaloriesByHour(Map<String, Object> payload) {

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z", Locale.getDefault());
		Date date = new Date();

		Map<String, Object> response = new LinkedHashMap<String, Object>();
		List<Integer> values = new LinkedList<Integer>();
		for (int i = 0; i < 24; i++) {
			values.add(ran.nextInt(100));
		}
		response.put("values",values);
		response.put("now",formatter.format(date));

		return response;

	}

	public Map<String, Object> GetSleepDetails(Map<String, Object> payload) {

		if (!payload.containsKey("day_start_time")) {
			Map<String, Object> error = new LinkedHashMap<String, Object>();
			error.put("error", "day_start_time not supplied");

			return error;
		}
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z", Locale.getDefault());
			Date date = formatter.parse((String) payload.get("day_start_time"));
			Map<String, Object> response = new LinkedHashMap<String, Object>();

			List<Map<String, String>> r1 = new LinkedList<Map<String, String>>();
			Map<String, String> val = new LinkedHashMap<>();
			val.put("start", "2020-09-29T00:00:00+02:00");
			val.put("end", "2020-09-29T04:00:00+02:00");
			r1.add(val);
			val = new LinkedHashMap<>();
			val.put("start", "2020-09-29T00:00:00+02:00");
			val.put("end", "2020-09-29T04:00:00+02:00");
			r1.add(val);
			response.put("shallow_sleep_time_frames", r1);

			r1 = new LinkedList<Map<String, String>>();
			val = new LinkedHashMap<>();
			val.put("start", "2020-09-29T00:00:00+02:00");
			val.put("end", "2020-09-29T04:00:00+02:00");
			r1.add(val);
			val = new LinkedHashMap<>();
			val.put("start", "2020-09-29T00:00:00+02:00");
			val.put("end", "2020-09-29T04:00:00+02:00");
			r1.add(val);
			response.put("deep_sleep_time_frames", r1);

			return response;

		} catch (Exception e) {
			Map<String, Object> error = new LinkedHashMap<String, Object>();
			error.put("error", "wrong date time fromat, please use yyyy-MM-dd'T'HH:mm:ss Z");
			return error;
		}

	}

	public Map<String, Object> GetMessageHistoryWith(Map<String, Object> payload) {

		if (!payload.containsKey("user_id")) {
			Map<String, Object> error = new LinkedHashMap<String, Object>();
			error.put("error", "user_id not supplied");

			return error;
		}
		Map<String, Object> response = new LinkedHashMap<String, Object>();

		List<Map<String, String>> r1 = new LinkedList<Map<String, String>>();
		Map<String, String> val = new LinkedHashMap<>();
		val.put("time", "2020-09-29T09:00:00+02:00");
		// val.put("from", (payload.containsKey("user_id") ? (String)
		// payload.get("userid") : ""));
		val.put("from", "doctor_001");
		val.put("text", "How are you doing today Mr. Brown?");
		r1.add(val);
		val = new LinkedHashMap<>();
		val.put("time", "2020-09-29T09:01:00+02:00");
		// val.put("from", (payload.containsKey("user_id") ? (String)
		// payload.get("userid") : ""));
		val.put("from", "patient_001");
		val.put("text", "I'm fine, thank you.");
		r1.add(val);
		response.put("messages", r1);

		return response;
	}

	public Map<String, Object> SendMessage(Map<String, Object> payload) {

		if (!payload.containsKey("to_user_id")) {
			Map<String, Object> error = new LinkedHashMap<String, Object>();
			error.put("error", "to_user_id not supplied");

			return error;
		}
		if (!payload.containsKey("message_text")) {
			Map<String, Object> error = new LinkedHashMap<String, Object>();
			error.put("error", "message_text not supplied");

			return error;
		}
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("response", true);

		return response;
	}

	public  Map<String, Object> GetTrends(Map<String, Object> payload) {
		Map<String, Object> error = new LinkedHashMap<String, Object>();
		List<String> errors = new LinkedList<String>();
		if (!payload.containsKey("type")) 
		{
			 
			errors.add("type not supplied");

			
		}
		if (!payload.containsKey("start_time")) {
			
			errors.add("start_time not supplied");

			
		}
		if (!payload.containsKey("end_time")) {
		
			errors.add("end_time not supplied");

		}
		
		if(errors.size() > 0)
		{
			
			error.put("error", errors);
			return error;
		}	
			
		try 
		{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z", Locale.getDefault());
		Date datestart = formatter.parse((String) payload.get("start_time"));
		
		Date dateend = formatter.parse((String) payload.get("end_time"));
		} catch (Exception e) 
		{
		
		error.put("error", "wrong date time fromat for start_time or end_time, please use yyyy-MM-dd'T'HH:mm:ss Z");
		return error;
		}
		Map<String, Object> response = new LinkedHashMap<String, Object>();

		List<Map<String, String>> r1 = new LinkedList<Map<String, String>>();
		Map<String, String> val = new LinkedHashMap<>();
		// response.put("type", "heartRate");
		val.put("time", "2020-09-29T00:00:00+02:00");
		val.put("value", "80");
		r1.add(val);
		val = new LinkedHashMap<>();
		val.put("time", "2020-09-29T00:10:00+02:00");
		val.put("value", "81");
		r1.add(val);
		val = new LinkedHashMap<>();
		val.put("time", "2020-09-29T00:20:00+02:00");
		val.put("value", "87");
		r1.add(val);
		val = new LinkedHashMap<>();
		val.put("time", "2020-09-29T00:30:00+02:00");
		val.put("value", "80");
		r1.add(val);
		val = new LinkedHashMap<>();
		val.put("time", "2020-09-29T00:40:00+02:00");
		val.put("value", "82");
		r1.add(val);
		val = new LinkedHashMap<>();
		val.put("time", "2020-09-29T00:50:00+02:00");
		val.put("value", "80");
		r1.add(val);
		val = new LinkedHashMap<>();
		val.put("time", "2020-09-29T01:00:00+02:00");
		val.put("value", "88");
		r1.add(val);
		val = new LinkedHashMap<>();
		val.put("time", "2020-09-29T00:10:00+02:00");
		val.put("value", "80");
		r1.add(val);
		val = new LinkedHashMap<>();
		val.put("time", "2020-09-29T00:00:00+02:00");
		val.put("value", "80");
		r1.add(val);
		response.put("values", r1);

		return response;

	}

	public Map<String, Object> GetPromQuestions(Map<String, Object> payload) {

		Map<String, Object> response = new LinkedHashMap<String, Object>();

		List<Map<String, Object>> r1 = new LinkedList<Map<String, Object>>();
		Map<String, Object> val = new LinkedHashMap<String, Object>();
		val.put("question_id", "0");
		val.put("question", "Are you feeling well?");
		val.put("type", "radiobox");
		val.put("domain", "psychological");
		
		List<Map<String, Object>> opts = new LinkedList<Map<String, Object>>();
		Map<String, Object> optitem =  new LinkedHashMap<String, Object>();
		optitem.put("option_id", 0);
		optitem.put("option_value", "Y");
		optitem.put("option_text", "YES");
		opts.add(optitem);
		
		optitem =  new LinkedHashMap<String, Object>();
		optitem.put("option_id", 1);
		optitem.put("option_value", "N");
		optitem.put("option_text", "NO");
		opts.add(optitem);
		val.put("options", opts);
		
		
		r1.add(val);
		val = new LinkedHashMap<String, Object>();
		val.put("question_id", "1");
		val.put("question", "Who did you meet today?");
		val.put("type", "input");
		val.put("domain", "social");
		r1.add(val);
		response.put("questions", r1);

		return response;

	}
	
	public Map<String, Object> GetMessagePeers(Map<String, Object> payload) {

		Map<String, Object> response = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> r1 = new LinkedList<Map<String, Object>>();
		Map<String, Object> val = new LinkedHashMap<String, Object>();
		val.put("user_id", "doctor_001");
		val.put("name", "Dr. John Anderson");

		Map<String, Object> opts = new LinkedHashMap<String, Object>();
		Map<String, Object> optitem =  new LinkedHashMap<String, Object>();
		optitem.put("time", "2020-09-29T09:00:00+02:00");
		optitem.put("from", "doctor_001");
		optitem.put("text", "How are you doing today Mr. Brown?");
		opts.putAll(optitem);
		val.put("last_message", opts);

		r1.add(val);
		response.put("peers", r1);

		return response;

	}
	
	public Map<String, Object> GetUserDetails(Map<String, Object> payload) {

		if (!payload.containsKey("user_id")) {
			Map<String, Object> error = new LinkedHashMap<String, Object>();
			error.put("error", "user_id not supplied");

			return error;
		}
		Map<String, Object> response = new LinkedHashMap<String, Object>();

		response.put("user_id", "doctor_001");
		response.put("name", "Dr. John Anderson");
		response.put("ppic", "http://example.com/profilepic.png");

		return response;
	}
	
	public Map<String, Object> SendPromAnswers(Map<String, Object> payload) {

		if (!payload.containsKey("answers")) {
			Map<String, Object> error = new LinkedHashMap<String, Object>();
			error.put("error", "answers not supplied");

			return error;
		}
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("response", true);

		return response;
	}


	public Boolean CheckPayload(Map<String, Object> payload) {
		if (payload.isEmpty())
			return false;
		return true;
		// System.out.println(payload);
	}

}
