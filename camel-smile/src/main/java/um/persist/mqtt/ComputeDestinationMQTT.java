package um.persist.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import um.persist.*;

import org.springframework.stereotype.Service;
import org.apache.camel.Exchange;
import org.apache.camel.Message;

import java.nio.charset.StandardCharsets;
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

import java.util.logging.Logger;

/**
 *
 * @author UM FERI
 * @date JAN 2021
 * @description Class for receiving Json from Swagger REST API and sending to MQTT broker
 *
 */

@Service("computeDestinationMQTT")

public class ComputeDestinationMQTT {
    public ComputeDestinationMQTT() {

    }
	
	Logger logger = Logger.getLogger(String.valueOf(this));

    public void setJmsHeader(Exchange exchange) {

        String mqtt_topic = "Topic not defined. Either patient_id or hospital_id must be left empty!";
		String mqtt_type = "MQTT type is not defined";
        String client_id = "Error. No client id.";
        String mqtt_message;
		String message_payload = "No message";
		String language = "No language";
		String message_type = "No message type";
		String questionnaire_id = "questionnaire_id not defined";
		String patient_id = "patient_id not defined";
		String clinician_id = "clinician_id not defined";
		String hospital_id = "hospital_id not defined";
		String task_id = "task_id not defined";
        Map<String, Object> data = new LinkedHashMap<>();
        Message camelMessage = exchange.getIn();
		Map<String, Object> val = new LinkedHashMap<>();

        Map<String, Object> payload = (Map<String, Object>) camelMessage.getBody();
                /*if (payload.containsKey("topic")) {
                    mqtt_topic = (String) payload.get("topic");
                }*/
        if (!payload.containsKey("data")) {
            val.put("error", "data not provided, add it");
        }
		if (!payload.containsKey("patient_id")) {
            val.put("error", "patient_id not provided, add it");
        }
        if (!payload.containsKey("language")) {
            val.put("error", "language not provided, add it");
        }
        if (!payload.containsKey("message")) {
            val.put("error", "message not provided, add it");
        }
        if (payload.containsKey("questionnaire_id")) {
            questionnaire_id = (String) payload.get("questionnaire_id");
        }
		if (payload.containsKey("clinician_id")) {
            clinician_id = (String) payload.get("clinician_id");
        }
		if (payload.containsKey("patient_id")) {
            patient_id = (String) payload.get("patient_id");
        }
		if (payload.containsKey("hospital_id")) {
            hospital_id = (String) payload.get("hospital_id");
        }
		if (payload.containsKey("task_id")) {
            task_id = (String) payload.get("task_id");
        }
		if (payload.containsKey("type")) {
            message_type = (String) payload.get("message_type");
        }
		if (payload.containsKey("language")) {
            language = (String) payload.get("language");
        }
		if (payload.containsKey("message")) {
            message_payload = (String) payload.get("message");
        }
        if (payload.containsKey("data")) {
            data = (Map<String, Object>) payload.get("data");
        }
		/*if (payload.containsKey("mqtt_type")) {
            mqtt_type = (String) payload.get("mqtt_type");
			System.out.println("Checking MQTT type: " + mqtt_type);
        }*/
		
		ObjectMapper objectMapper = new ObjectMapper();
		String json = "No json data. Invalid input.";

        try {
            json = objectMapper.writeValueAsString(payload);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        mqtt_message = camelMessage.getBody(String.class);
        //mqtt_topic = (String) camelMessage.getHeader("topic");
        //System.out.println("Camel got next topic from REST API: " + mqtt_topic);


		if (payload.containsKey("patient_id") && payload.containsKey("clinician_id") && payload.containsKey("hospital_id")  && payload.containsKey("type") && payload.containsKey("language") && payload.containsKey("message") && payload.containsKey("data")){
		/*System.out.println("Checking MQTT type (if): " + mqtt_type);
		System.out.println("Checking MQTT topic (if): " + mqtt_topic);
		if (mqtt_type.equals("unicast")){
		mqtt_topic = "patient/" + patient_id;
		System.out.println("Checking MQTT topic (in if): " + mqtt_topic);
		}
		if (mqtt_type.equals("multicast")){
		mqtt_topic = "patient/group/" + patient_id;
		}
		if (mqtt_type.equals("broadcast")){
		mqtt_topic = "patient/group/everyone";
		}
		System.out.println("Checking MQTT topic (after if): " + mqtt_topic);
		 */
		if(!hospital_id.isEmpty() && !patient_id.isEmpty() && clinician_id.isEmpty()){
			val.put("error", "hospital_id or patient_id need to be empty string. only one of them can be set as non-empty string in the same request");
		}else if(!hospital_id.isEmpty() && patient_id.isEmpty() && !clinician_id.isEmpty()){
			val.put("error", "hospital_id or clinician_id need to be empty string. only one of them can be set as non-empty string in the same request");
		}else if(hospital_id.isEmpty() && !patient_id.isEmpty() && !clinician_id.isEmpty()){
			val.put("error", "patient_id or clinician_id need to be empty string. only one of them can be set as non-empty string in the same request");
		}else{
		
		if(hospital_id.isEmpty() && patient_id.isEmpty()){
             mqtt_topic =  "persist/clinician/" + clinician_id;
            }
		if(hospital_id.isEmpty() && clinician_id.isEmpty()){
             mqtt_topic =  "persist/patient/" + patient_id;
            }
		if(patient_id.isEmpty() && clinician_id.isEmpty()){
             mqtt_topic =  "persist/group/" + hospital_id;
            }
		if(hospital_id.isEmpty() && patient_id.isEmpty() && clinician_id.isEmpty()){
             //mqtt_topic =  "persist/everyone";
			 mqtt_topic =  "persist/clinicians";
            }
		if(!hospital_id.isEmpty() && !patient_id.isEmpty() && !clinician_id.isEmpty()){
			 //mqtt_topic =  "Topic can't be defined. Either patient_id or hospital_id or clinician_id must be left empty!";
			 val.put("error", "Topic can't be defined. Either patient_id or hospital_id or clinician_id must be left empty!");
		}
		else{
				
        int qos = 0;
        String broker = "tcp://x.x.x.x:8883";
        String clientId = patient_id;
        MemoryPersistence persistence = new MemoryPersistence();
		String username = "artemis";
		String password = "simatreahcapa";
		
		//String broker = "tcp://164.8.66.96:8883";
        //String clientId = patient_id;
        //MemoryPersistence persistence = new MemoryPersistence();
		//String username = "artemis";
		//String password = "KFHE2s84ddSLt";

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
			connOpts.setUserName(username);
			connOpts.setPassword(password.toCharArray());
            //System.out.println("Swagger REST sendForMqtt MQTT Client Connecting to Artemis Broker: " + broker);
			logger.info("Swagger REST sendForMqtt MQTT Client Connecting to Artemis Broker: " + broker);
            sampleClient.connect(connOpts);
            //System.out.println("Swagger REST sendForMqtt MQTT Client Connected");
			logger.info("Swagger REST sendForMqtt MQTT Client Connected");
            //System.out.println("Swagger REST sendForMqtt MQTT Client Publishing Message: " + mqtt_message);
			logger.info("Swagger REST sendForMqtt MQTT Client Publishing Message: " + mqtt_message);
            MqttMessage message = new MqttMessage(json.getBytes());
            message.setQos(qos);
            sampleClient.publish(mqtt_topic, message);
            //System.out.println("Swagger REST sendForMqtt MQTT Client Message Published to Topic: " + mqtt_topic);
			logger.info("Swagger REST sendForMqtt MQTT Client Message Published to Topic: " + mqtt_topic);
            sampleClient.disconnect();
            //System.out.println("Swagger REST sendForMqtt MQTT Client Disconnected");
			logger.info("Swagger REST sendForMqtt MQTT Client Disconnected");
			
			val.put("info", "message sent");
			//val.put("topic", mqtt_topic);
			val.put("body", payload);
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
			
			val.put("error", "message not sent, invalid mqtt connection");
        }
		}
		}
		
		}else{
			val.put("error", "check that json contains clinician_id, patient_id, hospital_id, type, language, message, data");
		}
		

        exchange.getOut().setBody(val);
    }

}