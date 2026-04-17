package com.example.universeti.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.universeti.model.Shelf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class ShelfRepositoryTest {

    private ShelfRepository repo;

    @Before
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        File f = new File(ctx.getFilesDir(), "shelves.json");
        if (f.exists()) f.delete();
        repo = new ShelfRepository(ctx);
    }

    @Test
    public void loadAll_firstCall_returnsFourDefaults() {
        List<Shelf> shelves = repo.loadAll();
        assertEquals(ShelfRepository.MAX_SHELVES, shelves.size());
        for (Shelf s : shelves) {
            assertNotNull(s.getId());
            assertTrue(s.getW() > 0);
            assertTrue(s.getH() > 0);
        }
    }

    @Test
    public void loadAll_afterSave_returnsSavedList() {
        List<Shelf> custom = new ArrayList<>();
        custom.add(new Shelf("s-1", 0.1f, 0.2f, 0.3f, 0.4f));
        custom.add(new Shelf("s-2", 0.5f, 0.6f, 0.2f, 0.1f));
        assertTrue(repo.saveAll(custom));

        List<Shelf> loaded = repo.loadAll();
        assertEquals(2, loaded.size());
        assertEquals("s-1", loaded.get(0).getId());
        assertEquals(0.1f, loaded.get(0).getX(), 0.0001f);
        assertEquals("s-2", loaded.get(1).getId());
    }

    @Test
    public void saveAll_updatesPositions() {
        List<Shelf> shelves = repo.loadAll();
        shelves.get(0).setX(0.9f);
        shelves.get(0).setY(0.9f);
        repo.saveAll(shelves);

        Shelf reloaded = repo.loadAll().get(0);
        assertEquals(0.9f, reloaded.getX(), 0.0001f);
        assertEquals(0.9f, reloaded.getY(), 0.0001f);
    }

    @Test
    public void defaultShelves_haveUniqueIds() {
        List<Shelf> shelves = repo.loadAll();
        for (int i = 0; i < shelves.size(); i++) {
            for (int j = i + 1; j < shelves.size(); j++) {
                assertTrue("Duplicate shelf id: " + shelves.get(i).getId(),
                        !shelves.get(i).getId().equals(shelves.get(j).getId()));
            }
        }
    }
}
