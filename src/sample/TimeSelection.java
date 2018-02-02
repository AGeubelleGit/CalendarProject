package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;
import sample.schedule.Course;
import sample.schedule.Section;

import java.awt.*;
import java.util.ArrayList;

public class TimeSelection {

    private static Main main;

    private static Pair<Double, Double> startDrag; //x,y
    private static Pair<Double, Double> endDrag;

    private static boolean[][] cells; //half hours by days
    private static Rectangle[][] cellRects;

    private static Scene retScene;
    private static VBox vBox;
    private static Pane basePane;
    private static double timeSelectionScrollStartValue = 0.5;

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

    public static void initialize(Main inputMain) {
        main = inputMain;

        vBox = new VBox();
        retScene = new Scene(vBox);

        cells = new boolean[48][5];
        setAllCellsToValue(false, false);
    }

    public static Scene createTimeSelectionScene() {
        startDrag = null;
        endDrag = null;
        cellRects = new Rectangle[48][5];

        basePane = createBaseLayout();
        updateCells(); //Call update cells so that each time it will show that last user selection.

        basePane.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                startDrag = new Pair<Double, Double>(event.getX(), event.getY());
            }
        });

        basePane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                endDrag = new Pair<Double, Double>(event.getX(), event.getY());
                Pair<Integer, Integer> startCell = getCellPressed(startDrag.getKey(), startDrag.getValue());
                Pair<Integer, Integer> endCell = getCellPressed(endDrag.getKey(), endDrag.getValue());

                if (startCell == null || endCell == null) {
                    return;
                }

                int startX = Integer.min(startCell.getKey(), endCell.getKey());
                int endX = Integer.max(startCell.getKey(), endCell.getKey());
                int startY = Integer.min(startCell.getValue(), endCell.getValue());
                int endY = Integer.max(startCell.getValue(), endCell.getValue());

                boolean allWereSelected = true;
                for (int x = startX; x <= endX; x++) {
                    for (int y = startY; y <= endY; y++) {
                        allWereSelected = allWereSelected && cells[x][y]; // all selected becomes false if any of the cells were false
                        cells[x][y] = true;
                    }
                }
                //If all of the cells in the range were already selected, unselect them.
                if (allWereSelected) {
                    for (int x = startX; x <= endX; x++) {
                        for (int y = startY; y <= endY; y++) {
                            cells[x][y] = false;
                        }
                    }
                }

                updateCells();
            }
        });

        vBox.getChildren().clear();

        HBox menu = createMenuBar();

        ScrollPane scrollPane = new ScrollPane(basePane);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVvalue(timeSelectionScrollStartValue);
        scrollPane.setOnScrollFinished(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                timeSelectionScrollStartValue = scrollPane.getVvalue();
            }
        });

        vBox.getChildren().addAll(menu, scrollPane);
        return retScene;
    }

    private static HBox createMenuBar() {
        HBox menuBar = new HBox();
        final double spacing = 10;
        menuBar.setSpacing(spacing);
        menuBar.setPadding(new Insets(5, spacing, 5, spacing));

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefHeight(35);
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                main.setSceneInputCourses();
            }
        });

        Button clearButton = new Button("Clear");
        clearButton.setPrefHeight(35);
        clearButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setAllCellsToValue(false);
            }
        });

        Button selectAllButton = new Button("SelectAll");
        selectAllButton.setPrefHeight(35);
        selectAllButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setAllCellsToValue(true);
            }
        });

        Button submitButton = new Button("Submit");
        submitButton.setPrefHeight(35);
        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                main.setSceneInputCourses(createFilter());
            }
        });

        Button noOverlapButton = new Button("No Overlap");
        noOverlapButton.setPrefHeight(35);
        noOverlapButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setCellsNoOverlapWithCurrCourses();
            }
        });

        javafx.scene.control.Label zoomLabel = new Label("Zoom");
        zoomLabel.setPrefHeight(35);
        zoomLabel.setPadding(new Insets(0,0,0,10));

        Button zoomInButton = new Button("+");
        zoomInButton.setPrefHeight(35);
        zoomInButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                height = Integer.min(height+zoomAmount, maxHeight);
                realHeight = height-(height_split*2);
                main.setSceneTimeSelection();
            }
        });

        Button zoomOutButton = new Button("-");
        zoomOutButton.setPrefHeight(35);
        zoomOutButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                height = Integer.max(height-zoomAmount, minHeight);
                realHeight = height-(height_split*2);
                main.setSceneTimeSelection();
            }
        });

        menuBar.getChildren().addAll(cancelButton, clearButton, selectAllButton, noOverlapButton, submitButton,
                zoomLabel, zoomInButton, zoomOutButton);
        return menuBar;
    }

    private static void updateCells() {
        for (int r = 0; r < 48; r++) {
            for (int c = 0; c < 5; c++) {
                if (cells[r][c]) {
                    cellRects[r][c].setFill(Color.LIGHTGREEN);
                }else{
                    cellRects[r][c].setFill(Color.WHITE);
                }
            }
        }
    }

    private static boolean cellsPointsEqual(Pair<Integer, Integer> first, Pair<Integer, Integer> second) {
        return (first.getKey() == second.getKey()) && (first.getValue() == second.getValue());
    }

    private static Pair<Integer, Integer> getCellPressed(double x, double y) {
        //Move 0,0 to the top left corner of the pane with all the time cells.
        double realX = x - time_width;
        double realY = y - height;

        //Get which column (day) it is and which row (halfHour) the click is in.
        double day = realX / width;
        double halfHours = realY / height;


        if (day > 0 && halfHours > 0) {
            return new Pair<Integer, Integer>((int)(halfHours), (int)(day));
        }

        return null;
    }

    public static Pane createBaseLayout() {
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

        return parentPane;
    }

    public static Pane createCellsPane() {
        //Create a base pane containing all the time slot cells
        Pane cellPane = new Pane();
        cellPane.relocate(time_width, height);
        // For each half hour.
        for (int r = 0; r < 48; r++) {
            // For each day of the week.
            for (int c=0; c<5; c++) {
                Rectangle rect = JavaFXUtilities.createRect(width*c + width_split,
                        height*r + height_split, realWidth, realHeight);

                rect.setFill(Color.WHITE);
                cellRects[r][c] = rect;
                cellPane.getChildren().add(rect);
            }
        }

        return cellPane;
    }

    private static CourseSectionFilter createFilter() {
        return new CourseSectionFilter() {
            @Override
            public boolean filter(Section section) {
                boolean fits = true;
                for (int d=0; d < 5; d++) {
                    if (section.getDaysOfTheWeek() == null) {
                        return false;
                    }
                    if (section.getDaysOfTheWeek().contains(Section.daysAbbr[d])) {
                        int startRow = getHalfHours(section.getStartTime());
                        int endRow = getHalfHours(section.getEndTime());
                        for (int r=startRow; r<=endRow; r++) {
                            fits = fits && cells[r][d]; //fits becomes false if any of the cells are false.
                        }
                    }
                }
                //System.out.println("Section: " + section.getSectionNumber());
                //System.out.println("True/False: " + fits);
                return fits;
            }
            @Override
            public CourseSectionFilter copy() {
                return new CourseSectionFilter() {
                    @Override
                    public boolean filter(Section section) {
                        boolean fits = true;
                        for (int d=0; d < 5; d++) {
                            if (section.getDaysOfTheWeek() == null) {
                                return false;
                            }
                            if (section.getDaysOfTheWeek().contains(Section.daysAbbr[d])) {
                                int startRow = getHalfHours(section.getStartTime());
                                int endRow = getHalfHours(section.getEndTime());
                                for (int r=startRow; r<=endRow; r++) {
                                    fits = fits && cells[r][d]; //fits becomes false if any of the cells are false.
                                }
                            }
                        }
                        return fits;
                    }

                    @Override
                    public CourseSectionFilter copy() {
                        return null;
                    }
                };
            };
        };
    }

    private static int getHalfHours(int time) {
        int hours = time/100;
        int min = time%100;
        return hours*2 + (min >= 30 ? 1 : 0);
    }

    private static void setCellsNoOverlapWithCurrCourses() {
        ArrayList<Course> courses = ScheduleInfo.getCurrCourses();
        setAllCellsToValue(true);
        for (Course course: courses) {
            if (course.getSections() == null || course.getSections().length > 1) {
                continue;
            }
            for (Section section: course.getSections()) {
                for (int d=0; d < 5; d++) {
                    if (section.getDaysOfTheWeek().contains(Section.daysAbbr[d])) {
                        int startCellY = getHalfHours(section.getStartTime());
                        int endCellY = getHalfHours(section.getEndTime());
                        for (int r = startCellY; r <= endCellY; r++) {
                            cells[r][d] = false;
                        }
                    }
                }
            }
        }
        updateCells();
    }

    private static void setAllCellsToValue(boolean value) {
        setAllCellsToValue(value, true);
    }

    private static void setAllCellsToValue(boolean value, boolean update) {
        for (int r = 0; r < 48; r++) {
            for (int c = 0; c < 5; c++) {
                cells[r][c] = value;
            }
        }
        if (update) {
            updateCells();
        }
    }

    public static Pane createTimesOfTheDayPane() {
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
}
