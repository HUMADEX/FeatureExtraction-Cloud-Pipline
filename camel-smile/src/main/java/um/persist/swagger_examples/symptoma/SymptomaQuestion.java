package um.persist.swagger_examples.symptoma;

import io.swagger.annotations.ApiModelProperty;

/*
  "patient_id": "user123",
  "age": 44,
  "country": "es",
  "language": "en",
  "query": "Question",
  "answer": "yes",
  "previous_text": "Do you have a headache?",
  "previous_title": "Headache",
  "previous_symptomaId": 396095
  */

public class SymptomaQuestion{
	@ApiModelProperty(example = "user123")
    public String patient_id;
	@ApiModelProperty(example = "44")
    public int age;
	@ApiModelProperty(example = "es")
	public String country;
	//@ApiModelProperty(example = "STAGING")
	//public String environment;
	@ApiModelProperty(example = "en")
	public String language;
	//@ApiModelProperty(example = "DEFAULT")
	//public String origin;
	@ApiModelProperty(example = "Question")
	public String query;
	//@ApiModelProperty(example = "https://example.com")
	//public String referrer;
	//@ApiModelProperty(example = "0b78235e603b6dae07eea3b8d275f0cf")
	//public String sessionid;
	//@ApiModelProperty(example = "UNKNOWN")
	//public String sex;
	//@ApiModelProperty(example = "true")
	//public Boolean tracking;
	//@ApiModelProperty(example = "ee21d5f27a8401788147f6f6184ddb11")
	//public String userhash;
	//@ApiModelProperty(example = "Malaise")
	//public String title;
	@ApiModelProperty(example = "yes")
	public String answer;
	@ApiModelProperty(example = "Do you have a headache?")
	public String text;
	@ApiModelProperty(example = "Headache")
	public String title;
	@ApiModelProperty(example = "396095")
	public String symptomaId;
}