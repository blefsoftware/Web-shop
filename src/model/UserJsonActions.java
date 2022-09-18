package model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.ArrayList;

public class UserJsonActions {

    public static boolean writeToJSON(ArrayList<User> list, String path) {
        try {
            Writer writer = new FileWriter(path);
            Gson gson = new Gson();
            gson.toJson(list, writer);
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<User> readFromJson(String path) {
        if (!new File(path).exists()) {
            return new ArrayList<>();
        }
        try {
            JsonReader reader = new JsonReader(new FileReader(path));
            Gson gson = new Gson();
            return gson.fromJson(reader, new TypeToken<ArrayList<User>>() {
            }.getType());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
