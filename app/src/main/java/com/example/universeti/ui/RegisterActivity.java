package com.example.universeti.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.universeti.R;
import com.example.universeti.data.UserRepository;
import com.example.universeti.model.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private EditText etPasswordConfirm;
    private RadioGroup rgRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle(R.string.register_title);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        rgRole = findViewById(R.id.rgRole);
        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnBack = findViewById(R.id.btnBackToLogin);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { attemptRegister(); }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
    }

    private void attemptRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirm = etPasswordConfirm.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
            Toast.makeText(this, R.string.err_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, R.string.err_passwords_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        String role = rgRole.getCheckedRadioButtonId() == R.id.rbOperator
                ? User.ROLE_OPERATOR : User.ROLE_USER;

        UserRepository repo = new UserRepository(this);
        boolean added = repo.addUser(new User(username, password, role));
        if (!added) {
            Toast.makeText(this, R.string.err_user_exists, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, R.string.msg_registered, Toast.LENGTH_SHORT).show();
        finish();
    }
}
