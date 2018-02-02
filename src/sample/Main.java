package sample;

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import sample.firebase.FirebaseUtility;
import sample.schedule.Course;
import sample.schedule.Section;

import javax.swing.*;
import java.util.*;

public class Main extends Application {

    private Stage window;
    private Main main;

    // A hashmap of all the courses where the first set of keys are the departments
    // and the second set of keys is dep+course (CS125)
    private HashMap<String, HashMap<String, Course>> courses;

    // called on launch.
    // First downloads all the courses from json, then sets up the scene where the user inputs courses
    @Override
    public void start(Stage primaryStage) throws Exception{
        window = primaryStage;
        main = this;

        // https://gist.github.com/julianbonilla/2784293
        String filePath = "/Users/alexandregeubelle/Downloads/CalendarProject/src/sample/schedule_info.json";
        courses = JsonParser.getCourses(filePath);

        FirebaseUtility.initialize();
        ScheduleInfo.initialize(main);
        InputCourses.initialize(main, courses);
        TimeSelection.initialize(main);

        setSceneInputCourses();
        window.show();
    }

    private void setScene(Scene scene) {
        window.setScene(scene);
        window.centerOnScreen();
    }

    public void setSceneInputCourses() {
        setSceneInputCourses(null);
    }

    public void setSceneInputCourses(CourseSectionFilter filter) {
        //InputCourses.setFilter(filter);
        InputCourses.resetScene(filter); //Not supposed to be here
        Scene inputScene = InputCourses.getScene();
        setScene(inputScene);
        window.setTitle("Add Course");
    }

    public void setSceneInputCoursesTest() {
        Scene inputScene = InputCourses.getScene();
        setScene(inputScene);
        window.setTitle("Add Course");
    }

    public void setSceneScheduleInfo() {
        Scene scheduleScene = ScheduleInfo.createSchedule();
        setScene(scheduleScene);
        window.setTitle("Your Schedule");
    }

    public void setSceneTimeSelection() {
        Scene timeSelectionScene = TimeSelection.createTimeSelectionScene();
        setScene(timeSelectionScene);
        window.setTitle("Choose Time Frame");
    }

    public double getWindowWidth() {
        return window.getWidth();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
