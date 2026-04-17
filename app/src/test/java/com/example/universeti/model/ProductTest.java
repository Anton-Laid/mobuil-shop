package com.example.universeti.model;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ProductTest {

    @Test
    public void jsonRoundtrip_preservesAllFields() throws Exception {
        Product original = new Product("id-1", "Bread", 50, "/path/img.png", 0.25f, 0.75f);
        JSONObject json = original.toJson();
        Product restored = Product.fromJson(json);
        assertEquals("id-1", restored.getId());
        assertEquals("Bread", restored.getName());
        assertEquals(50, restored.getPrice());
        assertEquals("/path/img.png", restored.getImageUrl());
        assertEquals(0.25f, restored.getMapX(), 0.0001f);
        assertEquals(0.75f, restored.getMapY(), 0.0001f);
    }

    @Test
    public void fromJson_missingOptionalFields_usesDefaults() throws Exception {
        JSONObject json = new JSONObject();
        json.put("id", "id-1");
        json.put("name", "Bread");
        json.put("price", 50);
        Product p = Product.fromJson(json);
        assertEquals("", p.getImageUrl());
        assertEquals(-1f, p.getMapX(), 0.0001f);
        assertEquals(-1f, p.getMapY(), 0.0001f);
    }

    @Test
    public void setters_mutateFields() {
        Product p = new Product("id-1", "Bread", 50, "", -1, -1);
        p.setName("Milk");
        p.setPrice(100);
        p.setImageUrl("/new/path");
        p.setMapX(0.1f);
        p.setMapY(0.2f);
        assertEquals("Milk", p.getName());
        assertEquals(100, p.getPrice());
        assertEquals("/new/path", p.getImageUrl());
        assertEquals(0.1f, p.getMapX(), 0.0001f);
        assertEquals(0.2f, p.getMapY(), 0.0001f);
    }
}
