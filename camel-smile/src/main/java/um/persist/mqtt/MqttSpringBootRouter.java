package um.persist.mqtt;

import um.persist.*;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;


/**
 *
 * @author UM FERI
 * @date NOV 2020
 * @description MQTT main router class
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */



@Component
public class MqttSpringBootRouter extends RouteBuilder {
	
	
	private Processor print = new Processor() 
	 {
	        @Override
	        public void process(Exchange exchange) throws Exception {
	        	 String client_id = "unknown";
	        	 Message camelMessage = exchange.getIn();
	        	 System.out.println("Received message from MQTT client");
	        	/* RegisterToken payload = (RegisterToken) camelMessage.getBody();
	        	 System.out.println("key=" + payload.getClientId());*/
	        	 /*Map<String, Object> headerValues = camelMessage.getHeaders();
	             for (String header : headerValues.keySet()) {
	            	 topic += "[" + header + "#" + headerValues.get(header) + "]";
	             }
	                 /*results.put(headerLocation,
	                             headerValues.get(headerLocation));
	             }
	             String topic = (String) headers;*/
	            // Message camelMessage = exchange.getIn();

	             /*byte[] body = (byte[]) camelMessage.getBody();
	             String payload = new String(body, "utf-8");*/
	             
	             //System.out.println("topic=" + topic + ", payload=" + payload);
	             Map<String, Object> payload = (Map<String, Object>)camelMessage.getBody();
	             if(payload.containsKey("clientId"))
	             {
	            	 client_id = (String)payload.get("clientId");
	            	 System.out.println("ClientID" + (String)payload.get("clientId"));
	             }
	             
	             exchange.getOut().setBody("Hello " + client_id);
	             
	            /* for (String key : payload.keySet())
	             {
	            	
	            	// .getClass().getSimpleName();
	             }*/
	            	 //.getClass().getSimpleName();

	            // can cast to PahoMessage
	            // PahoMessage pahoMessage = (PahoMessage) camelMessage;
	        }
	  };

	
	@Override
    public void configure() throws Exception {
		
		
		// MQTT =============================================================
		
				// Receive a message from a queue
				//from("jms:topic:test.url?asyncConsumer=true").unmarshal().json(JsonLibrary.Gson, Map.class).process(print).to("jms:topic:response.1234");
				
				/*
		     		.log("JMS/MQTT received a message: ${body}")
					.delay(1000)	
					//.to("jms:topic:test.stej")
					.to("direct:processForKafka")
					.to("direct:processTheFileA") // send to direct endpoint
					.log("Body is now: ${body}")   // will print 'AAAAAAAA!'
					.to("direct:processTheFileB")
					.log("Body is now: ${body}"); // sync within....
					
				// meanwhile...    
				from("direct:processTheFileA")     // receive from direct endpoint
					.setBody(constant("AAAAAAAA! - 200 OK"));            // modify the message body</code>

				from("direct:processTheFileB")    // receive from direct endpoint
					.delay(5000)	
					.setBody(constant("BBBBBBBB! - 200 OK"));     // modify the message body</code>
					
					*/
				
				
				// MQTT to REST post =================================================
				
				/*from("jms:topic:test.url2")
					.log("JMS/MQTT recieved a message: ${body}")
					.setBody(constant("{\"name\": \"CamelMQTT\", \"balance\": 100}"))
					.process(new Processor() {
						@Override
						public void process(Exchange exchange) throws Exception {
							exchange.getIn().setHeader(Exchange.CONTENT_TYPE, MediaType.APPLICATION_JSON);
						}
					})
					.to("rest:post:account?host=x.x.x.x:8080");
				*/
				
				// JMS/MQTT CAMEL to KAFKA and KAFKA to CAMEL JMS/MQTT =============
				/*
				from("direct:processForKafka")
					//.setBody(constant("Message from Camel"))          // Message to send
					.setHeader(KafkaConstants.KEY, constant("Camel")) // Key of the message
					.log("Sending message to Kafka broker.")
					/**.process(new Processor() {
						public void process(Exchange exchange) throws Exception {
							String payload = exchange.getIn().getBody(String.class);
							// do something with the payload and/or exchange here
						   exchange.getIn().setBody(exchange.getIn().getHeaders());
					   }
					})**/ /*
					.to("kafka:test?brokers=x.x.x.x:9092");
					
				from("kafka:test?brokers=x.x.x.x:9092")
					.log("Message received from Kafka : ${body}")
					.log("    on the topic ${headers[kafka.TOPIC]}")
					.log("    on the partition ${headers[kafka.PARTITION]}")
					.log("    with the offset ${headers[kafka.OFFSET]}")
					.log("    with the key ${headers[kafka.KEY]}")
					.process(new Processor() {
						@Override
						public void process(Exchange exchange) throws Exception {
							exchange.getIn().setHeader("JMSDeliveryMode", "1");
					   }
					})
					.to("jms:topic:rtest.client1");
		            */
		
		
	}

}
