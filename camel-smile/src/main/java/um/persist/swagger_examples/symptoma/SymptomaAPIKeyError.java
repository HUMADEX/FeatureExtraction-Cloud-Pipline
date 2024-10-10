package um.persist.swagger_examples.symptoma;

import io.swagger.annotations.ApiModelProperty;

public class SymptomaAPIKeyError{
	@ApiModelProperty(example = "invalid_api_key")
    public String error;
	@ApiModelProperty(example = "API Key verification failed")
	public String error_description;
}