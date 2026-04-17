package com.example.universeti.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Shelf {
    private final String id;
    private float x;
    private float y;
    private float w;
    private float h;

    public Shelf(String id, float x, float y, float w, float h) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public String getId() { return id; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getW() { return w; }
    public float getH() { return h; }

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }

    public boolean contains(float px, float py) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("x", (double) x);
        o.put("y", (double) y);
        o.put("w", (double) w);
        o.put("h", (double) h);
        return o;
    }

    public static Shelf fromJson(JSONObject o) throws JSONException {
        return new Shelf(
                o.getString("id"),
                (float) o.getDouble("x"),
                (float) o.getDouble("y"),
                (float) o.getDouble("w"),
                (float) o.getDouble("h")
        );
    }
}
