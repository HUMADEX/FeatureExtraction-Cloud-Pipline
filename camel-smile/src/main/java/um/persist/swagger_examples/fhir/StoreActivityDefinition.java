package um.persist.swagger_examples.fhir;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@ApiModel(description = "Model representing a FHIR ActivityDefinition resource")
public class StoreActivityDefinition {

    @ApiModelProperty(example = "ActivityDefinition", required = true, notes = "Type of the FHIR resource")
    @JsonProperty("resourceType")
    public String resourceType;

    @ApiModelProperty(example = "weekly-diary", required = true, notes = "Unique identifier for the activity definition")
    @JsonProperty("id")
    public String id;

    @ApiModelProperty(example = "Weekly Diary", notes = "Title of the activity definition")
    @JsonProperty("title")
    public String title;

    @ApiModelProperty(example = "active", required = true, notes = "Status of the activity definition")
    @JsonProperty("status")
    public String status;

    @ApiModelProperty(notes = "Description of the activity definition")
    @JsonProperty("description")
    public String description;

    @ApiModelProperty(notes = "List of related artifacts for the activity definition")
    @JsonProperty("relatedArtifact")
    public List<RelatedArtifact> relatedArtifact;

    @ApiModelProperty(example = "Task", required = true, notes = "Kind of resource that the activity definition defines")
    @JsonProperty("kind")
    public String kind;

    @ApiModelProperty(notes = "Timing for the activity as Timing structure")
    @JsonProperty("timingTiming")
    public Timing timingTiming;

    @ApiModelProperty(notes = "List of participants for the activity")
    @JsonProperty("participant")
    public List<Participant> participant;

    @ApiModel(description = "Model representing related artifacts")
    public static class RelatedArtifact {

        @ApiModelProperty(example = "depends-on", required = true, notes = "Type of relationship for the related artifact")
        @JsonProperty("type")
        public String type;

        @ApiModelProperty(example = "Questionnaire/diary", required = true, notes = "Reference to the related resource")
        @JsonProperty("resource")
        public String resource;
    }

    @ApiModel(description = "Model representing the timing details of the activity")
    public static class Timing {

        @ApiModelProperty(notes = "Details about the repeat timing of the activity")
        @JsonProperty("repeat")
        public Repeat repeat;

        @ApiModel(description = "Model representing repeat timing")
        public static class Repeat {

            @ApiModelProperty(example = "1", required = true, notes = "Frequency of the activity")
            @JsonProperty("frequency")
            public int frequency;

            @ApiModelProperty(example = "1", required = true, notes = "Period between occurrences of the activity")
            @JsonProperty("period")
            public int period;

            @ApiModelProperty(example = "wk", required = true, notes = "Period unit for the activity timing")
            @JsonProperty("periodUnit")
            public String periodUnit;

            @ApiModelProperty(notes = "List of days of the week for the activity")
            @JsonProperty("dayOfWeek")
            public List<String> dayOfWeek;

            @ApiModelProperty(notes = "List of times of day for the activity")
            @JsonProperty("timeOfDay")
            public List<String> timeOfDay;
        }
    }

    @ApiModel(description = "Model representing participants of the activity")
    public static class Participant {

        @ApiModelProperty(example = "patient", required = true, notes = "Type of participant in the activity")
        @JsonProperty("type")
        public String type;
    }
}
