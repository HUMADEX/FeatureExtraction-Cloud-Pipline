package um.persist.kafka;

import org.apache.camel.component.kafka.KafkaConstants;
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
 * @author UM FERI
 * @date NOV 2020
 * @description A Camel route that send to and receive messages from Kafka broker
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */


@Component
public class KafkaSpringBootRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {
		/*
        // CAMEL receive a message from a JMS/MQTT topic test/url queue and send to processForKafka - MQTT to KAFKA
        from("jms:topic:test.url?asyncConsumer=true")
                .log("JMS/MQTT received a message: ${body}")
                .delay(1000)
                .to("direct:processForKafka");

        // JMS/MQTT CAMEL to KAFKA broker x.x.x.x:9092
        from("direct:processForKafka")
                // Message to send
                //.setBody(constant("Message from Camel"))
                // Key of the message
                .setHeader(KafkaConstants.KEY, constant("Camel"))
                .log("Sending message to Kafka broker")
                .to("kafka:test?brokers=x.x.x.x:9092");

        // KAFKA broker x.x.x.x:9092 to CAMEL JMS/MQTT topic test/client
        from("kafka:test?brokers=164.8.66.88:9092")
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
                .to("jms:topic:test.client");

        // REST POST to KAFKA
        //from("rest:post:persist-mongodb?protocol=https").to("direct:insertMongoDB");
		*/
    }

}
