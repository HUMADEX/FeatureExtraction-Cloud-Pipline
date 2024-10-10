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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Message;
import com.fasterxml.jackson.core.JsonParseException;

/**
 *
 * @author UM FERI
 * @date NOV 2020
 * @description Class that implements all the authentication services required to connect user with OHC
 * 
 */

public class PersistUserService 
{
	public PersistUserService() 
	{

	}
	public void PersistLogin(Exchange exchange) 
	{
		Message camelMessage = exchange.getIn();
		System.out.println("Test");

		Map<String, Object> payload = (Map<String, Object>) camelMessage.getBody();
					
		System.out.println("ClientID:" + (String) payload.get("patient_oid"));
		
        exchange.getIn().setBody("id value is too low");
        exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "text/plain");
        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
	}

	
}