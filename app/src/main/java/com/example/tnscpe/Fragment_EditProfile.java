package com.example.tnscpe;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class Fragment_EditProfile extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ProgressDialog progressDialog;
    private ImageView editImageProfile;
    private final ManageDataUserCollection manageDataUserCollection = new ManageDataUserCollection();
    private ActivityResultLauncher<Intent> activityResultEditImageProfile;
    private TextInputEditText editName, editSurname, editDepartment, editEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        View view = inflater.inflate(R.layout.fragment__edit_profile, container, false);
        editName = view.findViewById(R.id.edit_name);
        editSurname = view.findViewById(R.id.edit_surname);
        editDepartment = view.findViewById(R.id.edit_department);
        editEmail = view.findViewById(R.id.edit_email);
        editEmail.setEnabled(false);
        editImageProfile = view.findViewById(R.id.image_profile);
        Button buttonEditCancel = view.findViewById(R.id.button_edit_cancel);
        Button buttonEditSave = view.findViewById(R.id.button_edit_save);
        Button buttonEditPassword = view.findViewById(R.id.button_edit_password);
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.layout_refresh_edit_profile);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getUserData();
            swipeRefreshLayout.setRefreshing(false);
        });

        activityResultEditImageProfile = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData().getData() != null) {
                progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage("กรุณารอสักครู่...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                Uri fileUri = result.getData().getData();
                Date date = Calendar.getInstance().getTime();
                String checkIn = date.toString();
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Profile/" + checkIn + "." + fileUri.getLastPathSegment());
                storageReference.putFile(fileUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                db.collection("users")
                                        .whereEqualTo("email", firebaseUser.getEmail())
                                        .get()
                                        .addOnCompleteListener(task -> {
                                            DocumentSnapshot getDocId = task.getResult().getDocuments().get(0);
                                            String docId = getDocId.getId();

                                            db.collection("users").document(docId)
                                                    .update("imageProfile", "Profile/" + checkIn + "." + fileUri.getLastPathSegment())
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            editImageProfile.setImageURI(fileUri);
                                                            Log.d("edit image profile", "สำเร็จ");
                                                            progressDialog.dismiss();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w("edit image profile", e.getMessage());
                                                            progressDialog.dismiss();
                                                        }
                                                    });
                                        });
                                Log.d("Add image profile", "สำเร็จ");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("Add image profile", e.getMessage());
                                progressDialog.dismiss();
                            }
                        });
            }
        });

        editImageProfile.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("เปลี่ยนรูปภาพโปรไฟล์")
                    .setMessage("ท่านต้องการเปลี่ยนภาพโปรไฟล์ใช่หรือไม่")
                    .setPositiveButton("ยืนยัน", ((dialog, which) -> {
                        Dialog dialogEditProfile = new Dialog(getContext());
                        dialogEditProfile.setContentView(R.layout.edit_profile_dialog);
                        dialogEditProfile.setCanceledOnTouchOutside(false);
                        ImageView imageCancelEditProfileDialog = dialogEditProfile.findViewById(R.id.image_edit_profile_cancel);
                        TextInputEditText passwordForEditProfile = dialogEditProfile.findViewById(R.id.key_edit_profile);
                        Button buttonEditProfile = dialogEditProfile.findViewById(R.id.button_confirm_key_edit_profile);

                        imageCancelEditProfileDialog.setOnClickListener(v1 -> {
                            dialogEditProfile.dismiss();
                        });

                        buttonEditProfile.setOnClickListener(v1 -> {
                            if (passwordForEditProfile.getText().toString().isEmpty()) {
                                passwordForEditProfile.setError("กรุณากรอกรหัสผ่าน");
                            } else {
                                String getPasswordForSignUp = passwordForEditProfile.getText().toString().trim();
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
                                                                        passwordForEditProfile.setError("รหัสนี้ไม่สามารถใช้งานได้แล้ว กรุณาติดต่อเจ้าหน้าที่");
                                                                    } else if (!task1.getResult().get("key_code").equals(getPasswordForSignUp)) {
                                                                        passwordForEditProfile.setError("รหัสผ่านไม่ถูกต้อง");
                                                                    } else {
                                                                        dialogEditProfile.dismiss();
                                                                        Intent intentEditImageProfile = new Intent();
                                                                        intentEditImageProfile.setType("image/*");
                                                                        intentEditImageProfile.setAction(Intent.ACTION_GET_CONTENT);
                                                                        activityResultEditImageProfile.launch(intentEditImageProfile);
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        });
                            }
                        });
                        dialogEditProfile.show();
                    }))
                    .setNegativeButton("ยกเลิก", ((dialog, which) -> {
                        dialog.dismiss();
                    }))
                    .show();
        });

        buttonEditCancel.setOnClickListener(v -> {
            editName.setText("");
            editSurname.setText("");
            editDepartment.setText("");
            editEmail.setText("");
            getUserData();
        });

        buttonEditSave.setOnClickListener(v -> {
            Dialog dialogEditProfile = new Dialog(getContext());
            dialogEditProfile.setContentView(R.layout.edit_profile_dialog);
            dialogEditProfile.setCanceledOnTouchOutside(false);
            ImageView imageCancelEditProfileDialog = dialogEditProfile.findViewById(R.id.image_edit_profile_cancel);
            TextInputEditText passwordForEditProfile = dialogEditProfile.findViewById(R.id.key_edit_profile);
            Button buttonEditProfile = dialogEditProfile.findViewById(R.id.button_confirm_key_edit_profile);

            imageCancelEditProfileDialog.setOnClickListener(v1 -> {
                dialogEditProfile.dismiss();
            });

            buttonEditProfile.setOnClickListener(v1 -> {
                if (passwordForEditProfile.getText().toString().isEmpty()) {
                    passwordForEditProfile.setError("กรุณากรอกรหัสผ่าน");
                } else {
                    String getPasswordForSignUp = passwordForEditProfile.getText().toString().trim();
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
                                                            passwordForEditProfile.setError("รหัสนี้ไม่สามารถใช้งานได้แล้ว กรุณาติดต่อเจ้าหน้าที่");
                                                        } else if (!task1.getResult().get("key_code").equals(getPasswordForSignUp)) {
                                                            passwordForEditProfile.setError("รหัสผ่านไม่ถูกต้อง");
                                                        } else {
                                                            dialogEditProfile.dismiss();
                                                            String name = editName.getText().toString().trim();
                                                            String surname = editSurname.getText().toString().trim();
                                                            String department = editDepartment.getText().toString().trim();
                                                            String email = editEmail.getText().toString().trim();
                                                            String currentUserEmail = firebaseUser.getEmail();

                                                            if (!name.isEmpty() && surname.isEmpty()) {
                                                                editSurname.setError("กรุณากรอกนามสกุล");
                                                            } else if (!surname.isEmpty() && name.isEmpty()) {
                                                                editName.setError("กรุณากรอกชื่อ");
                                                            } else if (!name.isEmpty() && !surname.isEmpty()) {
                                                                manageDataUserCollection.updateNameUsersCollection(getActivity(), currentUserEmail, name, surname);
                                                                editName.setText("");
                                                                editSurname.setText("");
                                                                getUserData();
                                                            }

                                                            if (!department.isEmpty()) {
                                                                manageDataUserCollection.updateDepartmentUsersCollection(getActivity(), currentUserEmail, department);
                                                                editDepartment.setText("");
                                                                getUserData();
                                                            }

                                                            if (!email.isEmpty()) {
                                                                if (!invalidEmail(email)) {
                                                                    editEmail.setError("อีเมลล์ไม่ถูกต้อง");
                                                                } else {
                                                                    manageDataUserCollection.updateEmailUserCollection(getContext(), currentUserEmail, email);
                                                                    editEmail.setText("");
                                                                    getUserData();
                                                                }
                                                            }
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                }
            });
            dialogEditProfile.show();
        });

        buttonEditPassword.setOnClickListener(v -> {
            String currentUserEmail = firebaseUser.getEmail();
            manageDataUserCollection.updatePasswordUserCollection(getContext(), currentUserEmail);
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getUserData();
    }

    protected Boolean invalidEmail(CharSequence email) {
        return (Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    protected void getUserData() {
        db.collection("users")
                .whereEqualTo("email", firebaseUser.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                            editName.setText(documentSnapshot.getString("firstname"));
                            editSurname.setText(documentSnapshot.getString("lastname"));
                            editDepartment.setText(documentSnapshot.getString("department"));
                            editEmail.setText(documentSnapshot.getString("email"));

                            if (documentSnapshot.get("imageProfile").toString() != "") {
                                StorageReference storageReference;
                                storageReference = storage.getReference().child(documentSnapshot.getString("imageProfile"));

                                try {
                                    File file = File.createTempFile("Parcels", "jpg");
                                    storageReference.getFile(file)
                                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                                    editImageProfile.setImageBitmap(bitmap);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d("get image in card home page", e.getMessage());
                                                }
                                            });
                                } catch (IOException e) {
                                    Log.d("get image in card home page", e.getMessage());
                                }
                            } else {
                                editImageProfile.setImageDrawable(getActivity().getDrawable(R.drawable.ic_baseline_account_circle_24));
                                Log.d("RRR", "ไม่พบรูปโปรไฟล์");
                            }
                        }
                    }
                });
    }
}