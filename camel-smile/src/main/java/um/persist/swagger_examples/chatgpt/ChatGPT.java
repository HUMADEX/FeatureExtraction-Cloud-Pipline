package um.persist.swagger_examples.chatgpt;

import io.swagger.annotations.ApiModelProperty;

public class ChatGPT {
	@ApiModelProperty(example = "user_chatgpt")
    public String user_id;
    @ApiModelProperty(example = "What is the time difference between New York and Paris?")
    public String text;

}