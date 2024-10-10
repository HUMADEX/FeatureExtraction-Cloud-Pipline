package um.persist.swagger_examples.fhir;

import io.swagger.annotations.ApiModelProperty;

public class FHIR {
	@ApiModelProperty(example = "user-id")
    public String sender_id;
    @ApiModelProperty(example = "plevel_si_male")
    public String questionary_id;

}