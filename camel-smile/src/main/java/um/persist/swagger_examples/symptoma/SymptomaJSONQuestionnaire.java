package um.persist.swagger_examples.symptoma;

import io.swagger.annotations.ApiModelProperty;

public class SymptomaJSONQuestionnaire {
	@ApiModelProperty(example = "user-123")
    public String patient_id;
    @ApiModelProperty(example = "yes")
    public String answer;
    @ApiModelProperty(example = "Are you coughing?")
    public String text;
    @ApiModelProperty(example = "Cough")
    public String title;
    @ApiModelProperty(example = "12345")
    public String symptomaId;
    public SymptomaJSONQuestionnaireData data;

}