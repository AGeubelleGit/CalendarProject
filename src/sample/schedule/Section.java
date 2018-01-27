package sample.schedule;

import com.google.gson.annotations.SerializedName;
import javafx.scene.layout.Pane;

public class Section {
    @SerializedName("section_number")
    private String sectionNumber;

    @SerializedName("type")
    private String type;

    @SerializedName("days_of_the_week")
    private String daysOfTheWeek;

    @SerializedName("start_time")
    private int startTime;

    @SerializedName("end_time")
    private int endTime;

    // The transient keyword makes gson not try and parse this.
    // https://stackoverflow.com/questions/4802887/gson-how-to-exclude-specific-fields-from-serialization-without-annotations
    private transient Pane schedulePane;
    private transient boolean hidden = false;

    public static final String[] daysAbbr = {"M", "T", "W", "R", "F"};
    public static final String[] types = {"All", "Lecture", "Discussion", "Laboratory"};

    @Override
    public String toString() {
        return "Section{" +
                "sectionNumber='" + sectionNumber + '\'' +
                ", type='" + type + '\'' +
                ", daysOfTheWeek='" + daysOfTheWeek + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    public String getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(String sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDaysOfTheWeek() {
        return daysOfTheWeek;
    }

    public void setDaysOfTheWeek(String daysOfTheWeek) {
        this.daysOfTheWeek = daysOfTheWeek;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public Pane getSchedulePane() {
        return schedulePane;
    }

    public void setSchedulePane(Pane schedulePane) {
        this.schedulePane = schedulePane;
    }

    public void removeSchedulePane() {
        this.schedulePane.setId(this.sectionNumber);
        //remove the pane.
    }

    public boolean hasPane() {
        return this.schedulePane != null;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hide) {
        this.hidden = hide;
    }

    public boolean isAtTime(String day, int time) {
        return daysOfTheWeek.contains(day) && time >= startTime && time <= endTime;
    }

    public static String timeIntToString(int value) {
        String output = "";
        int hours = value/100;
        output += (hours < 10 ? '0' : "");
        output += hours;
        output += ":";
        int mins = value%100;
        output += (mins < 10 ? '0' : "");
        output += mins;
        return output;
    }
}
