package com.example.universeti.data;

import android.content.Context;

import com.example.universeti.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private static final String FILE_NAME = "users.json";
    private final File file;

    public UserRepository(Context context) {
        this.file = new File(context.getFilesDir(), FILE_NAME);
    }

    public List<User> loadAll() {
        List<User> users = new ArrayList<>();
        if (!file.exists()) {
            return users;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            int read = fis.read(data);
            if (read <= 0) return users;
            String json = new String(data, "UTF-8");
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                users.add(User.fromJson(arr.getJSONObject(i)));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean userExists(String username) {
        for (User u : loadAll()) {
            if (u.getUsername().equalsIgnoreCase(username)) return true;
        }
        return false;
    }

    public User findByCredentials(String username, String password) {
        for (User u : loadAll()) {
            if (u.getUsername().equalsIgnoreCase(username)
                    && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public boolean addUser(User user) {
        if (userExists(user.getUsername())) return false;
        List<User> users = loadAll();
        users.add(user);
        return saveAll(users);
    }

    private boolean saveAll(List<User> users) {
        try {
            JSONArray arr = new JSONArray();
            for (User u : users) {
                arr.put(u.toJson());
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(arr.toString(2).getBytes("UTF-8"));
            }
            return true;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
}
