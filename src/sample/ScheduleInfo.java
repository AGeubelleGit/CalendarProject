package sample;

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Pair;
import sample.schedule.Course;
import sample.schedule.Section;

import javax.swing.*;
import java.util.*;

public class ScheduleInfo {

    private static int minHeight = 30;
    private static int maxHeight = 70;
    private static int zoomAmount = 10;

    private static int height = 50;
    private static int height_split = 1;
    private static int realHeight = height-(height_split*2);
    private static int width = 250;
    private static int width_split = 5;
    private static int realWidth = width-(width_split*2);
    private static int time_width = 75;

    private static double scheduleInfoScrollStartValue = 0.45;

    private static Color[] courseColors;

    private static Main main;

    // Data source containing the courses to be displayed
    private static ArrayList<Course> currCourses = new ArrayList<>();
    private static ArrayList<Pair<Course, Boolean>> selectedCourses = new ArrayList<>(); //<The course, whether it is part of a group>

    // variables to hold the outer scene and vertical layout. These were created because creating a new
    // instace of the scene and vbox would cause problems.
    private static Scene scheduleScene;
    private static VBox screen;

    private static boolean minimzeList;
    private static final double minimizedListHeight = 40;
    private static final double regularListHeight = 200;
    private static ScrollPane listScrollPane;
    private static VBox listVBox;

    // Initialize the variables in this class.
    public static void initialize(Main mainInstance) {
        main = mainInstance;

        minimzeList = false;

        listVBox = new VBox();
        listVBox.setSpacing(5);
        listVBox.setPadding(new Insets(5,5,5,5));

        courseColors = new Color[] {Color.BLUE, Color.GREEN, Color.MEDIUMPURPLE, Color.RED, Color.DIMGRAY,
                Color.LIGHTBLUE, Color.CORAL, Color.LIGHTGREEN, Color.LIGHTCYAN, Color.MAGENTA,
                Color.ORANGE, Color.LIGHTSALMON, Color.GOLDENROD};

        listScrollPane = new ScrollPane(listVBox);
        listScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        listScrollPane.setMinHeight(regularListHeight);
        listScrollPane.setMaxHeight(regularListHeight);

        screen = new VBox();
        scheduleScene = new Scene(screen);

    }

    // Function to add a list of courses to the data source.
    public static void addCourse(ArrayList<Course> newCourse) {
        currCourses.addAll(newCourse);
    }

    // Function to add a single course to the data source.
    public static void addCourse(Course newCourse) {
        currCourses.add(newCourse);
    }

    // Function to be called whenever you want to rebuild the schedule window and show it to the user.
    public static Scene createSchedule() {

        // Create a menu bar that will be placed about the schedule.
        HBox menu = createMenuBar();

        // Create the base layout pane which has the time + days of week + cells.
        Pane pane = createBaseLayout();

        // Now go through and add each of the sections to the base pane.
        addCoursesToSchedule(pane);

        // Add a scroller to the pane with all of the schedule info so that we can display everything.
        ScrollPane scroller = new ScrollPane(pane);
        scroller.setVvalue(scheduleInfoScrollStartValue);
        scroller.setOnScrollFinished(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                scheduleInfoScrollStartValue = scroller.getVvalue();
            }
        });

        // Function that fills in the values of the pane.
        fillListPane();

        // Clear old values for the VBox screen and then add the menu->schedule->list
        screen.getChildren().clear();
        screen.getChildren().addAll(menu, scroller, listScrollPane);

        return scheduleScene;
    }

    private static HBox createMenuBar() {
        // The menu bar is a horizontal box with buttons.
        HBox menu = new HBox();
        menu.setSpacing(10);
        menu.setPadding(new Insets(5, 10, 5, 10));

        // Add a title label to the left corner.
        Label title = new Label("Your Schedule");
        title.setPrefHeight(35);

        // Create a back button that brings the user back to the input scene
        Button findCoursesButton = new Button("Select Course");
        findCoursesButton.setPrefHeight(35);
        findCoursesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                main.setSceneInputCourses();
            }
        });

        Label zoomLabel = new Label("Zoom");
        zoomLabel.setPrefHeight(35);
        zoomLabel.setPadding(new Insets(0,0,0,10));

        Button zoomInButton = new Button("+");
        zoomInButton.setPrefHeight(35);
        zoomInButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                height = Integer.min(height+zoomAmount, maxHeight);
                realHeight = height-(height_split*2);
                createSchedule();
            }
        });

        Button zoomOutButton = new Button("-");
        zoomOutButton.setPrefHeight(35);
        zoomOutButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                height = Integer.max(height-zoomAmount, minHeight);
                realHeight = height-(height_split*2);
                createSchedule();
            }
        });

        // Set the children of the menu and return it.
        menu.getChildren().addAll(title, findCoursesButton, zoomLabel, zoomInButton, zoomOutButton);
        return menu;
    }

    //The base layout contains the row for days of the week the column for the times and the individual cells.
    private static Pane createBaseLayout() {
        // create the days pane that shows the days of the week.
        // it is located at the top of the schedule, but to the right of the times column.
        Pane daysPane = createDaysOfTheWeekPane();
        daysPane.relocate(time_width,0);


        //Create a pane for the hours and relocate it below the row for the days of the week.
        Pane timesPane = createTimesOfTheDayPane();
        timesPane.relocate(0, height);

        //Create a base pane which will be below the days of the week and to the right of the times.
        Pane cellsPane = createCellsPane();
        cellsPane.relocate(time_width, height);

        // Create a pane that has background color black and contains all the other panes.
        Pane parentPane = new Pane();
        String black = "000000";
        parentPane.setStyle("-fx-background-color: #" + black);
        parentPane.getChildren().addAll(cellsPane, daysPane, timesPane);

        parentPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                selectedCourses.clear();
                Pair<String, Integer> dayTime = getCellPressed(event.getX(), event.getY());
                if (dayTime == null) {
                    return;
                }
                ArrayList<String> names = new ArrayList<>();
                for (Course course: currCourses) {
                    for (Section section: course.getSections()) {
                        if (section.isAtTime(dayTime.getKey(), dayTime.getValue())) {
                            if (course.getSections().length > 1) {
                                Course createdCourse = new Course(course);
                                Section[] createdSections = new Section[1];
                                createdSections[0] = section;
                                createdCourse.setSections(createdSections);
                                selectedCourses.add(new Pair<>(createdCourse, true)); //true is group
                            } else {
                                selectedCourses.add(new Pair<>(course, false)); //false is not group.
                            }
                        }
                    }
                }

                if (!selectedCourses.isEmpty()) {
                    fillListPaneSelected();
                } else {
                    fillListPane();
                }

            }
        });

        return parentPane;
    }

    private static Pane createDaysOfTheWeekPane() {
        // The strings for the days of the week.
        final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

        // create the days pane.
        Pane days_pane = new Pane();

        // For each column, add a cell with the day of the week.
        for (int c = 0; c < 5; c++) {
            Rectangle rect = JavaFXUtilities.createRect(width*c + width_split, 0,
                    realWidth,realHeight);
            rect.setFill(Color.WHITE);

            //Create a label with the day of the week.
            Label day_label = new Label(days[c]);
            day_label.setMinWidth(width);
            day_label.setMinHeight(height);
            day_label.relocate(width * c + width_split, 0);
            day_label.setAlignment(Pos.CENTER);

            days_pane.getChildren().addAll(rect, day_label);
        }

        return days_pane;
    }

    private static Pane createTimesOfTheDayPane() {
        // Create a pane for the hours of the day (every half hour)
        Pane times_pane = new Pane();

        // Made up of white rectangles of "realHeight" and "timeWidth" and a label with the string time value.
        for (int r = 0; r < 48; r++) {
            Rectangle time_rect = JavaFXUtilities.createRect(0, height*r + height_split, time_width, realHeight);
            time_rect.setFill(Color.WHITE);

            int timeVal = ((r / 2) * 100) + ((r % 2) * 30);
            Label time_label = new Label(Section.timeIntToString(timeVal));
            time_label.setMinWidth(time_width);
            time_label.setMinHeight(height);
            time_label.relocate(0, height * r);
            time_label.setAlignment(Pos.TOP_CENTER);
            times_pane.getChildren().addAll(time_rect, time_label);
        }

        return times_pane;
    }

    private static Pane createCellsPane() {
        //Create a base pane containing all the time slot cells
        Pane cellPane = new Pane();
        cellPane.relocate(time_width, height);
        // For each half hour.
        for (int r = 0; r < 48; r++) {
            // For each day of the week.
            for (int c=0; c<5; c++) {
                Rectangle rect = JavaFXUtilities.createRect(width*c + width_split, height*r + height_split, realWidth, realHeight);
                rect.setFill(Color.WHITE);
                cellPane.getChildren().add(rect);
            }
        }

        return cellPane;
    }

    private static void addCoursesToSchedule(Pane pane) {
        //For each course in our data source currCourses,
        // give the course a color and then display all its sections on the schedule.
        // First do non group courses because they are opaque.
        int counter = 0;
        HashMap<String, Color> coursesSetToColors = new HashMap<>();
        for (Course course: currCourses) {
            if (course.getSections() == null){
                continue;
            }
            boolean isGroup = course.getSections().length > 1;

            Color currColor;
            if (coursesSetToColors.containsKey(course.getName())) {
                currColor = coursesSetToColors.get(course.getName());
            } else {
                currColor = courseColors[counter % (courseColors.length)];
                coursesSetToColors.put(course.getName(), currColor);
                counter++;
            }

            double opacity = isGroup ? 0.5 : 1.0; //If it is ALL sections it is 50% transparent.
            currColor = setColorOpacity(currColor, opacity);

            // For each section, display the sections info unless it has be designated as hidden.
            for (Section section : course.getSections()) {
                if (section.isHidden()) {
                    continue;
                }
                String daysOfTheWeek = section.getDaysOfTheWeek();
                String sectionString = course.getName() + "-" + section.getSectionNumber() + ": " + section.getType();
                Pane addedPane =
                        addSectionInfoToPane(pane, daysOfTheWeek, section.getStartTime(), section.getEndTime(), sectionString, currColor);
                section.setSchedulePane(addedPane);
            }
        }
    }

    private static Pane addSectionInfoToPane(Pane base, String days, int startTime, int endTime, String name) {
        Color color = new Color(1, 0.647, 0, .5);
        return addSectionInfoToPane(base, days, startTime, endTime, name, color);
    }

    private static Pane addSectionInfoToPane(Pane base, String days, int startTime, int endTime, String name, Color color) {
        Pane sectionPane = new Pane();
        for (int day = 0; day < Section.daysAbbr.length; day++) {
            if (days.contains(Section.daysAbbr[day])) {
                int startY = getTimeY(startTime);
                int endY = getTimeY(endTime);
                int calcHeight = endY - startY;

                Rectangle rect = JavaFXUtilities.createRect(width*day + width_split, startY, realWidth, calcHeight);
                rect.setFill(color);
                rect.setStroke(Color.BLACK);
                rect.strokeWidthProperty().setValue(2.0);

                Label label = new Label(name);
                label.relocate(width*day + width_split, startY);
                label.setMinWidth(realWidth);
                label.setMinHeight(calcHeight);
                label.setMaxWidth(realWidth);
                label.setMaxHeight(calcHeight);
                label.setTextFill(Color.BLACK);
                label.setWrapText(true);

                Pane frontPane = new Pane();
                frontPane.relocate(time_width, height);
                frontPane.getChildren().addAll(rect, label);
                sectionPane.getChildren().add(frontPane);
            }
        }
        base.getChildren().add(sectionPane);
        return sectionPane;
    }

    private static void fillListPane() {
        listVBox.getChildren().clear();

        double listHeight = minimzeList ? minimizedListHeight: regularListHeight;
        double rotation = minimzeList ? 0 : 180;

        listScrollPane.setMinHeight(listHeight);
        listScrollPane.setMaxHeight(listHeight);

//        HBox testBox = new HBox();
//        Label one = new Label("");
//        one.setPrefWidth(1000);
//        Label two = new Label("");
//        two.setPrefWidth(1000);

        Button hideShowButton = new Button("^");
        hideShowButton.setMinSize(75,25);
        hideShowButton.setMaxSize(75,25);
        hideShowButton.setRotate(rotation);
        hideShowButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                minimzeList = !minimzeList;
                fillListPane();
            }
        });

//        testBox.getChildren().addAll(one, hideShowButton, two);
        listVBox.getChildren().add(hideShowButton);

        if (minimzeList) {
            return;
        }

        for (Course course: currCourses) {
            boolean group = false;
            if (course.getSections() == null){
                continue;
            }
            if (course.getSections().length > 1) {
                group = true;
                Label label = new Label();
                String text = "Course: " + course.getName() + "\t\t";
                label.setText(text);

                Button button = new Button("Remove Entire Course");
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        currCourses.remove(course);
                        createSchedule();
                    }
                });

                HBox hBox = new HBox();
                hBox.getChildren().addAll(label, button);
                listVBox.getChildren().add(hBox);
            }
            for (Section section: course.getSections()) {
                ArrayList<Node> labels = new ArrayList<>();
                if (group) {
                    labels.add(JavaFXUtilities.createLabel("", 50));
                }
                labels.add(JavaFXUtilities.createLabel("Course: " + course.getName(), 200));
                labels.add(JavaFXUtilities.createLabel("Section: " + section.getSectionNumber(), 100));
                labels.add(JavaFXUtilities.createLabel("Type: " + section.getType(), 200));
                String timeText = "Time: " + Section.timeIntToString(section.getStartTime()) + "-" +
                        Section.timeIntToString(section.getEndTime());
                labels.add(JavaFXUtilities.createLabel(timeText, 150));
                labels.add(JavaFXUtilities.createLabel("Days: " + section.getDaysOfTheWeek(), 100));

                Button button;
                if (group) {
                    button = new Button("Add");
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            Course newCourse = new Course(course);
                            Section[] sections = new Section[1];
                            sections[0] = section;
                            newCourse.setSections(sections);
                            addCourse(newCourse);
                            createSchedule();
                        }
                    });
                }else {
                    button = new Button("Remove");
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            currCourses.remove(course);
                            createSchedule();
                        }
                    });
                }

                String hideOrShowText = section.isHidden() ? "Show" : "Hide";
                Button hideButton = new Button(hideOrShowText);
                hideButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        section.setHidden(!section.isHidden());
                        createSchedule();
                    }
                });

                HBox hBox = new HBox();
                hBox.setSpacing(2);
                hBox.getChildren().addAll(labels);
                hBox.getChildren().addAll(button, hideButton);
                listVBox.getChildren().add(hBox);
            }
        }
    }

    private static void fillListPaneSelected() {
        listVBox.getChildren().clear();

        minimzeList = false;
        double listHeight = minimzeList ? minimizedListHeight: regularListHeight;
        double rotation = minimzeList ? 0 : 180;

        listScrollPane.setMinHeight(listHeight);
        listScrollPane.setMaxHeight(listHeight);

        Button hideShowButton = new Button("^");
        hideShowButton.setMinSize(75,25);
        hideShowButton.setMaxSize(75,25);
        hideShowButton.setRotate(rotation);
        hideShowButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                minimzeList = !minimzeList;
                fillListPane();
            }
        });
        listVBox.getChildren().add(hideShowButton);

        if (minimzeList) {
            return;
        }

        for (Pair<Course, Boolean> pair: selectedCourses) {
            Course course = pair.getKey();
            boolean group = pair.getValue();

            for (Section section: course.getSections()) {
                ArrayList<Node> labels = new ArrayList<>();
                labels.add(JavaFXUtilities.createLabel("Course: " + course.getName(), 200));
                labels.add(JavaFXUtilities.createLabel("Section: " + section.getSectionNumber(), 100));
                labels.add(JavaFXUtilities.createLabel("Type: " + section.getType(), 200));
                String timeText = "Time: " + Section.timeIntToString(section.getStartTime()) + "-" +
                        Section.timeIntToString(section.getEndTime());
                labels.add(JavaFXUtilities.createLabel(timeText, 150));
                labels.add(JavaFXUtilities.createLabel("Days: " + section.getDaysOfTheWeek(), 100));

                Button button;
                if (group) {
                    button = new Button("Add");
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            Course newCourse = new Course(course);
                            Section[] sections = new Section[1];
                            sections[0] = section;
                            newCourse.setSections(sections);
                            addCourse(newCourse);
                            createSchedule();
                        }
                    });
                }else {
                    button = new Button("Remove");
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            currCourses.remove(course);
                            createSchedule();
                        }
                    });
                }

                String hideOrShowText = section.isHidden() ? "Show" : "Hide";
                Button hideButton = new Button(hideOrShowText);
                hideButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        section.setHidden(!section.isHidden());
                        createSchedule();
                    }
                });

                HBox hBox = new HBox();
                hBox.setSpacing(2);
                hBox.getChildren().addAll(labels);
                hBox.getChildren().addAll(button, hideButton);
                listVBox.getChildren().add(hBox);
            }
        }

    }

    private static int getTimeY(int time) {
        int hours = time/100;
        int min = time%100;
        int halfHours = hours * 2 + (min>=30 ? 1:0);
        min = min%30;
        int extra = (int)(((double)(min)/30.0)*height);
        int yVal = extra + halfHours*height;
        return yVal;
    }

    private static Pair<String, Integer> getCellPressed(double x, double y) {
        //Move 0,0 to the top left corner of the pane with all the time cells.
        double realX = x - time_width;
        double realY = y - height;

        //Get which column (day) it is and which row (halfHour) the click is in.
        double day = realX / width;
        double halfHours = realY / height;

        if (day > 0 && halfHours > 0) {
            String dayString = Section.daysAbbr[(int) (day)];

            int hours = (int)(halfHours) / 2;
            int halfHour = (halfHours%2) > 1 ? 1 : 0;
            int minutes = (int)((halfHours % 1) * 30);
            int timeInt = (hours * 100) + (halfHour * 30) + minutes;
            return new Pair<String, Integer>(dayString, timeInt);
        }

        return null;
    }

    private static Color setColorOpacity(Color color, double opacity) {
        double red = color.getRed();
        double green = color.getGreen();
        double blue = color.getBlue();
        return new Color(red, green, blue, opacity);
    }

    public static ArrayList<Course> getCurrCourses() {
        return currCourses;
    }
}
