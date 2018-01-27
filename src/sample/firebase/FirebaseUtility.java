package sample.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

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
        return database.getReference("department_names");
    }

    public static DatabaseReference getDepartmentJson(String departmentAbbr) {
        return database.getReference("departments/" + departmentAbbr);
    }

    public static void addSingleListener(DatabaseReference ref) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
