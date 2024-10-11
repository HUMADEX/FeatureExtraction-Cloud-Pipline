package um.persist.swagger_examples.symptoma;

import io.swagger.annotations.ApiModelProperty;

public class SymptomaJSONQuestionnaireData {
    @ApiModelProperty(example = "es")
    public String country;
    @ApiModelProperty(example = "en")
    public String language;
    @ApiModelProperty(example = "user-123")
    public String patient_id;
    @ApiModelProperty(example = "44")
    public String age;
    @ApiModelProperty(example = "UNKNOWN")
    public String sex;
    @ApiModelProperty(example = "1234")
    public String careplan_id;
    
}
