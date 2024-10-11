package um.persist.swagger_examples.symptoma;

import io.swagger.annotations.ApiModelProperty;

public class SymptomaJSONText {
	@ApiModelProperty(example = "user-123")
    public String patient_id;
    @ApiModelProperty(example = "I have a low fever today, a headache and a sore throat.")
    public String text;
    @ApiModelProperty(example = "en")
    public String country;
    @ApiModelProperty(example = "en")
    public String language;
    @ApiModelProperty(example = "test-123")
    public String careplan_id;

}