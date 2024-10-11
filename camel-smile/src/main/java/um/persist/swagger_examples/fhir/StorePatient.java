package um.persist.swagger_examples.fhir;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@ApiModel(description = "Model representing a FHIR Patient resource")
public class StorePatient {

    @ApiModelProperty(example = "Patient", required = true, notes = "Type of the FHIR resource")
    @JsonProperty("resourceType")
    public String resourceType;

    @ApiModelProperty(example = "user-id", required = true, notes = "Unique identifier for the patient")
    @JsonProperty("id")
    public String id;

    @ApiModelProperty(notes = "List of patient names")
    @JsonProperty("name")
    public List<Name> name;

    @ApiModelProperty(notes = "List of contact information for the patient")
    @JsonProperty("telecom")
    public List<Telecom> telecom;

    @ApiModelProperty(example = "male", required = true, notes = "Gender of the patient")
    @JsonProperty("gender")
    public String gender;

    @ApiModelProperty(example = "1988-05-28", required = true, notes = "Birth date of the patient")
    @JsonProperty("birthDate")
    public String birthDate;

    @ApiModelProperty(notes = "List of patient addresses")
    @JsonProperty("address")
    public List<Address> address;

    @ApiModelProperty(notes = "Marital status of the patient")
    @JsonProperty("maritalStatus")
    public MaritalStatus maritalStatus;

    @ApiModelProperty(notes = "Patient's preferred languages for communication")
    @JsonProperty("communication")
    public List<Communication> communication;

    @ApiModel(description = "Model representing a patient name")
    public static class Name {

        @ApiModelProperty(example = "Surname", required = true, notes = "Family name of the patient")
        @JsonProperty("family")
        public String family;

        @ApiModelProperty(required = true, notes = "Given name(s) of the patient")
        @JsonProperty("given")
        public List<String> given;
    }

    @ApiModel(description = "Model representing telecom information")
    public static class Telecom {

        @ApiModelProperty(example = "phone", required = true, notes = "System used for contact information")
        @JsonProperty("system")
        public String system;

        @ApiModelProperty(example = "521-521-1113", required = true, notes = "Contact information value")
        @JsonProperty("value")
        public String value;

        @ApiModelProperty(example = "home", required = true, notes = "Usage type of the contact information")
        @JsonProperty("use")
        public String use;
    }

    @ApiModel(description = "Model representing a patient address")
    public static class Address {

        @ApiModelProperty(notes = "Street address lines")
        @JsonProperty("line")
        public List<String> line;

        @ApiModelProperty(example = "City", required = true, notes = "City of the address")
        @JsonProperty("city")
        public String city;

        @ApiModelProperty(example = "UK", required = true, notes = "State or region of the address")
        @JsonProperty("state")
        public String state;

        @ApiModelProperty(example = "1000", required = true, notes = "Postal code of the address")
        @JsonProperty("postalCode")
        public String postalCode;

        @ApiModelProperty(example = "UK", required = true, notes = "Country of the address")
        @JsonProperty("country")
        public String country;
    }

    @ApiModel(description = "Model representing marital status")
    public static class MaritalStatus {

        @ApiModelProperty(notes = "Coding system for the marital status")
        @JsonProperty("coding")
        public List<Coding> coding;

        @ApiModel(description = "Model representing coding for marital status")
        public static class Coding {

            @ApiModelProperty(example = "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus", required = true, notes = "System defining the coding for marital status")
            @JsonProperty("system")
            public String system;

            @ApiModelProperty(example = "M", required = true, notes = "Code for marital status")
            @JsonProperty("code")
            public String code;
        }
    }

    @ApiModel(description = "Model representing communication details")
    public static class Communication {

        @ApiModelProperty(notes = "Language for communication")
        @JsonProperty("language")
        public Language language;

        @ApiModel(description = "Model representing the language for communication")
        public static class Language {

            @ApiModelProperty(notes = "Coding system for the language")
            @JsonProperty("coding")
            public List<Coding> coding;

            @ApiModel(description = "Model representing coding for language")
            public static class Coding {

                @ApiModelProperty(example = "urn:ietf:bcp:47", required = true, notes = "System defining the coding for the language")
                @JsonProperty("system")
                public String system;

                @ApiModelProperty(example = "en", required = true, notes = "Language code")
                @JsonProperty("code")
                public String code;
            }
        }
    }
}