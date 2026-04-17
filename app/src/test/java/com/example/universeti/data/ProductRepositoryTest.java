package com.example.universeti.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.universeti.model.Product;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class ProductRepositoryTest {

    private ProductRepository repo;

    @Before
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        File f = new File(ctx.getFilesDir(), "products.json");
        if (f.exists()) f.delete();
        repo = new ProductRepository(ctx);
    }

    @Test
    public void loadAll_whenEmpty_returnsEmptyList() {
        assertTrue(repo.loadAll().isEmpty());
    }

    @Test
    public void newId_returnsUniqueIds() {
        String a = repo.newId();
        String b = repo.newId();
        assertNotNull(a);
        assertFalse(a.equals(b));
    }

    @Test
    public void upsert_addsNewProduct() {
        Product p = new Product("id-1", "Bread", 50, "", 0.1f, 0.2f);
        assertTrue(repo.upsert(p));
        List<Product> list = repo.loadAll();
        assertEquals(1, list.size());
        assertEquals("Bread", list.get(0).getName());
    }

    @Test
    public void upsert_existingId_replacesProduct() {
        repo.upsert(new Product("id-1", "Bread", 50, "", 0.1f, 0.2f));
        repo.upsert(new Product("id-1", "Milk", 100, "", 0.3f, 0.4f));
        List<Product> list = repo.loadAll();
        assertEquals(1, list.size());
        assertEquals("Milk", list.get(0).getName());
        assertEquals(100, list.get(0).getPrice());
    }

    @Test
    public void delete_removesProduct() {
        repo.upsert(new Product("id-1", "Bread", 50, "", 0.1f, 0.2f));
        repo.upsert(new Product("id-2", "Milk", 100, "", 0.3f, 0.4f));
        assertTrue(repo.delete("id-1"));
        List<Product> list = repo.loadAll();
        assertEquals(1, list.size());
        assertEquals("id-2", list.get(0).getId());
    }

    @Test
    public void delete_missingId_returnsFalse() {
        repo.upsert(new Product("id-1", "Bread", 50, "", 0.1f, 0.2f));
        assertFalse(repo.delete("missing"));
        assertEquals(1, repo.loadAll().size());
    }

    @Test
    public void loadAll_persistsImageUrlAndCoords() {
        repo.upsert(new Product("id-1", "Bread", 50, "/local/path.png", 0.25f, 0.75f));
        Product loaded = repo.loadAll().get(0);
        assertEquals("/local/path.png", loaded.getImageUrl());
        assertEquals(0.25f, loaded.getMapX(), 0.0001f);
        assertEquals(0.75f, loaded.getMapY(), 0.0001f);
    }
}
