package sample;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import sample.firebase.FirebaseUtility;
import sample.schedule.Course;
import sample.schedule.GenEdCategories;
import sample.schedule.Section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class GenEdCourseFinder {
    private ArrayList<Course> currCourses;
    private CourseSectionFilter filter;
    private GenEdCategories category;

    private ArrayList<String> possibleCourseNames;
    private ArrayList<Course> possibleCourses;

    public GenEdCourseFinder(ArrayList<Course> currCourses, GenEdCategories category) {
        this(currCourses, category, null);
    }

    public GenEdCourseFinder(ArrayList<Course> currCourses, GenEdCategories category, CourseSectionFilter inputFilter) {
        this.currCourses = currCourses;
        this.category = category;
        if (inputFilter != null) {
            this.filter = inputFilter.copy();
        } else {
            this.filter = null;
        }
    }

    public ArrayList<Course> run() {
        possibleCourses = new ArrayList<>();
        possibleCourseNames = new ArrayList<>();

        getCoursesInCategory();

        return possibleCourses;
    }

    public void getCoursesInCategory() {
        DatabaseReference ref = FirebaseUtility.getGenEdListReference(category);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> genEdCourses;
                genEdCourses = (HashMap<String, String>) dataSnapshot.getValue();
                possibleCourseNames = new ArrayList<>(genEdCourses.keySet());
                Collections.sort(possibleCourseNames);
                System.out.println(possibleCourseNames);
                tryAllCourses();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void tryAllCourses() {
        if (! possibleCourseNames.isEmpty()){
            String courseName = possibleCourseNames.remove(0);
            String department = courseName.replaceAll("[^a-zA-Z]", "");
            //System.out.println(department + " - " + courseName);

            DatabaseReference ref = FirebaseUtility.getCourseReference(department, courseName);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String json = dataSnapshot.getValue().toString();
                    Gson gson = new Gson();
                    Course currCourse = gson.fromJson(json, Course.class );
                    if (doesCourseWork(currCourse)) {
                        possibleCourses.add(currCourse);
                    }

                    tryAllCourses();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            System.out.println(possibleCourses);
            System.out.println(possibleCourses.size());
            //InputCourses.testFunc();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    FindGenEdCoursesUI.displayPossibleCourses(possibleCourses);
                }
            });
        }
    }

    private boolean doesCourseWork(Course course) {
        HashMap<String,Boolean> workingStringTypes = new HashMap<>();
        for (Section section: course.getSections()) {
            if(filter == null || filter.filter(section)) {
                workingStringTypes.put(section.getType(), true);
            }
        }

        String[] requiredTypes = course.getSectionTypes();
        boolean output = true;
        for (String type: requiredTypes) {
            if (! workingStringTypes.containsKey(type)) {
                output = false;
            }
        }
        //System.out.println(output);

        return output;

    }
}
