package com.example.tnscpe;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class SignUpActivity extends AppCompatActivity {

    private final ManageDataUserCollection manageDataUserCollection = new ManageDataUserCollection();
    private final String[] department = new String[]{
            "วิศวกรรมโยธา", "วิศวกรรมสิ่งแวดล้อม", "วิศวกรรมไฟฟ้า", "วิศวกรรมเครื่องกล", "วิศวกรรมอุตสาหการ",
            "วิศวกรรมสิ่งทอ", "วิศวกรรมอิเล็กทรอนิกส์และโทรคมนาคม", "วิศวกรรมคอมพิวเตอร์", "วิศวกรรมเคมี",
            "วิศวกรรมวัสดุ", "วิศวกรรมเครื่องจักรกลเกษตร", "วิศวกรรมเกษตรอุตสาหกรรม", "วิศวกรรมชลประทานและการจัดการน้ำ",
            "วิศวกรรมอาหาร ", "วิศวกรรมอิเล็กทรอนิกส์อากาศยาน", "วิศวกรรมเคมีสิ่งทอและเส้นใย", "วิศวกรรมนวัตกรรมสิ่งทอ",
            "วิศวกรรมระบบราง", "วิศวกรรมพลังงานและวัสดุ"
    };
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = mAuth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    protected void onStart() {
        super.onStart();
        final EditText registerName = findViewById(R.id.input_sign_up_name);
        final EditText registerSurname = findViewById(R.id.input_sign_up_surname);
        final AutoCompleteTextView registerDepartment = findViewById(R.id.input_sign_up_department);
        final EditText registerEmail = findViewById(R.id.input_sign_up_email);
        final EditText registerPassword = findViewById(R.id.input_sign_up_password);
        final EditText registerVerifyPassword = findViewById(R.id.input_sign_up_verify_password);
        final Button buttonRegister = findViewById(R.id.button_sign_up);
        final TextView linkSignIn = findViewById(R.id.link_sign_in);

        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(this, R.layout.department_item, department);
        registerDepartment.setAdapter(departmentAdapter);

        linkSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
        });

        buttonRegister.setOnClickListener(v -> {
            String name = registerName.getText().toString().trim();
            String surname = registerSurname.getText().toString().trim();
            String department = registerDepartment.getText().toString().trim();
            String email = registerEmail.getText().toString().trim();
            String password = registerPassword.getText().toString().trim();
            byte[] encodePassword = Base64.encode(password.getBytes(), Base64.DEFAULT);
            String passwordEncode = new String(encodePassword);
            String password_verify = registerVerifyPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                registerName.setError("กรุณากรอกชื่อ");
                return;
            } else if (TextUtils.isEmpty(surname)) {
                registerSurname.setError("กรุณากรอกนามสกุล");
                return;
            } else if (TextUtils.isEmpty(department)) {
                registerDepartment.setError("กรุณาเลือกภาควิชา");
                return;
            } else if (TextUtils.isEmpty(email)) {
                registerEmail.setError("กรุณากรอกอีเมลล์");
                return;
            } else if (!invalidEmail(email)) {
                registerEmail.setError("อีเมลล์ไม่ถูกต้อง");
                return;
            } else if (TextUtils.isEmpty(password)) {
                registerPassword.setError("กรุณากรอกรหัสผ่าน");
                return;
            } else if (!password.equals(password_verify)) {
                registerVerifyPassword.setError("กรุณาตรวจสอบรหัสผ่านอีกครั้ง");
                return;
            } else if (password.length() < 6 | password.length() > 18) {
                registerPassword.setError("รหัสผ่านควรมีมากกว่า 6 ตัวและน้อยกว่า 18 ตัว");
                return;
            } else {
                Dialog dialogSignUp = new Dialog(this);
                dialogSignUp.setContentView(R.layout.sign_up_dialog);
                dialogSignUp.setCanceledOnTouchOutside(false);
                ImageView imageCancelSignUpDialog = dialogSignUp.findViewById(R.id.image_sign_up_cancel);
                TextInputEditText passwordForSignUp = dialogSignUp.findViewById(R.id.key_sign_up);
                Button buttonSignUp = dialogSignUp.findViewById(R.id.button_confirm_key_sign_up);

                imageCancelSignUpDialog.setOnClickListener(v1 -> {
                    dialogSignUp.dismiss();
                });

                buttonSignUp.setOnClickListener(v1 -> {
                    if (passwordForSignUp.getText().toString().isEmpty()) {
                        passwordForSignUp.setError("กรุณากรอกรหัสผ่าน");
                    } else {
                        String getPasswordForSignUp = passwordForSignUp.getText().toString().trim();
                        db.collection("users")
                                .whereEqualTo("role", "admin")
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot admin : task.getResult()) {
                                            db.collection("users").document(admin.getId())
                                                    .get()
                                                    .addOnCompleteListener(task1 -> {
                                                        if (task1.isSuccessful()) {
                                                            if (task1.getResult().get("key_code") == null) {
                                                                passwordForSignUp.setError("รหัสนี้ไม่สามารถใช้งานได้แล้ว กรุณาติดต่อเจ้าหน้าที่");
                                                            } else if (!task1.getResult().get("key_code").equals(getPasswordForSignUp)) {
                                                                passwordForSignUp.setError("รหัสผ่านไม่ถูกต้อง");
                                                            } else {
                                                                dialogSignUp.dismiss();
                                                                manageDataUserCollection.addDataUsersCollection(SignUpActivity.this, name, surname, department, email, password, passwordEncode);
                                                                Intent intent = new Intent(this, MainActivity.class);
                                                                startActivity(intent);
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }
                });
                dialogSignUp.show();
            }
        });
    }

    protected Boolean invalidEmail(CharSequence email) {
        return (Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }
}
