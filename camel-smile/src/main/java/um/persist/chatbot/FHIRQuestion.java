package um.persist.chatbot;

import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.codesystems.ItemType;

import java.util.LinkedList;

public class FHIRQuestion
{
    private String linkId; //ID: Questionary/Item/question1_phq2_si_start
    private ItemType type; //
    private String text; //question
    private Boolean required; //
    private LinkedList<String> options; // possible answers


    public void setType()
    {
        ItemType it = ItemType.QUESTION;
        //Coding c = new Coding().setCode("active").setDisplay("active").setSystem("http://hl7.org/fhir/publication-status");
        this.type = it;
    };
    public ItemType getType()
    {
        return this.type;
    }


    public String getLinkId() {
        return this.linkId;
    }
    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getText() {
        return this.text;
    }
    public void setText(String text) {this.text = text;}

    public Boolean getRequired() {
        return this.required;
    }
    public void setRequired(Boolean required) {
        this.required = required;
    }

    public LinkedList<String> getOptions() {
        return this.options;
    }
    public void setOptions(LinkedList<String> options) {
        this.options = options;
    }
}

