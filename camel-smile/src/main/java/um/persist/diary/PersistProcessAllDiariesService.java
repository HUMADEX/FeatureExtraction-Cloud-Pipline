package um.persist.diary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.protocol.types.Field.Bool;
import org.bouncycastle.pqc.jcajce.provider.qtesla.SignatureSpi.qTESLA;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import java.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import io.sentry.Sentry;
import um.persist.chatbot.SymptomaQuestionnaire;
import um.persist.swagger_examples.symptoma.*;

import com.mashape.unirest.http.HttpResponse;
import com.google.common.reflect.TypeToken;

public class PersistProcessAllDiariesService {

    //public String OHC_endpoint_diary = "https://domain.com/internal-fhir/persist/DiagnosticReport?code=diary";
    public String OHC_endpoint_diary = "https://domain.com/internal-fhir/project/DiagnosticReport?code=diary&_count=10000";
    ResponseEntity<Object> response2;
    ResponseEntity<byte[]> response3;
    String reference_patient = null;

    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    String language = null;

    int tester = 1;

    List<String> processed_diaries = new LinkedList<String>();

    public PersistProcessAllDiariesService() {

    }

    public String PERSISTOHCLogin() throws Exception {
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest
                .post("https://domain.com/auth/realms/project/protocol/openid-connect/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .field("grant_type", "password")
                .field("username", "username")
                .field("password", "password")
                .field("client_id", "id")
                .asString();

        String body = response.getBody();

        Map<String, Object> retMap = new Gson().fromJson(
                body, new TypeToken<HashMap<String, Object>>() {
                }.getType());

        String token = retMap.get("access_token").toString();
        // Unirest.shutdown();
        return token;
    }

    public void SetRestResponse(boolean sucess, Map<String, Object> Message, Exchange exchange) {
        exchange.getIn().setBody(Message);
        exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");

        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, (sucess) ? 200 : 400);
    }

    private void ResourceAddReuest(String token) {
        HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + token);
        headers.add("Accept", "*/*");
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        String Uri = this.OHC_endpoint_diary; // uri to service which you get the token from
        System.out.println("Sending to OHC: GET " + Uri);
        response2 = restTemplate.exchange(Uri, HttpMethod.GET, entity,
                Object.class); // TODO: SWAGER does not show us what is the structure of response

        // return response2;
    }

    private void DownloadVideo(String video_id, String reference_patient) {
        Integer count1 = 0;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "api-key");
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        String Uri = "http://x.x.x.x:8080/api/FHIR/downloadDiaryVideo/" + video_id; // uri to service which you get
                                                                                         // the token from
        System.out.println("GET Download video: " + Uri);
        // try {
        response3 = restTemplate.exchange(Uri, HttpMethod.GET, entity,
                byte[].class); // TODO: SWAGER does not show us what is the structure of response
        // }
        // catch(Exception e) {
        // System.out.println(++count1 + " diary video failed with ID: " + video_id);
        // }

        if (response3.getStatusCode().is2xxSuccessful()) {
            System.out.println(++count1 + " diary video complete with ID: " + video_id);
        } else {
            System.out.println(++count1 + " diary video failed with ID: " + video_id);
        }

        try {
            Files.write(Paths.get("/home/user/camel/patient_videos/" + video_id + ".mp4"), response3.getBody());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // return response2;
    }

    private void VideoProcessing(String video_id, String reference_patient) {
        Integer count1 = 0;
        HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        // headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "api-key");
        headers.add("reference", video_id);
        headers.add("reference_patient", reference_patient);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // SENDING FILE TO REST API ENDPOINT
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
       
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Paths.get("/home/user/camel/patient_videos/" + video_id + ".mp4"));
            body.add("file", bytes);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String tmp = reference_patient.toLowerCase();
        System.out.println("PATIENT ID: "+tmp);
        Pattern pattern_ukcm = Pattern.compile("ukcm", Pattern.CASE_INSENSITIVE);
        Matcher matcher_ukcm = pattern_ukcm.matcher(tmp);
        boolean matchFound_ukcm = matcher_ukcm.find();
        if (matchFound_ukcm) {
            language = "sl";
            // System.out.println("Match found");
        }
        Pattern pattern_sergas = Pattern.compile("sergas", Pattern.CASE_INSENSITIVE);
        Matcher matcher_sergas = pattern_sergas.matcher(tmp);
        boolean matchFound_sergas = matcher_sergas.find();
        if (matchFound_sergas) {
            language = "es";
            // System.out.println("Match found");
        }
        Pattern pattern_ul = Pattern.compile("ul", Pattern.CASE_INSENSITIVE);
        Matcher matcher_ul = pattern_ul.matcher(tmp);
        boolean matchFound_ul = matcher_ul.find();
        if (matchFound_ul) {
            language = "lv";
            // System.out.println("Match found");
        }
        Pattern pattern_liege = Pattern.compile("liege", Pattern.CASE_INSENSITIVE);
        Matcher matcher_liege = pattern_liege.matcher(tmp);
        boolean matchFound_liege = matcher_liege.find();
        if (matchFound_liege) {
            language = "fr";
            // System.out.println("Match found");
        }

        RestTemplate restTemplate = new RestTemplate();
        String Uri = "http://x.x.x.x/api/dev/umut/" + language; // uri to service which you get the token from
        System.out.println("Processing Download video: " + Uri);
        // try {
        // response2 = restTemplate.exchange(Uri, HttpMethod.POST, entity,
        // Object.class); // TODO: SWAGER does not show us what is the structure of
        // response
        ResponseEntity<Object> response1 = restTemplate.postForEntity(Uri, requestEntity, Object.class);
        // }
        // catch(Exception e) {
        // System.out.println(++count1 + " diary video failed with ID: " + video_id);
        // }

        if (response1.getStatusCode().is2xxSuccessful()) {
            System.out.println(++count1 + " diary video processing complete with ID: " + video_id);
            processed_diaries.add(video_id);
        } else {
            System.out.println(++count1 + " diary video processing failed with ID: " + video_id);
        }

        
    }

    public void ProcessAllVideos(Exchange exchange) {

        long startTime = System.nanoTime();

        Message camelMessage = exchange.getIn();
        // String video_id = (String) camelMessage.getHeader("id");

        System.out.println(camelMessage);
        // String dummy_json = "{\"sender\":\"dd\",\"message\":\"hey\"}";

        ObjectMapper mapObject = new ObjectMapper();

        Map<String, Object> mapErrorKey = new LinkedHashMap<>();
        mapErrorKey.put("error", "invalid_api_key");
        mapErrorKey.put("error_description", "API Key verification failed. Please provide the correct API Key");

        String api_key = String.valueOf(camelMessage.getHeader("Authorization"));
        System.out.println("API KEY: " + api_key);

        if (api_key.equals("api-key")) {

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

            // String video_id = String.valueOf(mapObj.get("video_id"));

            this.ResourceAddReuest(accesstoken);
            // File file = new File("/home/matejr/camel/depression.mp4");

            Map<String, Object> mapObj = mapObject.convertValue(response2.getBody(), Map.class);
            JSONObject json = new JSONObject(mapObj);

            // Boolean video_flag = false;
            Integer counting = 0;
            Hashtable<String, String> videos = new Hashtable<>();
            if (json.has("entry") && !json.isNull("entry")) {
                JSONArray entry = json.getJSONArray("entry");
                for (int i = 0; i < entry.length(); ++i) {
                    JSONObject dic = entry.getJSONObject(i);
                    if (dic.has("resource") && !dic.isNull("resource")) {
                        JSONObject res = dic.getJSONObject("resource");
                        if (res.has("subject") && !res.isNull("subject")) {
                            JSONObject subject = res.getJSONObject("subject");
                            reference_patient = subject.getString("reference");
                        }
                        if (res.has("image") && !res.isNull("image")) {
                            JSONArray image = res.getJSONArray("image");
                            for (int j = 1; j < image.length(); ++j) {
                                JSONObject dic2 = image.getJSONObject(j);
                                // if (dic2.has("comment") && !dic2.isNull("comment")) {
                                // video_flag = true;
                                // dic2 = image.getJSONObject(++j);
                                if (dic2.has("link") && !dic2.isNull("link")) {
                                    JSONObject link = dic2.getJSONObject("link");
                                    String reference = link.getString("reference");
                                    try {
                                        System.out.println(" test1-predb");
                                        //if (!videos.contains(reference)) {
                                        if (tester <= 1) {
                                            tester = tester + 1;
                                            System.out.println("DIARY VIDEO NUMBER: " + tester);
                                        if (readDataBase(reference).equals(false)) {
                                            System.out.println(" test2-podb");
                                            System.out.println(++counting + " diary to process video ID: " + reference);
                                            long startTime1 = System.nanoTime();
                                            DownloadVideo(reference, reference_patient);
                                            long stopTime1 = System.nanoTime();
                                            System.out.println("==========================================================");
                                            System.out.println("Endpoint /umut DownloadVideo running time: " + ((stopTime1/1000000000) - (startTime1/1000000000)) + " s");
                                            System.out.println("==========================================================");
                                            long startTime2 = System.nanoTime();
                                            VideoProcessing(reference, reference_patient);
                                            long stopTime2 = System.nanoTime();
                                            System.out.println("==========================================================");
                                            System.out.println("Endpoint /umut VideoProcessing running time: " + ((stopTime2/1000000000) - (startTime2/1000000000)) + " s = " + (((stopTime2/1000000000)/60) - ((startTime2/1000000000)/60)) + " min");
                                            System.out.println("==========================================================");
                                            videos.put(reference, reference_patient);
                                            writeResult(reference);
                                        } else {
                                            System.out.println(
                                                    ++counting + " diary already processed with video ID: "
                                                            + reference);
                                        }
                                    }
                                    } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                        System.out.println(" test3-exception");
                                    }

                                }
                                // }
                                /*
                                 * if (video_flag) {
                                 * video_flag = false;
                                 * if (dic2.has("link") && !dic2.isNull("link")) {
                                 * JSONObject link = dic2.getJSONObject("link");
                                 * reference = link.getString("reference");
                                 * }
                                 * }
                                 */
                            }
                        }
                    }

                }
            }
            System.out.println("List of diary videos: " + videos.toString());
            long stopTime = System.nanoTime();
            System.out.println("==========================================================");
            System.out.println("Endpoint /umut total running time: " + ((stopTime/1000000000) - (startTime/1000000000)) + " s = " + (((stopTime/1000000000)/60) - ((startTime/1000000000)/60)) + " min");
            System.out.println("==========================================================");

            // return JSON
            exchange.getOut().setBody(videos);
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

            // return File
            /*
             * exchange.getOut().setBody(file);
             * exchange.getOut().setHeader(Exchange.CONTENT_TYPE,
             * MediaType.APPLICATION_OCTET_STREAM);
             * exchange.getOut().setHeader("Content-Type", "video/mp4");
             * exchange.getOut().setHeader("Transfer-Encoding", "chunked");
             * exchange.getOut().setHeader("Content-Disposition", "attachment; filename=\""
             * + "depression" + ".mp4\"");
             * //exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
             * exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
             */

        }

    }

    public Boolean readDataBase(String video_id) throws Exception {
        Connection connect;
        Properties properties = new Properties();
        properties.setProperty("user", "username");
        properties.setProperty("password", "password");
        properties.setProperty("useSSL", "false");
        try {
            // This will load the MySQL driver, each DB has its own driver
            try {
                System.out.println(" test2-1-prereddb");
                //Class.forName("com.mysql.jdbc.Driver");  
                //Class.forName("com.mysql.cj.jdbc.Driver");
                // Setup the connection with the DB
                connect = DriverManager.getConnection("jdbc:mysql://x.x.x.x:3306/persist", properties);
                System.out.println(" test2-2poreaddb");
            } catch (SQLException e) {
                throw new IllegalStateException("Cannot connect the database!", e);
            }
            /*
             * // Statements allow to issue SQL queries to the database
             * statement = connect.createStatement();
             * // Result set get the result of the SQL query
             * resultSet = statement
             * .executeQuery("select * from persist.diaries");
             * writeResultSet(resultSet);
             */
            System.out.println(" test2-3poreaddb");
            statement = connect.createStatement();
            // Result set get the result of the SQL query
            resultSet = statement
                    .executeQuery("select diary_id from persist.diaries where diary_id='" + video_id + "';");
            // writeResultSet(resultSet);
            if (resultSet.next()) {
                // yes exist
                return true;
            } else {
                return false;
            }

            /*
             * // PreparedStatements can use variables and are more efficient
             * preparedStatement = connect
             * .prepareStatement("insert into  persist.diaries values (default, ?)");
             * // "myuser, webpage, datum, summary, COMMENTS from feedback.comments");
             * // Parameters start with 1
             * preparedStatement.setString(1,"Test");
             * preparedStatement.executeUpdate();
             * 
             * preparedStatement = connect
             * .prepareStatement("SELECT video_id from persist.diaries");
             * resultSet = preparedStatement.executeQuery();
             * writeResultSet(resultSet);
             * 
             * // Remove again the insert comment
             * preparedStatement = connect
             * .prepareStatement("delete from persist.diaries where video_id= ? ; ");
             * preparedStatement.setString(1, "Test");
             * preparedStatement.executeUpdate();
             * 
             * resultSet = statement
             * .executeQuery("select * from persist.diaries");
             * writeMetaData(resultSet);
             */

        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }

    }

    /*
     * private void writeMetaData(ResultSet resultSet) throws SQLException {
     * // Now get some metadata from the database
     * // Result set get the result of the SQL query
     * 
     * System.out.println("The columns in the table are: ");
     * 
     * System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
     * for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
     * System.out.println("Column " + i + " " +
     * resultSet.getMetaData().getColumnName(i));
     * }
     * }
     */

    /*
     * private void writeResultSet(ResultSet resultSet) throws SQLException {
     * // ResultSet is initially before the first data set
     * while (resultSet.next()) {
     * // It is possible to get the columns via name
     * // also possible to get the columns via the column number
     * // which starts at 1
     * // e.g. resultSet.getSTring(2);
     * String video_id = resultSet.getString("video_id");
     * System.out.println("video_id: " + video_id);
     * }
     * }
     */

    private void writeResult(String video_id) throws SQLException {
        Connection connect;
        Properties properties = new Properties();
        properties.setProperty("user", "username");
        properties.setProperty("password", "password");
        properties.setProperty("useSSL", "false");
        try {
            try {
                System.out.println(" test3-1prewritedb");
                connect = DriverManager.getConnection("jdbc:mysql://x.x.x.x:3306/persist", properties);
                    // use con here
            } catch (SQLException e) {
                throw new IllegalStateException("Cannot connect the database!", e);
            }
            // ResultSet is initially before the first data set
            /*
             * while (resultSet.next()) {
             * // It is possible to get the columns via name
             * // also possible to get the columns via the column number
             * // which starts at 1
             * // e.g. resultSet.getSTring(2);
             * String video_id = resultSet.getString("video_id");
             * System.out.println("video_id: " + video_id);
             * }
             */
            // PreparedStatements can use variables and are more efficient
            preparedStatement = connect
                    .prepareStatement("insert into persist.diaries (diary_id) values (?)");
            // "myuser, webpage, datum, summary, COMMENTS from feedback.comments");
            // Parameters start with 1
            preparedStatement.setString(1, video_id);
            preparedStatement.executeUpdate();
            System.out.println(" test3-2powritedb");

        } catch (Exception e) {
            try {
                throw e;
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } finally {
            close();
        }
    }

    // You need to close the resultSet
    private void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connect != null) {
                connect.close();
            }
        } catch (Exception e) {

        }
    }

}
