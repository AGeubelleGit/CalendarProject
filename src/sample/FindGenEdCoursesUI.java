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

public class FindGenEdCoursesUI {

    private static Scene scene;
    private static Pane pane;

    private static Main main;
    private static final double windowWidth = 850;

    public static void initialize(Main inputMain) {
        main = inputMain;

        pane = new Pane(null);
        scene = new Scene(pane, windowWidth, 550);

        // Create the scene using resetScene.
        scene = resetScene(null);
    }

    public static Scene resetScene(CourseSectionFilter newFilter) {
        return scene;
    }

    public static Scene getScene() {
        return scene;
    }

}

