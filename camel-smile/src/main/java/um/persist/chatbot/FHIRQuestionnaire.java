package um.persist.chatbot;

import org.hl7.fhir.dstu3.model.*;

import java.util.*;

public class FHIRQuestionnaire
{
    private String url; //Questionary/vprašalnik_o_zdravju_pacientov_2_si
    private String identifier; //Questionary/vprašalnik_o_zdravju_pacientov_2_si
    private String name; //PHQ2-SI
    private String title; //Vprašalnik o zdravju pacientov, PHQ-2
    private String publisher; // UKCM
    private String description; // "The PHQ-2 inquires about the frequency of depressed mood and anhedonia over the past two weeks. The PHQ-2 includes the first two items of the PHQ-9.The purpose of the PHQ-2 is to screen for depression in a “first-step” approach. Patients who screen positive should be further evaluated with the PHQ-9 to determine whether they meet criteria for a depressive disorder."
    private String purpose; //Assesing Mental Helath
    public LinkedList<FHIRQuestion> item = new LinkedList<FHIRQuestion>(); //lis tof questions
    private Coding code; // code: 10190-7, dispaly: Mental status Narrative, system: http://www.hl7.org/Special/committees/patientcare
    private Coding status ;  //should be always active
    private Coding subectType;


    public void setStatus()
    {
        Coding c = new Coding().setCode("active").setDisplay("active").setSystem("http://hl7.org/fhir/publication-status");
        this.status = c;
    };

    public Coding getStatus()
    {
        return this.status;
    }

    public void setCode(String code, String display, String system)
    {
        Coding c = new Coding().setCode(code).setDisplay(display).setSystem(system);
        this.code = c;
    };

    public Coding getCode()
    {
        return this.code;
    }

    public void setSubectType()
    {
        Coding c = new Coding().setCode("Questionnaire").setDisplay("Questionnaire").setSystem("http://hl7.org/fhir/resource-types");
        this.subectType = c;
    };

    public Coding getSubectType()
    {
        return this.subectType;
    }


    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public String getIdentifier() {
        return this.identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher() {
        return this.publisher;
    }
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getPurpose() {
        return this.purpose;
    }
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }





}
