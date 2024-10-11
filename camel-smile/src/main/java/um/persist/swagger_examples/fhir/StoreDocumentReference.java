package um.persist.swagger_examples.fhir;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Model representing the FHIR DocumentReference details")
public class StoreDocumentReference {
    
    @ApiModelProperty(example = "subject-um-0001", required = true, notes = "Unique identifier for the user")
    public String UserID;

    @ApiModelProperty(example = "smartphone-0001", required = true, notes = "Unique identifier for the device")
    public String DeviceID;

    @ApiModelProperty(example = "task-diary-subject-um-0001-12347", required = true, notes = "Unique identifier for the task")
    public String TaskID;

    @ApiModelProperty(example = "Diary", required = true, notes = "Intent of the task")
    public String TaskIntent;

    @ApiModelProperty(example = "CarePlan/careplan3", required = true, notes = "Unique identifier for the care plan")
    public String CarePlanID;

    @ApiModelProperty(example = "diary", required = true, notes = "Output of the task")
    public String TaskOutput;

    @ApiModelProperty(example = "2024-07-01T10:00:00Z", required = true, notes = "Date and time of the task in ISO 8601 format")
    public String Date;

    @ApiModelProperty(example = "https://example.com/path/to/video.mp4", required = true, notes = "URL path to the video")
    public String PathURL;

    @ApiModelProperty(example = "video/mp4", required = true, notes = "Content type of the video")
    public String ContentType;

}
