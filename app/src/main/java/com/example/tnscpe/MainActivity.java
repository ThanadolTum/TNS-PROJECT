package com.example.tnscpe;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private SharedPreferences pref;
    private ProgressDialog progressdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        pref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        if (!pref.getString("CurrentEmailUser", "No data").equals("No data") & firebaseUser != null) {
            Intent intent = new Intent(this, HomePage.class);
            startActivity(intent);
        }
    }

    protected void onStart() {
        super.onStart();
        final EditText inputEmail = findViewById(R.id.input_email);
        final EditText inputPassword = findViewById(R.id.input_password);
        final Button buttonSignIn = findViewById(R.id.button_sign_in);
        final TextView registerLink = findViewById(R.id.link_sign_up);
        final TextView resetPassword = findViewById(R.id.text_forgetpassword);
        final CheckBox rememberUser = findViewById(R.id.checkbox_rememberme);

        inputEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String email = inputEmail.getText().toString().trim();
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("RememberEmail", email);
                editor.apply();
            }
        });

        rememberUser.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("RememberEmailStatus", isChecked);
            editor.apply();
        });

        boolean checkboxRememberStatus = pref.getBoolean("RememberEmailStatus", false);
        rememberUser.setChecked(checkboxRememberStatus);
        if (checkboxRememberStatus) {
            inputEmail.setText(pref.getString("RememberEmail", ""));
        }

        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        resetPassword.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            if (!email.isEmpty()) {
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "กรุณาตรวจสอบอีเมลล์ของท่านเพื่อรีเซ็ตรหัสผ่าน", Toast.LENGTH_LONG).show();
                        }
                    });
            } else {
                inputEmail.setError("กรุณากรอกอีเมลล์เพื่อใช้ในการรีเซ็ตรหัสผ่าน");
            }
        });

        buttonSignIn.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            byte[] encodePassword = Base64.encode(password.getBytes(),Base64.DEFAULT);
            String passwordEncode = new String(encodePassword);
            if (TextUtils.isEmpty(email)) {
                inputEmail.setError("กรุณากรอกอีเมลล์");
                return;
            } else if (TextUtils.isEmpty(password)) {
                inputPassword.setError("กรุณากรอกรหัสผ่าน");
                return;
            }

            progressdialog = new ProgressDialog(this);
            progressdialog.setMessage("กรุณารอสักครู่...");
            progressdialog.show();
            progressdialog.setCanceledOnTouchOutside(false);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(MainActivity.this, task -> {
                        if (task.isSuccessful()) {
                            if (Objects.requireNonNull(mAuth.getCurrentUser()).isEmailVerified()) {
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("CurrentEmailUser", email);
                                editor.putString("CurrentPasswordUser",passwordEncode);
                                editor.commit();
                                Intent intent = new Intent(MainActivity.this, HomePage.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "กรุณาตรวจสอบอีเมลล์ของท่าน เพื่อยืนยันตัวตน", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                        progressdialog.dismiss();
                    });
        });
    }
}