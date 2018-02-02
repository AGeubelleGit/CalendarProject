package sample.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import sample.schedule.GenEdCategories;

import java.io.FileInputStream;
import java.util.HashMap;

public class FirebaseUtility {

    public static FirebaseDatabase database = null;

    public static void initialize() {
        FileInputStream serviceAccount = null;
        FirebaseOptions options = null;

        try {
            serviceAccount = new FileInputStream("/Users/alexandregeubelle/Documents/workspace/firebaseTestProject/src/sample/uiucscheduledatabase-firebase-adminsdk-12s6b-3fcd281eb7.json");
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://uiucscheduledatabase.firebaseio.com/")
                    .build();
        } catch (Exception e) {
            return;
        }

        FirebaseApp.initializeApp(options);
        database = FirebaseDatabase.getInstance();
    }

    public static DatabaseReference getDepartmentNamesReference() {
        if (database == null) {
            return null;
        } else {
            return database.getReference("department_names");
        }
    }

    public static DatabaseReference getDepartmentReference(String departmentAbbr) {
        return database.getReference("departments/" + departmentAbbr);
    }

    public static DatabaseReference getCourseReference(String departmentAbbr, String courseAbbr) {
        return database.getReference("departments/" + departmentAbbr + "/" + courseAbbr);
    }

    public static DatabaseReference getGenEdListReference() {
        return database.getReference("gen_ed_courses");
    }

    public static DatabaseReference getGenEdListReference(GenEdCategories category) {
        return database.getReference("gen_ed_courses/" + category.name());
    }

}
