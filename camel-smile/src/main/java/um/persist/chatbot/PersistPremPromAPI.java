package um.persist.chatbot;

import java.util.LinkedList;
//import javafx.util;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * @author UM FERI
 * @date JAN 2021
 * @description Chatbot PREM/PROM available lists of questionnaires for all languages
 *
 */

@Service("persistPremPromAPI")

public class PersistPremPromAPI
{
	
	public PersistPremPromAPI()  { }
	
	
    private String language_code ="";
	private String persist_oid = "";
	private String chatbot_id = "";
	private String questionary_title = "";
	
	 public String getLanguageCode() 
	 {
	        return language_code;
	 }

	public void setLanguageCode(String language_code) 
	{
	        this.language_code = language_code;
	}
	
	public String getPersistOid() 
    {
	        return persist_oid;
	}

	public void setPersistOid(String persist_oid) 
	{
	        this.persist_oid = persist_oid;
	}
	
	public String getChatbotId() 
    {
	        return chatbot_id;
	}

	public void setChatbotId(String chatbot_id) 
	{
	        this.chatbot_id = chatbot_id;
	}
	
	public void setQuestionaryTitle(String questionary_title) 
	{
	        this.questionary_title = questionary_title;
	}
	
	public String getQuestionaryTitle() 
    {
	        return questionary_title;
	}

	public LinkedList<PersistPremPromAPI> LangEnglish()
	{
		LinkedList<PersistPremPromAPI> q =  new LinkedList<PersistPremPromAPI>();
		//PersistPremPromAPI pq = new PersistPremPromAPI();
		PersistPremPromManual pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("general_anxiety_disorder_en");
		pq2.setPersistOid("oid1_en");
		pq2.setQuestionaryTitle("General Anxiety Disorder, 7");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
	    pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("patient_health_questionnaire_two_en");
		pq2.setPersistOid("oid2_en");
		pq2.setQuestionaryTitle("Patient Health Questionnaire, 2");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		/*pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("patient_health_quality_questionnaire_en");
		pq2.setPersistOid("oid3_en");
		pq2.setQuestionaryTitle("Patient Health Questionnaire, 9");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("quality_of_life_c30_en");
		pq2.setPersistOid("oid4_en");
		pq2.setQuestionaryTitle("Quality of Life of Cancer Patients, 30");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);*/
		
		
		/*pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("insomnia_severity_index_en");
		pq2.setPersistOid("oid5");
		pq2.setQuestionaryTitle("Insomnia Sleep Questionnaire");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);*/
		
		
		/*pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("general_self_efficacy_scale_en");
		pq2.setPersistOid("oid5_en");
		pq2.setQuestionaryTitle("General Self-Efficacy Scale");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);*/
		
		
		/*pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("world_health_organization_disability_assessment_en");
		pq2.setPersistOid("oid6");
		pq2.setQuestionaryTitle("World health organiation disability assesment schedule 2.0");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);*/
		
		
		//==========================================================================
		/*
		
		//PersistPremPromManual pq22 = new PersistPremPromManual();
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("colorectal_cancer_interval_en");
		pq2.setPersistOid("oid6_en");
		pq2.setQuestionaryTitle("Colorectal cancer interval");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("breast_cancer_interval_en");
		pq2.setPersistOid("oid7_en");
		pq2.setQuestionaryTitle("Breast cancer recurrence or new primary breast cancer");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("Lymphedema_ques_en");
		pq2.setPersistOid("oid8_en");
		pq2.setQuestionaryTitle("Lymphedema");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("pain_en");
		pq2.setPersistOid("oid9_en");
		pq2.setQuestionaryTitle("Pain");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("Hormonal_disbalances_en");
		pq2.setPersistOid("oid10_en");
		pq2.setQuestionaryTitle("Hormonal disbalances");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("Sexual_dsyfunction_ques_en");
		pq2.setPersistOid("oid11_en");
		pq2.setQuestionaryTitle("Sexual dysfunction");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("fatigue_ques_en");
		pq2.setPersistOid("oid12_en");
		pq2.setQuestionaryTitle("Fatigue");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("cognitive_function_ques_en");
		pq2.setPersistOid("oid13_en");
		pq2.setQuestionaryTitle("Cognitive function");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("Inadequate_nutrition_en");
		pq2.setPersistOid("oid14_en");
		pq2.setQuestionaryTitle("Malnutrition");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("Gastrointestinal_conditions_ques_en");
		pq2.setPersistOid("oid15_en");
		pq2.setQuestionaryTitle("Gastrointestinal conditions");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("gpaq_ques_en");
		pq2.setPersistOid("oid16_en");
		pq2.setQuestionaryTitle("Global Physical Activity Questionnaire");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("en");
		pq2.setChatbotId("Cardio_vacularrisk_en");
		pq2.setPersistOid("oid17_en");
		pq2.setQuestionaryTitle("Cardiovascular risk");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		return q;
				
	}
	
	public LinkedList<PersistPremPromAPI> LangSlovenian()
	{
		LinkedList<PersistPremPromAPI> q =  new LinkedList<PersistPremPromAPI>();
		//PersistPremPromAPI pq = new PersistPremPromAPI();
		PersistPremPromManual pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("vprašalnik_o_zdravju_pacientov_devet_si");
		pq2.setPersistOid("oid1_sl");
		pq2.setQuestionaryTitle("Vprašalnik o bolnikovem zdravju, 9");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("vprašalnik_o_zdravju_pacientov_2_si");
		pq2.setPersistOid("oid2_sl");
		pq2.setQuestionaryTitle("Vprašalnik o bolnikovem zdravju, 2");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("splošna_anksiozna_motnja_si");
		pq2.setPersistOid("oid3_sl");
		pq2.setQuestionaryTitle("Generalizirana anksiozna motnja, 7");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);

		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("kakovost_življenja_si");
		pq2.setPersistOid("oid4_sl");
		pq2.setQuestionaryTitle("Kvaliteta življenja bolnikov z rakom, 30");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);

		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("splošna_lestvica_lastne_učinkovitosti_si");
		pq2.setPersistOid("oid5_sl");
		pq2.setQuestionaryTitle("Splošna lestvica samo-učinkovitosti");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);*/

		/*pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("ocena_invalidnosti_svetovne_zdravstvene_organizacije_si");
		pq2.setPersistOid("oid6_sl");
		pq2.setQuestionaryTitle("Vprašalnik svetovne zdravstvene organizacije (SZO) za ocenjevanje zmanjšane zmožnosti");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);*/


		//==========================================================================
/*

		//PersistPremPromManual pq2 = new PersistPremPromManual();
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("Interval_raka_debelega_črevesa_danke_si");
		pq2.setPersistOid("oid6_sl");
		pq2.setQuestionaryTitle("Interval raka debelega črevesa in danke");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("žilno_tveganje_si");
		pq2.setPersistOid("oid7_sl");
		pq2.setQuestionaryTitle("Srčno-žilno tveganje");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("rak_dojke_si");
		pq2.setPersistOid("oid8_sl");
		pq2.setQuestionaryTitle("Ponovitev raka dojke ali novi primarni rak dojke");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("Limfedemrak_si");
		pq2.setPersistOid("oid9_sl");
		pq2.setQuestionaryTitle("Limfedem");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("bolečina_si");
		pq2.setPersistOid("oid10_sl");
		pq2.setQuestionaryTitle("Bolečina");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("Hormonsko_neravnovesje_si");
		pq2.setPersistOid("oid11_sl");
		pq2.setQuestionaryTitle("Hormonsko neravnovesje");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("Motnje_spolnih_funkcij_si");
		pq2.setPersistOid("oid12_sl");
		pq2.setQuestionaryTitle("Motnje spolnih funkcij");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("Utrujenost_si");
		pq2.setPersistOid("oid13_sl");
		pq2.setQuestionaryTitle("Utrujenost");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("Kognitivne_funkcije_si");
		pq2.setPersistOid("oid14_sl");
		pq2.setQuestionaryTitle("Kognitivne funkcije");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("Bolezni_prebavil_si");
		pq2.setPersistOid("oid15_sl");
		pq2.setQuestionaryTitle("Bolezni prebavil");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("prehranjenost_si");
		pq2.setPersistOid("oid16_sl");
		pq2.setQuestionaryTitle("Neustrezna prehranjenost");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("sl");
		pq2.setChatbotId("Globalni_vprašalnik_za_telesno_dejavnost_si");
		pq2.setPersistOid("oid17_sl");
		pq2.setQuestionaryTitle("Globalni vprašalnik za telesno dejavnost");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);

		return q;

	}
	
	public LinkedList<PersistPremPromAPI> LangLatvian()
	{
		LinkedList<PersistPremPromAPI> q =  new LinkedList<PersistPremPromAPI>();
		//PersistPremPromAPI pq = new PersistPremPromAPI();
		PersistPremPromManual pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("vispārējs_trauksmes_traucējums_lt");
		pq2.setPersistOid("oid1_lv");
		pq2.setQuestionaryTitle("Vispārējs trauksme, 7");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
	    pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("pacienta_veselības_anketa_2_lt");
		pq2.setPersistOid("oid2_lv");
		pq2.setQuestionaryTitle("Pacienta veselības anketa, 2");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("pacientu_veselības_anketa_deviņi_lt");
		pq2.setPersistOid("oid3_lv");
		pq2.setQuestionaryTitle("Pacienta veselības anketa, 9");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("dzīves_kvalitāte_lt");
		pq2.setPersistOid("oid4_lv");
		pq2.setQuestionaryTitle("Dzīves kvalitāte, 30");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("vispārējā_pašefektivitātes_skala_lt");
		pq2.setPersistOid("oid5_lv");
		pq2.setQuestionaryTitle("Vispārējā pašefektivitātes skala");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		//==========================================================================


		//PersistPremPromManual pq2 = new PersistPremPromManual();
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("Kolorektālais_audzējs_lt");
		pq2.setPersistOid("oid6_lv");
		pq2.setQuestionaryTitle("Kolorektālais audzējs");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("Kardiovaskulārie_riski_lt");
		pq2.setPersistOid("oid7_lv");
		pq2.setQuestionaryTitle("Kardiovaskulārie riski");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("Krūts_audzēja_atkārtošanās_lt");
		pq2.setPersistOid("oid8_lv");
		pq2.setQuestionaryTitle("Krūts audzēja atkārtošanās vai jauns pirmreizējs krūts audzējs");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("Limfostāze_lt");
		pq2.setPersistOid("oid9_lv");
		pq2.setQuestionaryTitle("Limfostāze");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("Sāpes_lt");
		pq2.setPersistOid("oid10_lv");
		pq2.setQuestionaryTitle("Sāpes");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("Hormonālais_disbalanss_lt");
		pq2.setPersistOid("oid11_lv");
		pq2.setQuestionaryTitle("Hormonālais disbalanss");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("Seksuālā_disfunkcija_lt");
		pq2.setPersistOid("oid12_lv");
		pq2.setQuestionaryTitle("Seksuālā disfunkcija");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("Nogurums_lt");
		pq2.setPersistOid("oid13_lv");
		pq2.setQuestionaryTitle("Nogurums");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("Kognitīvās_funkcijas_lt");
		pq2.setPersistOid("oid14_lv");
		pq2.setQuestionaryTitle("Kognitīvās funkcijas");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("Gastrointestinālais_stāvoklis_lt");
		pq2.setPersistOid("oid15_lv");
		pq2.setQuestionaryTitle("Gastrointestinālais stāvoklis (kuņģa-zarnu trakta stāvoklis)");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("malnutrīcija_lt");
		pq2.setPersistOid("oid16_lv");
		pq2.setQuestionaryTitle("Uztura traucējumi (malnutrīcija)");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("lv");
		pq2.setChatbotId("Globālā_fiziskās_aktivitātes_anketa_lt");
		pq2.setPersistOid("oid17_lv");
		pq2.setQuestionaryTitle("Globālā fiziskās aktivitātes anketa");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		return q;
				
	}
	
	public LinkedList<PersistPremPromAPI> LangSpanish()
	{
		LinkedList<PersistPremPromAPI> q =  new LinkedList<PersistPremPromAPI>();
		//PersistPremPromAPI pq = new PersistPremPromAPI();
		PersistPremPromManual pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("trastorno_de_ansiedad_generalizada_es");
		pq2.setPersistOid("oid1_es");
		pq2.setQuestionaryTitle("Trastorno de ansiedad general, 7");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
	    pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("cuestionario_de_salud_del_paciente_2_es");
		pq2.setPersistOid("oid2_es");
		pq2.setQuestionaryTitle("Cuestionario de salud del paciente, 2");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("cuestionario_de_salud_del_paciente_nueve_es");
		pq2.setPersistOid("oid3_es");
		pq2.setQuestionaryTitle("Cuestionario de salud del paciente, 9");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("calidad_de_vida_es");
		pq2.setPersistOid("oid4_es");
		pq2.setQuestionaryTitle("Calidad de vida de los pacientes con cáncer, 30");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("escala_de_autoeficacia_general_es");
		pq2.setPersistOid("oid5_es");
		pq2.setQuestionaryTitle("Escala de autoeficacia general");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		//==========================================================================


		//PersistPremPromManual pq2 = new PersistPremPromManual();
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("Intervalo_de_cáncer_colorrectal_es");
		pq2.setPersistOid("oid6_es");
		pq2.setQuestionaryTitle("Intervalo de cáncer colorrectal");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("Riesgo_cardio_es");
		pq2.setPersistOid("oid7_es");
		pq2.setQuestionaryTitle("Riesgo cardiovascular");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("Recurrencia_en_cáncer_es");
		pq2.setPersistOid("oid8_es");
		pq2.setQuestionaryTitle("Recurrencia en cáncer de mama o nuevo cáncer de mama primario");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("Linfedema_es");
		pq2.setPersistOid("oid9_es");
		pq2.setQuestionaryTitle("Linfedema ");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("Dolor_es");
		pq2.setPersistOid("oid10_es");
		pq2.setQuestionaryTitle("Dolor");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("Desbalance_hormonal_es");
		pq2.setPersistOid("oid11_es");
		pq2.setQuestionaryTitle("Desbalance hormonal");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("Disfunción_sexual_es");
		pq2.setPersistOid("oid12_es");
		pq2.setQuestionaryTitle("Disfunción sexual");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("Fatiga_es");
		pq2.setPersistOid("oid13_es");
		pq2.setQuestionaryTitle("Fatiga");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("Función_cognitiva_es");
		pq2.setPersistOid("oid14_es");
		pq2.setQuestionaryTitle("Función cognitiva");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("Afecciones_gastrointestinales_es");
		pq2.setPersistOid("oid15_es");
		pq2.setQuestionaryTitle("Afecciones gastrointestinales");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("Malnutrición_es");
		pq2.setPersistOid("oid16_es");
		pq2.setQuestionaryTitle("Malnutrición");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("es");
		pq2.setChatbotId("Cuestionario_Mundial_sobre_Actividad_Física_es");
		pq2.setPersistOid("oid17_es");
		pq2.setQuestionaryTitle("Cuestionario Mundial sobre Actividad Física");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		return q;
				
	}
	
	public LinkedList<PersistPremPromAPI> LangFrench()
	{
		LinkedList<PersistPremPromAPI> q =  new LinkedList<PersistPremPromAPI>();
		//PersistPremPromAPI pq = new PersistPremPromAPI();
		PersistPremPromManual pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("trouble_anxieux_général_fr");
		pq2.setPersistOid("oid1_fr");
		pq2.setQuestionaryTitle("Trouble d'anxiété générale, 7");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
	    pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("questionnaire_de_santé_des_patients_2_fr");
		pq2.setPersistOid("oid2_fr");
		pq2.setQuestionaryTitle("Questionnaire sur la santé des patients, 2");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("questionnaire_sur_la_santé_des_patients_fr");
		pq2.setPersistOid("oid3_fr");
		pq2.setQuestionaryTitle("Questionnaire sur la santé des patients, 9");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("qualité_de_vie_fr");
		pq2.setPersistOid("oid4_fr");
		pq2.setQuestionaryTitle("Ualité de la vie des patients cancéreux, 30");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("échelle_d'auto-efficacité_générale_fr");
		pq2.setPersistOid("oid5_fr");
		pq2.setQuestionaryTitle("Échelle d'auto-efficacité générale");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		//==========================================================================


		//PersistPremPromManual pq2 = new PersistPremPromManual();
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("saignement_rectal_interval_fr");
		pq2.setPersistOid("oid6_fr");
		pq2.setQuestionaryTitle("Colorectal cancer interval");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("Risques_Cardiovasculaires_fr");
		pq2.setPersistOid("oid7_fr");
		pq2.setQuestionaryTitle("Risques Cardiovasculaires");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("rechute_d’un_cancer_fr");
		pq2.setPersistOid("oid8_fr");
		pq2.setQuestionaryTitle("Rrechute d’un cancer du sein ou l’apparition d’une nouvelle masse");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("Lymphœdème_fr");
		pq2.setPersistOid("oid8_fr");
		pq2.setQuestionaryTitle("Lymphœdème ");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("Douleur_fr");
		pq2.setPersistOid("oid9_fr");
		pq2.setQuestionaryTitle("Douleur");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("Déséquilibres_Hormonaux_fr");
		pq2.setPersistOid("oid10_fr");
		pq2.setQuestionaryTitle("Déséquilibres Hormonaux");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("Troubles_Sexuels_fr");
		pq2.setPersistOid("oid11_fr");
		pq2.setQuestionaryTitle("Troubles Sexuels");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("Fatigue_french_lg");
		pq2.setPersistOid("oid12_fr");
		pq2.setQuestionaryTitle("Fatigue");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("Problèmes_Gastrointestinaux_fr");
		pq2.setPersistOid("oid13_fr");
		pq2.setQuestionaryTitle("Problèmes Gastrointestinaux");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("Malnutrition_french_ques_lg");
		pq2.setPersistOid("oid14_fr");
		pq2.setQuestionaryTitle("Malnutrition");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("Questionnaire_mondial_activité_physique_fr");
		pq2.setPersistOid("oid15_fr");
		pq2.setQuestionaryTitle("Questionnaire mondial sur l'activité physique");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("fr");
		pq2.setChatbotId("Fonction_cognitive_fr");
		pq2.setPersistOid("oid16_fr");
		pq2.setQuestionaryTitle("Fonction cognitive");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		return q;
				
	}
	
	public LinkedList<PersistPremPromAPI> LangRussian()
	{
		LinkedList<PersistPremPromAPI> q =  new LinkedList<PersistPremPromAPI>();
		//PersistPremPromAPI pq = new PersistPremPromAPI();
		PersistPremPromManual pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("общее_тревожное_расстройство_ru");
		pq2.setPersistOid("oid1_ru");
		pq2.setQuestionaryTitle("Общее тревожное расстройство, 7");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
	    pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("Анкета_здоровья_пациента_2_ru");
		pq2.setPersistOid("oid2_ru");
		pq2.setQuestionaryTitle("Опросник здоровья пациента, 2");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("анкета_здоровья_пациента_девять_ru");
		pq2.setPersistOid("oid3_ru");
		pq2.setQuestionaryTitle("Опросник здоровья пациента, 9");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("качество_жизни_ru");
		pq2.setPersistOid("oid4_ru");
		pq2.setQuestionaryTitle("Качество жизни, 30");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("шкала_общей_самоэффективности_ru");
		pq2.setPersistOid("oid5_ru");
		pq2.setQuestionaryTitle("Общая шкала самоэффективности");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		//==========================================================================


		//PersistPremPromManual pq2 = new PersistPremPromManual();
		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("Интервал_между_колоректальным_раком_ru");
		pq2.setPersistOid("oid6_ru");
		pq2.setQuestionaryTitle("Интервал между колоректальным раком  ");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("сердечно-сосудистый_риск_ru");
		pq2.setPersistOid("oid7_ru");
		pq2.setQuestionaryTitle("Cердечно-сосудистый риск");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("рецидив_рака_груди_ru");
		pq2.setPersistOid("oid8_ru");
		pq2.setQuestionaryTitle("Pецидив рака груди или первичный рак груди");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("лимфостаз_ru");
		pq2.setPersistOid("oid9_ru");
		pq2.setQuestionaryTitle("Лимфостаз (лимфедема)");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("боль_ru");
		pq2.setPersistOid("oid10_ru");
		pq2.setQuestionaryTitle("боль");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("гормональный_дисбаланс_ru");
		pq2.setPersistOid("oid11_ru");
		pq2.setQuestionaryTitle("гормональный дисбаланс");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("усталость_ru");
		pq2.setPersistOid("oid12_ru");
		pq2.setQuestionaryTitle("усталость");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("когнитивные_функции_ru");
		pq2.setPersistOid("oid13_ru");
		pq2.setQuestionaryTitle("когнитивные функции");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("сексуальная_дисфункция_ru");
		pq2.setPersistOid("oid14_ru");
		pq2.setQuestionaryTitle("сексуальная дисфункция ");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("Заболевания_желудочно-кишечного_тракта_ru");
		pq2.setPersistOid("oid15_ru");
		pq2.setQuestionaryTitle("Заболевания желудочно-кишечного тракта");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("недоедание_ru");
		pq2.setPersistOid("oid16_ru");
		pq2.setQuestionaryTitle("Расстройства пищевого поведения (недоедание)");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);


		pq2 = new PersistPremPromManual();
		pq2.setLanguageCode("ru");
		pq2.setChatbotId("нГлобальный_опросник_по_физической_активности_ru");
		pq2.setPersistOid("oid17_ru");
		pq2.setQuestionaryTitle("Глобальный опросник по физической активности");
		pq2.setQuestionaryType("non-loop");
		q.add(pq2);
		*/
		return q;
				
	}

}

