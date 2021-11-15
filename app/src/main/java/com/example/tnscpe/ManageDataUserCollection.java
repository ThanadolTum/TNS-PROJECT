package com.example.tnscpe;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class ManageDataUserCollection extends AppCompatActivity {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser firebaseUser = mAuth.getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressDialog progressdialog;

    protected void addDataUsersCollection(Context context, String name, String surname, String department, String email, String password, String encodePassword) {
        Map<String, Object> user = new HashMap<>();
        user.put("agent", null);
        user.put("firstname", name);
        user.put("lastname", surname);
        user.put("email", email);
        user.put("password", encodePassword);
        user.put("department", department);
        user.put("imageProfile", "");
        user.put("role", "");

        Map<String, Object> agent = new HashMap<>();
        agent.put("agent0", null);
        agent.put("agent1", null);
        agent.put("agent2", null);

        user.put("agent", agent);

        Map<String, Object> subAgent = new HashMap<>();
        subAgent.put("firstname", "");
        subAgent.put("lastname", "");
        subAgent.put("status_receiver", false);
        subAgent.put("email","");

        for (int i = 0; i < 3; i++) {
            agent.put("agent" + i, subAgent);
        }

        progressdialog = new ProgressDialog(context);
        progressdialog.setMessage("กรุณารอสักครู่...");
        progressdialog.show();
        progressdialog.setCanceledOnTouchOutside(false);

        db.collection("users")
                .whereEqualTo("firstname", name)
                .whereEqualTo("lastname", surname)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            Toast.makeText(context, "ชื่อ-นามสกุลนี้ใช้ลงทะเบียนแล้ว", Toast.LENGTH_SHORT).show();
                        } else {
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Objects.requireNonNull(mAuth.getCurrentUser()).sendEmailVerification()
                                                    .addOnCompleteListener(task2 -> {
                                                        if (task1.isSuccessful()) {
                                                            finish();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(context,task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                        progressdialog.dismiss();
                                    });

                            db.collection("users")
                                    .add(user)
                                    .addOnSuccessListener(documentReference -> finish())
                                    .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
                            Toast.makeText(context, "ลงทะเบียนเสร็จสิ้น กรุณาตรวจสอบอีเมลล์เพื่อทำการยืนยัน", Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressdialog.dismiss();
                });
    }

    protected void updateNameUsersCollection(Context context, String email, String name, String surname) {
        db.collection("users")
                .whereEqualTo("firstname", name)
                .whereEqualTo("lastname", surname)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            Toast.makeText(context, "ชื่อ-นามสกุลนี้ใช้ลงทะเบียนแล้ว", Toast.LENGTH_SHORT).show();
                        } else {
                            db.collection("users")
                                    .whereEqualTo("email", email)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot documentSnapshot = task1.getResult().getDocuments().get(0);
                                            String docID = documentSnapshot.getId();

                                            db.collection("users").document(docID)
                                                    .get()
                                                    .addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful()) {
                                                            progressdialog = new ProgressDialog(context);
                                                            progressdialog.setMessage("กรุณารอสักครู่...");
                                                            progressdialog.show();
                                                            progressdialog.setCanceledOnTouchOutside(false);
                                                            if (task2.getResult().getString("firstname").equals(name) && task2.getResult().getString("lastname").equals(surname)) {
                                                                Toast.makeText(context, "ท่านใช้ชื่อ-นามสกุลนี้อยู่แล้ว", Toast.LENGTH_SHORT).show();
                                                                progressdialog.dismiss();
                                                            } else {
                                                                db.collection("users").document(docID)
                                                                        .update("firstname", name, "lastname", surname)
                                                                        .addOnCompleteListener(task3 -> Toast.makeText(context, "อัพเดทเสร็จสิ้น", Toast.LENGTH_SHORT).show());
                                                                progressdialog.dismiss();
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                        }
                    }
                });
    }

    protected void updateDepartmentUsersCollection(Context context, String email, String department) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        String docID = documentSnapshot.getId();

                        db.collection("users").document(docID)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        progressdialog = new ProgressDialog(context);
                                        progressdialog.setMessage("กรุณารอสักครู่...");
                                        progressdialog.show();
                                        progressdialog.setCanceledOnTouchOutside(false);
                                        if (task1.getResult().getString("department").equals(department)) {
                                            Toast.makeText(context, "ท่านมีข้อมูลภาควิชานี้แล้ว", Toast.LENGTH_SHORT).show();
                                            progressdialog.dismiss();
                                        } else {
                                            db.collection("users").document(docID)
                                                    .update("department", department.trim().toLowerCase())
                                                    .addOnCompleteListener(task2 -> Toast.makeText(context, "อัพเดทภาควิชาเสร็จสิ้น", Toast.LENGTH_SHORT).show());
                                            progressdialog.dismiss();
                                        }
                                    }
                                });
                    }
                });
    }

    protected void updateEmailUserCollection(Context context, String email, String newEmail) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.re_auth_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextInputEditText reAuthPassword = dialog.findViewById(R.id.re_authen_password);
        Button buttonReAuth = dialog.findViewById(R.id.button_re_auth);
        ImageView imageViewCancelUpdateEmailDialog = dialog.findViewById(R.id.image_re_auth_cancel);

        imageViewCancelUpdateEmailDialog.setOnClickListener(v1 -> dialog.dismiss());

        buttonReAuth.setOnClickListener(v2 -> {
            String passwordReAuth = Objects.requireNonNull(reAuthPassword.getText()).toString().trim();

            if (passwordReAuth.isEmpty()) {
                reAuthPassword.setError("กรุณากรอกรหัสผ่าน");
            }

            progressdialog = new ProgressDialog(context);
            progressdialog.setMessage("กรุณารอสักครู่...");
            progressdialog.show();
            progressdialog.setCanceledOnTouchOutside(false);
        });
    }

    protected void updatePasswordUserCollection(Context context, String email) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.re_auth_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        TextInputEditText reAuthPassword = dialog.findViewById(R.id.re_authen_password);
        Button buttonReAuth = dialog.findViewById(R.id.button_re_auth);
        ImageView imageViewCancel = dialog.findViewById(R.id.image_re_auth_cancel);

        imageViewCancel.setOnClickListener(v1 -> dialog.dismiss());

        buttonReAuth.setOnClickListener(v -> {
            if (reAuthPassword.getText().toString().isEmpty()) {
                reAuthPassword.setError("กรุณากรอกรหัสผ่าน");
            } else {
                String password = Objects.requireNonNull(reAuthPassword.getText()).toString().trim();
                AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                firebaseUser.reauthenticate(credential)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                dialog.dismiss();
                                Dialog dialogNewPassword = new Dialog(context);
                                dialogNewPassword.setContentView(R.layout.update_password_dialog);
                                dialogNewPassword.setCanceledOnTouchOutside(false);
                                dialogNewPassword.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialogNewPassword.show();
                                TextInputEditText editTextNewPassword = dialogNewPassword.findViewById(R.id.new_password);
                                TextInputEditText editTextConfirmNewPassword = dialogNewPassword.findViewById(R.id.confirm_new_password);
                                Button buttonNewPassword = dialogNewPassword.findViewById(R.id.button_new_password);
                                ImageView imageViewCancelUpdateDialog = dialogNewPassword.findViewById(R.id.image_dialog_new_password_cancel);

                                imageViewCancelUpdateDialog.setOnClickListener(v1 -> dialogNewPassword.dismiss());

                                buttonNewPassword.setOnClickListener(v1 -> {
                                    String newPassword = Objects.requireNonNull(editTextNewPassword.getText()).toString().trim();
                                    String confirmNewPassword = Objects.requireNonNull(editTextConfirmNewPassword.getText()).toString().trim();

                                    if (newPassword.isEmpty()) {
                                        editTextNewPassword.setError("กรุณากรอกรหัสผ่าน");
                                    } else if (confirmNewPassword.isEmpty()) {
                                        editTextConfirmNewPassword.setError("กรุณากรอกรหัสผ่าน");
                                    } else if (!newPassword.equals(confirmNewPassword)) {
                                        editTextConfirmNewPassword.setError("กรุณาตรวจสอบรหัสผ่านอีกครั้ง");
                                    } else {
                                        progressdialog = new ProgressDialog(context);
                                        progressdialog.setMessage("กรุณารอสักครู่...");
                                        progressdialog.show();
                                        progressdialog.setCanceledOnTouchOutside(false);
                                        byte[] encodePassword = Base64.encode(newPassword.getBytes(), Base64.DEFAULT);
                                        String passwordEncode = new String(encodePassword);
                                        db.collection("users")
                                                .whereEqualTo("email", email)
                                                .get()
                                                .addOnCompleteListener(task1 -> {
                                                    DocumentSnapshot documentSnapshot = task1.getResult().getDocuments().get(0);
                                                    String docId = documentSnapshot.getId();

                                                    db.collection("users").document(docId)
                                                            .get()
                                                            .addOnCompleteListener(task2 -> {
                                                                DocumentSnapshot document = task2.getResult();
                                                                if (document.exists()) {
                                                                    String oldPassword = document.getString("password");
                                                                    if (Objects.requireNonNull(oldPassword).equals(passwordEncode)) {
                                                                        Toast.makeText(context, "ท่านใช้รหัสผ่านนี้อยู่แล้ว", Toast.LENGTH_SHORT).show();
                                                                        progressdialog.dismiss();
                                                                    } else {
                                                                        db.collection("users").document(docId)
                                                                                .update("password", passwordEncode)
                                                                                .addOnCompleteListener(task3 -> {
                                                                                    if (task3.isSuccessful()) {
                                                                                        firebaseUser.updatePassword(newPassword)
                                                                                                .addOnCompleteListener(task4 -> {
                                                                                                    if (task4.isSuccessful()) {
                                                                                                        dialogNewPassword.dismiss();
                                                                                                        Toast.makeText(context, "อัพเดทรหัสผ่านเสร็จสิ้น", Toast.LENGTH_SHORT).show();
                                                                                                        progressdialog.dismiss();
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            });
                                                });
                                    }
                                });
                            } else {
                                reAuthPassword.setError("รหัสผ่านไม่ถูกต้อง");
                            }
                        });
            }
        });
    }
}