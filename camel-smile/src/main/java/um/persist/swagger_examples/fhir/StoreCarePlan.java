package um.persist.swagger_examples.fhir;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@ApiModel(description = "Model representing a FHIR CarePlan resource")
public class StoreCarePlan {

    @ApiModelProperty(example = "CarePlan", required = true, notes = "Type of the FHIR resource")
    @JsonProperty("resourceType")
    public String resourceType;

    @ApiModelProperty(example = "careplan1", required = true, notes = "Unique identifier for the care plan")
    @JsonProperty("id")
    public String id;

    @ApiModelProperty(notes = "Canonical URL of the plan definition")
    @JsonProperty("instantiatesCanonical")
    public List<String> instantiatesCanonical;

    @ApiModelProperty(example = "active", required = true, notes = "Status of the care plan")
    @JsonProperty("status")
    public String status;

    @ApiModelProperty(example = "plan", required = true, notes = "Intent of the care plan")
    @JsonProperty("intent")
    public String intent;

    @ApiModelProperty(example = "Patient's Care Plan", notes = "Title of the care plan")
    @JsonProperty("title")
    public String title;

    @ApiModelProperty(notes = "Description of the care plan")
    @JsonProperty("description")
    public String description;

    @ApiModelProperty(notes = "Reference to the subject (patient) of the care plan")
    @JsonProperty("subject")
    public Reference subject;

    @ApiModelProperty(notes = "Period during which the care plan is valid")
    @JsonProperty("period")
    public Period period;

    @ApiModelProperty(notes = "Reference to the custodian (practitioner) of the care plan")
    @JsonProperty("custodian")
    public Reference custodian;

    @ApiModelProperty(notes = "List of planned activities in the care plan")
    @JsonProperty("activity")
    public List<Activity> activity;

    @ApiModel(description = "Model representing a reference to another FHIR resource")
    public static class Reference {

        @ApiModelProperty(example = "Patient/2e7cfdab-f63c-4e10-ba68-c0a0413a8d57", notes = "Reference to a FHIR resource")
        @JsonProperty("reference")
        public String reference;
    }

    @ApiModel(description = "Model representing the period of the care plan")
    public static class Period {

        @ApiModelProperty(example = "2024-07-01", required = true, notes = "Start date of the care plan period")
        @JsonProperty("start")
        public String start;

        @ApiModelProperty(example = "2024-12-31", required = true, notes = "End date of the care plan period")
        @JsonProperty("end")
        public String end;
    }

    @ApiModel(description = "Model representing an activity in the care plan")
    public static class Activity {

        @ApiModelProperty(notes = "Reference to the planned activity")
        @JsonProperty("plannedActivityReference")
        public Reference plannedActivityReference;
    }
}
