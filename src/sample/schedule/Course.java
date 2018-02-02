package sample.schedule;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;

public class Course {
    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("credit_hours")
    private int creditHours;

    @SerializedName("gen_ed_categories")
    private String[] genEdReqs;

    @SerializedName("section_types")
    private String[] sectionTypes;

    @SerializedName("section_list")
    private Section[] sections;

    public Course(Course other) {
        this.name = other.name;
        this.description = other.description;
        this.creditHours = other.creditHours;
        this.genEdReqs = other.genEdReqs;
        this.sections = new Section[0];
    }

    @Override
    public String toString() {
        return "Course{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", creditHours=" + creditHours +
                ", genEdReqs=" + Arrays.toString(genEdReqs) +
                ", sections=" + sections +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }

    public String[] getGenEdReqs() {
        return genEdReqs;
    }

    public void setGenEdReqs(String[] genEdReqs) {
        this.genEdReqs = genEdReqs;
    }

    public Section[] getSections() {
        return sections;
    }

    public void setSections(Section[] sections) {
        this.sections = sections;
    }

    public String[] getSectionTypes() {
        return sectionTypes;
    }

    public void setSectionTypes(String[] sectionTypes) {
        this.sectionTypes = sectionTypes;
    }
}
