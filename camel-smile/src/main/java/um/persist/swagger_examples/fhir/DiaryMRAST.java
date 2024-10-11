package um.persist.swagger_examples.fhir;

import io.swagger.annotations.ApiModelProperty;

public class DiaryMRAST {
	@ApiModelProperty(example = "http://x.x.x.x/smile/diary_video/patient_video_2min.mov")
    public String video_url;
    /*@ApiModelProperty(example = "en")
    public String language_code;*/
    @ApiModelProperty(example = "user-id")
    public String user_id;
    @ApiModelProperty(example = "DocumentReference/video-subject-um-0001")
    public String document_reference;

}