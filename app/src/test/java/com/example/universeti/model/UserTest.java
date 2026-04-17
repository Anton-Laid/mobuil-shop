package com.example.universeti.model;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UserTest {

    @Test
    public void jsonRoundtrip_preservesFields() throws Exception {
        User original = new User("anna", "secret", User.ROLE_USER);
        JSONObject json = original.toJson();
        User restored = User.fromJson(json);
        assertEquals("anna", restored.getUsername());
        assertEquals("secret", restored.getPassword());
        assertEquals(User.ROLE_USER, restored.getRole());
    }

    @Test
    public void roleConstants_matchExpectedValues() {
        assertEquals("user", User.ROLE_USER);
        assertEquals("operator", User.ROLE_OPERATOR);
    }
}
