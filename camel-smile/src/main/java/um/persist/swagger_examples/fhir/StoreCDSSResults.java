package um.persist.swagger_examples.fhir;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@ApiModel(description = "Model representing a FHIR CDSS Results to store are Observation")
public class StoreCDSSResults {

    @ApiModelProperty(example = "subject-um-0001", required = true, notes = "Unique identifier for the user")
    @JsonProperty("UserID")
    public String userID;

    @ApiModelProperty(example = "smartphone-0001", required = true, notes = "Unique identifier for the device")
    @JsonProperty("DeviceID")
    public String deviceID;

    @ApiModelProperty(example = "task-diary-subject-um-0001-12347", required = true, notes = "Unique identifier for the task")
    @JsonProperty("TaskID")
    public String taskID;

    @ApiModelProperty(example = "Questionnaire", required = true, notes = "Intent of the task")
    @JsonProperty("TaskIntent")
    public String taskIntent;

    @ApiModelProperty(example = "CarePlan/careplan3", required = true, notes = "Unique identifier for the care plan")
    @JsonProperty("CarePlanID")
    public String carePlanID;

    @ApiModelProperty(example = "phq9", required = true, notes = "Output of the task")
    @JsonProperty("TaskOutput")
    public String taskOutput;

    @ApiModelProperty(example = "QuestionnaireResponse/phq9-response-001", required = true, notes = "Unique identifier for the resource")
    @JsonProperty("ResourceID")
    public String resourceID;

    @ApiModelProperty(notes = "User features and their details")
    @JsonProperty("User_Features")
    public List<UserFeature> userFeatures;

    @ApiModel(description = "Model representing a user feature with details")
    public static class UserFeature {

        @ApiModelProperty(example = "phq9_score", required = true, notes = "Feature name")
        @JsonProperty("Feature")
        public String feature;

        @ApiModelProperty(example = "17", required = true, notes = "Feature value")
        @JsonProperty("Value")
        public String value;

        @ApiModelProperty(example = "Awareness App", required = true, notes = "Source of the feature data")
        @JsonProperty("Source")
        public String source;

        @ApiModelProperty(example = "22/05/2024 09:27", required = true, notes = "Timestamp of when the feature data was recorded")
        @JsonProperty("Timestamp")
        public String timestamp;
    }
}