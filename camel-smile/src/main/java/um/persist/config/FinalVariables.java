package um.persist.config;

public interface FinalVariables {
    
    // 0.0.0.0 = 164.8.22.204 --> It's the mapping of the current IP that this machine has.

    final String rest_api_key = "api-key";
    final String hosmartai_api_version = "DEV-2022.5.1";
    final String kafka_broker = "x.x.x.x:9092"; // Not accessible outside UM internal network!
    final String local_asr_flask_endpoint = "http://0.0.0.0:5000/asr";
    final String local_tts_flask_endpoint = "http://0.0.0.0:5000/tts";
    final String sgg_eva_endpoint = "http://0.0.0.0:5001/sgg_eva";
    final String sgg_eva_endpointv2 = "http://0.0.0.0:5001/sgg_eva_new";
    final String sgg_endpoint = "http://0.0.0.0:5001/sgg";
    final String sgg_endpointv2 = "http://0.0.0.0:5001/sgg_new";
    final String dr_endpoint = "http://0.0.0.0:5002/dr";
    final String transform_endpoint = "http://0.0.0.0:5010/transform";
    final String xml_endpoint = "http://0.0.0.0:5010/xml";
    final String rasa_endpoint = "http://0.0.0.0:5005/webhooks/rest/webhook";
    final String pepper_workstation = "x.x.x.x";
    final String gestures_filePath = "/home/user/hosmartaigestures/gestures.csv";
    final String text_topic = "text4tts_";
    final String audio_topic = "tts4fa_";
}
