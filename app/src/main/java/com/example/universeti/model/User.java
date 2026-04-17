package com.example.universeti.model;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    public static final String ROLE_USER = "user";
    public static final String ROLE_OPERATOR = "operator";

    private final String username;
    private final String password;
    private final String role;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("username", username);
        obj.put("password", password);
        obj.put("role", role);
        return obj;
    }

    public static User fromJson(JSONObject obj) throws JSONException {
        return new User(
                obj.getString("username"),
                obj.getString("password"),
                obj.getString("role")
        );
    }
}
