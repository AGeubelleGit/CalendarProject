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

    private transient ArrayList<String> requiredSections;

    public Course(Course other) {
        this.name = other.name;
        this.description = other.description;
        this.creditHours = other.creditHours;
        this.genEdReqs = other.genEdReqs;
        this.sections = new Section[0];
        this.requiredSections = new ArrayList<>();
    }

    public void addRequiredSection(String newSection) {
        if (requiredSections != null) {
            requiredSections.add(newSection);
        }else{
            requiredSections = new ArrayList<>();
            requiredSections.add(newSection);
        }
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

    public String sectionTypesToString() {
        if (sectionTypes.length > 0) {
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < sectionTypes.length - 1; i++) {
                String type = sectionTypes[i];
                output.append(type);
                output.append(", ");
            }
            output.append(sectionTypes[sectionTypes.length-1]);
            return output.toString();
        }else{
            return "None.";
        }
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

    public ArrayList<String> getRequiredSections() {
        return requiredSections;
    }

    public void setRequiredSections(ArrayList<String> requiredSections) {
        this.requiredSections = requiredSections;
    }
}
