package com.example.universeti.data;

import android.content.Context;

import com.example.universeti.model.Shelf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShelfRepository {
    public static final int MAX_SHELVES = 4;
    private static final String FILE_NAME = "shelves.json";
    private final File file;

    public ShelfRepository(Context context) {
        this.file = new File(context.getFilesDir(), FILE_NAME);
    }

    public List<Shelf> loadAll() {
        List<Shelf> list = new ArrayList<>();
        if (!file.exists()) {
            list.addAll(defaults());
            saveAll(list);
            return list;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            int read = fis.read(data);
            if (read <= 0) return list;
            JSONArray arr = new JSONArray(new String(data, "UTF-8"));
            for (int i = 0; i < arr.length(); i++) {
                list.add(Shelf.fromJson(arr.getJSONObject(i)));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean saveAll(List<Shelf> list) {
        try {
            JSONArray arr = new JSONArray();
            for (Shelf s : list) arr.put(s.toJson());
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(arr.toString(2).getBytes("UTF-8"));
            }
            return true;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<Shelf> defaults() {
        List<Shelf> d = new ArrayList<>(MAX_SHELVES);
        d.add(new Shelf("shelf-1", 0.08f, 0.18f, 0.36f, 0.14f));
        d.add(new Shelf("shelf-2", 0.56f, 0.18f, 0.36f, 0.14f));
        d.add(new Shelf("shelf-3", 0.08f, 0.55f, 0.36f, 0.14f));
        d.add(new Shelf("shelf-4", 0.56f, 0.55f, 0.36f, 0.14f));
        return d;
    }
}
