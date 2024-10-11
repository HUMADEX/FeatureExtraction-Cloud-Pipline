package um.persist.swagger_examples.fhir;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@ApiModel(description = "Model representing a FHIR Task resource")
public class StoreTask {

    @ApiModelProperty(example = "Task", required = true, notes = "Type of the FHIR resource")
    @JsonProperty("resourceType")
    public String resourceType;

    @ApiModelProperty(example = "task-weekly-phq9-completed", required = true, notes = "Unique identifier for the task")
    @JsonProperty("id")
    public String id;

    @ApiModelProperty(example = "ActivityDefinition/weekly-phq9", notes = "Canonical URL of the activity definition")
    @JsonProperty("instantiatesCanonical")
    public String instantiatesCanonical;

    @ApiModelProperty(notes = "List of references to related resources the task is based on")
    @JsonProperty("basedOn")
    public List<Reference> basedOn;

    @ApiModelProperty(example = "completed", required = true, notes = "Status of the task")
    @JsonProperty("status")
    public String status;

    @ApiModelProperty(example = "order", required = true, notes = "Intent of the task")
    @JsonProperty("intent")
    public String intent;

    @ApiModelProperty(example = "stat", notes = "Priority of the task")
    @JsonProperty("priority")
    public String priority;

    @ApiModelProperty(notes = "Code for the task")
    @JsonProperty("code")
    public Code code;

    @ApiModelProperty(notes = "Reference to the focus of the task")
    @JsonProperty("focus")
    public Reference focus;

    @ApiModelProperty(notes = "Reference to the subject (patient) of the task")
    @JsonProperty("for")
    public Reference forPatient;

    @ApiModelProperty(notes = "Execution period of the task")
    @JsonProperty("executionPeriod")
    public Period executionPeriod;

    @ApiModelProperty(example = "2024-06-18T00:00:00Z", notes = "Date when the task was authored")
    @JsonProperty("authoredOn")
    public String authoredOn;

    @ApiModelProperty(notes = "Reference to the requester of the task")
    @JsonProperty("requester")
    public Reference requester;

    @ApiModelProperty(notes = "Reference to the owner of the task")
    @JsonProperty("owner")
    public Reference owner;

    @ApiModelProperty(notes = "List of outputs produced by the task")
    @JsonProperty("output")
    public List<Output> output;

    @ApiModel(description = "Model representing a reference to another FHIR resource")
    public static class Reference {

        @ApiModelProperty(example = "CarePlan/careplan3", notes = "Reference to a FHIR resource")
        @JsonProperty("reference")
        public String reference;
    }

    @ApiModel(description = "Model representing the task code")
    public static class Code {

        @ApiModelProperty(notes = "List of codings for the task code")
        @JsonProperty("coding")
        public List<Coding> coding;

        @ApiModel(description = "Model representing coding details")
        public static class Coding {

            @ApiModelProperty(example = "http://hl7.org/fhir/task-code", required = true, notes = "System defining the coding for the task")
            @JsonProperty("system")
            public String system;

            @ApiModelProperty(example = "questionnaire", required = true, notes = "Code for the task")
            @JsonProperty("code")
            public String code;

            @ApiModelProperty(example = "Questionnaire", required = true, notes = "Display text for the task code")
            @JsonProperty("display")
            public String display;
        }
    }

    @ApiModel(description = "Model representing the execution period of the task")
    public static class Period {

        @ApiModelProperty(example = "2024-07-19T19:00:00Z", required = true, notes = "Start time of the execution period")
        @JsonProperty("start")
        public String start;

        @ApiModelProperty(example = "2024-07-19T20:00:00Z", required = true, notes = "End time of the execution period")
        @JsonProperty("end")
        public String end;
    }

    @ApiModel(description = "Model representing task output")
    public static class Output {

        @ApiModelProperty(notes = "Type of the output")
        @JsonProperty("type")
        public Code type;

        @ApiModelProperty(notes = "Reference to the value of the output")
        @JsonProperty("valueReference")
        public Reference valueReference;
    }
}