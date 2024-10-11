package um.persist.chatbot;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringEscapeUtils;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemOptionComponent;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

import java.util.logging.Logger;


/**
 * @author UM FERI
 * @date NOV 2020
 * @description Class that implements all the FHIR related operations
 */

public class PersistFHIRService {

	Logger logger = Logger.getLogger(String.valueOf(this));

    private IParser parser;

    public String OHC_endpoint = "https://dev.domain.com/fhir/test/";
    private String accesstoken;

    public PersistFHIRService() {
        FhirContext ctx = FhirContext.forDstu3();
        this.parser = ctx.newJsonParser();
    }

    public void DeleteObservationById(Exchange exchange) {
        Message camelMessage = exchange.getIn();
        Map<String, Object> inpayload = (Map<String, Object>) camelMessage.getBody();
        List<String> errors = new LinkedList<String>();

        //System.out.println("report_date:" + (String) inpayload.get("report_date"));
        if (!inpayload.containsKey("token")) {
            errors.add("token is required parameter");
        }
        if (!inpayload.containsKey("observation_id")) {
            errors.add("observation_id is required parameter");
        }

        String accesstoken = (String) inpayload.get("token");
        String observation_id = (String) inpayload.get("observation_id");

        boolean sucess = this.ResourceDeleteReuest(observation_id, accesstoken, "Observation");
        if (sucess) {
            Map<String, String> s = new LinkedHashMap<String, String>();
            s.put("message", "Observation with id {" + observation_id + "} deleted");

            exchange.getIn().setBody(s);
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
            return;
        } else {
            Map<String, String> s = new LinkedHashMap<String, String>();
            s.put("message", "Failed to delete Observation with id {" + observation_id + "}");

            exchange.getIn().setBody(s);
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
        }

    }

    public void SetRestResponse(boolean sucess, Map<String, Object> Message, Exchange exchange) {
        exchange.getIn().setBody(Message);
        exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");

        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, (sucess) ? 200 : 400);
    }

    public void CreateAndSaveDiganosticReport(Exchange exchange) {
        Message camelMessage = exchange.getIn();
        Map<String, Object> inpayload = (Map<String, Object>) camelMessage.getBody();
        List<String> errors = new LinkedList<String>();
        String accesstoken = "";
        //System.out.println("report_date:" + (String) inpayload.get("report_date"));
        if (!inpayload.containsKey("token")) {
            errors.add("token is required parameter");
        }
        if (!inpayload.containsKey("report_date_from")) {
            errors.add("report_date_from is required parameter");
        }
        if (!inpayload.containsKey("report_date_to")) {
            errors.add("report_date_to is required parameter");
        }
        if (!inpayload.containsKey("patient_oid")) {
            errors.add("patient_oid is required parameter");
        }


        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            exchange.getIn().setBody(r);
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
            return;
        }

        Reference subject = new Reference();
        subject.setReference(((String) inpayload.get("patient_oid")));
        subject.setDisplay("Patient");


        accesstoken = (String) inpayload.get("token");

        List<Reference> Observations = new LinkedList<Reference>();
        //Observations.add(new Reference().setReference("abcdefff"));

        if (inpayload.containsKey("steps")) {
            for (Map<String, Object> pair : (List<Map<String, Object>>) inpayload.get("steps")) {
                Observation obs = new Observation();
                CodeableConcept category = new CodeableConcept();
                Coding categorycode = new Coding();
                categorycode.setCode("fitness");
                categorycode.setSystem("http://phr.kanta.fi/fiphr-cs-fitnesscategory ");//TODO Extend http://hl7.org/fhir/observation-category with fitness
                categorycode.setDisplay("Fitness");
                category.addCoding(categorycode);
                obs.addCategory(category);

                CodeableConcept code = new CodeableConcept();
                Coding codecoding = new Coding();
                codecoding.setCode("66334-4");
                codecoding.setSystem("http://loinc.org");
                codecoding.setDisplay("Steps");
                obs.setCode(code);


                obs.setStatus(Observation.ObservationStatus.FINAL);
                obs.setSubject(subject);

                try {
                    // SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd h:m:ss.SSS");
                    // Date EffDate = formatter.parse((String)pair.get("datetime"));
                    DateTimeType effectie = DateTimeType.parseV3((String) pair.get("datetime"));
                    obs.setEffective(effectie);
                } catch (Exception e) {
                    System.out.println("Error with steps.datetime:" + (String) pair.get("datetime") + ":" + e.getMessage());
                    errors.add("datetime in steps wrong format {" + (String) pair.get("datetime") + "} , expected yyyy-mm-dd h:m:ss.SSS");
                    continue;
                }


                try {
                    Quantity val = new Quantity();
                    val.setUnit("steps");
                    val.setValue((double) pair.get("value"));
                    obs.setValue(val);
                } catch (Exception e) {
                    System.out.println("Error with steps.value:" + pair.get("value") + ":" + e.getMessage());
                    errors.add("Value in steps wrong format {" + pair.get("value") + "} , expected double");
                    continue;
                }

                //System.out.println("Fitness resource steps: ");

                String serialized = this.parser.encodeResourceToString(obs);
                //System.out.println(serialized);

                String reference = this.ResourceAddReuest(serialized, accesstoken, "Observation", serialized);
                //System.out.println("Reference received:"+ reference);
                Observations.add(new Reference().setReference(reference).setDisplay("Observation"));
            }

        }

        if (inpayload.containsKey("calories")) {
            for (Map<String, Object> pair : (List<Map<String, Object>>) inpayload.get("steps")) {
                Observation obs = new Observation();
                CodeableConcept category = new CodeableConcept();
                Coding categorycode = new Coding();
                categorycode.setCode("fitness");
                categorycode.setSystem("http://hl7.org/fhir/observation-category"); //TODO Extend http://hl7.org/fhir/observation-category with fitness
                categorycode.setDisplay("Fitness");
                category.addCoding(categorycode);
                obs.addCategory(category);

                CodeableConcept code = new CodeableConcept();
                Coding codecoding = new Coding();
                codecoding.setCode("55421-2");
                codecoding.setSystem("http://loinc.org");
                codecoding.setDisplay("Calories burned");
                obs.setCode(code);


                obs.setStatus(Observation.ObservationStatus.FINAL);
                obs.setSubject(subject);

                try {
                    // SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd h:m:ss.SSS");
                    // Date EffDate = formatter.parse((String)pair.get("datetime"));
                    DateTimeType effectie = DateTimeType.parseV3((String) pair.get("datetime"));
                    obs.setEffective(effectie);
                } catch (Exception e) {
                    System.out.println("Error with calories.datetime:" + (String) pair.get("datetime") + ":" + e.getMessage());
                    errors.add("datetime in calories.datetime wrong format {" + (String) pair.get("datetime") + "} , expected yyyy-mm-dd h:m:ss.SSS");
                    continue;
                }


                try {
                    Quantity val = new Quantity();
                    val.setUnit("kCal");
                    val.setValue((double) pair.get("value"));
                    obs.setValue(val);
                } catch (Exception e) {
                    System.out.println("Error with calories.value:" + pair.get("value") + ":" + e.getMessage());
                    errors.add("value in calories wrong format {" + pair.get("value") + "} , expected double");
                    continue;
                }

                //System.out.println("Fitness resource calories: ");

                String serialized = this.parser.encodeResourceToString(obs);
                //System.out.println(serialized);
                String reference = this.ResourceAddReuest(serialized, accesstoken, "Observation", serialized);
                //System.out.println("Reference received:"+ reference);
                Observations.add(new Reference().setReference(reference).setDisplay("Observation"));


                //System.out.println("datetime:" + (String) pair.get("datetime"));
                //System.out.println("value:" + (int) pair.get("value"));
            }
        }

        if (inpayload.containsKey("heartrate")) {

            for (Map<String, Object> pair : (List<Map<String, Object>>) inpayload.get("heartrate")) {
                Observation obs = new Observation();
                CodeableConcept category = new CodeableConcept();
                Coding categorycode = new Coding();
                categorycode.setCode("vital-signs");
                categorycode.setSystem("http://hl7.org/fhir/observation-category");
                categorycode.setDisplay("Vital Signs");
                category.addCoding(categorycode);
                obs.addCategory(category);

                CodeableConcept code = new CodeableConcept();
                Coding codecoding = new Coding();
                codecoding.setCode("8867-4");
                codecoding.setSystem("http://loinc.org");
                codecoding.setDisplay("Heart rate");
                obs.setCode(code);


                obs.setStatus(Observation.ObservationStatus.FINAL);
                obs.setSubject(subject);

                try {
                    // SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd h:m:ss.SSS");
                    // Date EffDate = formatter.parse((String)pair.get("datetime"));
                    DateTimeType effectie = DateTimeType.parseV3((String) pair.get("datetime"));
                    obs.setEffective(effectie);
                } catch (Exception e) {
                    System.out.println("Error with heartrate.datetime:" + (String) pair.get("datetime") + ":" + e.getMessage());
                    errors.add("datetime in heartrate wrong format {" + (String) pair.get("datetime") + "} , expected yyyy-mm-dd h:m:ss.SSS");
                    continue;
                }


                try {
                    Quantity val = new Quantity();
                    val.setUnit("beats/min");
                    val.setValue((double) pair.get("value"));
                    obs.setValue(val);
                } catch (Exception e) {
                    System.out.println("Error with heartrate.value:" + pair.get("value") + ":" + e.getMessage());
                    errors.add("Heartrate.value wrong format {" + pair.get("value") + "} , expected double");
                    continue;
                }

                String serialized = this.parser.encodeResourceToString(obs);
                //System.out.println(serialized);
                String reference = this.ResourceAddReuest(serialized, accesstoken, "Observation", serialized);
                //System.out.println("Reference received:"+ reference);
                Observations.add(new Reference().setReference(reference).setDisplay("Observation"));
                //System.out.println(serialized);
            }

            if (inpayload.containsKey("bloodpressure")) {
                for (Map<String, Object> pair : (List<Map<String, Object>>) inpayload.get("bloodpressure")) {
                    Observation obs = new Observation();
                    CodeableConcept category = new CodeableConcept();
                    Coding categorycode = new Coding();
                    categorycode.setCode("vital-signs");
                    categorycode.setSystem("http://hl7.org/fhir/observation-category");
                    categorycode.setDisplay("Vital Signs");
                    category.addCoding(categorycode);
                    obs.addCategory(category);

                    CodeableConcept code = new CodeableConcept();
                    Coding codecoding = new Coding();
                    codecoding.setCode("8462-4"); //or 8480-6 	Systolic blood pressure
                    codecoding.setSystem("http://loinc.org");
                    codecoding.setDisplay("Diastolic blood pressure");
                    obs.setCode(code);


                    obs.setStatus(Observation.ObservationStatus.FINAL);
                    obs.setSubject(subject);

                    try {
                        // SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd h:m:ss.SSS");
                        // Date EffDate = formatter.parse((String)pair.get("datetime"));
                        DateTimeType effectie = DateTimeType.parseV3((String) pair.get("datetime"));
                        obs.setEffective(effectie);
                    } catch (Exception e) {
                        System.out.println("Error with bloodpressure.datetime:" + (String) pair.get("datetime") + ":" + e.getMessage());
                        errors.add("value in bloodpressure wrong format {" + (String) pair.get("datetime") + "} , expected yyyy-mm-dd h:m:ss.SSS");
                        continue;
                    }


                    try {
                        Quantity val = new Quantity();
                        val.setUnit("mm[Hg]");
                        val.setValue((double) pair.get("value"));
                        obs.setValue(val);
                    } catch (Exception e) {
                        System.out.println("Error with bloodpressure.value:" + pair.get("value") + ":" + e.getMessage());
                        errors.add("bloodpressure.value wrong format {" + pair.get("value") + "} , expected double");
                        continue;
                    }

                    //System.out.println("vital signs resource bloodpressure: ");

                    String serialized = this.parser.encodeResourceToString(obs);
                    //System.out.println(serialized);
                    String reference = this.ResourceAddReuest(serialized, accesstoken, "Observation", serialized);
                    //System.out.println("Reference received:"+ reference);
                    Observations.add(new Reference().setReference(reference).setDisplay("Observation"));
                }
            }

            if (inpayload.containsKey("oxygensaturation")) {
                for (Map<String, Object> pair : (List<Map<String, Object>>) inpayload.get("oxygensaturation")) {
                    Observation obs = new Observation();
                    CodeableConcept category = new CodeableConcept();
                    Coding categorycode = new Coding();
                    categorycode.setCode("vital-signs");
                    categorycode.setSystem("http://hl7.org/fhir/observation-category");
                    categorycode.setDisplay("Vital Signs");
                    category.addCoding(categorycode);
                    obs.addCategory(category);

                    CodeableConcept code = new CodeableConcept();
                    Coding codecoding = new Coding();
                    codecoding.setCode("59408-5"); //or 8480-6 	Systolic blood pressure
                    codecoding.setSystem("http://loinc.org");
                    codecoding.setDisplay("Oxygen saturation");
                    obs.setCode(code);


                    obs.setStatus(Observation.ObservationStatus.FINAL);
                    obs.setSubject(subject);

                    try {
                        // SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd h:m:ss.SSS");
                        // Date EffDate = formatter.parse((String)pair.get("datetime"));
                        DateTimeType effectie = DateTimeType.parseV3((String) pair.get("datetime"));
                        obs.setEffective(effectie);
                    } catch (Exception e) {
                        System.out.println("Error with oxygensaturation.datetime:" + (String) pair.get("datetime") + ":" + e.getMessage());
                        errors.add("value in oxygensaturation wrong format {" + (String) pair.get("datetime") + "} , expected yyyy-mm-dd h:m:ss.SSS");
                        continue;
                    }


                    try {
                        Quantity val = new Quantity();
                        val.setUnit("%");
                        val.setValue((double) pair.get("value"));
                        obs.setValue(val);
                    } catch (Exception e) {
                        System.out.println("Error with oxygensaturation.value:" + pair.get("value") + ":" + e.getMessage());
                        errors.add("oxygensaturation.value wrong format {" + pair.get("value") + "} , expected double");
                        continue;
                    }

                    //System.out.println("vital signs resource bloodpressure: ");

                    String serialized = this.parser.encodeResourceToString(obs);
                    //System.out.println(serialized);
                    String reference = this.ResourceAddReuest(serialized, accesstoken, "Observation", serialized);
                    //System.out.println("Reference received:"+ reference);
                    Observations.add(new Reference().setReference(reference).setDisplay("Observation"));
                }
            }

        }


        if (Observations.size() > 0) {
            System.out.println("------------CREATE DIAGNOSTIC REPORT-------------------------");
            DiagnosticReport fhir_report = new DiagnosticReport();
            fhir_report.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);

            CodeableConcept category = new CodeableConcept();
            Coding categorycode = new Coding();
            categorycode.setCode("OSL");
            categorycode.setSystem("http://hl7.org/fhir/ValueSet/diagnostic-service-sections");
            categorycode.setDisplay("Outside Lab");
            category.addCoding(categorycode);

            categorycode = new Coding(); //TODO: can we extedned to include data collected by mobile app from the fitness device as new coding
            categorycode.setCode("OTH");
            categorycode.setSystem("http://hl7.org/fhir/ValueSet/diagnostic-service-sections");
            categorycode.setDisplay("Other");
            category.addCoding(categorycode);
            fhir_report.setCategory(category);

            CodeableConcept code = new CodeableConcept();
            Coding codecoding = new Coding(); //TODDO
            codecoding.setCode("85353-1"); //or 8480-6 	Systolic blood pressure
            codecoding.setSystem("http://loinc.org"); //TODO: define a code for this report
            codecoding.setDisplay("Vital signs, oxygen saturation & fitness data");
            fhir_report.setCode(code);
            fhir_report.setSubject(subject);

            Period reportperiod = new Period();


            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd h:m:ss.SSS");
                Date startd = formatter.parse((String) inpayload.get("report_date_from"));
                //DateTimeType startd = DateTimeType.parseV3((String)inpayload.get("report_date_from"));
                reportperiod.setStart(startd);
            } catch (Exception e) {
                System.out.println("Error with inpayload.report_date_from:" + (String) inpayload.get("report_date_from") + ":" + e.getMessage());
                errors.add("report_date_from in inpayload wrong format {" + (String) inpayload.get("report_date_from") + "} , expected yyyy-mm-dd h:m:ss.SSS");
                return;
            }

            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd h:m:ss.SSS");
                Date endd = formatter.parse((String) inpayload.get("report_date_to"));
                //DateTimeType endd = DateTimeType.parseV3((String)inpayload.get("report_date_to"));
                reportperiod.setEnd(endd);
            } catch (Exception e) {
                System.out.println("Error with inpayload.report_date_to:" + (String) inpayload.get("report_date_to") + ":" + e.getMessage());
                errors.add("report_date_to in inpayload wrong format {" + (String) inpayload.get("report_date_to") + "} , expected yyyy-mm-dd h:m:ss.SSS");
                return;
            }

            fhir_report.setEffective(reportperiod);
            fhir_report.setResult(Observations);

            String serialized = this.parser.encodeResourceToString(fhir_report);
            System.out.println(serialized);

            System.out.println("------------ADD DIAGNOSTIC REPORT TO OHC-------------------------");
            String reference = this.ResourceAddReuest(serialized, accesstoken, "DiagnosticReport", serialized);
            System.out.println("Reference received:" + reference);
            // Observations.add(new Reference().setReference(reference));


            if (errors.size() > 0) {

                Map<String, Object> r = new LinkedHashMap<String, Object>();
                r.put("error", String.join(";", errors));
                this.SetRestResponse(false, r, exchange);
            } else {
                Map<String, Object> s = new LinkedHashMap<String, Object>();
                s.put("message", "Sucess! Added Diagnostic Report with id {" + reference + "}");
                this.SetRestResponse(true, s, exchange);
            }
        }


    }

    private String ResourceAddReuest(String resource, String token, String resurcetype, String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<?> entity = new HttpEntity<>(resource, headers);

        RestTemplate restTemplate = new RestTemplate();
        String authUri = this.OHC_endpoint + resurcetype + "/" + id; // uri to service which you get the token from
        //System.out.println("Sending to OHC: " + authUri);
        ResponseEntity<?> response2 =
                restTemplate.exchange(authUri, HttpMethod.PUT, entity,
                        Object.class); //TODO: SWAGER does not show us what is the structure of response

        //System.out.println("Create Resource: " + response2);
        //String id = response2.getBody().getId();
        //return id;

        String response_body = response2.getStatusCode().toString();
		headers.clear();
        entity = null;
        response2 = null;

        return response_body;
    }

    private boolean ResourceDeleteReuest(String resourceid, String token, String resurcetype) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<?> entity = new HttpEntity<>("", headers);
        RestTemplate restTemplate = new RestTemplate();
        String authUri = this.OHC_endpoint + resurcetype + "/" + resourceid;// uri to service which you get the token from

        try {
            ResponseEntity<String> response2 =
                    restTemplate.exchange(authUri, HttpMethod.DELETE, entity,
                            String.class);

            return true;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.out.println("{ResourceDeleteReuest} " + e.getMessage());
            return false;
        }

    }

    public String PERSISTOHCLogin() throws Exception {
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post("https://dev.domain.com/auth/realms/project/protocol/openid-connect/token")
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

    public void CreateAndSavePERSISTQuestionary(Exchange exchange) {
        Message camelMessage = exchange.getIn();
        Map<String, Object> inpayload = (Map<String, Object>) camelMessage.getBody();
        List<String> errors = new LinkedList<String>();
        String accesstoken = "";
        try {
            accesstoken = PERSISTOHCLogin();
        } catch (Exception e) {
            errors.add("could not retreive token {" + e.getMessage() + "}");
        }
        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
            return;
        }

        if (!inpayload.containsKey("url")) {
            errors.add("url is required parameter");
        }
        if (!inpayload.containsKey("identifier")) {
            errors.add("identifier is required parameter");
        }
        if (!inpayload.containsKey("name")) {
            errors.add("name is required parameter");
        }
        if (!inpayload.containsKey("title")) {
            errors.add("title is required parameter");
        }
        if (!inpayload.containsKey("publisher")) {
            errors.add("publisher is required parameter");
        }
        if (!inpayload.containsKey("description")) {
            errors.add("description is required parameter");
        }
        if (!inpayload.containsKey("purpose")) {
            errors.add("purpose is required parameter");
        }
        if (!inpayload.containsKey("code")) {
            errors.add("code is required parameter");
        }
        if (!inpayload.containsKey("item")) {
            errors.add("item is required parameter");
        }
        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
            return;
        }


        Questionnaire Question = new Questionnaire();
        Question.setStatus(Enumerations.PublicationStatus.ACTIVE);

        //TODO: Create the object from JSON

        String url; //Questionary/vprašalnik_o_zdravju_pacientov_2_si
        String identifier; //vprašalnik_o_zdravju_pacientov_2_si
        String name; //PHQ2-SI
        String title; //Vprašalnik o zdravju pacientov, PHQ-2
        String publisher; // UKCM
        String description; // "The PHQ-2 inquires about the frequency of depressed mood and anhedonia over the past two weeks. The PHQ-2 includes the first two items of the PHQ-9.The purpose of the PHQ-2 is to screen for depression in a “first-step” approach. Patients who screen positive should be further evaluated with the PHQ-9 to determine whether they meet criteria for a depressive disorder."
        String purpose; //Assesing Mental Helath
        //LinkedList<FHIRQuestion> item = new LinkedList<FHIRQuestion>(); //lis tof questions
        //Coding codec; // code: 10190-7, dispaly: Mental status Narrative, system: http://www.hl7.org/Special/committees/patientcare
        //Coding status ;  //should be always active
        //Coding subectType;



        url = (String) inpayload.get("url");
        identifier = (String) inpayload.get("identifier");
        name = (String) inpayload.get("name");
        title = (String) inpayload.get("title");
        publisher = (String) inpayload.get("publisher");
        description = (String) inpayload.get("description");
        purpose = (String) inpayload.get("purpose");

        JSONObject json = new JSONObject(inpayload);
        JSONObject codes = json.getJSONObject("code");
        String code = codes.getString("code");
        System.out.println(code);
        String display = codes.getString("display");
        System.out.println(display);
        String system = codes.getString("system");
        System.out.println(system);
        Coding codecoding = new Coding();
        codecoding.setCode(code);
        codecoding.setSystem(system);
        codecoding.setDisplay(display);
        List<Coding> codings = new LinkedList<>();
        codings.add(codecoding);

        List<Questionnaire.QuestionnaireItemComponent> itemss = new LinkedList<>();
        JSONArray items = json.getJSONArray("item");
        for (int i = 0; i < items.length(); ++i) {
            JSONObject rec = items.getJSONObject(i);
            String linkId = rec.getString("linkId");
            System.out.println(linkId);
            String text = rec.getString("text");
            System.out.println(text);
            JSONArray options = rec.getJSONArray("options");
            System.out.println(options);
            JSONObject cod = rec.getJSONObject("code");
            String code2 = cod.getString("code");
            System.out.println(code2);
            String system2 = cod.getString("system");
            System.out.println(system2);

            //TODO: Add "options" for FHIR to OHC

            QuestionnaireItemOptionComponent test = new QuestionnaireItemOptionComponent();
			QuestionnaireItemOptionComponent test2 = new QuestionnaireItemOptionComponent();
            List<QuestionnaireItemOptionComponent> listdata = new ArrayList<>();
			StringType stringType = new StringType();
            if (options != null) {
                for (int j=0;j<options.length();j++){
                    //test.addChild("valueString").addChild(options.getString(j).toString());
					//test.addChild("valueString").setProperty(options.getString(j),test);
					//test.addChild("valueString").setUserData(options.getString(j),options);
					//test.addChild("valueString").setIdBase(options.getString(j));
					stringType.setValue(options.getString(j));
					//logger.info("VVVVVVAAAAAAALLLLLLLUUUUUUEEEEEE: "+stringType.getValueAsString());
					test.setValue(stringType);
					
                    //type.setUserData("valueString",options.getString(j));
                    //test.setValue();
                    //test.setUserData("valueString",options.getString(j));
                    //test = options.getString(j);
                    //test.setValue((Type) type.getUserData("valueString"));
                    listdata.add(test);
                }
            }

            Questionnaire.QuestionnaireItemComponent iten = new Questionnaire.QuestionnaireItemComponent();
            iten.setLinkId(linkId);
            iten.setText(text);
            iten.setOption(listdata);
            Coding codecoding2 = new Coding();
            codecoding2.setCode(code2);
            codecoding2.setSystem(system2);
            List<Coding> codings2 = new LinkedList<>();
            codings2.add(codecoding2);
            iten.setCode(codings2);
            itemss.add(iten);
        }


        Question.setUrl(url);
        Question.setId(identifier);
        Question.setName(name);
        Question.setTitle(title);
        Question.setPublisher(publisher);
        Question.setDescription(description);
        Question.setPurpose(purpose);
        Question.setCode(codings);
        Question.setItem(itemss);


        String serialized = this.parser.encodeResourceToString(Question);
		System.out.println(Question);
        System.out.println(serialized);
		//logger.info("SERIALIZED: "+serialized);
        // Sending to https://dev.ohc.projectpersist.eu/fhir/org1/Questionnaire/vprasalnik_id.... (USE PUT instead of POST) :)
        String reference = this.ResourceAddReuest(serialized, accesstoken, "Questionnaire", identifier);
        //s.put("message", "Sucess! Added Diagnostic Report with id {" + reference + "}");
        //this.SetRestResponse(true, s, exchange);

        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
        } else {
            Map<String, Object> s = new LinkedHashMap<String, Object>();
            s.put("message", "Sucess! Added Diagnostic Report");
			s.put("payload", reference);
            this.SetRestResponse(true, s, exchange);
        }
        System.out.println("Reference received:"+ reference);
        //Observations.add(new Reference().setReference(reference).setDisplay("Observation"));
    }
	
	public void CreateAndSubmitResource(Exchange exchange) {
        Message camelMessage = exchange.getIn();
        Map<String, String> inpayload = (Map<String, String>) camelMessage.getBody();

        String identifier = "";
        String resource;

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

        try {
            identifier = URLEncoder.encode((String)inpayload.get("id"), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        resource = (String) inpayload.get("resourceType");


        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = null;
        try {
            jsonResult = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(inpayload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
		logger.info("String payload sent to FHIR server: " + jsonResult);

        this.ResourceAddReuest(jsonResult,accesstoken,resource,identifier);

        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
        } else {
            Map<String, Object> s = new LinkedHashMap<String, Object>();
            s.put("message", "Success! Success! Resource added.");
            s.put("resource_id", identifier);
            //s.put("payload", reference);
            this.SetRestResponse(true, s, exchange);
            s = null;
            System.gc();
        }

    }
	
	public void CreateAndSavePERSISTPatient(Exchange exchange, JSONObject jsons) {
        Map<String, Object> inpayload = new Gson().fromJson(jsons.toString(), HashMap.class);
        //Message camelMessage = exchange.getIn();
        //Map<String, Object> inpayload = (Map<String, Object>) camelMessage.getBody();
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

        Patient patient = new Patient();

        //TODO: Create the object from JSON

        String identifier = ""; //vprašalnik_o_zdravju_pacientov_2_si
        String deceased; // Whether this patient's record is in active useal
        String resource;
        String text;
		String gender;
		String date;

        try {
            identifier = URLEncoder.encode((String)inpayload.get("id"), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        deceased = String.valueOf(inpayload.get("deceasedBoolean"));
        resource = (String) inpayload.get("resourceType");

        CodeableConcept cdcp = new CodeableConcept();
        JSONObject json = new JSONObject(inpayload);

        if(json.has("maritalStatus") && !json.isNull("maritalStatus")) {
            JSONObject martial = json.getJSONObject("maritalStatus");
            if(martial.has("text") && !martial.isNull("text")) {
                text = (String) martial.get("text");
                cdcp.setText(text);
            }
            if(json.has("coding") && !json.isNull("coding")) {
            JSONArray items = martial.getJSONArray("coding");
            for (int i = 0; i < items.length(); ++i) {
                Coding cd = new Coding();
                JSONObject rec = items.getJSONObject(i);
                if(rec.has("code") && !rec.isNull("code")) {
                    String code = rec.getString("code");
                    cd.setCode(code);
                }
                if(rec.has("system") && !rec.isNull("system")) {
                    String system = rec.getString("system");
                    cd.setSystem(system);
                }

                martial = null;
                items = null;
                rec = null;
                System.gc();

                List<Coding> lcd = new LinkedList<>();
                lcd.add(cd);

                cdcp.setCoding(lcd);
            }

                patient.setMaritalStatus(cdcp);
            }
        }


        gender = (String) inpayload.get("gender");
        date = (String) inpayload.get("birthDate");
		LocalDateTime localDateTime = LocalDateTime.parse(date);
        localDateTime.getDayOfMonth();
		date = String.format("%04d-%02d-%02d", localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth());
        System.out.println(date);

        patient.setId(identifier);
        patient.setDeceased(new BooleanType(deceased));
        patient.setGender(Enumerations.AdministrativeGender.valueOf(gender.toUpperCase()));
        patient.setMaritalStatus(cdcp);
        patient.setBirthDateElement(new DateType(date));

        String serialized = this.parser.encodeResourceToString(patient);
        //System.out.println(serialized);
		logger.info(resource + " FHIR resource: " + serialized);
        //serialized = this.parser.encodeResourceToString((IBaseResource) inpayload);
        // Sending to https://dev.ohc.projectpersist.eu/fhir/org1/Questionnaire/vprasalnik_id.... (USE PUT instead of POST) :)
        this.ResourceAddReuest(serialized, accesstoken, resource, identifier);
        //s.put("message", "Sucess! Added Diagnostic Report with id {" + reference + "}");
        //this.SetRestResponse(true, s, exchange);

        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
        } else {
            Map<String, Object> s = new LinkedHashMap<String, Object>();
            s.put("message", "Sucess! Sucess! Resource added.");
			s.put("resource_id", identifier);
            //s.put("payload", reference);
            this.SetRestResponse(true, s, exchange);
            s = null;
            System.gc();
        }
        //System.out.println("Reference received:"+ reference);
		logger.info("Sucess! " + resource + " FHIR resource added to FHIR server. ID: " + identifier);
        //Observations.add(new Reference().setReference(reference).setDisplay("Observation"));

        identifier = null;
        deceased = null;
        resource = null;
        text = null;
        gender = null;
        date = null;
        inpayload = null;
        errors = null;
        json = null;
        serialized = null;
		Runtime.getRuntime().gc();
        System.gc();

        //======================================== PATIENT ========================================
/*
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

        String identifier;
        String deceased;
        String resource;
        String text;
        String gender;
        String date;

        Patient patient = new Patient();
        CodeableConcept cdcp = new CodeableConcept();

        JsonElement jelement = new JsonParser().parse(jsons.toString());
        JsonObject jobject = jelement.getAsJsonObject();

        identifier = jobject.get("id").getAsString();
        deceased = jobject.get("deceasedBoolean").getAsString();
        resource = jobject.get("resourceType").getAsString();

        if (jobject.has("maritalStatus") && !jobject.getAsJsonObject("maritalStatus").isJsonNull()) {
            JsonObject martial = jobject.getAsJsonObject("maritalStatus");
            if (martial.has("text") && !martial.getAsJsonPrimitive("text").isJsonNull()) {
                text = martial.get("text").getAsString();
                cdcp.setText(text);
            }
            if (martial.has("coding") && !martial.getAsJsonArray("coding").isJsonNull()) {
                JsonArray items = martial.getAsJsonArray("coding");
                for (int i = 0; i < items.size(); ++i) {
                    Coding cd = new Coding();
                    JsonObject rec = items.get(i).getAsJsonObject();
                    if (rec.has("code") && !rec.getAsJsonPrimitive("code").isJsonNull()) {
                        String code = rec.get("code").getAsString();
                        cd.setCode(code);
                    }
                    if (rec.has("system") && !rec.getAsJsonPrimitive("system").isJsonNull()) {
                        String system = rec.get("system").getAsString();
                        cd.setSystem(system);
                    }

                    List<Coding> lcd = new LinkedList<>();
                    lcd.add(cd);

                    cdcp.setCoding(lcd);
                }

                patient.setMaritalStatus(cdcp);

                System.gc();
            }
        }


        gender = jobject.get("gender").getAsString();
        date = jobject.get("birthDate").getAsString();
        LocalDateTime localDateTime = LocalDateTime.parse(date);
        localDateTime.getDayOfMonth();
        date = String.format("%04d-%02d-%02d", localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth());

        patient.setId(identifier);
        patient.setDeceased(new BooleanType(deceased));
        patient.setGender(Enumerations.AdministrativeGender.valueOf(gender.toUpperCase()));
        patient.setMaritalStatus(cdcp);
        patient.setBirthDateElement(new DateType(date));

        String serialized = this.parser.encodeResourceToString(patient);
        logger.info(resource + " FHIR resource: " + serialized);
        // Sending to https://dev.domain.com/fhir/org1/Questionnaire/vprasalnik_id.... (USE PUT instead of POST) :)
        this.ResourceAddReuest(serialized, accesstoken, resource, identifier);

        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
        } else {
            Map<String, Object> s = new LinkedHashMap<String, Object>();
            s.put("message", "Sucess! Sucess! Resource added.");
            s.put("resource_id", identifier);
            this.SetRestResponse(true, s, exchange);
        }
        logger.info("Sucess! " + resource + " FHIR resource added to FHIR server. ID: " + identifier);

        System.gc();


        //======================================== PATIENT ========================================

*/
    }
	
     public void CreateAndSavePERSISTEncounter(Exchange exchange, JSONObject jsons) {
        Map<String, Object> inpayload = new Gson().fromJson(jsons.toString(), HashMap.class);
        //Message camelMessage = exchange.getIn();
        //Map<String, Object> inpayload = (Map<String, Object>) camelMessage.getBody();
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

        if (!inpayload.containsKey("resourceType")) {
            errors.add("resourceType is required parameter");
        }
        if (!inpayload.containsKey("id")) {
            errors.add("id is required parameter");
        }
        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
            return;
        }

        //TODO: Create the object from JSON

        String identifier = ""; //vprašalnik_o_zdravju_pacientov_2_si
        String resource;
        String code; // Whether this patient's record is in active useal
        String system;
        String status;
        String end;
        String start;
        String display;
        String refe;

        try {
            identifier = URLEncoder.encode((String)inpayload.get("id"), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        status = (String) inpayload.get("status");
        resource = (String) inpayload.get("resourceType");

        Encounter encounter = new Encounter();
        encounter.setId(identifier);
        encounter.setStatus(Encounter.EncounterStatus.valueOf(status.toUpperCase()));
        Coding cd = new Coding();
        JSONObject json = new JSONObject(inpayload);
        if (json.has("class") && !json.isNull("class")) {
            JSONObject classs = json.getJSONObject("class");
            if (classs.has("code") && !classs.isNull("code")) {
                code = (String) classs.get("code");
                cd.setCode(code);
            }
            if (classs.has("code") && !classs.isNull("code")) {
                system = (String) classs.get("system");
                cd.setSystem(system);
            }
            classs = null;
            System.gc();

            encounter.setClass_(cd);
        }
        if (json.has("period") && !json.isNull("period")) {
            Period pd = new Period();
            JSONObject period = json.getJSONObject("period");
            start = (String) period.get("start");
            //pd.setStartElement(DateTimeType.parseV3(start));
            pd.setStartElement(new DateTimeType(start));
            end = (String) period.get("end");
            //pd.setEndElement(DateTimeType.parseV3(end));
            pd.setEndElement(new DateTimeType(end));
            encounter.setPeriod(pd);

            period = null;
            System.gc();
        }
        Reference ref = new Reference();
        if (json.has("subject") && !json.isNull("subject")) {
            JSONObject subject = json.getJSONObject("subject");
            if (subject.has("display") && !subject.isNull("display")) {
                display = (String) subject.get("display");
                ref.setDisplay(display);
            }
            if (subject.has("reference") && !subject.isNull("reference")) {
                refe = (String) subject.get("reference");
                ref.setReference(refe);
            }

            subject = null;
            System.gc();
            encounter.setSubject(ref);
        }

        String serialized = this.parser.encodeResourceToString(encounter);
        //System.out.println(serialized);
        logger.info(resource + " FHIR resource: " + serialized);
        //serialized = this.parser.encodeResourceToString((IBaseResource) inpayload);
        // Sending to https://dev.ohc.projectpersist.eu/fhir/org1/Questionnaire/vprasalnik_id.... (USE PUT instead of POST) :)
        this.ResourceAddReuest(serialized, accesstoken, resource, identifier);
        //s.put("message", "Sucess! Added Diagnostic Report with id {" + reference + "}");
        //this.SetRestResponse(true, s, exchange);

        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
        } else {
            Map<String, Object> s = new LinkedHashMap<String, Object>();
            s.put("message", "Sucess! Sucess! Resource added.");
            s.put("resource_id", identifier);
            //s.put("payload", reference);
            this.SetRestResponse(true, s, exchange);
            s = null;
            System.gc();
        }
        //System.out.println("Reference received:"+ reference);
        logger.info("Sucess! " + resource + " FHIR resource added to FHIR server. ID: " + identifier);
        //Observations.add(new Reference().setReference(reference).setDisplay("Observation"));

        identifier = null;
        resource = null;
        code = null;
        system = null;
        status = null;
        end = null;
        start = null;
        display = null;
        refe = null;
        inpayload = null;
        errors = null;
        json = null;
        serialized = null;
        System.gc();

        //======================================== ENCOUNTER ========================================
/*
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

        String identifier;
        String resource;
        String code;
        String system;
        String status;
        String end;
        String start;
        String display;
        String refe;

        Encounter encounter = new Encounter();

        JsonElement jelement = new JsonParser().parse(jsons.toString());
        JsonObject jobject = jelement.getAsJsonObject();

        identifier = jobject.get("id").getAsString();
        status = jobject.get("status").getAsString();
        resource = jobject.get("resourceType").getAsString();

        encounter.setId(identifier);
        encounter.setStatus(Encounter.EncounterStatus.valueOf(status.toUpperCase()));

        if (jobject.has("class") && !jobject.getAsJsonObject("class").isJsonNull()) {
            Coding cd = new Coding();
            JsonObject classs = jobject.getAsJsonObject("class");
            if (classs.has("code") && !classs.getAsJsonPrimitive("code").isJsonNull()) {
                code = classs.getAsJsonPrimitive("code").getAsString();
                cd.setCode(code);
            }
            if (classs.has("system") && !classs.getAsJsonPrimitive("system").isJsonNull()) {
                system = classs.getAsJsonPrimitive("system").getAsString();
                cd.setSystem(system);
            }

            encounter.setClass_(cd);
        }

        if (jobject.has("period") && !jobject.getAsJsonObject("period").isJsonNull()) {
            Period pd = new Period();
            JsonObject period = jobject.getAsJsonObject("period");
            if (period.has("start") && !period.getAsJsonPrimitive("start").isJsonNull()) {
                start = period.getAsJsonPrimitive("start").getAsString();
                pd.setStartElement(new DateTimeType(start));
            }
            if (period.has("end") && !period.getAsJsonPrimitive("end").isJsonNull()) {
                end = period.getAsJsonPrimitive("end").getAsString();
                pd.setEndElement(new DateTimeType(end));
            }

            encounter.setPeriod(pd);

        }

        if (jobject.has("subject") && !jobject.getAsJsonObject("subject").isJsonNull()) {
            Reference ref = new Reference();
            JsonObject subject = jobject.getAsJsonObject("subject");
            if (subject.has("display") && !subject.getAsJsonPrimitive("display").isJsonNull()) {
                display = subject.getAsJsonPrimitive("display").getAsString();
                ref.setDisplay(display);
            }
            if (subject.has("reference") && !subject.getAsJsonPrimitive("reference").isJsonNull()) {
                refe = subject.getAsJsonPrimitive("reference").getAsString();
                ref.setReference(refe);
            }

            encounter.setSubject(ref);
        }

        String serialized = this.parser.encodeResourceToString(encounter);
        logger.info(resource + " FHIR resource: " + serialized);
        // Sending to https://dev.domain.com/fhir/org1/Questionnaire/vprasalnik_id.... (USE PUT instead of POST) :)
        this.ResourceAddReuest(serialized, accesstoken, resource, identifier);

        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
        } else {
            Map<String, Object> s = new LinkedHashMap<String, Object>();
            s.put("message", "Sucess! Sucess! Resource added.");
            s.put("resource_id", identifier);
            this.SetRestResponse(true, s, exchange);
            s = null;
            System.gc();
        }
        logger.info("Sucess! " + resource + " FHIR resource added to FHIR server. ID: " + identifier);

        System.gc();

        //======================================== ENCOUNTER ========================================

*/
    }
	
	public void CreateAndSavePERSISTProcedure(Exchange exchange, JSONObject jsons) {
        Map<String, Object> inpayload = new Gson().fromJson(jsons.toString(), HashMap.class);
        //Message camelMessage = exchange.getIn();
        //Map<String, Object> inpayload = (Map<String, Object>) camelMessage.getBody();
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

        if (!inpayload.containsKey("resourceType")) {
            errors.add("resourceType is required parameter");
        }
        if (!inpayload.containsKey("id")) {
            errors.add("id is required parameter");
        }
        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
            return;
        }

        //TODO: Create the object from JSON

        String identifier = ""; //vprašalnik_o_zdravju_pacientov_2_si
        String resource;
        String status;
        String performedDateTime;
        String text;


        try {
            identifier = URLEncoder.encode((String)inpayload.get("id"), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        status = (String) inpayload.get("status");
        resource = (String) inpayload.get("resourceType");
        performedDateTime = (String) inpayload.get("performedDateTime");

        Procedure procedure = new Procedure();
        procedure.setId(identifier);
        procedure.setStatus(Procedure.ProcedureStatus.valueOf(status.toUpperCase()));
        DateTimeType dateTimeType = new DateTimeType();
        dateTimeType.setValueAsString(performedDateTime);
        procedure.setPerformed(dateTimeType);
        CodeableConcept codeableConcept = new CodeableConcept();
        JSONObject json = new JSONObject(inpayload);
        if(json.has("category") && !json.isNull("category")) {
            JSONObject category = json.getJSONObject("category");
            if(category.has("coding") && !category.isNull("coding")) {
                JSONArray items = category.getJSONArray("coding");
                for (int i = 0; i < items.length(); ++i) {
                    Coding cd = new Coding();
                    JSONObject rec = items.getJSONObject(i);
                    if(rec.has("code") && !rec.isNull("code")) {
                        String cod = rec.getString("code");
                        cd.setCode(cod);
                    }
                    if(rec.has("system") && !rec.isNull("system")) {
                        String syste = rec.getString("system");
                        cd.setSystem(syste);
                    }
                    if(rec.has("display") && !rec.isNull("display")) {
                        String displa = rec.getString("display");
                        cd.setDisplay(displa);
                    }


                    List<Coding> lcd = new LinkedList<>();
                    lcd.add(cd);

                    codeableConcept.setCoding(lcd);
                }
                    procedure.setCategory(codeableConcept);
            }
        }
        if(json.has("code") && !json.isNull("code")) {
            CodeableConcept codeableConcept2 = new CodeableConcept();
            JSONObject codes = json.getJSONObject("code");
            if (codes.has("coding") && !codes.isNull("coding")) {
                JSONArray itemss = codes.getJSONArray("coding");
                for (int i = 0; i < itemss.length(); ++i) {
                    JSONObject rec = itemss.getJSONObject(i);
                    Coding cd = new Coding();
                    if (rec.has("code") && !rec.isNull("code")) {
                        String cod = rec.getString("code");
                        cd.setCode(cod);
                    }
                    if (rec.has("system") && !rec.isNull("system")) {
                        String syste = rec.getString("system");
                        cd.setSystem(syste);
                    }
                    if (rec.has("display") && !rec.isNull("display")) {
                        String displa = rec.getString("display");
                        cd.setDisplay(displa);
                    }

                    List<Coding> lcd = new LinkedList<>();
                    lcd.add(cd);

                    codeableConcept2.setCoding(lcd);

                }
            }

            if (codes.has("text") && !codes.isNull("text")) {
                text = (String) codes.get("text");
                codeableConcept2.setText(text);
            }
            procedure.setCode(codeableConcept2);
        }

        if(json.has("context") && !json.isNull("context")) {
            Reference ref = new Reference();
            JSONObject context = json.getJSONObject("context");
            if(context.has("id") && !context.isNull("id")) {
                String ids = context.getString("id");
                ref.setId(ids);
            }
            if (context.has("display") && !context.isNull("display")) {
                String disp = context.getString("display");
                ref.setDisplay(disp);
            }
            if (context.has("reference") && !context.isNull("reference")) {
                String referen = context.getString("reference");
                ref.setReference(referen);
            }
            procedure.setContext(ref);
        }

        Reference refe = new Reference();
        if (json.has("subject") && !json.isNull("subject")) {
            JSONObject sub = json.getJSONObject("subject");
            if (sub.has("display") && !sub.isNull("display")) {
                String displ = sub.getString("display");
                refe.setDisplay(displ);
            }
            if (sub.has("reference") && !sub.isNull("reference")) {
                String references = sub.getString("reference");
                refe.setReference(references);
            }
            procedure.setSubject(refe);
        }

        String serialized = this.parser.encodeResourceToString(procedure);
        //System.out.println(serialized);
		logger.info(resource + " FHIR resource: " + serialized);
        //serialized = this.parser.encodeResourceToString((IBaseResource) inpayload);
        // Sending to https://dev.ohc.projectpersist.eu/fhir/org1/Questionnaire/vprasalnik_id.... (USE PUT instead of POST) :)
        this.ResourceAddReuest(serialized, accesstoken, resource, identifier);
        //s.put("message", "Sucess! Added Diagnostic Report with id {" + reference + "}");
        //this.SetRestResponse(true, s, exchange);

        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
        } else {
            Map<String, Object> s = new LinkedHashMap<String, Object>();
            s.put("message", "Sucess! Sucess! Resource added.");
			s.put("resource_id", identifier);
            //s.put("payload", reference);
            this.SetRestResponse(true, s, exchange);
        }
        //System.out.println("Reference received:"+ reference);
		logger.info("Sucess! " + resource + " FHIR resource added to FHIR server. ID: " + identifier);
        //Observations.add(new Reference().setReference(reference).setDisplay("Observation"));
		
		inpayload = null;
        errors = null;
        json = null;
        serialized = null;
		Runtime.getRuntime().gc();
        System.gc();
    }
	
	public void CreateAndSavePERSISTObservation(Exchange exchange, JSONObject jsons) {
        Map<String, Object> inpayload = new Gson().fromJson(jsons.toString(), HashMap.class);
        //Message camelMessage = exchange.getIn();
        //Map<String, Object> inpayload = (Map<String, Object>) camelMessage.getBody();
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

        if (!inpayload.containsKey("resourceType")) {
            errors.add("resourceType is required parameter");
        }
        if (!inpayload.containsKey("id")) {
            errors.add("id is required parameter");
        }
        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
            return;
        }

        //TODO: Create the object from JSON

        String identifier = ""; //vprašalnik_o_zdravju_pacientov_2_si
        String resource;
        String status;
        String comment;
        String effectiveDateTime;
        String valueString;


        try {
            identifier = URLEncoder.encode((String)inpayload.get("id"), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        status = (String) inpayload.get("status");
        resource = (String) inpayload.get("resourceType");
        effectiveDateTime = (String) inpayload.get("effectiveDateTime");
        valueString = (String) inpayload.get("valueString");
        comment = (String) inpayload.get("comment");

        Observation observation = new Observation();
        observation.setId(identifier);
        observation.setStatus(Observation.ObservationStatus.valueOf(status.toUpperCase()));
        observation.setComment(comment);
        DateTimeType dateTimeType = new DateTimeType();
        dateTimeType.setValueAsString(effectiveDateTime);
        observation.setEffective(dateTimeType);
        StringType stringType = new StringType();
        stringType.setValue(valueString);
        observation.setValue(stringType);

        JSONObject json = new JSONObject(inpayload);
        Reference refe = new Reference();
        if (json.has("subject") && !json.isNull("subject")) {
            JSONObject sub = json.getJSONObject("subject");
            if (sub.has("display") && !sub.isNull("display")) {
                String displ = sub.getString("display");
                refe.setDisplay(displ);
            }
            if (sub.has("reference") && !sub.isNull("reference")) {
                String references = sub.getString("reference");
                refe.setReference(references);
            }
            observation.setSubject(refe);
        }

        CodeableConcept codeableConcept = new CodeableConcept();
        if(json.has("category") && !json.isNull("category")) {
            JSONArray category = json.getJSONArray("category");
            if(((JSONObject) category.get(0)).has("coding") && !((JSONObject) category.get(0)).isNull("coding")) {
                JSONArray items = (JSONArray) ((JSONObject) category.get(0)).get("coding");
                for (int i = 0; i < items.length(); ++i) {
                    Coding cd = new Coding();
                    JSONObject rec = items.getJSONObject(i);
                    if(rec.has("code") && !rec.isNull("code")) {
                        String cod = rec.getString("code");
                        cd.setCode(cod);
                    }
                    if(rec.has("system") && !rec.isNull("system")) {
                        String syste = rec.getString("system");
                        cd.setSystem(syste);
                    }
                    if(rec.has("display") && !rec.isNull("display")) {
                        String displa = rec.getString("display");
                        cd.setDisplay(displa);
                    }
                    List<Coding> lcd = new LinkedList<>();
                    lcd.add(cd);

                    codeableConcept.setCoding(lcd);

                    List<CodeableConcept> listcode = new LinkedList<>();
                    listcode.add(codeableConcept);

                    observation.setCategory(listcode);
                }
            }
        }
		
        CodeableConcept codeableConcept2 = new CodeableConcept();
        if(json.has("code") && !json.isNull("code")) {
            JSONObject codes = json.getJSONObject("code");
            if(codes.has("coding") && !codes.isNull("coding")) {
                JSONArray itemss = codes.getJSONArray("coding");
                for (int i = 0; i < itemss.length(); ++i) {
                    Coding cd = new Coding();
                    JSONObject rec = itemss.getJSONObject(i);
                    if(rec.has("code") && !rec.isNull("code")) {
                        String cod = rec.getString("code");
                        cd.setCode(cod);
                    }
                    if(rec.has("system") && !rec.isNull("system")) {
                        String syste = rec.getString("system");
                        cd.setSystem(syste);
                    }
                    if(rec.has("display") && !rec.isNull("display")) {
                        String displa = rec.getString("display");
                        cd.setDisplay(displa);
                    }


                    List<Coding> lcd = new LinkedList<>();
                    lcd.add(cd);

                    codeableConcept2.setCoding(lcd);

                }

                observation.setCode(codeableConcept2);
            }
        }


        String serialized = this.parser.encodeResourceToString(observation);
        //System.out.println(serialized);
		logger.info(resource + " FHIR resource: " + serialized);
        //serialized = this.parser.encodeResourceToString((IBaseResource) inpayload);
        // Sending to https://dev.domain.com/fhir/org1/Questionnaire/vprasalnik_id.... (USE PUT instead of POST) :)
        this.ResourceAddReuest(serialized, accesstoken, resource, identifier);
        //s.put("message", "Sucess! Added Diagnostic Report with id {" + reference + "}");
        //this.SetRestResponse(true, s, exchange);

        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
        } else {
            Map<String, Object> s = new LinkedHashMap<String, Object>();
            s.put("message", "Sucess! Sucess! Resource added.");
			s.put("resource_id", identifier);
            //s.put("payload", reference);
            this.SetRestResponse(true, s, exchange);
        }
        //System.out.println("Reference received:"+ reference);
		logger.info("Sucess! " + resource + " FHIR resource added to FHIR server. ID: " + identifier);
        //Observations.add(new Reference().setReference(reference).setDisplay("Observation"));
		
		inpayload = null;
        errors = null;
        json = null;
        serialized = null;
		Runtime.getRuntime().gc();
        System.gc();
    }
	
	
	public void CreateAndSavePERSISTDiagnosticReport(Exchange exchange, JSONObject jsons) {
       Map<String, Object> inpayload = new Gson().fromJson(jsons.toString(), HashMap.class);
        //Message camelMessage = exchange.getIn();
        //Map<String, Object> inpayload = (Map<String, Object>) camelMessage.getBody();
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

        if (!inpayload.containsKey("resourceType")) {
            errors.add("resourceType is required parameter");
        }
        if (!inpayload.containsKey("id")) {
            errors.add("id is required parameter");
        }
        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
            return;
        }

        //TODO: Create the object from JSON

        String identifier = ""; //vprašalnik_o_zdravju_pacientov_2_si
        String resource;
        String status;
        String effectiveDateTime;


        try {
            identifier = URLEncoder.encode((String)inpayload.get("id"), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        status = (String) inpayload.get("status");
        resource = (String) inpayload.get("resourceType");
        effectiveDateTime = (String) inpayload.get("effectiveDateTime");

        DiagnosticReport diagnosticReport = new DiagnosticReport();
        diagnosticReport.setId(identifier);
        diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.valueOf(status.toUpperCase()));
        DateTimeType dateTimeType = new DateTimeType();
        dateTimeType.setValueAsString(effectiveDateTime);
        diagnosticReport.setEffective(dateTimeType);

        JSONObject json = new JSONObject(inpayload);
        Reference refe = new Reference();
        if(json.has("subject") && !json.isNull("subject")) {
            JSONObject sub = json.getJSONObject("subject");
            if(sub.has("display") && !sub.isNull("display")) {
                String displ = sub.getString("display");
                refe.setDisplay(displ);
            }
            if(sub.has("reference") && !sub.isNull("reference")) {
                String references = sub.getString("reference");
                refe.setReference(references);
            }
            diagnosticReport.setSubject(refe);
        }

        CodeableConcept codeableConcept = new CodeableConcept();
        if(json.has("category") && !json.isNull("category")) {
            JSONObject category = json.getJSONObject("category");
            if(category.has("coding") && !category.isNull("coding")) {
                JSONArray items = category.getJSONArray("coding");
                for (int i = 0; i < items.length(); ++i) {
                    Coding cd = new Coding();
                    JSONObject rec = items.getJSONObject(i);
                    if(rec.has("code") && !rec.isNull("code")) {
                        String cod = rec.getString("code");
                        cd.setCode(cod);
                    }
                    if(rec.has("system") && !rec.isNull("system")) {
                        String syste = rec.getString("system");
                        cd.setSystem(syste);
                    }
                    if(rec.has("display") && !rec.isNull("display")) {
                        String displa = rec.getString("display");
                        cd.setDisplay(displa);
                    }

                    List<Coding> lcd = new LinkedList<>();
                    lcd.add(cd);

                    codeableConcept.setCoding(lcd);
                }
            }
            if(category.has("text") && !category.isNull("text")) {
                String text = (String) category.get("text");
                codeableConcept.setText(text);
            }
            diagnosticReport.setCategory(codeableConcept);
        }

        CodeableConcept codeableConcept2 = new CodeableConcept();
        if(json.has("code") && !json.isNull("code")) {
            JSONObject codes = json.getJSONObject("code");
            if(codes.has("coding") && !codes.isNull("coding")) {
                JSONArray itemss = codes.getJSONArray("coding");
                for (int i = 0; i < itemss.length(); ++i) {
                    Coding cd = new Coding();
                    JSONObject rec = itemss.getJSONObject(i);
                    if(rec.has("code") && !rec.isNull("code")) {
                        String cod = rec.getString("code");
                        cd.setCode(cod);
                    }
                    if(rec.has("system") && !rec.isNull("system")) {
                        String syste = rec.getString("system");
                        cd.setSystem(syste);
                    }
                    if(rec.has("display") && !rec.isNull("display")) {
                        String displa = rec.getString("display");
                        cd.setDisplay(displa);
                    }


                    List<Coding> lcd = new LinkedList<>();
                    lcd.add(cd);

                    codeableConcept2.setCoding(lcd);

                }
            }
            if(codes.has("text") && !codes.isNull("text")) {
                String text = (String) codes.get("text");
                codeableConcept.setText(text);
            }
            diagnosticReport.setCode(codeableConcept2);
        }

        List<Reference> listref = new LinkedList<>();
        if(json.has("result") && !json.isNull("result")) {
            JSONArray res = json.getJSONArray("result");
            for (int i = 0; i < res.length(); ++i) {
                Reference refere = new Reference();
                JSONObject rec = res.getJSONObject(i);
                if(rec.has("id") && !rec.isNull("id")) {
                    String id = rec.getString("id");
                    refere.setId(id);
                }
                if(rec.has("reference") && !rec.isNull("reference")) {
                    String refer = rec.getString("reference");
                    refere.setReference(refer);
                }
                listref.add(refere);

            }
            diagnosticReport.setResult(listref);
        }

        String serialized = this.parser.encodeResourceToString(diagnosticReport);
        //System.out.println(serialized);
		logger.info(resource + " FHIR resource: " + serialized);
        //serialized = this.parser.encodeResourceToString((IBaseResource) inpayload);
        // Sending to https://dev.domain.com/fhir/org1/Questionnaire/vprasalnik_id.... (USE PUT instead of POST) :)
        this.ResourceAddReuest(serialized, accesstoken, resource, identifier);
        //s.put("message", "Sucess! Added Diagnostic Report with id {" + reference + "}");
        //this.SetRestResponse(true, s, exchange);

        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
        } else {
            Map<String, Object> s = new LinkedHashMap<String, Object>();
            s.put("message", "Sucess! Sucess! Resource added.");
			s.put("resource_id", identifier);
            //s.put("payload", reference);
            this.SetRestResponse(true, s, exchange);
        }
        //System.out.println("Reference received:"+ reference);
		logger.info("Sucess! " + resource + " FHIR resource added to FHIR server. ID: " + identifier);
        //Observations.add(new Reference().setReference(reference).setDisplay("Observation"));
		
		inpayload = null;
        errors = null;
        json = null;
        serialized = null;
		Runtime.getRuntime().gc();
        System.gc();
    }
	
	public void CreateAndSavePERSISTCondition(Exchange exchange, JSONObject jsons) {
        Map<String, Object> inpayload = new Gson().fromJson(jsons.toString(), HashMap.class);
        //Message camelMessage = exchange.getIn();
        //Map<String, Object> inpayload = (Map<String, Object>) camelMessage.getBody();
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

        if (!inpayload.containsKey("resourceType")) {
            errors.add("resourceType is required parameter");
        }
        if (!inpayload.containsKey("id")) {
            errors.add("id is required parameter");
        }
        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
            return;
        }

        //TODO: Create the object from JSON

        String identifier = ""; //vprašalnik_o_zdravju_pacientov_2_si
        String resource;
        String status;
        String onsetDateTime;


        try {
            identifier = URLEncoder.encode((String)inpayload.get("id"), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        status = (String) inpayload.get("clinicalStatus");
        resource = (String) inpayload.get("resourceType");
        onsetDateTime = (String) inpayload.get("onsetDateTime");

        Condition condition = new Condition();
        condition.setId(identifier);
        condition.setClinicalStatus(Condition.ConditionClinicalStatus.valueOf(status.toUpperCase()));
        DateTimeType dateTimeType = new DateTimeType();
        dateTimeType.setValueAsString(onsetDateTime);
        condition.setOnset(dateTimeType);

        JSONObject json = new JSONObject(inpayload);
        Reference refe = new Reference();
        if(json.has("subject") && !json.isNull("subject")) {
            JSONObject sub = json.getJSONObject("subject");
            if(sub.has("display") && !sub.isNull("display")) {
                String displ = sub.getString("display");
                refe.setDisplay(displ);
            }
            if(sub.has("reference") && !sub.isNull("reference")) {
                String references = sub.getString("reference");
                refe.setReference(references);
            }
            condition.setSubject(refe);
        }

        Age age = new Age();
        if(json.has("onsetAge") && !json.isNull("onsetAge")) {
            JSONObject sub2 = json.getJSONObject("onsetAge");
            if(sub2.has("value") && !sub2.isNull("value")) {
                long val = sub2.getLong("value");
                age.setValue(val);
            }
            condition.setOnset(age);
            if (json.has("context") && !json.isNull("context")) {
                Reference refer = new Reference();
                JSONObject sub3 = json.getJSONObject("context");
                if (sub3.has("id") && !sub3.isNull("id")) {
                    String id = sub3.getString("id");
                    refer.setId(id);
                }
                if (sub3.has("reference") && !sub3.isNull("reference")) {
                    String ref = sub3.getString("reference");
                    refer.setReference(ref);
                }
                condition.setContext(refer);
            }
        }

        CodeableConcept codeableConcept = new CodeableConcept();
        List<CodeableConcept> listcode = new LinkedList<>();
        if(json.has("category") && !json.isNull("category")) {
            JSONArray category = json.getJSONArray("category");
            if(((JSONObject) category.get(0)).has("coding") && !((JSONObject) category.get(0)).isNull("coding")) {
                JSONArray items = (JSONArray) ((JSONObject) category.get(0)).get("coding");
                for (int i = 0; i < items.length(); ++i) {
                    Coding cd = new Coding();
                    JSONObject rec = items.getJSONObject(i);
                    if(rec.has("code") && !rec.isNull("code")) {
                        String cod = rec.getString("code");
                        cd.setCode(cod);
                    }
                    if(rec.has("system") && !rec.isNull("system")) {
                        String syste = rec.getString("system");
                        cd.setSystem(syste);
                    }
                    if(rec.has("display") && !rec.isNull("display")) {
                        String displa = rec.getString("display");
                        cd.setDisplay(displa);
                    }

                    List<Coding> lcd = new LinkedList<>();
                    lcd.add(cd);

                    codeableConcept.setCoding(lcd);
                }
            }
            if(((JSONObject) category.get(0)).has("text") && !((JSONObject) category.get(0)).isNull("text")) {
                String texta = (String) ((JSONObject) category.get(0)).get("text");
                codeableConcept.setText(texta);
            }
            listcode.add(codeableConcept);
            condition.setCategory(listcode);
        }


        CodeableConcept codeableConcept2 = new CodeableConcept();
        if(json.has("code") && !json.isNull("code")) {
            JSONObject codes = json.getJSONObject("code");
            if(codes.has("coding") && !codes.isNull("coding")) {
                JSONArray itemss = codes.getJSONArray("coding");
                for (int i = 0; i < itemss.length(); ++i) {
                    Coding cd = new Coding();
                    JSONObject rec = itemss.getJSONObject(i);
                    if(rec.has("code") && !rec.isNull("code")) {
                        String cod = rec.getString("code");
                        cd.setCode(cod);
                    }
                    if(rec.has("system") && !rec.isNull("system")) {
                        String syste = rec.getString("system");
                        cd.setSystem(syste);
                    }
                    if(rec.has("display") && !rec.isNull("display")) {
                        String displa = rec.getString("display");
                        cd.setDisplay(displa);
                    }

                    List<Coding> lcd = new LinkedList<>();
                    lcd.add(cd);

                    codeableConcept2.setCoding(lcd);

                }
            }
            if(codes.has("text") && !codes.isNull("text")) {
                String texts = (String) codes.get("text");
                codeableConcept.setText(texts);
            }
            condition.setCode(codeableConcept2);
        }

        String serialized = this.parser.encodeResourceToString(condition);
        //System.out.println(serialized);
		logger.info(resource + " FHIR resource: " + serialized);
        //serialized = this.parser.encodeResourceToString((IBaseResource) inpayload);
        // Sending to https://dev.domain.com/fhir/org1/Questionnaire/vprasalnik_id.... (USE PUT instead of POST) :)
        this.ResourceAddReuest(serialized, accesstoken, resource, identifier);
        //s.put("message", "Sucess! Added Diagnostic Report with id {" + reference + "}");
        //this.SetRestResponse(true, s, exchange);

        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
        } else {
            Map<String, Object> s = new LinkedHashMap<String, Object>();
            s.put("message", "Sucess! Sucess! Resource added.");
			s.put("resource_id", identifier);
            //s.put("payload", reference);
            this.SetRestResponse(true, s, exchange);
        }
        //System.out.println("Reference received:"+ reference);
		logger.info("Sucess! " + resource + " FHIR resource added to FHIR server. ID: " + identifier);
        //Observations.add(new Reference().setReference(reference).setDisplay("Observation"));
		
		inpayload = null;
        errors = null;
        json = null;
        serialized = null;
		Runtime.getRuntime().gc();
        System.gc();
    }
	
	public void CreateAndSavePERSISTMedicationStatement(Exchange exchange, JSONObject jsons) {
        Map<String, Object> inpayload = new Gson().fromJson(jsons.toString(), HashMap.class);
        //Message camelMessage = exchange.getIn();
        //Map<String, Object> inpayload = (Map<String, Object>) camelMessage.getBody();
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

        if (!inpayload.containsKey("resourceType")) {
            errors.add("resourceType is required parameter");
        }
        if (!inpayload.containsKey("id")) {
            errors.add("id is required parameter");
        }
        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
            return;
        }

        //TODO: Create the object from JSON

        String identifier = ""; //vprašalnik_o_zdravju_pacientov_2_si
        String resource;
        String status;
        String text;
        String value;
        String taken;


        try {
            identifier = URLEncoder.encode((String)inpayload.get("id"), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        status = (String) inpayload.get("status");
        resource = (String) inpayload.get("resourceType");
        taken = (String) inpayload.get("taken");

        MedicationStatement medicationStatement = new MedicationStatement();
        medicationStatement.setId(identifier);
        medicationStatement.setStatus(MedicationStatement.MedicationStatementStatus.valueOf(status.toUpperCase()));
        medicationStatement.setTaken(MedicationStatement.MedicationStatementTaken.valueOf(taken.toUpperCase()));

        JSONObject json = new JSONObject(inpayload);
        Reference refe = new Reference();
        refe.isEmpty();
        medicationStatement.setSubject(refe);
        DateTimeType dateTimeType = new DateTimeType();

        if(json.has("effectiveDateTime") && !json.isNull("effectiveDateTime")) {
            value = (String) inpayload.get("effectiveDateTime");
            dateTimeType.setValueAsString(value);
            medicationStatement.setEffective(dateTimeType);
        }

        if(json.has("effectivePeriod") && !json.isNull("effectivePeriod")) {
            JSONObject sub2 = json.getJSONObject("effectivePeriod");
            String end = sub2.getString("end");
            String start = sub2.getString("start");
            Period period = new Period();
            dateTimeType.setValueAsString(start);
            period.setStartElement(dateTimeType);
            dateTimeType.setValueAsString(end);
            period.setEndElement(dateTimeType);
            medicationStatement.setEffective(period);
            if(json.has("medicationCodeableConcept") && !json.isNull("medicationCodeableConcept")) {
                Reference refer = new Reference();
                JSONObject sub3 = json.getJSONObject("medicationCodeableConcept");
                if(sub3.has("text") && !sub3.isNull("text")) {
                    String texty = sub3.getString("text");
                    CodeableConcept codeableConcept2 = new CodeableConcept();
                    codeableConcept2.setText(texty);
                    medicationStatement.setMedication(codeableConcept2);
                }
            }
        }

        if(json.has("category") && !json.isNull("category")) {
            CodeableConcept codeableConcept = new CodeableConcept();
            JSONObject category = json.getJSONObject("category");
            if (json.has("coding") && !json.isNull("coding")) {
                JSONArray items = category.getJSONArray("coding");
                for (int i = 0; i < items.length(); ++i) {
                    Coding cd = new Coding();
                    JSONObject rec = items.getJSONObject(i);
                    if(rec.has("code") && !rec.isNull("code")) {
                        String cod = rec.getString("code");
                        cd.setCode(cod);
                    }
                    if(rec.has("system") && !rec.isNull("system")) {
                        String syste = rec.getString("system");
                        cd.setSystem(syste);
                    }
                    if(rec.has("display") && !rec.isNull("display")) {
                        String displa = rec.getString("display");
                        cd.setDisplay(displa);
                    }

                    List<Coding> lcd = new LinkedList<>();
                    lcd.add(cd);

                    codeableConcept.setCoding(lcd);

                }
                medicationStatement.setCategory(codeableConcept);
            }
        }

        List<Dosage> ldosage = new LinkedList<>();
        Dosage dosage = new Dosage();
        if(json.has("dosage") && !json.isNull("dosage")) {
            JSONArray dosagea = json.getJSONArray("dosage");
            if(json.has("dosage") && !json.isNull("dosage")) {
                JSONObject rec = (JSONObject) ((JSONObject) dosagea.get(0)).get("doseQuantity");
                double valu = rec.getDouble("value");
                SimpleQuantity quantity = new SimpleQuantity();
                quantity.setValue(valu);
                dosage.setDose(quantity);
            }
            if(json.has("text") && !json.isNull("text")) {
                text = (String) ((JSONObject) dosagea.get(0)).get("text");
                dosage.setText(text);
            }
            ldosage.add(dosage);
            medicationStatement.setDosage(ldosage);
        }


        String serialized = this.parser.encodeResourceToString(medicationStatement);
        //System.out.println(serialized);
		logger.info(resource + " FHIR resource: " + serialized);
        //serialized = this.parser.encodeResourceToString((IBaseResource) inpayload);
        // Sending to https://dev.domain.com/fhir/org1/Questionnaire/vprasalnik_id.... (USE PUT instead of POST) :)
        this.ResourceAddReuest(serialized, accesstoken, resource, identifier);
        //s.put("message", "Sucess! Added Diagnostic Report with id {" + reference + "}");
        //this.SetRestResponse(true, s, exchange);

        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
        } else {
            Map<String, Object> s = new LinkedHashMap<String, Object>();
            s.put("message", "Sucess! Sucess! Resource added.");
			s.put("resource_id", identifier);
            //s.put("payload", reference);
            this.SetRestResponse(true, s, exchange);
        }
        //System.out.println("Reference received:"+ reference);
		logger.info("Sucess! " + resource + " FHIR resource added to FHIR server. ID: " + identifier);
        //Observations.add(new Reference().setReference(reference).setDisplay("Observation"));
		
		inpayload = null;
        errors = null;
        json = null;
        serialized = null;
		Runtime.getRuntime().gc();
        System.gc();
    }
	
	public void CreateAndSavePERSISTComposition(Exchange exchange, JSONObject jsons) {
        Map<String, Object> inpayload = new Gson().fromJson(jsons.toString(), HashMap.class);
        //Message camelMessage = exchange.getIn();
        //Map<String, Object> inpayload = (Map<String, Object>) camelMessage.getBody();
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

        if (!inpayload.containsKey("resourceType")) {
            errors.add("resourceType is required parameter");
        }
        if (!inpayload.containsKey("id")) {
            errors.add("id is required parameter");
        }
        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
            return;
        }

        //TODO: Create the object from JSON

        String identifier = ""; //vprašalnik_o_zdravju_pacientov_2_si
        String resource;
        String status;
        String date;
        String title;


        try {
            identifier = URLEncoder.encode((String)inpayload.get("id"), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        status = (String) inpayload.get("status");
        resource = (String) inpayload.get("resourceType");
        title = (String) inpayload.get("title");
        date = (String) inpayload.get("date");

        Composition composition = new Composition();
        composition.setId(identifier);
        composition.setStatus(Composition.CompositionStatus.valueOf(status.toUpperCase()));
        composition.setTitle(title);
        DateTimeType dateTimeType = new DateTimeType();
        dateTimeType.setValueAsString(date);
        composition.setDateElement(dateTimeType);

        JSONObject json = new JSONObject(inpayload);
        Reference referencez = new Reference();
        if (json.has("encounter") && !json.isNull("encounter")) {
            JSONObject sub1 = json.getJSONObject("encounter");
            if (sub1.has("display") && !sub1.isNull("display")) {
                String displays = sub1.getString("display");
                referencez.setDisplay(displays);
            }
            composition.setEncounter(referencez);
        }
        CodeableConcept codeableConcept = new CodeableConcept();
        if (json.has("type") && !json.isNull("type")) {
            JSONObject sub0 = json.getJSONObject("type");
            if (sub0.has("text") && !sub0.isNull("text")) {
                String text1 = sub0.getString("text");
                codeableConcept.setText(text1);
            }
            composition.setType(codeableConcept);
        }

        if (json.has("section") && !json.isNull("section")) {
            JSONArray section = json.getJSONArray("section");
            if (((JSONObject) section.get(0)).has("text") && !((JSONObject) section.get(0)).isNull("text")) {
                JSONObject rec = (JSONObject) ((JSONObject) section.get(0)).get("text");
                Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
                Narrative narrative = new Narrative();
                XhtmlNode xhtmlNode = new XhtmlNode();
                if (rec.has("div") && !rec.isNull("div")) {
                    String div = rec.getString("div");
                    div = div.replace("<div>", "");
                    div = div.replace("</div>", "");
                    div = StringEscapeUtils.escapeXml(div);
                    xhtmlNode.setValueAsString(div);
                    narrative.setDiv(xhtmlNode);
                }
                if (rec.has("status") && !rec.isNull("status")) {
                    String statu = rec.getString("status");
                    narrative.setStatusAsString(statu);
                }
                sectionComponent.setText(narrative);
                List<Composition.SectionComponent> lsect = new LinkedList<>();
                lsect.add(sectionComponent);
                composition.setSection(lsect);
            }
        }

        if (json.has("author") && !json.isNull("author")) {
            JSONArray section = json.getJSONArray("author");
            if (((JSONObject) section.get(0)).has("display") && !((JSONObject) section.get(0)).isNull("display")) {
                JSONObject rec = ((JSONObject) ((JSONObject) section.get(0)));
                String display = rec.getString("display");

                Reference reference = new Reference();
                reference.setDisplay(display);
                List<Reference> auth = new LinkedList<>();
                auth.add(reference);
                composition.setAuthor(auth);
            }
        }

        String serialized = this.parser.encodeResourceToString(composition);
        //System.out.println(serialized);
        logger.info(resource + " FHIR resource: " + serialized);
        //serialized = this.parser.encodeResourceToString((IBaseResource) inpayload);
        // Sending to https://dev.domain.com/fhir/org1/Questionnaire/vprasalnik_id.... (USE PUT instead of POST) :)
        this.ResourceAddReuest(serialized, accesstoken, resource, identifier);
        //s.put("message", "Sucess! Added Diagnostic Report with id {" + reference + "}");
        //this.SetRestResponse(true, s, exchange);

        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
        } else {
            Map<String, Object> s = new LinkedHashMap<String, Object>();
            s.put("message", "Sucess! Sucess! Resource added.");
            s.put("resource_id", identifier);
            //s.put("payload", reference);
            this.SetRestResponse(true, s, exchange);
        }
        //System.out.println("Reference received:"+ reference);
        logger.info("Sucess! " + resource + " FHIR resource added to FHIR server. ID: " + identifier);
        //Observations.add(new Reference().setReference(reference).setDisplay("Observation"));

        inpayload = null;
        errors = null;
        json = null;
        serialized = null;
        Runtime.getRuntime().gc();
        System.gc();

        //======================================== COMPOSITIONS ========================================
/*
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

        String identifier;
        String resource;
        String status;
        String date;
        String title;

        JsonElement jelement = new JsonParser().parse(jsons.toString());
        JsonObject jobject = jelement.getAsJsonObject();

        identifier = jobject.get("id").getAsString();
        status = jobject.get("status").getAsString();
        resource = jobject.get("resourceType").getAsString();
        title = jobject.get("title").getAsString();
        date = jobject.get("date").getAsString();

        Composition composition = new Composition();
        composition.setId(identifier);
        composition.setStatus(Composition.CompositionStatus.valueOf(status.toUpperCase()));
        composition.setTitle(title);
        DateTimeType dateTimeType = new DateTimeType();
        dateTimeType.setValueAsString(date);
        composition.setDateElement(dateTimeType);

        if (jobject.has("encounter") && !jobject.getAsJsonArray("encounter").isJsonNull()) {
            Reference referencez = new Reference();
            JsonObject sub1 = jobject.getAsJsonObject("encounter");
            if (sub1.has("display") && !sub1.getAsJsonPrimitive("display").isJsonNull()) {
                String displays = sub1.getAsJsonPrimitive("display").getAsString();
                referencez.setDisplay(displays);
            }
            if (sub1.has("id") && !sub1.getAsJsonPrimitive("id").isJsonNull()) {
                String id = sub1.getAsJsonPrimitive("id").getAsString();
                referencez.setId(id);
            }
            if (sub1.has("reference") && !sub1.getAsJsonPrimitive("reference").isJsonNull()) {
                String refer = sub1.getAsJsonPrimitive("reference").getAsString();
                referencez.setReference(refer);
            }
            composition.setEncounter(referencez);
        }

        if (jobject.has("type") && !jobject.getAsJsonObject("type").isJsonNull()) {
            CodeableConcept codeableConcept = new CodeableConcept();
            JsonObject sub0 = jobject.getAsJsonObject("type");
            if (sub0.has("text") && !sub0.getAsJsonPrimitive("text").isJsonNull()) {
                String text1 = sub0.getAsJsonPrimitive("text").getAsString();
                codeableConcept.setText(text1);
            }
            composition.setType(codeableConcept);
        }

        if (jobject.has("section") && !jobject.getAsJsonObject("section").isJsonNull()) {
            JsonArray section = jobject.getAsJsonArray("section");
            if (((JsonObject) section.get(0)).has("text") && !((JsonObject) section.get(0)).getAsJsonObject("text").isJsonNull()) {
                JsonObject rec = (JsonObject) ((JsonObject) section.get(0)).get("text");
                Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
                Narrative narrative = new Narrative();
                XhtmlNode xhtmlNode = new XhtmlNode();
                if (rec.has("div") && !rec.getAsJsonPrimitive("div").isJsonNull()) {
                    String div = rec.getAsJsonPrimitive("div").getAsString();
                    div = div.replace("<div>", "");
                    div = div.replace("</div>", "");
                    div = StringEscapeUtils.escapeXml(div);
                    xhtmlNode.setValueAsString(div);
                    narrative.setDiv(xhtmlNode);
                }
                if (rec.has("status") && !rec.getAsJsonPrimitive("status").isJsonNull()) {
                    String statu = rec.getAsJsonPrimitive("status").getAsString();
                    narrative.setStatusAsString(statu);
                }
                sectionComponent.setText(narrative);
                List<Composition.SectionComponent> lsect = new LinkedList<>();
                lsect.add(sectionComponent);
                composition.setSection(lsect);
            }
        }

        if (jobject.has("author") && !jobject.getAsJsonArray("author").isJsonNull()) {
            JsonArray section = jobject.getAsJsonArray("author");
            if (((JsonObject) section.get(0)).has("display") && !(section.get(0)).getAsJsonObject().isJsonNull()) {
                JsonObject rec = section.get(0).getAsJsonObject();
                String display = rec.get("display").getAsString();

                Reference reference = new Reference();
                reference.setDisplay(display);
                List<Reference> auth = new LinkedList<>();
                auth.add(reference);
                composition.setAuthor(auth);
            }
        }

        String serialized = this.parser.encodeResourceToString(composition);
        logger.info(resource + " FHIR resource: " + serialized);
        // Sending to https://dev.domain.com/fhir/org1/Questionnaire/vprasalnik_id.... (USE PUT instead of POST) :)
        this.ResourceAddReuest(serialized, accesstoken, resource, identifier);

        if (errors.size() > 0) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("error", String.join(";", errors));
            this.SetRestResponse(false, r, exchange);
        } else {
            Map<String, Object> s = new LinkedHashMap<String, Object>();
            s.put("message", "Sucess! Sucess! Resource added.");
            s.put("resource_id", identifier);
            this.SetRestResponse(true, s, exchange);
        }
        logger.info("Sucess! " + resource + " FHIR resource added to FHIR server. ID: " + identifier);

        System.gc();

        //======================================== COMPOSITIONS ========================================
*/
    }
	
	 public void CreateAndSavePERSISTinPatient(Exchange exchange) {
        Message camelMessage = exchange.getIn();
        Map<String, Object> inpayload = (Map<String, Object>) camelMessage.getBody();

        //TODO: Create the FHIR object from JSON

        JSONObject json = new JSONObject(inpayload);
        JSONArray patientss = json.getJSONArray("patients");
        for (int j = 0; j < patientss.length(); ++j) {
            JSONObject inoutpatient = patientss.getJSONObject(j);
            JSONObject inpatient = (JSONObject) inoutpatient.get("inPatient");
            JSONObject patient = inpatient.getJSONObject("patient");
            CreateAndSavePERSISTPatient(exchange,patient);
            patient = null;
            System.gc();
            Runtime.getRuntime().gc();


            if(inpatient.has("procedures") && !inpatient.isNull("procedures")) {
                JSONArray procedures = inpatient.getJSONArray("procedures");
                for (int i = 0; i < procedures.length(); ++i) {
                    JSONObject rec = procedures.getJSONObject(i);
                    CreateAndSavePERSISTProcedure(exchange,rec);
                    rec = null;
                    System.gc();
                    Runtime.getRuntime().gc();
                }
                procedures = null;
                System.gc();
                Runtime.getRuntime().gc();
            }

            if(inpatient.has("medication") && !inpatient.isNull("medication")) {
                JSONArray medication = inpatient.getJSONArray("medication");
                for (int i = 0; i < medication.length(); ++i) {
                    JSONObject rec = medication.getJSONObject(i);
                    CreateAndSavePERSISTMedicationStatement(exchange,rec);
                    rec = null;
                    System.gc();
                    Runtime.getRuntime().gc();
                }
                medication = null;
                System.gc();
                Runtime.getRuntime().gc();
            }

            if(inpatient.has("encounters") && !inpatient.isNull("encounters")) {
                JSONArray encounters = inpatient.getJSONArray("encounters");
                for (int i = 0; i < encounters.length(); ++i) {
                    JSONObject rec = encounters.getJSONObject(i);
                    CreateAndSavePERSISTEncounter(exchange,rec);
                    rec = null;
                    System.gc();
                    Runtime.getRuntime().gc();
                }
                encounters = null;
                System.gc();
                Runtime.getRuntime().gc();
            }

            if(inpatient.has("observations") && !inpatient.isNull("observations")) {
                JSONArray observations = inpatient.getJSONArray("observations");
                for (int i = 0; i < observations.length(); ++i) {
                    JSONObject rec = observations.getJSONObject(i);
                    CreateAndSavePERSISTObservation(exchange,rec);
                    rec = null;
                    System.gc();
                    Runtime.getRuntime().gc();
                }
                observations = null;
                System.gc();
                Runtime.getRuntime().gc();
            }

            if(inpatient.has("diagnostic reports") && !inpatient.isNull("diagnostic reports")) {
                JSONArray diagnostic = inpatient.getJSONArray("diagnostic reports");
                for (int i = 0; i < diagnostic.length(); ++i) {
                    JSONObject rec = diagnostic.getJSONObject(i);
                    CreateAndSavePERSISTDiagnosticReport(exchange,rec);
                    rec = null;
                    System.gc();
                    Runtime.getRuntime().gc();
                }
                diagnostic = null;
                System.gc();
                Runtime.getRuntime().gc();
            }

            if(inpatient.has("conditions") && !inpatient.isNull("conditions")) {
                JSONArray conditions = inpatient.getJSONArray("conditions");
                for (int i = 0; i < conditions.length(); ++i) {
                    JSONObject rec = conditions.getJSONObject(i);
                    CreateAndSavePERSISTCondition(exchange,rec);
                    rec = null;
                    System.gc();
                    Runtime.getRuntime().gc();
                }
                conditions = null;
                System.gc();
                Runtime.getRuntime().gc();
            }

            if(inpatient.has("compositions") && !inpatient.isNull("compositions")) {
                JSONArray compositions = inpatient.getJSONArray("compositions");
                for (int i = 0; i < compositions.length(); ++i) {
                    JSONObject rec = compositions.getJSONObject(i);
                    CreateAndSavePERSISTComposition(exchange,rec);
                    rec = null;
                    System.gc();
                    Runtime.getRuntime().gc();
                }
                compositions = null;
                System.gc();
                Runtime.getRuntime().gc();
            }

            inpatient = null;
            System.gc();
            Runtime.getRuntime().gc();

            if(inoutpatient.has("outPatient") && !inoutpatient.isNull("outPatient")) {
                JSONObject outpatient = (JSONObject) inoutpatient.get("outPatient");


                if(outpatient.has("encounters") && !outpatient.isNull("encounters")) {
                    JSONArray encounterss = outpatient.getJSONArray("encounters");
                    for (int i = 0; i < encounterss.length(); ++i) {
                        JSONObject rec = encounterss.getJSONObject(i);
                        CreateAndSavePERSISTEncounter(exchange,rec);
                        rec = null;
                        System.gc();
                        Runtime.getRuntime().gc();
                    }
                    encounterss = null;
                    System.gc();
                    Runtime.getRuntime().gc();
                }

                if(outpatient.has("procedures") && !outpatient.isNull("procedures")) {
                    JSONArray proceduress = outpatient.getJSONArray("procedures");
                    for (int i = 0; i < proceduress.length(); ++i) {
                        JSONObject rec = proceduress.getJSONObject(i);
                        CreateAndSavePERSISTProcedure(exchange,rec);
                        rec = null;
                        System.gc();
                        Runtime.getRuntime().gc();
                    }
                    proceduress = null;
                    System.gc();
                    Runtime.getRuntime().gc();
                }

                if(outpatient.has("observations") && !outpatient.isNull("observations")) {
                    JSONArray observationss = outpatient.getJSONArray("observations");
                    for (int i = 0; i < observationss.length(); ++i) {
                        JSONObject rec = observationss.getJSONObject(i);
                        CreateAndSavePERSISTObservation(exchange,rec);
                        rec = null;
                        System.gc();
                        Runtime.getRuntime().gc();
                    }
                    observationss = null;
                    System.gc();
                    Runtime.getRuntime().gc();
                }

                if(outpatient.has("diagnostic reports") && !outpatient.isNull("diagnostic reports")) {
                    JSONArray diagnostics = outpatient.getJSONArray("diagnostic reports");
                    for (int i = 0; i < diagnostics.length(); ++i) {
                        JSONObject rec = diagnostics.getJSONObject(i);
                        CreateAndSavePERSISTDiagnosticReport(exchange,rec);
                        rec = null;
                        System.gc();
                        Runtime.getRuntime().gc();
                    }
                    diagnostics = null;
                    System.gc();
                    Runtime.getRuntime().gc();
                }

                if(outpatient.has("conditions") && !outpatient.isNull("conditions")) {
                    JSONArray conditionss = outpatient.getJSONArray("conditions");
                    for (int i = 0; i < conditionss.length(); ++i) {
                        JSONObject rec = conditionss.getJSONObject(i);
                        CreateAndSavePERSISTCondition(exchange,rec);
                        rec = null;
                        System.gc();
                        Runtime.getRuntime().gc();
                    }
                    conditionss = null;
                    System.gc();
                    Runtime.getRuntime().gc();
                }

                if(outpatient.has("medication") && !outpatient.isNull("medication")) {
                    JSONArray medications = outpatient.getJSONArray("medication");
                    for (int i = 0; i < medications.length(); ++i) {
                        JSONObject rec = medications.getJSONObject(i);
                        CreateAndSavePERSISTMedicationStatement(exchange,rec);
                        rec = null;
                        System.gc();
                        Runtime.getRuntime().gc();
                    }
                    medications = null;
                    System.gc();
                    Runtime.getRuntime().gc();
                }

                if(outpatient.has("compositions") && !outpatient.isNull("compositions")) {
                    JSONArray compositionss = outpatient.getJSONArray("compositions");
                    for (int i = 0; i < compositionss.length(); ++i) {
                        JSONObject rec = compositionss.getJSONObject(i);
                        CreateAndSavePERSISTComposition(exchange,rec);
                        rec = null;
                        System.gc();
                        Runtime.getRuntime().gc();
                    }
                    compositionss = null;
                    System.gc();
                    Runtime.getRuntime().gc();
                }

                outpatient = null;
                System.gc();
                Runtime.getRuntime().gc();
            }

            inoutpatient = null;
            System.gc();
            Runtime.getRuntime().gc();
        }

        json = null;
        patientss = null;
        camelMessage = null;
        inpayload = null;
        System.gc();
        Runtime.getRuntime().gc();

    }


}
