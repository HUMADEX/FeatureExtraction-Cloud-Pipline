package um.persist.swagger_examples.fhir;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@ApiModel(description = "Model representing a FHIR QuestionnaireResponse")
public class StoreQuestionnaireResponse {

    @ApiModelProperty(example = "QuestionnaireResponse", required = true, notes = "Type of the FHIR resource")
    @JsonProperty("resourceType")
    public String resourceType;

    @ApiModelProperty(example = "subject-um-0001-gad7", required = true, notes = "Unique identifier for the questionnaire response")
    @JsonProperty("id")
    public String id;

    @ApiModelProperty(notes = "Questionnaire reference details")
    @JsonProperty("_questionnaire")
    public Questionnaire questionnaire;

    @ApiModelProperty(example = "completed", required = true, notes = "Status of the questionnaire response")
    @JsonProperty("status")
    public String status;

    @ApiModelProperty(notes = "Details about the subject")
    @JsonProperty("subject")
    public Subject subject;

    @ApiModelProperty(notes = "List of items in the questionnaire response")
    @JsonProperty("item")
    public List<Item> item;

    @ApiModel(description = "Model representing a questionnaire reference")
    public static class Questionnaire {
        @ApiModelProperty(notes = "List of extensions for the questionnaire")
        @JsonProperty("extension")
        public List<Extension> extension;

        @ApiModel(description = "Model representing an extension")
        public static class Extension {
            @ApiModelProperty(example = "http://x.x.x.x:8080/fhir/Questionnaire/gad7", required = true, notes = "URL of the extension")
            @JsonProperty("url")
            public String url;

            @ApiModelProperty(example = "gad7", required = true, notes = "Value of the extension")
            @JsonProperty("valueString")
            public String valueString;
        }
    }

    @ApiModel(description = "Model representing the subject of the questionnaire response")
    public static class Subject {
        @ApiModelProperty(example = "Patient/subject-um-0001", required = true, notes = "Reference to the subject")
        @JsonProperty("reference")
        public String reference;

        @ApiModelProperty(example = "gad7", required = true, notes = "Display name of the subject")
        @JsonProperty("display")
        public String display;
    }

    @ApiModel(description = "Model representing an item in the questionnaire response")
    public static class Item {
        @ApiModelProperty(example = "0", required = true, notes = "Link ID of the item")
        @JsonProperty("linkId")
        public String linkId;

        @ApiModelProperty(example = "questionnaire_start", required = true, notes = "Text of the item")
        @JsonProperty("text")
        public String text;

        @ApiModelProperty(notes = "List of answers for the item")
        @JsonProperty("answer")
        public List<Answer> answer;

        @ApiModel(description = "Model representing an answer in the questionnaire response")
        public static class Answer {

            @ApiModelProperty(value = "String value of the answer", example = "gad7")
            @JsonProperty("valueString")
            public String valueString;

            @ApiModelProperty(value = "Integer value of the answer", example = "0")
            @JsonProperty("valueInteger")
            public Integer valueInteger;

            // Default constructor
            public Answer() {
            }

            // Constructor for valueString
            public Answer(String valueString) {
                this.valueString = valueString;
            }

            // Constructor for valueInteger
            public Answer(Integer valueInteger) {
                this.valueInteger = valueInteger;
            }
        }
    }
}
