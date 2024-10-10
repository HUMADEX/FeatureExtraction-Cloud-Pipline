package um.persist.swagger_examples.synthaudio;

import io.swagger.annotations.ApiModelProperty;

public class SynthAudio{
	@ApiModelProperty(example = "Hi, this is some speech text.")
    public String text;
    @ApiModelProperty(example = "en") // en | es | fr | lv | ru | sl
    public String language;
    @ApiModelProperty(example = "female") // male | female
    public String gender;
}
