package com.example.universeti.data;

import android.content.Context;

import com.example.universeti.model.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductRepository {
    private static final String FILE_NAME = "products.json";
    private final File file;

    public ProductRepository(Context context) {
        this.file = new File(context.getFilesDir(), FILE_NAME);
    }

    public List<Product> loadAll() {
        List<Product> list = new ArrayList<>();
        if (!file.exists()) return list;
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            int read = fis.read(data);
            if (read <= 0) return list;
            JSONArray arr = new JSONArray(new String(data, "UTF-8"));
            for (int i = 0; i < arr.length(); i++) {
                list.add(Product.fromJson(arr.getJSONObject(i)));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public String newId() {
        return UUID.randomUUID().toString();
    }

    public boolean upsert(Product product) {
        List<Product> list = loadAll();
        boolean replaced = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(product.getId())) {
                list.set(i, product);
                replaced = true;
                break;
            }
        }
        if (!replaced) list.add(product);
        return saveAll(list);
    }

    public boolean delete(String id) {
        List<Product> list = loadAll();
        boolean removed = list.removeIf(p -> p.getId().equals(id));
        if (!removed) return false;
        return saveAll(list);
    }

    private boolean saveAll(List<Product> list) {
        try {
            JSONArray arr = new JSONArray();
            for (Product p : list) arr.put(p.toJson());
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
