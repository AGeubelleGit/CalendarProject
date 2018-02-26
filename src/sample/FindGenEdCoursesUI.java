package sample;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;
import sample.firebase.FirebaseUtility;
import sample.schedule.Course;
import sample.schedule.GenEdCategories;
import sample.schedule.Section;

import java.util.*;

public class FindGenEdCoursesUI {

    private static Scene scene;
    private static Pane pane;
    private static VBox genEdPaneVBox;
    private static VBox genEdDisplayVBox;
    private static CourseSectionFilter filter;
    private static ScrollPane genEdDisplayScroller;

    private static String[] genEdCategories;

    private static Main main;
    private static final double windowWidth = 850;

    public static void initialize(Main inputMain) {
        main = inputMain;
        filter = null;

        genEdCategories = getNames(GenEdCategories.class);

        genEdPaneVBox = new VBox();
        genEdPaneVBox.setSpacing(10);

        pane = new Pane(genEdPaneVBox);
        scene = new Scene(pane, windowWidth, 550);

        // Create the scene using resetScene.
        scene = resetScene(null);
    }

    public static Scene resetScene() {
        return resetScene(filter);
    }

    public static Scene resetScene(CourseSectionFilter newFilter) {
        filter = newFilter;

        genEdPaneVBox.getChildren().clear();

        HBox actionButtonsHBox = new HBox();
        actionButtonsHBox.setPadding(new Insets(5, 5, 0, 5));
        actionButtonsHBox.setAlignment(Pos.CENTER_RIGHT);
        actionButtonsHBox.setSpacing(10);

        Button scheduleButton = new Button("To Schedule");
        scheduleButton.setPrefHeight(35);
        scheduleButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                main.setSceneScheduleInfo();
            }
        });

        Button inputCoursesButton = new Button("Input Courses");
        inputCoursesButton.setPrefHeight(35);
        inputCoursesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                main.setSceneInputCourses();
            }
        });

        actionButtonsHBox.getChildren().addAll(inputCoursesButton, scheduleButton);


        HBox inputHBox = new HBox();
        inputHBox.setAlignment(Pos.CENTER);
        inputHBox.setPadding(new Insets(0, 10, 0, 10));
        inputHBox.setSpacing(20);

        ComboBox genEdDropdown = new ComboBox(FXCollections.observableArrayList(genEdCategories));
        genEdDropdown.setPrefWidth(300);
        genEdDropdown.setPrefHeight(50);
        genEdDropdown.setPromptText("Choose Category");

        genEdDropdown.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                GenEdCategories category = GenEdCategories.valueOf(genEdDropdown.getValue().toString());
                GenEdCourseFinder finder = new GenEdCourseFinder(null, category, filter);
                new Thread(()->finder.run()).start();
            }
        });

        Button toTimeSelectionButton = new Button("Filter By Times");
        toTimeSelectionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                main.setSceneTimeSelection(UIScene.GEN_ED_FINDER);
            }
        });

        // create a button to clear the time selection filter.
        Button clearFilterButton = new Button("Clear Times");
        clearFilterButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                main.setSceneGenEdScene(null);
            }
        });

        inputHBox.getChildren().addAll(genEdDropdown, toTimeSelectionButton, clearFilterButton);

        // This VBox will hold the list of all the sections for the selected course as information about the course.
        genEdDisplayVBox = new VBox();
        genEdDisplayVBox.setSpacing(10);
        final double sidePadding = 5;
        genEdDisplayVBox.setPadding(new Insets(sidePadding, sidePadding, sidePadding, sidePadding));

        // The scroller contains the sectionsListVBox and allows the user to scroll through all the sections.
        genEdDisplayScroller = new ScrollPane(genEdDisplayVBox);
        genEdDisplayScroller.setMinSize(windowWidth, 445);
        genEdDisplayScroller.setMaxSize(windowWidth, 445);
        genEdDisplayScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        genEdPaneVBox.getChildren().addAll(actionButtonsHBox, inputHBox, genEdDisplayScroller);
        return scene;
    }

    public static Scene getScene() {
        return scene;
    }

    public static void displayPossibleCourses(ArrayList<Course> courses) {
        genEdDisplayVBox.getChildren().clear();
        for (Course course: courses) {
            genEdDisplayVBox.getChildren().add(createCourseInfoPane(course));
        }
    }

    private static Pane createCourseInfoPane(Course course) {
        final int paneWidth = 800;
        final int paneHeight = 50;

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.TOP_CENTER);
        hbox.setSpacing(25);

        ArrayList<Node> labels = new ArrayList<>();
        labels.add(JavaFXUtilities.createLabel("Course Name: " + course.getName(), 250));
        labels.add(JavaFXUtilities.createLabel("Types: " + course.sectionTypesToString(), 250));
        labels.add(JavaFXUtilities.createLabel("Credit Hours: " + course.getCreditHours(), 100));

        Button button = new Button("Add Entire Course");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ScheduleInfo.addCourse(course);
                button.setDisable(true);
            }
        });

        hbox.getChildren().addAll(labels);
        hbox.getChildren().addAll(button);

        Pane pane = new Pane(hbox);
        pane.setMinSize(paneWidth, paneHeight);
        pane.setMaxSize(paneWidth, paneHeight);
        return pane;
    }

    public static String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }

}

