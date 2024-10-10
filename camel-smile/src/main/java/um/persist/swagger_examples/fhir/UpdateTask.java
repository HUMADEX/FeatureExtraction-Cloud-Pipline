package um.persist.swagger_examples.fhir;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@ApiModel(description = "Model representing a task with questionnaire response details")
public class UpdateTask {

    @ApiModelProperty(example = "subject-um-0001", required = true, notes = "Unique identifier for the user")
    @JsonProperty("UserID")
    public String userID;

    @ApiModelProperty(example = "smartphone-0001", required = true, notes = "Unique identifier for the device")
    @JsonProperty("DeviceID")
    public String deviceID;

    @ApiModelProperty(example = "task-phq9-subject-um-0001-12347", required = true, notes = "Unique identifier for the task")
    @JsonProperty("TaskID")
    public String taskID;

    @ApiModelProperty(example = "Questionnaire", required = true, notes = "Intent of the task")
    @JsonProperty("TaskIntent")
    public String taskIntent;

    @ApiModelProperty(example = "CarePlan/careplan3", required = true, notes = "Unique identifier for the care plan")
    @JsonProperty("CarePlanID")
    public String carePlanID;

    @ApiModelProperty(example = "in-progress", required = true, notes = "Status of the task")
    @JsonProperty("TaskStatus")
    public String taskStatus;

    @ApiModelProperty(example = "phq9", required = true, notes = "Output of the task")
    @JsonProperty("TaskOutput")
    public String taskOutput;

    @ApiModelProperty(example = "QuestionnaireResponse/phq9-response-001", required = true, notes = "Unique identifier for the resource")
    @JsonProperty("ResourceID")
    public String resourceID;
}