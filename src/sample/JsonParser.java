package sample;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import sample.schedule.Course;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
}
