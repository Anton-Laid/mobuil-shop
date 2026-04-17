package com.example.universeti.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Product {
    private final String id;
    private String name;
    private int price;
    private String imageUrl;
    private float mapX;
    private float mapY;

    public Product(String id, String name, int price, String imageUrl, float mapX, float mapY) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.mapX = mapX;
        this.mapY = mapY;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public float getMapX() { return mapX; }
    public float getMapY() { return mapY; }

    public void setName(String name) { this.name = name; }
    public void setPrice(int price) { this.price = price; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setMapX(float mapX) { this.mapX = mapX; }
    public void setMapY(float mapY) { this.mapY = mapY; }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("name", name);
        o.put("price", price);
        o.put("imageUrl", imageUrl);
        o.put("mapX", (double) mapX);
        o.put("mapY", (double) mapY);
        return o;
    }

    public static Product fromJson(JSONObject o) throws JSONException {
        return new Product(
                o.getString("id"),
                o.getString("name"),
                o.getInt("price"),
                o.optString("imageUrl", ""),
                (float) o.optDouble("mapX", -1),
                (float) o.optDouble("mapY", -1)
        );
    }
}
