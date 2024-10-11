package um.persist.chatbot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SymptomaQuestionnaire {
    
    private String symptoma_id;
    private String text; //question text
    private String answer;
    private String title;
    private String country;
    private String language;
    private String patient_id;
    private String age;
    private String sex;

    public String getSymptomaID() {
        return symptoma_id;
    }

    public void setSymptomaID(String symptoma_id) {
        this.symptoma_id = symptoma_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
            this.answer = answer;
        
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if(getAnswer().toLowerCase().equals("yes")){
            this.title = title;
        }else if(getAnswer().toLowerCase().equals("no")){
            this.title = "-"+title;
        }else{
            this.title = "?"+title;
        }
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
            this.country = country;
        
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
            this.language = language;
        
    }

    public String getPatientId() {
        return patient_id;
    }

    public void setPatientId(String patient_id) {
            this.patient_id = patient_id;
        
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
            this.age = age;
        
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
            this.sex = sex;
        
    }

    @Override
    public String toString() {
        return "{" +
                "symptomaId=" + symptoma_id +
                ", text=" + text +
                ", answer=" + answer +
                ", title=" + title +
                '}';
    }
    

}
