package com.example.universeti.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShelfTest {

    @Test
    public void contains_pointInside_returnsTrue() {
        Shelf s = new Shelf("shelf-1", 0.1f, 0.2f, 0.3f, 0.4f);
        assertTrue(s.contains(0.2f, 0.3f));
    }

    @Test
    public void contains_pointOnEdge_returnsTrue() {
        Shelf s = new Shelf("shelf-1", 0.1f, 0.2f, 0.3f, 0.4f);
        assertTrue(s.contains(0.1f, 0.2f));
        assertTrue(s.contains(0.4f, 0.6f));
    }

    @Test
    public void contains_pointOutside_returnsFalse() {
        Shelf s = new Shelf("shelf-1", 0.1f, 0.2f, 0.3f, 0.4f);
        assertFalse(s.contains(0.05f, 0.3f));
        assertFalse(s.contains(0.5f, 0.3f));
        assertFalse(s.contains(0.2f, 0.7f));
    }

    @Test
    public void setters_updatePositionButNotSize() {
        Shelf s = new Shelf("shelf-1", 0.1f, 0.2f, 0.3f, 0.4f);
        s.setX(0.5f);
        s.setY(0.6f);
        assertEquals(0.5f, s.getX(), 0.0001f);
        assertEquals(0.6f, s.getY(), 0.0001f);
        assertEquals(0.3f, s.getW(), 0.0001f);
        assertEquals(0.4f, s.getH(), 0.0001f);
    }

    @Test
    public void jsonRoundtrip_preservesFields() throws Exception {
        Shelf original = new Shelf("shelf-1", 0.1f, 0.2f, 0.3f, 0.4f);
        JSONObject json = original.toJson();
        Shelf restored = Shelf.fromJson(json);
        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getX(), restored.getX(), 0.0001f);
        assertEquals(original.getY(), restored.getY(), 0.0001f);
        assertEquals(original.getW(), restored.getW(), 0.0001f);
        assertEquals(original.getH(), restored.getH(), 0.0001f);
    }
}
