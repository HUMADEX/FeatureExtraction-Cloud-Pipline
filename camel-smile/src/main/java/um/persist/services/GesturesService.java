package um.persist.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import um.persist.config.FinalVariables;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class GesturesService implements FinalVariables {

    String input_filename = "gesture";
    String strip_input_filename = "gesture";

    public void Gestures(Exchange exchange)
            throws IOException, MessagingException, JSchException, SftpException, InterruptedException {

        Message camelMessage = exchange.getIn();

        ObjectMapper mapObject = new ObjectMapper();
        // Map<String, Object> mapObj = mapObject.convertValue(camelMessage.getBody(),
        // Map.class);

        Map<String, Object> mapErrorKey = new LinkedHashMap<>();
        mapErrorKey.put("error", "invalid_api_key");
        mapErrorKey.put("error_description", "API Key verification failed. Please provide the correct API Key");
        String error_msg = mapObject.writeValueAsString(mapErrorKey);

        String api_key = String.valueOf(camelMessage.getHeader("Authorization"));
        System.out.println("API KEY: " + api_key);

        if (api_key.equals(rest_api_key)) {

            // String text = mapObj.get("text").toString();

            ObjectMapper objectMapper = new ObjectMapper();

            try {
                // String json2 = objectMapper.writeValueAsString(mapObj);
                String json2;
                // System.out.println(json);

                // whenUploadFileUsingJsch_thenSuccess();

                // Map<String, Map<String, Object>> out = new HashMap<>();
                // out.put("sent_content", mapObj);
                String gesture_model = (String) camelMessage.getHeader("gesture_model");

                InputStream is = exchange.getIn().getBody(InputStream.class);
                // byte[] bytes = exchange.getIn().getBody(byte[].class);
                MimeBodyPart mimeMessage = new MimeBodyPart(is);
                DataHandler dh = mimeMessage.getDataHandler();
                exchange.getIn().setBody(dh.getInputStream());
                exchange.getIn().setHeader(Exchange.FILE_NAME, dh.getName());
                input_filename = dh.getName();
                strip_input_filename = input_filename.split("\\.", 2)[0];

                File targetFile = new File("/home/user/hosmartai-api/gestures.wav");
                // File targetFile = new File("/home/user/hosmartai-api/" + dh.getName());

                FileUtils.copyInputStreamToFile(dh.getInputStream(), targetFile);
                

                System.out.println("File " + dh.getName() + " uploaded!");

                LinkedHashMap<String, Object> response = new LinkedHashMap<>();
                response.put("status", "File " + dh.getName() + " uploaded!");
                String json = mapObject.writeValueAsString(response);

                whenUploadFileUsingJsch_thenSuccess("0.0.0.0", "karlo", "CKEDS82!");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                // headers.set("Authorization", "test123");
                HttpEntity<?> entity = new HttpEntity<>(json, headers);

                String authUri3 = sgg_endpoint;
                RestTemplate restTemplate3 = new RestTemplate();
                if (gesture_model.equals("gestures")) {
                    authUri3 = sgg_endpoint;
                }
                if (gesture_model.equals("gesturesv2")) {
                    authUri3 = sgg_endpointv2;
                }
                if (gesture_model.equals("evagestures") || gesture_model.equals("csvevagestures")
                        || gesture_model.equals("bvhevagestures")) {
                    authUri3 = sgg_eva_endpoint;
                }
                if (gesture_model.equals("evagesturesv2") || gesture_model.equals("csvevagesturesv2")
                        || gesture_model.equals("bvhevagestures")) {
                    authUri3 = sgg_eva_endpointv2;
                }

                ResponseEntity<Object> res3 = restTemplate3.exchange(authUri3, HttpMethod.POST, entity, Object.class);
                if (res3.getStatusCodeValue() == 200) {
                    System.out.println("Gestures retrieved from the SGG engine.");

                    Map<String, Object> mapObj = new LinkedHashMap<>();
                    String csv_string = "";
                    String bvh_string = "";

                    if (gesture_model.equals("gestures") || gesture_model.equals("gesturesv2")) {
                        json2 = res3.getBody().toString();
                        Map<String, String> out = new HashMap<>();
                        out.put("csv_string",json2);
                        // out.put("csv_string", readAllBytes(gestures_filePath));
                        String json_out = mapObject.writeValueAsString(out);
                        exchange.getOut().setBody(json_out);
                        exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
                    }

                    if (gesture_model.equals("evagestures") || gesture_model.equals("evagesturesv2")) {
                        mapObj = mapObject.convertValue(res3.getBody(), Map.class);
                        System.out.println("Gestures retrieved from the SGG engine: " + mapObj);
                        csv_string = mapObj.get("csv").toString();
                        bvh_string = mapObj.get("bvh").toString();

                        // String csv_string = (String) res3.getBody();
                        response.put("csv_string", csv_string);
                        response.put("bvh_string", bvh_string);
                        json2 = mapObject.writeValueAsString(response);
                        // create csv file
                        File targetFileCSV = new File("/home/user/persist-eva/deep-models/gestures.csv");
                        FileUtils.writeStringToFile(targetFileCSV, csv_string, "UTF-8");

                        // create bvh file
                        //File targetFileBVH = new File("/home/matejr/persist-eva/deep-models/gestures.bvh");
                        //FileUtils.writeStringToFile(targetFileBVH, bvh_string, "UTF-8");

                        LinkedHashMap<String, Object> request = new LinkedHashMap<>();
                        request.put("inputfile", "gestures.csv");
                        request.put("gesture", strip_input_filename);

                        String json_request = mapObject.writeValueAsString(request);
                        System.out.println("Sending to x.x.x.x:5010/transform " + json_request);

                        HttpHeaders headers2 = new HttpHeaders();
                        headers2.setContentType(MediaType.APPLICATION_JSON);
                        // headers.set("Authorization", "test123");
                        HttpEntity<?> entity2 = new HttpEntity<>(json_request, headers2);

                        RestTemplate restTemplate4 = new RestTemplate();
                        String authUri4 = transform_endpoint;
                        ResponseEntity<Object> res4 = restTemplate4.exchange(authUri4,
                                HttpMethod.POST, entity2, Object.class);
                        if (res4.getStatusCodeValue() == 200) {
                            System.out.println("Transform from CSV to XML done.");

                            // this uncommment
                            File file_xml = new File(
                                    "/home/user/persist-eva/deep-models/converted/" + strip_input_filename + ".xml");

                            //exchange.getOut().setBody(file_xml);
                            //exchange.getOut().setHeader(Exchange.CONTENT_TYPE,
                            //MediaType.APPLICATION_OCTET_STREAM);
                            //exchange.getOut().setHeader("Content-Disposition","attachment; filename=\"" +
                            //strip_input_filename + ".xml\"");
                            //exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
                            // this end

                            exchange.getOut().setBody(json2);
                            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

                        } else {
                            response.put("status",
                                    "Error. Problem is related to the transformation of CSV to XML - x.x.x.x in eva-helper-scripts.py file.");
                            response.put("error", res4.getBody());
                            json2 = mapObject.writeValueAsString(response);

                            exchange.getOut().setBody(json2);
                            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
                        }
                        // this end

                        /*
                         * executeCommand();
                         * 
                         * try {
                         * Process process =
                         * Runtime.getRuntime().exec("sh /home/matejr/persist-eva/run_transform.sh "
                         * +strip_input_filename);
                         * }
                         * catch (IOException e){e.printStackTrace();}
                         * 
                         * ProcessBuilder processBuilder = new ProcessBuilder("python3",
                         * "/home/matejr/persist-eva/transform_karlo_e2e.py","-i "+targetFileCSV,"-o "
                         * +strip_input_filename);
                         * processBuilder.redirectErrorStream(true);
                         * 
                         * Process process = processBuilder.start();
                         * 
                         * Process process;
                         * process = Runtime.getRuntime().exec(new
                         * String[]{"transform_karlo_e2e.py","-i "+targetFileCSV,"-o "
                         * +strip_input_filename}, null, new File("/home/matejr/persist-eva/"));
                         */

                        /*
                         * Process process = Runtime.getRuntime()
                         * .exec("python transform_karlo_e2e.py -i "+targetFileCSV+" -o "
                         * +strip_input_filename, null, new File("/home/matejr/persist-eva"));
                         * //.exec("sh -c ls", null, new File("/home/matejr/persist-eva")); for
                         * non-Windows users
                         * printResults(process);
                         */
                        // Process process = Runtime.getRuntime().exec("cd /home/matejr/persist-eva/;
                        // transform_karlo_e2e.py -i "+targetFileCSV+" -o "+strip_input_filename);
                        // Runtime rt = Runtime.getRuntime();
                        // Process pr = rt.exec("cd /home/matejr/persist-eva/; transform_karlo_e2e.py -i
                        // "+targetFileCSV+" -o "+strip_input_filename);
                    }

                    if (gesture_model.equals("csvevagestures") || gesture_model.equals("csvevagesturesv2")) {
                        mapObj = mapObject.convertValue(res3.getBody(), Map.class);
                        System.out.println("Gestures retrieved from the SGG engine: " + mapObj);
                        csv_string = mapObj.get("csv").toString();
                        bvh_string = mapObj.get("bvh").toString();

                        // String csv_string = (String) res3.getBody();
                        response.put("csv_string", csv_string);
                        response.put("bvh_string", bvh_string);
                        json2 = mapObject.writeValueAsString(response);
                        // this uncomment
                        File targetFileCSV = new File("/home/user/persist-eva/deep-models/gestures.csv");
                        FileUtils.writeStringToFile(targetFileCSV, csv_string, "UTF-8");

                        exchange.getOut().setBody(targetFileCSV);
                        exchange.getOut().setHeader(Exchange.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
                        exchange.getOut().setHeader("Content-Disposition",
                                "attachment; filename=\"" + strip_input_filename + ".csv\"");
                        exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
                        // this end

                    }

                    if (gesture_model.equals("bvhevagestures") || gesture_model.equals("bvhevagesturesv2")) {
                        mapObj = mapObject.convertValue(res3.getBody(), Map.class);
                        System.out.println("Gestures retrieved from the SGG engine: " + mapObj);
                        csv_string = mapObj.get("csv").toString();
                        bvh_string = mapObj.get("bvh").toString();

                        // String csv_string = (String) res3.getBody();
                        response.put("csv_string", csv_string);
                        response.put("bvh_string", bvh_string);
                        json2 = mapObject.writeValueAsString(response);
                        // this uncomment
                        File targetFileBVH = new File("/home/user/persist-eva/deep-models/gestures.bvh");
                        FileUtils.writeStringToFile(targetFileBVH, bvh_string, "UTF-8");

                        exchange.getOut().setBody(targetFileBVH);
                        exchange.getOut().setHeader(Exchange.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
                        exchange.getOut().setHeader("Content-Disposition",
                                "attachment; filename=\"" + strip_input_filename + ".bvh\"");
                        exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
                        // this end

                    }

                } else {
                    response.put("status", "Error. Problem is related to the Gestures SGG endpoint.");
                    response.put("error", res3.getBody());
                    json2 = mapObject.writeValueAsString(response);

                    exchange.getOut().setBody(json2);
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
                }

                // Map<String, String> out = new HashMap<>();
                // out.put("csv_string",
                // ",LElbowRoll,LElbowYaw,LShoulderPitch,LShoulderRoll,RElbowRoll,RElbowYaw,RShoulderPitch,RShoulderRoll"+"\n"+
                // "00:00:00,-1.32503520657644,-0.71607998275713,1.535154068371866,0.5976866541896615,1.4082178778846934,0.716697931710563,1.3672653832728388,-0.5187229798360451");
                // out.put("csv_string", readAllBytes(gestures_filePath));
                // String json_out = mapObject.writeValueAsString(out);

                // exchange.getOut().setBody(json2);
                // exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                // exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

                

            } catch (JsonProcessingException jsonProcessingException) {
                jsonProcessingException.printStackTrace();
            }

        } else {
            exchange.getOut().setBody(error_msg);
            exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
        }

    }

    public static void printResults(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }

    private static String readAllBytes(String filePath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    private ChannelSftp setupJsch(String remoteHost, String username, String password) throws JSchException {
        JSch jsch = new JSch();
        // jsch.setKnownHosts("/home/matejr/.ssh/known_hosts");
        Session jschSession = jsch.getSession(username, remoteHost);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        jschSession.setConfig(config);
        jschSession.setPassword(password);
        jschSession.connect();
        return (ChannelSftp) jschSession.openChannel("sftp");
    }

    @Test
    public void whenUploadFileUsingJsch_thenSuccess(String remoteHost, String username, String password)
            throws JSchException, SftpException {
        ChannelSftp channelSftp = setupJsch(remoteHost, username, password);
        channelSftp.connect();

        String localFile = "/home/user/hosmartai-api/gestures.wav";
        // String remoteDir = "/var/www/html/hosmartai/";

        // if(username.equals("matejr")){
        // channelSftp.put(localFile, remoteDir + "rasaaudio.wav");
        // }
        if (username.equals("karlo")) {
            channelSftp.put(localFile, "/home/karlo/Documents/rasaaudio.wav");
        }

        File file = new File("/home/dsplab/hosmartaiaudio/rasaaudio.wav");
        file.delete();

        channelSftp.exit();
    }

    private String remoteHost = "x.x.x.x";
    private String username = "username";
    private String password = "password";

    private void executeCommand() throws JSchException, IOException, InterruptedException {
        JSch jsch = new JSch();
        // jsch.setKnownHosts("/home/user/.ssh/known_hosts");
        Session jschSession = jsch.getSession(username, remoteHost);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        jschSession.setConfig(config);
        jschSession.setPassword(password);
        jschSession.connect();

        ChannelExec ce = (ChannelExec) jschSession.openChannel("exec");
        ce.setCommand("cd /home/user/persist-eva; sh run_transform.sh test");
        // python3 /home/user/persist-eva/transform_karlo_e2e.py -i gestures.csv -o
        // "+strip_input_filename

        InputStream in = ce.getInputStream();
        ce.connect();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                System.out.print(new String(tmp, 0, i));
            }
            if (ce.isClosed()) {
                break;
            }
            Thread.sleep(1000);

        }

        ce.disconnect();
        jschSession.disconnect();
    }

}
