package com.example.universeti.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.universeti.R;
import com.example.universeti.data.UserRepository;
import com.example.universeti.model.User;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.login_title);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnGoRegister = findViewById(R.id.btnGoRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { attemptLogin(); }
        });

        btnGoRegister.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.err_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        UserRepository repo = new UserRepository(this);
        User user = repo.findByCredentials(username, password);
        if (user == null) {
            Toast.makeText(this, R.string.err_invalid_creds, Toast.LENGTH_SHORT).show();
            return;
        }

        Class<?> target = User.ROLE_OPERATOR.equals(user.getRole())
                ? OperatorHomeActivity.class
                : UserHomeActivity.class;
        Intent intent = new Intent(this, target);
        intent.putExtra("username", user.getUsername());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
