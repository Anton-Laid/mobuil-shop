package com.example.universeti.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.universeti.model.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;

@RunWith(RobolectricTestRunner.class)
public class UserRepositoryTest {

    private UserRepository repo;

    @Before
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        File f = new File(ctx.getFilesDir(), "users.json");
        if (f.exists()) f.delete();
        repo = new UserRepository(ctx);
    }

    @Test
    public void loadAll_whenEmpty_returnsEmptyList() {
        assertTrue(repo.loadAll().isEmpty());
    }

    @Test
    public void addUser_persistsAcrossReloads() {
        assertTrue(repo.addUser(new User("anna", "pass", User.ROLE_USER)));
        assertEquals(1, repo.loadAll().size());
        assertEquals("anna", repo.loadAll().get(0).getUsername());
    }

    @Test
    public void addUser_duplicateUsername_returnsFalse() {
        repo.addUser(new User("anna", "pass", User.ROLE_USER));
        assertFalse(repo.addUser(new User("anna", "other", User.ROLE_OPERATOR)));
        assertEquals(1, repo.loadAll().size());
    }

    @Test
    public void userExists_caseInsensitive() {
        repo.addUser(new User("anna", "pass", User.ROLE_USER));
        assertTrue(repo.userExists("ANNA"));
        assertTrue(repo.userExists("Anna"));
        assertFalse(repo.userExists("bob"));
    }

    @Test
    public void findByCredentials_correctCreds_returnsUser() {
        repo.addUser(new User("anna", "pass", User.ROLE_USER));
        User u = repo.findByCredentials("anna", "pass");
        assertNotNull(u);
        assertEquals(User.ROLE_USER, u.getRole());
    }

    @Test
    public void findByCredentials_wrongPassword_returnsNull() {
        repo.addUser(new User("anna", "pass", User.ROLE_USER));
        assertNull(repo.findByCredentials("anna", "wrong"));
    }

    @Test
    public void findByCredentials_wrongUsername_returnsNull() {
        repo.addUser(new User("anna", "pass", User.ROLE_USER));
        assertNull(repo.findByCredentials("bob", "pass"));
    }

    @Test
    public void findByCredentials_usernameIsCaseInsensitive() {
        repo.addUser(new User("anna", "pass", User.ROLE_USER));
        assertNotNull(repo.findByCredentials("ANNA", "pass"));
    }
}
