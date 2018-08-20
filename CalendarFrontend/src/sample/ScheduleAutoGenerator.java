package sample;

import sample.schedule.Course;
import sample.schedule.Section;

import java.util.ArrayList;

public class ScheduleAutoGenerator {
    public ArrayList<Course> courseList;

    public ScheduleAutoGenerator() {
        courseList = new ArrayList<>();
    }

    public void addCourse(Course course) {
        courseList.add(course);
    }

//    public ArrayList<ArrayList<Course>> createSchedules() {
//        ArrayList<ArrayList<Course>> output = new ArrayList<>();
//        for (Course course: courseList) {
//            ArrayList<String> requiredSections = course.getRequiredSections();
//            for (String sectionType: requiredSections) {
//                for (Section section: course.getSections()) {
//                    Course created_course = new Course(course);
//                    Section[] sections = new Section[1];
//                    sections[0] = section;
//                    created_course.setSections(sections);
//                }
//            }
//        }
//    }
}
