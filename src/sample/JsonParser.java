package sample;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import sample.schedule.Course;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class JsonParser {

    public static HashMap<String, HashMap<String, Course>> getCourses(String filePath) {
        File file = new File(filePath);
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
        } catch (Exception e) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(bufferedReader, new TypeToken<HashMap<String, HashMap<String, Course>>>() {}.getType());

    }

    public static void saveCourses(String filePath, ArrayList<Course> courses) {
        try {
            File file = new File(filePath);
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);

            Gson gson = new Gson();
            String json_string = gson.toJson(courses);
            System.out.println(json_string);

            fileWriter.write(json_string);
            fileWriter.flush();
            fileWriter.close();
        }catch (Exception e) {
            System.out.println("ERROR");
        }

    }

    public static ArrayList<Course> loadSaved(String filePath) {
        File file = new File(filePath);
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
        } catch (Exception e) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(bufferedReader, new TypeToken<ArrayList<Course>>() {}.getType());
    }
}
