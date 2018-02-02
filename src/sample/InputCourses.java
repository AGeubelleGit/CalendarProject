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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class InputCourses {

    private static Scene scene;
    private static VBox sectionsListVBox;
    private static ScrollPane sectionsListScroller;
    private static Main main = null;

    private static Course shownCourse;

    private static CourseSectionFilter filter;
    private static Button clearFilterButton;

    private static String savedCourseString;
    private static String savedDeptString;

    private static final double windowWidth = 850;

    private static String currSectionType;
    private static final String allSectionTypeString = "All";
    private static String currSectionDay;
    private static final String allSectionDaysString = "All";
    private static String[] daysAndAll = null;

    private static ArrayList<String> departmentNames;
    private static DatabaseReference ref;
    private static ObservableList<String> options = null;
    private static ObservableList<String> courseInDep = null;
    private static VBox vertLayout;
    private static Pane pane;
    private static ComboBox departmentDropdown;
    private static ComboBox coursesDropdown;
    private static boolean updateReq;

    private static HashMap<String, Course> currDep;

    private static HashMap<String, HashMap<String, Course>> courses;

    /**
     * Called by main on start, initializes the static variables and calls resetScene to create the scene.
     *
     * @param inputMain    the instance of main that is used to travel between screens.
     * @param givenCourses A hashmap of all the courses that the user can select.
     */
    public static void initialize(Main inputMain, HashMap<String, HashMap<String, Course>> givenCourses) {
        main = inputMain;
        courses = givenCourses;
        filter = null;
        savedDeptString = null;
        savedCourseString = null;

        // daysAndAll is an array containing the days of the week as well as the string "All"
        // array is used to display the filter by days of the week buttons.
        daysAndAll = new String[6];
        daysAndAll[0] = allSectionDaysString;
        for (int d = 1; d < 6; d++) {
            daysAndAll[d] = Section.daysAbbr[d - 1];
        }

        departmentNames = new ArrayList<>();

        // The vert layout holds the three main parts of the screen
        vertLayout = new VBox();
        vertLayout.setSpacing(5);

        updateReq = false;
        ref = FirebaseUtility.getDepartmentNamesReference();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap depMap = (HashMap<String, String>) dataSnapshot.getValue();
                departmentNames = new ArrayList<>(depMap.values());
                System.out.println(departmentNames);
                Collections.sort(departmentNames);
                options = FXCollections.observableArrayList(departmentNames);
                System.out.println("TEST");
                updateReq = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Canceled");
            }
        });

        departmentDropdown = new ComboBox(null);
        coursesDropdown = new ComboBox(null);
        pane = new Pane(vertLayout);
        scene = new Scene(pane, windowWidth, 550);

        // Create the scene using resetScene.
        scene = resetScene(null);
    }

    public static Scene resetScene(CourseSectionFilter newFilter) {
        // Set the current filter settings to All for both days of the week and section type.
        currSectionType = allSectionTypeString;
        currSectionDay = allSectionDaysString;

        // Change filter value
        filter = newFilter;

        shownCourse = null;

        vertLayout.getChildren().clear();

        // This VBox will hold the list of all the sections for the selected course as information about the course.
        sectionsListVBox = new VBox();
        sectionsListVBox.setSpacing(10);

        // The scroller contains the sectionsListVBox and allows the user to scroll through all the sections.
        sectionsListScroller = new ScrollPane(sectionsListVBox);
        sectionsListScroller.setMinSize(windowWidth, 445);
        sectionsListScroller.setMaxSize(windowWidth, 445);
        sectionsListScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        // This HBox contains the dropdowns for selecting the course as well as the filter buttons.
        HBox courseDropdownSelection = new HBox();
        courseDropdownSelection.setAlignment(Pos.CENTER);
        courseDropdownSelection.setSpacing(10);

        // A list of department names which will be the values in the first dropdown.
        departmentDropdown = new ComboBox(options);
        departmentDropdown.setPrefWidth(300);
        departmentDropdown.setPrefHeight(50);
        departmentDropdown.setPromptText("Choose Department");

        // The dropdown that will show all the courses in the selected department.
        coursesDropdown = new ComboBox();
        coursesDropdown.setPrefWidth(300);
        coursesDropdown.setPrefHeight(50);
        coursesDropdown.setPromptText("Choose Course");

        if (currDep != null) {
            ArrayList<String> courseNames = new ArrayList<String>(currDep.keySet());
            Collections.sort(courseNames);
            coursesDropdown.setItems(FXCollections.observableArrayList(courseNames));
        }

        // When the department is changed, update the keys in the courses dropdown to be all the courses in said dept.
        departmentDropdown.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (updateReq) {
                    updateReq = false;
                    departmentDropdown.setItems(options);
                }
            }
        });
        departmentDropdown.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                savedDeptString = departmentDropdown.getValue().toString();

                //TESTING
                ref = FirebaseUtility.getDepartmentReference(savedDeptString);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String json = dataSnapshot.getValue().toString();
                        Gson gson = new Gson();
                        currDep = gson.fromJson(json, new TypeToken<HashMap<String, Course>>() {}.getType() );
                        ArrayList<String> courses = new ArrayList<>(currDep.keySet());
                        Collections.sort(courses);
                        courseInDep = FXCollections.observableArrayList(courses);
                        updateReq = true;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("Canceled");
                    }
                });

            }
        });

        // When the user chooses a specific course, save the values for the dept and course.
        // Then reset the day/type filters and finally, set the list of sections.
        coursesDropdown.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (updateReq) {
                    updateReq = false;
                    coursesDropdown.setItems(courseInDep);
                }
            }
        });
        coursesDropdown.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (coursesDropdown.getValue() != null && departmentDropdown.getValue() != null) {
                    savedCourseString = coursesDropdown.getValue().toString();
                    savedDeptString = departmentDropdown.getValue().toString();
                    currSectionType = allSectionTypeString;
                    currSectionDay = allSectionDaysString;

                    setSectionList(savedDeptString, savedCourseString);
                } else {
                    savedCourseString = null;
                    savedDeptString = null;
                }
            }
        });

        Button toTimeSelectionButton = new Button("Filter By Times");
        toTimeSelectionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                main.setSceneTimeSelection();
            }
        });

        // create a button to clear the time selection filter.
        clearFilterButton = new Button("Clear Times");
        // disable the button if there is no filter
        if (filter == null) {
            clearFilterButton.setDisable(true);
        }
        clearFilterButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                filter = null;
                clearFilterButton.setDisable(true);
                setSectionList();
            }
        });

        // Add the 4 nodes to the course dropdown HBox.
        courseDropdownSelection.getChildren().addAll(departmentDropdown, coursesDropdown, toTimeSelectionButton, clearFilterButton);

        // Create an action HBox that contains the button to send user to the schedule.
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

        Button testButton = new Button("Test");
        testButton.setPrefHeight(35);
        testButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                GenEdCourseFinder finder = new GenEdCourseFinder(null, GenEdCategories.HUM, filter);
                new Thread(()->finder.run()).start();
//                finder.run();
            }
        });

        actionButtonsHBox.getChildren().addAll(testButton, scheduleButton);

        // The vert labout is just a verticle container for the three main components.
        vertLayout.getChildren().addAll(actionButtonsHBox, courseDropdownSelection, sectionsListScroller);

        return scene;
    }

    public static Scene getScene() {
        return scene;
    }

    /**
     * Set the filter value and update both the sectionList and the clearFilterButton.
     *
     * @param newFilter the new filter variable.
     */
    public static void setFilter(CourseSectionFilter newFilter) {
        filter = newFilter;
        clearFilterButton.setDisable(filter == null);
        setSectionList();
    }

    // Sets the section list to display the information from the saved course and department.
    private static void setSectionList() {
        if (savedDeptString != null && savedCourseString != null) {
            setSectionList(savedDeptString, savedCourseString);
        }
    }

    // Set the section list to display information about a specific course.
    private static void setSectionList(String dept, String course) {
        //Course selectedCourse = courses.get(dept).get(course);
        Course selectedCourse = currDep.get(course);
        System.out.println(selectedCourse);
        if (selectedCourse != null) {
            setSectionList(selectedCourse);
        }
    }

    // Given a course, show the name, description and all sections.
    private static void setSectionList(Course course) {
        final double sidePadding = 5;
        final double sideWidthNoHorizontalBar = windowWidth - (10 * sidePadding);

        // Clear old sections.
        sectionsListVBox.getChildren().clear();
        sectionsListVBox.setPadding(new Insets(0, sidePadding, 0, sidePadding));

        // Show the course name.
        Label courseNameLabel = new Label(course.getName());
        courseNameLabel.setPadding(new Insets(5, 0, 0, 0));
        courseNameLabel.setWrapText(true);

        // Show the course description.
        Label courseDescriptionLabel = new Label(course.getDescription());
        courseDescriptionLabel.setMaxWidth(sideWidthNoHorizontalBar);
        courseDescriptionLabel.setWrapText(true);

        sectionsListVBox.getChildren().addAll(courseNameLabel, courseDescriptionLabel);

        // An HBox that will be used to seperate the filters for "days of week" and "section types"
        HBox filterContainer = new HBox();
        filterContainer.setMaxWidth(sideWidthNoHorizontalBar);

        // Create an HBox with buttons to allow the user to select the section type they want to show.
        HBox sectionTypeSelection = new HBox();
        sectionTypeSelection.setAlignment(Pos.CENTER_LEFT);
        // Set the pref width to be much higher than the actual width so that this hbox is pushed to the left.
        sectionTypeSelection.setPrefWidth(1400);
        sectionTypeSelection.setMinWidth(300);
        sectionTypeSelection.setSpacing(5);
        // Create a button for each section type that when pressed filters the sections by that type.
        for (String type : Section.types) {
            Button button = new Button(type);
            // Disable the button if the current filter type is already the displayed time
            button.setDisable(type.equals(currSectionType));
            button.setPrefHeight(35);
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    currSectionType = type;
                    setSectionList(course);
                }
            });
            sectionTypeSelection.getChildren().add(button);
        }

        HBox sectionDaysOfWeekSelector = new HBox();
        sectionDaysOfWeekSelector.setAlignment(Pos.CENTER_RIGHT);
        sectionDaysOfWeekSelector.setPrefWidth(1400);
        sectionDaysOfWeekSelector.setMinWidth(300);
        sectionDaysOfWeekSelector.setSpacing(5);
        for (int d = 0; d < 6; d++) {
            String day = daysAndAll[d];
            Button button = new Button(day);
            button.setDisable(day.equalsIgnoreCase(currSectionDay));
            button.setPrefHeight(35);
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    currSectionDay = day;
                    setSectionList(course);
                }
            });
            sectionDaysOfWeekSelector.getChildren().add(button);
        }

        filterContainer.getChildren().addAll(sectionTypeSelection, sectionDaysOfWeekSelector);
        sectionsListVBox.getChildren().add(filterContainer);

        // The add all button will add a course with all the displayed sections to the schedule.
        Button addAllButton = new Button("Add all");
        addAllButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ScheduleInfo.addCourse(shownCourse);
                addAllButton.setDisable(true);
            }
        });
        sectionsListVBox.getChildren().add(addAllButton);

        // The array list shownSections will hold all the sections that fit the filter criteria.
        ArrayList<Section> shownSections = new ArrayList<>();
        for (Section section : course.getSections()) {
            if (filter == null || filter.filter(section)) { //If there is no filter or the section passes the filter.
                if (currSectionType.equals(allSectionTypeString) || section.getType().contains(currSectionType)) {
                    if (currSectionDay.equals(allSectionDaysString) || section.getDaysOfTheWeek().contains(currSectionDay)) {
                        shownSections.add(section);
                        Pane sectionPane = createSectionInfoPane(section, course);
                        sectionsListVBox.getChildren().add(sectionPane);
                    }
                }
            }
        }
        shownCourse = new Course(course);
        Section[] sectionsArray = new Section[shownSections.size()];
        for (int i = 0; i < shownSections.size(); i++) {
            sectionsArray[i] = shownSections.get(i);
        }
        shownCourse.setSections(sectionsArray);
    }

    public static void displayPossibleCourses(ArrayList<Course> genEdCourses) {
        sectionsListVBox.getChildren().clear();
        for (Course course: genEdCourses) {
            sectionsListVBox.getChildren().add(new Label(course.getName()));
        }
    }

    /**
     * Create a little pane with info about a specific section.
     * This includes the section number, type, time, and days of the week.
     * Also include a button to add the section to the schedule.
     * @param section
     * @param course
     * @return
     */
    private static Pane createSectionInfoPane(Section section, Course course) {
        final int paneWidth = 800;
        final int paneHeight = 50;

        HBox hbox = new HBox();
        hbox.setSpacing(10);

        ArrayList<Node> labels = new ArrayList<>();
        labels.add(JavaFXUtilities.createLabel("Section: " + section.getSectionNumber(), 100));
        labels.add(JavaFXUtilities.createLabel("Type: " + section.getType(), 200));
        String timeText = "Time: " + Section.timeIntToString(section.getStartTime()) + "-" +
                Section.timeIntToString(section.getEndTime());
        labels.add(JavaFXUtilities.createLabel(timeText, 150));
        labels.add(JavaFXUtilities.createLabel("Days: " + section.getDaysOfTheWeek(), 100));

        Button button = new Button("Add");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Course newCourse = new Course(course);
                Section[] sections = new Section[1];
                sections[0] = section;
                newCourse.setSections(sections);
                ScheduleInfo.addCourse(newCourse);
                button.setDisable(true);
            }
        });

        hbox.getChildren().addAll(labels);
        hbox.getChildren().addAll(button);

        Pane pane = new Pane(hbox);
        pane.setMaxSize(paneWidth, paneHeight);
        return pane;
    }

}
