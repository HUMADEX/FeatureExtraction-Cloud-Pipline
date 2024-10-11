package um.persist.rest;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Message;
import com.fasterxml.jackson.core.JsonParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

// Authorization encoding

//import org.springframework.security.authentication.*;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import um.persist.services.SynthAudioPersistService;
import um.persist.services.UploadSMILEVideo;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.apache.camel.model.dataformat.JsonLibrary;

import org.springframework.beans.factory.annotation.Value;
import um.persist.chatbot.*;
import um.persist.chatgpt.ChatGPTService;
import um.persist.diary.PersistAllDiariesService;
import um.persist.diary.PersistDiaryService;
import um.persist.diary.PersistFilterDiaryService;
import um.persist.diary.PersistProcessAllDiariesService;
import um.persist.mqtt.ComputeDestinationMQTT;
import um.persist.mqtt.RegisterMQTT;
import um.persist.services.ASRService;
import um.persist.services.DiaryService;
import um.persist.services.DiaryServiceV2;
import um.persist.services.DiaryServiceV2D;
import um.persist.services.GesturesService;
import um.persist.test.Patient;
import um.persist.swagger_examples.chatgpt.ChatGPT;
import um.persist.swagger_examples.fhir.DiaryMRAST;
import um.persist.swagger_examples.fhir.FHIR;
import um.persist.swagger_examples.fhir.StoreActivityDefinition;
import um.persist.swagger_examples.fhir.StoreCDSSResults;
import um.persist.swagger_examples.fhir.StoreCarePlan;
import um.persist.swagger_examples.fhir.StoreDocumentReference;
import um.persist.swagger_examples.fhir.StorePatient;
import um.persist.swagger_examples.fhir.StoreQuestionnaireResponse;
import um.persist.swagger_examples.fhir.StoreTask;
import um.persist.swagger_examples.fhir.UpdateTask;
import um.persist.swagger_examples.symptoma.*;
import um.persist.swagger_examples.symptoma.SymptomaJSONQuestionnaire;
import um.persist.swagger_examples.synthaudio.SynthAudio;

import static org.apache.camel.model.rest.RestParamType.body;
import static org.apache.camel.model.rest.RestParamType.path;
import static org.apache.camel.model.rest.RestParamType.query;

/**
 * @author UM FERI
 * @date DEC 2020
 * @description Camel REST DSL and routing
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */

@Component
public class RestSpringBootRouter extends RouteBuilder {

    @Value("${camel.component.servlet.mapping.context-path}")
    private String contextPath;

    private Processor RestLogin = new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
            String client_id = "unknown";
            Message camelMessage = exchange.getIn();
            System.out.println("Test");

            Map<String, Object> payload = (Map<String, Object>) camelMessage.getBody();
            if (payload.containsKey("clientId")) {
                client_id = (String) payload.get("clientId");
                System.out.println("ClientID" + (String) payload.get("clientId"));
            }


            Map<String, String> resonse = new LinkedHashMap<String, String>();

            resonse.put("steps", Integer.toString(1000));
            resonse.put("userid", (payload.containsKey("userid") ? (String) payload.get("userid") : ""));

            exchange.getOut().setBody(resonse);

        }
    };

    private Processor ObtainToken = new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
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

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            HttpEntity<?> entity = new HttpEntity<>("{\"resourceType\": \"Patient\"}", headers);

            RestTemplate restTemplate = new RestTemplate();
            String authUri = "https://domain.com/fhir/org1/Patient"; // uri to service which you get the token from
            ResponseEntity<Patient> response2 =
                    restTemplate.exchange(authUri, HttpMethod.POST, entity,
                            Patient.class);

            //System.out.println("Create Resource: " + response2);

            String id = response2.getBody().getId();

            exchange.getOut().setBody("Obtained Token: " + token + "\n\nCreated Resource ID: " + id + "\n\nCreated Resource Response: " + response2);

        }
    };

    private Processor PersistLogin = new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
			
			
			/*Message camelMessage = exchange.getIn();
			String body = camelMessage.getBody();

			Map<String, Object> payload = new Gson().fromJson(
					body, new TypeToken<HashMap<String, Object>>() {}.getType()
			);*/

            Message camelMessage = exchange.getIn();
            System.out.println("Test");

            Map<String, Object> payload = (Map<String, Object>) camelMessage.getBody();

            System.out.println("ClientID:" + (String) payload.get("patient_oid"));


            if (payload.isEmpty()) {
                exchange.getOut().setBody("please provide  payload in correct format");
            } else {

            }

        }
    };

    public class ReadJsonAsString {

            String file;
            String json;

        public String readFileAsString(String file)throws Exception
        {
            return new String(Files.readAllBytes(Paths.get(file)));
        }

        public String getJson(String s) {
            file = "src/main/resources/" + s;
            {
                try {
                    json = readFileAsString(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return json;
        }

    }

    // Redirects for Swagger UI

    @Controller
    public class SwaggerConfig {
        @RequestMapping("/api-doc")
        public String redirectToUi() {
            return "redirect:/webjars/swagger-ui/index.html?url=/api/swagger&validatorUrl=";
        }
    }


    @Controller
    public class SwaggerConfig2 {
        @RequestMapping("/swagger-ui")
        public String redirectToUi() {
            return "redirect:/webjars/swagger-ui/index.html?url=/api/swagger&validatorUrl=";
        }
    }

     @Controller
    public class SwaggerConfig3 {
        @RequestMapping("/")
        public String redirectToUi() {
            return "redirect:/webjars/swagger-ui/index.html?url=/api/swagger&validatorUrl=";
        }
    }

    @Override
    public void configure() throws Exception {

        ReadJsonAsString readJson = new ReadJsonAsString();

        // REST DSL CONFIG =======================================================================================
		
        String listenAddress = "0.0.0.0";
        int listenPort = 8000;

        restConfiguration()
                // .component("netty-http")
                .component("servlet")
                //.component("jetty")
                .scheme("https")
                .host(listenAddress)
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true")
                .enableCORS(true)
                .port(listenPort)
                .apiContextPath("/swagger") //swagger endpoint path
                .apiContextRouteId("swagger") //id of route providing the swagger endpoint
                .contextPath("/api") //base.path swagger property; use the mapping path set for CamelServlet
                .apiProperty("api.title", "SMILE Swagger - UM REST API")
                .apiProperty("api.version", "2024.09.12")
                .apiProperty("api.description", "Contact info: admin");

        onException(JsonParseException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .setBody().constant("Invalid json data. Body must not be empty.");

        // REST DSL ==============================================================================================
				
		
		/*rest("/Chatbot").description("Chatbot Questionnaire Service").securityDefinitions()
					.apiKey("api_key").withHeader("Authorization").end()
				.end()
                .consumes("application/json").produces("application/json")
                .post("/listQuestionaries").id("listQuestionaries").security("api_key")
                .description("List all available questionaries by language")
				//.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
                //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
                .param().name("body").type(body).description("Assign desired Language"+
				"\nExamples: {\"language\": \"en\"} or add multiple and separate by comma {\"language\": \"en,sl,lv,es,fr,ru\"}").required(true)
                //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
				.endParam()
                .route()
				//.bean(new PersistFHIRService(), "DeleteObservationById");
				//.to("direct:KafkaSendMessage");
				.to("bean:chatServiceLanguage?method=ListQuestionaries");*/
				
		rest("/Chatbot").description("Chatbot Questionnaire Service").securityDefinitions()
					.apiKey("api_key").withHeader("Authorization").end()
				.end()
                .consumes("application/json").produces("application/json")
				.post("/conversationEvents/{id}").id("conversationEvents").security("api_key")
                .description("Conversation events for user administration")
				//.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
				//.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
				.param().name("id").type(path).description("Set user ID").endParam()
				.param().name("body").type(body).description("Steps:"+
				"\n1. restart session for user: {\"event\": \"restart\"}" +
                "\n2. start session for user: {\"event\": \"session_started\"}").required(true)
				.endParam()
				.route()
				//.bean(new PersistFHIRService(), "DeleteObservationById");
				//.to("direct:KafkaSendMessage");
				//.to("bean:chatService?method=SendMessage");
				.bean(new ChatbotServiceEvents());
		
		rest("/Chatbot").description("Chatbot Questionnaire Service").securityDefinitions()
					.apiKey("api_key").withHeader("Authorization").end()
				.end()
                .consumes("application/json").produces("application/json")
                .post("/sendMessage").id("sendMessage").security("api_key") // API-KEY: um1234
                .description("Webhook endpoint for user, chat between bot and user")
				//.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
                //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
                .param().name("body").type(body).description("Steps:"+
                "\n1. restart questionnaire engine: {\"sender\": \"user-id\",\"message\": \"/restart\",\"language\": \"en\",\"chatbot\": \"gad7\"}"+
                "\n2. initiate the questionnaire: {\"sender\": \"user-id\",\"message\": \"gad7\",\"language\": \"en\",\"chatbot\": \"gad7\"}"+
				"\n3. start questionnaire: {\"sender\": \"user-id\",\"message\": \"gad7_start\",\"language\": \"en\",\"chatbot\": \"gad7\"}"+
                "\n4. answer questionnaire: {\"sender\": \"user-id\",\"message\": \"Not at all\",\"language\": \"en\",\"chatbot\": \"gad7\"}")
                .required(true)
				.endParam()
                .route()
				//.bean(new PersistFHIRService(), "DeleteObservationById");
				//.to("direct:KafkaSendMessage");
				//.to("bean:chatService?method=SendMessage");
				.bean(new ChatbotServiceWebhook());

        /*rest("/ChatGPT").description("ChatGPT Service").securityDefinitions()
                .apiKey("api_key").withHeader("Authorization").end()
            .end()
            .consumes("application/json").produces("application/json")
            .post("/sendMessage").id("sendMessage").security("api_key") // API-KEY: um1234
            .description("ChatGPT endpoint for user, chat between ChatGPT model and user")
            //.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
            //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
            .type(ChatGPT.class).param().name("body").type(body).required(true)
            .endParam()
            .route()
            //.bean(new PersistFHIRService(), "DeleteObservationById");
            //.to("direct:KafkaSendMessage");
            //.to("bean:chatService?method=SendMessage");
            .bean(new ChatGPTService());*/

        rest("/Chatbot").description("Chatbot Questionnaire Service").securityDefinitions()
                .apiKey("api_key").withHeader("Authorization").end()
            .end()
            .consumes("application/json").produces("application/json")
            .post("/conversationTrackerToFHIR").id("conversationTrackerToFHIR").security("api_key")
            .description("Endpoint to track user information for questionnaire")
            .param().name("body").type(body).description("Steps:"+
                "\n1. assign sender and questionnaire ID: {\"sender_id\": \"user-id\",\"questionary_id\": \"plevel_si_male\"}").required(true)
            .endParam()
            .route()
            //.bean(new PersistFHIRService(), "DeleteObservationById");
            //.to("direct:KafkaSendMessage");
            //.to("bean:chatService?method=SendMessage");
            .bean(new ChatbotServiceTrackerToFHIR());

        rest("/Chatbot").description("Chatbot Questionnaire Service").securityDefinitions()
					.apiKey("api_key").withHeader("Authorization").end()
				.end()
                .consumes("application/json").produces("application/json")
                .get("/conversationTracker/{id}/{questionnaire_id}").id("conversationTracker").security("api_key")
                .description("Endpoint to track user information for questionnaire")
				//.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
                //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
                .param().name("id").type(path).description("Set user ID").endParam()
                .param().name("questionnaire_id").type(path).description("Set chatbot questionnaire_id").endParam()
                //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
                .route()
				//.bean(new PersistFHIRService(), "DeleteObservationById");
				//.to("direct:KafkaSendMessage");
				//.to("bean:chatService?method=SendMessage");
				.bean(new ChatbotServiceTracker());

        rest("/Chatbot").description("Chatbot Questionnaire Service").securityDefinitions()
                .apiKey("api_key").withHeader("Authorization").end()
            .end()
            .consumes("application/json").produces("application/json")
            .get("/conversationPredict/{id}/{questionnaire_id}").id("conversationPredict").security("api_key")
            .description("Endpoint to predict user information for questionnaire")
            //.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
            //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
            .param().name("id").type(path).description("Set user ID").endParam()
            .param().name("questionnaire_id").type(path).description("Set chatbot questionnaire_id").endParam()
            //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
            .route()
            //.bean(new PersistFHIRService(), "DeleteObservationById");
            //.to("direct:KafkaSendMessage");
            //.to("bean:chatService?method=SendMessage");
            .bean(new ChatbotServicePredict());
				
		rest("/Chatbot").description("Chatbot Questionnaire Service").securityDefinitions()
					.apiKey("api_key").withHeader("Authorization").end()
				.end()
                .consumes("application/json").produces("application/json")
                .get("/conversationStory/{id}/{questionnaire_id}").id("conversationStory").security("api_key")
                .description("Endpoint to get user story for questionnaire")
				//.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
                //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
                .param().name("id").type(path).description("Set user ID").endParam()
                .param().name("questionnaire_id").type(path).description("Set chatbot questionnaire_id").endParam()
                //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
                .route()
				//.bean(new PersistFHIRService(), "DeleteObservationById");
				//.to("direct:KafkaSendMessage");
				//.to("bean:chatService?method=SendMessage");
				.bean(new ChatbotServiceStory());

        rest("/FHIR").description("FHIR Service").securityDefinitions()
                .apiKey("api_key").withHeader("Authorization").end()
            .end()
            .consumes("application/json").produces("application/json")
            .get("/allQuestionnaireResponse/{id}").id("allQuestionnaireResponse").security("api_key")
            .description("Endpoint to get all user answer history for questionnaire")
            //.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
            //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
            .param().name("id").type(path).description("Set user ID").endParam()
            //.param().name("questionnaire_id").type(path).description("Set chatbot questionnaire_id").endParam()
            //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
            .route()
            //.bean(new PersistFHIRService(), "DeleteObservationById");
            //.to("direct:KafkaSendMessage");
            //.to("bean:chatService?method=SendMessage");
            .bean(new FHIRGetQuestionnaireResponse());
            
        rest("/FHIR").description("FHIR Service").securityDefinitions()
            .apiKey("api_key").withHeader("Authorization").end()
            .end()
            .consumes("application/json").produces("application/json")
            .get("/questionnaireResponse/{id}").id("questionnaireResponse").security("api_key")
            .description("Endpoint to get specific user answer history for questionnaire")
            //.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
            //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
            .param().name("id").type(path).description("Set questionnaire ID").endParam()
            //.param().name("questionnaire_id").type(path).description("Set chatbot questionnaire_id").endParam()
            //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
            .route()
            //.bean(new PersistFHIRService(), "DeleteObservationById");
            //.to("direct:KafkaSendMessage");
            //.to("bean:chatService?method=SendMessage");
            .bean(new FHIRGetQuestionnaireResponse());

        rest("/FHIR").description("FHIR Service").securityDefinitions()
            .apiKey("api_key").withHeader("Authorization").end()
            .end()
            .consumes("application/json").produces("application/json")
            .get("/observation/{id}").id("observation").security("api_key")
            .description("Endpoint to get specific user CDSS results - returns the observation")
            //.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
            //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
            .param().name("id").type(path).description("Set observation ID").endParam()
            //.param().name("questionnaire_id").type(path).description("Set chatbot questionnaire_id").endParam()
            //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
            .route()
            //.bean(new PersistFHIRService(), "DeleteObservationById");
            //.to("direct:KafkaSendMessage");
            //.to("bean:chatService?method=SendMessage");
            .bean(new FHIRGetObservation());

        rest("/FHIR").description("FHIR Service").securityDefinitions()
            .apiKey("api_key").withHeader("Authorization").end()
            .end()
            .consumes("application/json").produces("application/json")
            .get("/observations/{patient_id}").id("observations").security("api_key")
            .description("Endpoint to returns all observations for specificpatient")
            //.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
            //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
            .param().name("patient_id").type(path).description("Set Patient ID").endParam()
            //.param().name("questionnaire_id").type(path).description("Set chatbot questionnaire_id").endParam()
            //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
            .route()
            //.bean(new PersistFHIRService(), "DeleteObservationById");
            //.to("direct:KafkaSendMessage");
            //.to("bean:chatService?method=SendMessage");
            .bean(new FHIRGetObservationByPatientID());

            rest("/FHIR").description("FHIR Service").securityDefinitions()
            .apiKey("api_key").withHeader("Authorization").end()
            .end()
            .consumes("application/json").produces("application/json")
    
            // New endpoint for getting Composition
            .get("/compositions/{patient_id}").id("compositions").security("api_key")
            .description("Endpoint to return all compositions for a specific patient")
            .param().name("patient_id").type(path).description("Set Patient ID").endParam()
            .param().name("section").type(query).description("Set section (optional)").required(false).endParam()
            .param().name("entry").type(query).description("Set entry (optional)").required(false).endParam()
            .route()
            .bean(new FHIRGetComposition(), "GetResult");
        
        
        rest("/FHIR").description("FHIR Service").securityDefinitions()
                .apiKey("api_key").withHeader("Authorization").end()
            .end()
            .consumes("application/json").produces("application/json")
            .post("/updateTask").id("updateTask").security("api_key")
            .description("Endpoint to update the Task resource")
            //.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
            //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
            //.param().name("id").type(path).description("Set user ID").endParam()
            //.param().name("questionnaire_id").type(path).description("Set chatbot questionnaire_id").endParam()
            //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
            .type(UpdateTask.class).param().name("body").type(body).required(true)
            .endParam()
            .route()
            //.bean(new PersistFHIRService(), "DeleteObservationById");
            //.to("direct:KafkaSendMessage");
            //.to("bean:chatService?method=SendMessage");
            .bean(new FHIRUpdateTask());

            rest("/FHIR").description("FHIR Service").securityDefinitions()
            .apiKey("api_key").withHeader("Authorization").end()
            .end()
            .consumes("application/json").produces("application/json")
            .post("/storeCDSSResults").id("storeCDSSResults").security("api_key")
            .description("Endpoint to store the CDSS results")
            //.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
            //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
            //.param().name("id").type(path).description("Set user ID").endParam()
            //.param().name("questionnaire_id").type(path).description("Set chatbot questionnaire_id").endParam()
            //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
            .type(StoreCDSSResults.class).param().name("body").type(body).required(true)
                .endParam()
            .route()
            //.bean(new PersistFHIRService(), "DeleteObservationById");
            //.to("direct:KafkaSendMessage");
            //.to("bean:chatService?method=SendMessage");
            .bean(new FHIRStoreCDSSResults());

        rest("/FHIR").description("FHIR Service").securityDefinitions()
        .apiKey("api_key").withHeader("Authorization").end()
        .end()
        .consumes("application/json").produces("application/json")
        .post("/storeQuestionnaireResponse").id("storeQuestionnaireResponse").security("api_key")
        .description("Endpoint to store or update the QuesitonnaireResponse resource")
        //.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
        //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
        //.param().name("id").type(path).description("Set user ID").endParam()
        //.param().name("questionnaire_id").type(path).description("Set chatbot questionnaire_id").endParam()
        //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
        //.type(StoreQuestionnaireResponse.class)
        .param().name("body").type(body).required(true)
            .endParam()
        .route()
        //.bean(new PersistFHIRService(), "DeleteObservationById");
        //.to("direct:KafkaSendMessage");
        //.to("bean:chatService?method=SendMessage");
        .bean(new FHIRStoreQuestionnaireResponse());

        rest("/FHIR").description("FHIR Service").securityDefinitions()
        .apiKey("api_key").withHeader("Authorization").end()
        .end()
        .consumes("application/json").produces("application/json")
        .post("/storePlanDefinition").id("storePlanDefinition").security("api_key")
        .description("Endpoint to store or update the PlanDefinition resource")
        //.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
        //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
        //.param().name("id").type(path).description("Set user ID").endParam()
        //.param().name("questionnaire_id").type(path).description("Set chatbot questionnaire_id").endParam()
        //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
        //.type(StoreQuestionnaireResponse.class)
        .param().name("body").type(body).required(true)
            .endParam()
        .route()
        //.bean(new PersistFHIRService(), "DeleteObservationById");
        //.to("direct:KafkaSendMessage");
        //.to("bean:chatService?method=SendMessage");
        .bean(new FHIRStorePlanDefinition());

        rest("/FHIR").description("FHIR Service").securityDefinitions()
        .apiKey("api_key").withHeader("Authorization").end()
        .end()
        .consumes("application/json").produces("application/json")
        .post("/storePractitioner").id("storePractitioner").security("api_key")
        .description("Endpoint to store or update the Practitioner resource")
        //.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
        //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
        //.param().name("id").type(path).description("Set user ID").endParam()
        //.param().name("questionnaire_id").type(path).description("Set chatbot questionnaire_id").endParam()
        //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
        //.type(StoreQuestionnaireResponse.class)
        .param().name("body").type(body).required(true)
            .endParam()
        .route()
        //.bean(new PersistFHIRService(), "DeleteObservationById");
        //.to("direct:KafkaSendMessage");
        //.to("bean:chatService?method=SendMessage");
        .bean(new FHIRStorePractitioner());

        rest("/FHIR").description("FHIR Service").securityDefinitions()
        .apiKey("api_key").withHeader("Authorization").end()
        .end()
        .consumes("application/json").produces("application/json")
        .post("/storePatient").id("storePatient").security("api_key")
        .description("Endpoint to store or update the Patient resource")
        //.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
        //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
        //.param().name("id").type(path).description("Set user ID").endParam()
        //.param().name("questionnaire_id").type(path).description("Set chatbot questionnaire_id").endParam()
        //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
        .type(StorePatient.class).param().name("body").type(body).required(true)
            .endParam()
        .route()
        //.bean(new PersistFHIRService(), "DeleteObservationById");
        //.to("direct:KafkaSendMessage");
        //.to("bean:chatService?method=SendMessage");
        .bean(new FHIRStorePatient());

        rest("/FHIR").description("FHIR Service").securityDefinitions()
        .apiKey("api_key").withHeader("Authorization").end()
        .end()
        .consumes("application/json").produces("application/json")
        .post("/storeCarePlan").id("storeCarePlan").security("api_key")
        .description("Endpoint to store or update the CarePlan resource")
        //.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
        //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
        //.param().name("id").type(path).description("Set user ID").endParam()
        //.param().name("questionnaire_id").type(path).description("Set chatbot questionnaire_id").endParam()
        //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
        .type(StoreCarePlan.class).param().name("body").type(body).required(true)
            .endParam()
        .route()
        //.bean(new PersistFHIRService(), "DeleteObservationById");
        //.to("direct:KafkaSendMessage");
        //.to("bean:chatService?method=SendMessage");
        .bean(new FHIRStoreCarePlan());

        rest("/FHIR").description("FHIR Service").securityDefinitions()
        .apiKey("api_key").withHeader("Authorization").end()
        .end()
        .consumes("application/json").produces("application/json")
        .post("/storeTask").id("storeTask").security("api_key")
        .description("Endpoint to store or update the Task resource")
        //.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
        //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
        //.param().name("id").type(path).description("Set user ID").endParam()
        //.param().name("questionnaire_id").type(path).description("Set chatbot questionnaire_id").endParam()
        //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
        .type(StoreTask.class).param().name("body").type(body).required(true)
            .endParam()
        .route()
        //.bean(new PersistFHIRService(), "DeleteObservationById");
        //.to("direct:KafkaSendMessage");
        //.to("bean:chatService?method=SendMessage");
        .bean(new FHIRStoreTask());

        rest("/FHIR").description("FHIR Service").securityDefinitions()
        .apiKey("api_key").withHeader("Authorization").end()
        .end()
        .consumes("application/json").produces("application/json")
        .post("/storeActivityDefinition").id("storeActivityDefinition").security("api_key")
        .description("Endpoint to store or update the ActivityDefinition resource")
        //.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
        //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
        //.param().name("id").type(path).description("Set user ID").endParam()
        //.param().name("questionnaire_id").type(path).description("Set chatbot questionnaire_id").endParam()
        //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
        .type(StoreActivityDefinition.class).param().name("body").type(body).required(true)
            .endParam()
        .route()
        //.bean(new PersistFHIRService(), "DeleteObservationById");
        //.to("direct:KafkaSendMessage");
        //.to("bean:chatService?method=SendMessage");
        .bean(new FHIRStoreActivityDefinition());

        rest("/FHIR").description("FHIR Service").securityDefinitions()
        .apiKey("api_key").withHeader("Authorization").end()
        .end()
        .consumes("application/json").produces("application/json")
        .post("/storeDocumentReference").id("storeDocumentReference").security("api_key")
        .description("Endpoint to store or update the DocumentReference resource")
        //.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
        //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
        .type(StoreDocumentReference.class).param().name("body").type(body).required(true)
            .endParam()
        .route()
        //.bean(new PersistFHIRService(), "DeleteObservationById");
        //.to("direct:KafkaSendMessage");
        //.to("bean:chatService?method=SendMessage");
        .bean(new FHIRStoreDocumentReference());


        rest("/Diary").description("SMILE Video Diary Service")
            .consumes("multipart/form-data").produces("application/json")
            .securityDefinitions()
                .apiKey("api_key").withHeader("Authorization").end()
            .end()
            .post("/video/{language_code}/{user_id}").id("diary").security("api_key")
            .description("Function to send video to MRAST core endpoint").bindingMode(RestBindingMode.off)
            .param().name("language_code").type(RestParamType.path).required(true).description("Language code - available: en, sl, it, de, pl, gr, es").endParam()
            .param().name("user_id").type(RestParamType.path).required(true).description("User id - e.g. user-id").endParam()
            .param().name("file").type(RestParamType.formData).dataType("file")
            .required(true).description("Video file to upload in .wmv format").endParam()
            //.to("direct:upload");
            .route().bean(new DiaryService(), "Diary");

        rest("/Diary").description("SMILE Video Diary Service")
            .consumes("multipart/form-data").produces("application/json")
            .securityDefinitions()
                .apiKey("api_key").withHeader("Authorization").end()
            .end()
            .post("/upload_video").id("upload_video").security("api_key")
            .description("Function to store ddiary video to server").bindingMode(RestBindingMode.off)
            .param().name("file").type(RestParamType.formData).dataType("file")
            .required(true).description("Video file to upload in .mov format").endParam()
            //.to("direct:upload");
            .route().bean(new UploadSMILEVideo(), "UploadSMILEVideo");

        rest("/Diary").description("SMILE Video Diary Service")
            .consumes("application/json").produces("application/json")
            .securityDefinitions()
                .apiKey("api_key").withHeader("Authorization").end()
            .end()
            .post("/mrast").id("mrast_diary").security("api_key")
            .description("Function to send video to MRAST core endpoint")
            .type(DiaryMRAST.class).param().name("body").type(body).required(true)
                .endParam()
            .route().bean(new DiaryServiceV2(), "Diary");

        rest("/Diary").description("SMILE Video Diary Service V2D")
            .consumes("application/json").produces("application/json")
            .securityDefinitions()
                .apiKey("api_key").withHeader("Authorization").end()
            .end()
            .post("/mrast-v2d").id("mrast_diary_v2d").security("api_key")
            .description("Function to send video to MRAST core endpoint")
            .type(DiaryMRAST.class).param().name("body").type(body).required(true)
                .endParam()
            .route().bean(new DiaryServiceV2D(), "Diary");
				
		/*rest("/Chatbot").description("Chatbot Questionnaire Service").securityDefinitions()
					.apiKey("api_key").withHeader("Authorization").end()
				.end()
                .consumes("application/json").produces("application/json")
                .get("/domain").id("domain").security("api_key")
                .description("Endpoint to show whole domain of Chatbot")
				//.param().name("sender").type(query).description("Assign Sender UserName").required(true).endParam()
                //.param().name("content").type(query).description("Assign Message Content").required(true).endParam()
                //.example(String.valueOf(simple("{\"sender\": \"John\",\"content\": \"Test content\"}")))
                .route()
				//.bean(new PersistFHIRService(), "DeleteObservationById");
				//.to("direct:KafkaSendMessage");
				//.to("bean:chatService?method=SendMessage");
				.bean(new ChatbotServiceDomain());*/

        /*rest("/TTS").description("Swagger TTS functions").consumes("application/json").produces("application/json")
				.securityDefinitions()
					.apiKey("api_key").withHeader("Authorization").end()
				.end()
                .post("/tts").id("tts").security("api_key")
                .description("Function to send the data for TTS")
                .type(SynthAudio.class).param().name("body").type(body)
				//.description(readJson.getJson("json_example.txt"))
				.required(true).endParam()
                .route().bean(new SynthAudioPersistService(), "SynthAudioPersist");

        rest("/ASR").description("Swagger ASR functions")
                .consumes("multipart/form-data").produces("application/json")
                .securityDefinitions()
                    .apiKey("api_key").withHeader("Authorization").end()
                .end()
                .post("/asr/{language_code}").id("asr").security("api_key")
                .description("Function to send wav audio file to ASR engine").bindingMode(RestBindingMode.off)
                .param().name("language_code").type(RestParamType.path).required(true).description("Language code - available: en, sl, fr, es, lv, ru").endParam()
                .param().name("file").type(RestParamType.formData).dataType("file")
                .required(true).description("File to upload in .wav format").endParam()
                //.to("direct:upload");
                .route().bean(new ASRService(), "ASR");

        rest("/Gestures").description("Swagger gestures functions")
                .consumes("multipart/form-data").produces("application/json")
				.securityDefinitions()
					.apiKey("api_key").withHeader("Authorization").end()
				.end()
                .post("/gestures/{gesture_model}").id("gestures").security("api_key")
                .description("Function to send audio and receive csv string for gestures /sgg - Karlo").bindingMode(RestBindingMode.off)
                .param().name("gesture_model").type(RestParamType.path).required(true).description("Select gesture model - available: gestures, gesturesv2, evagestures, evagesturesv2, csvevagestures, csvevagesturesv2, bvhevagestures, bvhevagesturesv2").endParam()
                .param().name("file").type(RestParamType.formData).dataType("file")
				//.description(readJson.getJson("json_example.txt"))
				.required(true).endParam()
                .route().bean(new GesturesService(), "Gestures");

        from("direct:PersistLogin").process(PersistLogin);

        from("direct:update-user").choice().when(method(UserDataService.class, "CheckPayload"))
                .to("bean:userService?method=updateUser").setHeader(Exchange.HTTP_RESPONSE_CODE, constant(204))
                .setBody(constant(""));

        from("rest:get:ovh-auth?protocol=https").process(ObtainToken);

        from("rest:post:persist-login?protocol=https").unmarshal().json(JsonLibrary.Gson, Map.class).process(RestLogin);

        from("rest:post:persist-mongodb?protocol=https").to("direct:insertMongoDB");

        from("direct:insertMongoDB").to("mongodb:mongo?database=persist&collection=persistCollection&operation=insert");

        from("rest:get:user/{payload}?protocol=https") // intercept get
                .log("With REST GET retrieved: ${header.payload}.");*/

    }

}
