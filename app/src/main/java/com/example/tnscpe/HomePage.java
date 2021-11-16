package com.example.tnscpe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class HomePage extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = mAuth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private static final int CAMERA_PERMISSION_CODE = 100;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ArrayList<TrackNumberModel> trackNumberModelList = new ArrayList<>();
    private TrackNumberAdapter trackNumberAdapter;
    private ArrayList<String> trackNumberSet = new ArrayList<>();
    private ArrayList<String> redTrackNumber = new ArrayList<>();
    private HashSet<String> ownerName = new HashSet<>();
    private SharedPreferences pref;
    private ActivityResultLauncher<Intent> activityResultAddImageParcel;
    private Dialog dialogAddImage, dialogAdmin;
    private Map<String, Integer> indexOfTrackNumber = new HashMap<>();
    private String trackNumberPosition;
    private String department;
    private ProgressDialog progressDialog;
    private final Fragment_HomePage fragmentHomePage = new Fragment_HomePage();
    private final Fragment_Agent fragmentAgent = new Fragment_Agent();
    private final Fragment_EditProfile fragmentEditProfile = new Fragment_EditProfile();
    private final Fragment_History fragmentHistory = new Fragment_History();
    private final Fragment_Static fragmentStatic = new Fragment_Static();
    private final Fragment_ScanBarcode fragmentScanBarcode = new Fragment_ScanBarcode();
    private final Fragment_Notification fragmentNotification = new Fragment_Notification();
    private String getOwnerName = "";
    private String getOwnerSurname = "";
    private TextView displayName;
    private MenuBuilder menuBuilder;
    private int countOfMessage = 0;

    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_home);
        permissionRequest();
        pref = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        ImageView imageProfile = findViewById(R.id.profile);
        db.collection("users")
                .whereEqualTo("email", firebaseUser.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot getDocIdUser = task.getResult().getDocuments().get(0);
                        String docIdUser = getDocIdUser.getId();

                        db.collection("users").document(docIdUser)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        if (task1.getResult().getString("role").equals("")) {
                                            SharedPreferences.Editor editor = pref.edit();
                                            editor.putString("Role", "user");
                                            editor.commit();
                                            if (!task1.getResult().getString("password").equals(pref.getString("CurrentPasswordUser", "NoData"))) {
                                                db.collection("users").document(docIdUser)
                                                        .update("password", pref.getString("CurrentPasswordUser", "NoData"))
                                                        .addOnCompleteListener(task2 -> {
                                                            Log.d("Password has been reset", "อัพเดทรหัสผ่าน");
                                                        });
                                            } else {
                                                Log.d("Password has been changed", "ไม่มีการรีเซ็ตรหัสผ่าน");
                                            }
                                            tabLayout.setupWithViewPager(viewPager);
                                            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
                                            viewPagerAdapter.addFragment(fragmentHomePage);
                                            viewPagerAdapter.addFragment(fragmentAgent);
                                            viewPagerAdapter.addFragment(fragmentEditProfile);
                                            viewPagerAdapter.addFragment(fragmentHistory);
                                            viewPagerAdapter.addFragment(fragmentStatic);
                                            viewPagerAdapter.addFragment(fragmentScanBarcode);
                                            viewPagerAdapter.addFragment(fragmentNotification);
                                            viewPager.setAdapter(viewPagerAdapter);
                                            Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(R.drawable.ic_action_home);
                                            Objects.requireNonNull(tabLayout.getTabAt(1)).setIcon(R.drawable.ic_action_agent);
                                            Objects.requireNonNull(tabLayout.getTabAt(2)).setIcon(R.drawable.ic_action_edit_profile);
                                            Objects.requireNonNull(tabLayout.getTabAt(3)).setIcon(R.drawable.ic_action_history);
                                            Objects.requireNonNull(tabLayout.getTabAt(4)).setIcon(R.drawable.ic_action_static);
                                            Objects.requireNonNull(tabLayout.getTabAt(5)).setIcon(R.drawable.ic_action_scan);
                                            Objects.requireNonNull(tabLayout.getTabAt(6)).setIcon(R.drawable.ic_action_notification);
                                            db.collection("users")
                                                    .whereEqualTo("email", firebaseUser.getEmail())
                                                    .get()
                                                    .addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful()) {
                                                            DocumentSnapshot getDocIdCurrentUser = task2.getResult().getDocuments().get(0);
                                                            String docIdCurrentUser = getDocIdCurrentUser.getId();

                                                            db.collection("users").document(docIdCurrentUser)
                                                                    .get()
                                                                    .addOnCompleteListener(task3 -> {
                                                                        if (task3.isSuccessful()) {
                                                                            db.collection("messages")
                                                                                    .whereEqualTo("read", false)
                                                                                    .whereEqualTo("receiver.firstname", task3.getResult().getString("firstname"))
                                                                                    .whereEqualTo("receiver.lastname", task3.getResult().getString("lastname"))
                                                                                    .get()
                                                                                    .addOnCompleteListener(task4 -> {
                                                                                        if (task4.isSuccessful() && task4.getResult() != null) {
                                                                                            for (QueryDocumentSnapshot getCountMessage : task4.getResult()) {
                                                                                                countOfMessage++;
                                                                                            }
                                                                                            editor.putInt("CountOfMessage", countOfMessage);
                                                                                            editor.commit();
                                                                                        }
                                                                                    });
                                                                        }
                                                                    });
                                                        }
                                                    });
                                            if (pref.getInt("CountOfMessage", 0) != 0) {
                                                int getCountOfMessage = pref.getInt("CountOfMessage", 0);
                                                BadgeDrawable badgeDrawable = tabLayout.getTabAt(6).getOrCreateBadge();
                                                badgeDrawable.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                                                badgeDrawable.setNumber(getCountOfMessage);
                                                badgeDrawable.setBadgeTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                                badgeDrawable.setVisible(true);
                                            }
                                        } else {
                                            SharedPreferences.Editor editor = pref.edit();
                                            editor.putString("Role", "admin");
                                            editor.commit();
                                            tabLayout.setupWithViewPager(viewPager);
                                            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
                                            viewPagerAdapter.addFragment(fragmentHomePage);
                                            viewPagerAdapter.addFragment(fragmentHistory);
                                            viewPagerAdapter.addFragment(fragmentStatic);
                                            viewPagerAdapter.addFragment(fragmentScanBarcode);
                                            viewPagerAdapter.addFragment(fragmentNotification);
                                            viewPager.setAdapter(viewPagerAdapter);
                                            Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(R.drawable.ic_action_home);
                                            Objects.requireNonNull(tabLayout.getTabAt(1)).setIcon(R.drawable.ic_action_history);
                                            Objects.requireNonNull(tabLayout.getTabAt(2)).setIcon(R.drawable.ic_action_static);
                                            Objects.requireNonNull(tabLayout.getTabAt(3)).setIcon(R.drawable.ic_action_scan);
                                            Objects.requireNonNull(tabLayout.getTabAt(4)).setIcon(R.drawable.ic_action_notification);
                                        }

                                        if (task1.getResult().getString("imageProfile") != "") {
                                            StorageReference storageReference;
                                            storageReference = storage.getReference().child(task1.getResult().getString("imageProfile"));

                                            try {
                                                File file = File.createTempFile("Parcels", "jpg");
                                                storageReference.getFile(file)
                                                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                                                imageProfile.setImageBitmap(bitmap);
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
                                            imageProfile.setImageDrawable(getDrawable(R.drawable.ic_baseline_account_circle_24));
                                        }
                                    }
                                });
                    }
                });
    }

    @SuppressLint("RestrictedApi")
    protected void onStart() {
        super.onStart();
        displayName = findViewById(R.id.display_name);
        menuBuilder = new MenuBuilder(this);
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu_item, menuBuilder);
        displayName.setOnClickListener(v -> {
            MenuPopupHelper popupHelper = new MenuPopupHelper(this, menuBuilder, v);
            popupHelper.setForceShowIcon(true);
            menuBuilder.setCallback(new MenuBuilder.Callback() {
                @Override
                public boolean onMenuItemSelected(@NonNull MenuBuilder menu, @NonNull MenuItem item) {
                    if (item.getItemId() == R.id.menu_sign_out) {
                        mAuth.signOut();
                        SharedPreferences pref = getSharedPreferences("CurrentUser", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.remove("CurrentEmailUser");
                        editor.remove("Role");
                        editor.remove("CountOfMessage");
                        editor.remove("CurrentPasswordUser");
                        editor.commit();
                        Intent intent = new Intent(HomePage.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        finish();
                        return true;
                    }
                    return false;
                }

                @Override
                public void onMenuModeChange(@NonNull MenuBuilder menu) {
                }
            });
            popupHelper.show();
        });
        viewPager = findViewById(R.id.view_page);
        tabLayout = findViewById(R.id.taps_layout);
        dialogAddImage = new Dialog(this);

        getAllNameUser();
        getDepartmentUser();

        db.collection("users")
                .whereEqualTo("email", firebaseUser.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot getDocId = task.getResult().getDocuments().get(0);
                        String docId = getDocId.getId();

                        db.collection("users").document(docId)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        String userName = task1.getResult().getString("firstname");
                                        displayName.setText(userName);
                                    }
                                });
                    }
                });

        activityResultAddImageParcel = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                dialogAddImage.setContentView(R.layout.add_images_parcel);
                dialogAddImage.setCanceledOnTouchOutside(false);
                dialogAddImage.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                ImageView imageParcelOne = dialogAddImage.findViewById(R.id.image_parcel_1);
                ImageView imageParcelTwo = dialogAddImage.findViewById(R.id.image_parcel_2);
                ImageView imageParcelThree = dialogAddImage.findViewById(R.id.image_parcel_3);
                ImageView imageParcelFour = dialogAddImage.findViewById(R.id.image_parcel_4);
                TextView buttonAddImage = dialogAddImage.findViewById(R.id.button_add_image_parcel);

                if (result.getData().getClipData() != null) {
                    ClipData clipData = result.getData().getClipData();
                    if (result.getData().getClipData().getItemCount() > 4) {
                        Toast.makeText(getApplicationContext(), "สามารถเลือกรูปภาพสูงสุด 4 รูปภาพ", Toast.LENGTH_SHORT).show();
                        dialogAddImage.dismiss();
                    }

                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri fileUri = clipData.getItemAt(i).getUri();
                        if (i == 0) {
                            imageParcelOne.setImageURI(fileUri);
                            int position = indexOfTrackNumber.get(trackNumberPosition).intValue();
                            trackNumberModelList.get(position).setImage0(fileUri);
                            trackNumberAdapter.notifyDataSetChanged();
                        } else if (i == 1) {
                            imageParcelTwo.setImageURI(fileUri);
                            int position = indexOfTrackNumber.get(trackNumberPosition).intValue();
                            trackNumberModelList.get(position).setImage1(fileUri);
                            trackNumberAdapter.notifyDataSetChanged();
                        } else if (i == 2) {
                            imageParcelThree.setImageURI(fileUri);
                            int position = indexOfTrackNumber.get(trackNumberPosition).intValue();
                            trackNumberModelList.get(position).setImage2(fileUri);
                            trackNumberAdapter.notifyDataSetChanged();
                        } else if (i == 3) {
                            imageParcelFour.setImageURI(fileUri);
                            int position = indexOfTrackNumber.get(trackNumberPosition).intValue();
                            trackNumberModelList.get(position).setImage3(fileUri);
                            trackNumberAdapter.notifyDataSetChanged();
                        }
                    }
                } else if (result.getData().getData() != null) {
                    Uri fileUri = result.getData().getData();
                    imageParcelOne.setImageURI(fileUri);
                    int position = indexOfTrackNumber.get(trackNumberPosition).intValue();
                    trackNumberModelList.get(position).setImage0(fileUri);
                    trackNumberAdapter.notifyDataSetChanged();
                }
                buttonAddImage.setOnClickListener(v -> {
                    dialogAddImage.dismiss();
                });
                dialogAddImage.show();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            new AlertDialog.Builder(this)
                    .setTitle("ออกจากแอปพลิเคชัน")
                    .setMessage("ท่านแน่ใจใช่หรือไม่ที่จะออกจากแอปพลิเคชัน")
                    .setCancelable(true)
                    .setPositiveButton("ยืนยัน", ((dialog, which) -> {
                        mAuth.signOut();
                        SharedPreferences pref = getSharedPreferences("CurrentUser", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.remove("CurrentEmailUser");
                        editor.remove("Role");
                        editor.remove("CountOfMessage");
                        editor.remove("CurrentPasswordUser");
                        editor.commit();
                        Intent intent = new Intent(HomePage.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        finish();
                    }))
                    .setNegativeButton("ยกเลิก", ((dialog, which) -> {
                        dialog.dismiss();
                    }))
                    .show();
        }
        return super.onKeyDown(keyCode, event);
    }

    protected static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final ArrayList<Fragment> fragmentList = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }

        public void addFragment(Fragment fragment) {
            fragmentList.add(fragment);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                if (pref.getString("Role", "NoData").equals("admin")) {
                    dialogAdmin = new Dialog(this);
                    dialogAdmin.setContentView(R.layout.scanner_dialog_admin);
                    dialogAdmin.setCanceledOnTouchOutside(false);
                    dialogAdmin.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    Button buttonAddParcel = dialogAdmin.findViewById(R.id.button_add_parcel);
                    Button buttonSendNotification = dialogAdmin.findViewById(R.id.button_send_notification);
                    ImageView imageDialogCancel = dialogAdmin.findViewById(R.id.image_dialog_scanner_cancel);
                    ListView listViewTrackNumber = dialogAdmin.findViewById(R.id.listview_track_number);
                    AutoCompleteTextView selectOwnerName = dialogAdmin.findViewById(R.id.auto_complete_owner_name);
                    ArrayList<String> ownerNameList = new ArrayList<>(ownerName);
                    ArrayAdapter<String> ownerNameAdapter = new ArrayAdapter<>(dialogAdmin.getContext(), R.layout.department_item, ownerNameList);
                    selectOwnerName.setAdapter(ownerNameAdapter);
                    EditText addTrackNumber = dialogAdmin.findViewById(R.id.add_track_number);
                    ImageButton buttonAddTrackNumber = dialogAdmin.findViewById(R.id.button_add_track_number);

                    selectOwnerName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String owner = selectOwnerName.getText().toString();
                            String[] getName = owner.split(" ");
                            ArrayList<String> ownerName = new ArrayList<>();
                            for (String name : getName) {
                                ownerName.add(name);
                            }
                            trackNumberAdapter.setUsername(ownerName.get(0));
                            trackNumberAdapter.setUserSurname(ownerName.get(1));
                            trackNumberAdapter.notifyDataSetChanged();
                        }
                    });

                    String resultTrackNumber = result.getContents().trim().toUpperCase();
                    db.collection("parcels")
                            .whereEqualTo("trackNumber", resultTrackNumber)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    DocumentSnapshot getDocIdParcels = task.getResult().getDocuments().get(0);
                                    String docIdParcels = getDocIdParcels.getId();

                                    db.collection("parcels").document(docIdParcels)
                                            .get()
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.getResult().getString("checkIn") != null) {
                                                    Toast.makeText(dialogAdmin.getContext(), "พัสดุนี้ถูกเชคอินแล้ว", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    if (!trackNumberSet.isEmpty()) {
                                                        if (trackNumberSet.indexOf(result.getContents()) != -1) {
                                                            Toast.makeText(this, "คุณเพิ่มหมายเลขพัสดุนี้แล้ว", Toast.LENGTH_LONG).show();
                                                        } else {
                                                            trackNumberSet.add(result.getContents());
                                                            TrackNumberModel resultForScan = new TrackNumberModel();
                                                            resultForScan.setTrackNumber(result.getContents());
                                                            trackNumberModelList.add(resultForScan);
                                                            trackNumberAdapter.notifyDataSetChanged();
                                                        }
                                                    } else {
                                                        trackNumberSet.add(result.getContents());
                                                        TrackNumberModel resultForScan = new TrackNumberModel();
                                                        resultForScan.setTrackNumber(result.getContents());
                                                        trackNumberModelList.add(resultForScan);
                                                        trackNumberAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                            });
                                } else {
                                    if (!trackNumberSet.isEmpty()) {
                                        if (trackNumberSet.indexOf(result.getContents()) != -1) {
                                            Toast.makeText(this, "คุณเพิ่มหมายเลขพัสดุนี้แล้ว", Toast.LENGTH_LONG).show();
                                        } else {
                                            trackNumberSet.add(result.getContents());
                                            TrackNumberModel resultForScan = new TrackNumberModel();
                                            resultForScan.setTrackNumber(result.getContents());
                                            trackNumberModelList.add(resultForScan);
                                            trackNumberAdapter.notifyDataSetChanged();
                                        }
                                    } else {
                                        trackNumberSet.add(result.getContents());
                                        TrackNumberModel resultForScan = new TrackNumberModel();
                                        resultForScan.setTrackNumber(result.getContents());
                                        trackNumberModelList.add(resultForScan);
                                        trackNumberAdapter.notifyDataSetChanged();
                                    }
                                }
                            });

                    trackNumberAdapter = new TrackNumberAdapter(dialogAdmin.getContext(), trackNumberModelList);
                    listViewTrackNumber.setAdapter(trackNumberAdapter);
                    trackNumberAdapter.notifyDataSetChanged();
                    listViewTrackNumber.setClickable(true);

                    buttonAddTrackNumber.setOnClickListener(v -> {
                        if (addTrackNumber.getText().toString().isEmpty()) {
                            Toast.makeText(dialogAdmin.getContext(), "กรุณากรอกเลขติดตามพัสดุที่ท่านต้องการ", Toast.LENGTH_SHORT).show();
                        } else {
                            String trackNumber = addTrackNumber.getText().toString().trim().toUpperCase();
                            db.collection("parcels")
                                    .whereEqualTo("trackNumber", trackNumber)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                            DocumentSnapshot getDocIdParcels = task.getResult().getDocuments().get(0);
                                            String docIdParcels = getDocIdParcels.getId();

                                            db.collection("parcels").document(docIdParcels)
                                                    .get()
                                                    .addOnCompleteListener(task1 -> {
                                                        if (task1.getResult().getString("checkIn") != null) {
                                                            Toast.makeText(dialogAdmin.getContext(), "พัสดุนี้ถูกเชคอินแล้ว", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            if (!trackNumberSet.isEmpty()) {
                                                                if (trackNumberSet.indexOf(trackNumber) != -1) {
                                                                    Toast.makeText(this, "คุณเพิ่มหมายเลขพัสดุนี้แล้ว", Toast.LENGTH_LONG).show();
                                                                } else {
                                                                    trackNumberSet.add(trackNumber);
                                                                    TrackNumberModel resultForScan = new TrackNumberModel();
                                                                    resultForScan.setTrackNumber(trackNumber);
                                                                    trackNumberModelList.add(resultForScan);
                                                                    trackNumberAdapter.notifyDataSetChanged();
                                                                }
                                                            } else {
                                                                trackNumberSet.add(trackNumber);
                                                                TrackNumberModel resultForScan = new TrackNumberModel();
                                                                resultForScan.setTrackNumber(trackNumber);
                                                                trackNumberModelList.add(resultForScan);
                                                                trackNumberAdapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            if (!trackNumberSet.isEmpty()) {
                                                if (trackNumberSet.indexOf(trackNumber) != -1) {
                                                    Toast.makeText(this, "คุณเพิ่มหมายเลขพัสดุนี้แล้ว", Toast.LENGTH_LONG).show();
                                                } else {
                                                    trackNumberSet.add(trackNumber);
                                                    TrackNumberModel resultForScan = new TrackNumberModel();
                                                    resultForScan.setTrackNumber(trackNumber);
                                                    trackNumberModelList.add(resultForScan);
                                                    trackNumberAdapter.notifyDataSetChanged();
                                                }
                                            } else {
                                                trackNumberSet.add(trackNumber);
                                                TrackNumberModel resultForScan = new TrackNumberModel();
                                                resultForScan.setTrackNumber(trackNumber);
                                                trackNumberModelList.add(resultForScan);
                                                trackNumberAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                        }
                    });

                    listViewTrackNumber.setOnItemClickListener((parent, view, position, id) -> {
                        TextView trackNumber = view.findViewById(R.id.tracknumber);
                        String trackNumberSelect = trackNumber.getText().toString();
                        trackNumberPosition = listViewTrackNumber.getItemAtPosition(position).toString();
                        for (int i = 0; i < trackNumberModelList.size(); i++) {
                            String positionName = trackNumberModelList.get(i).toString();
                            indexOfTrackNumber.put(positionName, i);
                        }
                        new AlertDialog.Builder(this)
                                .setTitle(trackNumberSelect)
                                .setMessage("หากต้องการเพิ่มรูปภาพ สามารถเพิ่มรูปภาพได้สูงสุด 4 รูปภาพ")
                                .setPositiveButtonIcon(getDrawable(R.drawable.ic_baseline_image_search_24))
                                .setPositiveButton("เพิ่มรูปภาพ", (addImage, which) -> {
                                    Intent intentAddImages = new Intent();
                                    intentAddImages.setType("image/*");
                                    intentAddImages.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                    intentAddImages.setAction(Intent.ACTION_GET_CONTENT);
                                    activityResultAddImageParcel.launch(intentAddImages);
                                })
                                .setNegativeButtonIcon(getDrawable(R.drawable.ic_baseline_delete_24))
                                .setNegativeButton("ลบหมายเลขติดตามพัสดุ", (deleteTrackNumber, which) -> {
                                    if (redTrackNumber.indexOf(trackNumberSelect) != -1) {
                                        redTrackNumber.remove(trackNumberSelect);
                                        trackNumberModelList.remove(position);
                                        trackNumberSet.remove(trackNumberSelect);
                                        trackNumberAdapter.notifyDataSetChanged();
                                    } else {
                                        trackNumberModelList.remove(position);
                                        trackNumberSet.remove(trackNumberSelect);
                                        trackNumberAdapter.notifyDataSetChanged();
                                    }
                                })
                                .setNeutralButton("ยกเลิก", (dialog, which) -> {
                                    dialog.dismiss();
                                })
                                .show();
                    });

                    imageDialogCancel.setOnClickListener(v -> {
                        trackNumberSet.clear();
                        trackNumberModelList.clear();
                        trackNumberAdapter.notifyDataSetChanged();
                        redTrackNumber.clear();
                        indexOfTrackNumber.clear();
                        dialogAdmin.dismiss();
                    });

                    buttonAddParcel.setOnClickListener(v -> {
                        scanBarcode();
                        dialogAdmin.dismiss();
                    });

                    buttonSendNotification.setOnClickListener(v -> {
                        redTrackNumber.clear();
                        for (int i = 0; i < trackNumberModelList.size(); i++) {
                            if (trackNumberModelList.get(i).getRedTrackNumber().equals(true)) {
                                Toast.makeText(this, "กรุณาลบหมายเลขติดตามพัสดุที่มีสถานะสีแดง", Toast.LENGTH_SHORT).show();
                                redTrackNumber.add(trackNumberModelList.get(i).getTrackNumber());
                            }
                        }

                        if (selectOwnerName.getText().toString().isEmpty()) {
                            Toast.makeText(this, "กรุณาเลือกเจ้าของพัสดุ", Toast.LENGTH_SHORT).show();
                        } else if (trackNumberModelList.isEmpty()) {
                            Toast.makeText(this, "กรุณาเพิ่มหมายเลขติดตามพัสดุที่ท่านต้องการ", Toast.LENGTH_SHORT).show();
                        } else if (redTrackNumber.isEmpty()) {
                            progressDialog = new ProgressDialog(this);
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.setMessage("กรุณารอสักครู่");
                            progressDialog.show();
                            ArrayList<String> getNameForMessage = new ArrayList<>();
                            ArrayList<String> emailForMessage = new ArrayList<>();
                            String[] getNameOwner = selectOwnerName.getText().toString().split(" ");
                            db.collection("users")
                                    .whereEqualTo("firstname", getNameOwner[0])
                                    .whereEqualTo("lastname", getNameOwner[1])
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot getDocIdOwner = task.getResult().getDocuments().get(0);
                                            String docIdOwner = getDocIdOwner.getId();

                                            db.collection("users").document(docIdOwner)
                                                    .get()
                                                    .addOnCompleteListener(task1 -> {
                                                        if (task1.isSuccessful()) {
                                                            getNameForMessage.add(getNameOwner[0] + " " + getNameOwner[1] + " " + task1.getResult().getString("email"));
                                                            if (task1.getResult().getString("agent.agent0.firstname") != "" && task1.getResult().getString("agent.agent0.lastname") != "" && task1.getResult().get("agent.agent0.status_receiver").equals(true)) {
                                                                getNameForMessage.add(task1.getResult().getString("agent.agent0.firstname") + " " + task1.getResult().getString("agent.agent0.lastname") + " " + task1.getResult().getString("agent.agent0.email"));
                                                            }
                                                            if (task1.getResult().getString("agent.agent1.firstname") != "" && task1.getResult().getString("agent.agent1.lastname") != "" && task1.getResult().get("agent.agent1.status_receiver").equals(true)) {
                                                                getNameForMessage.add(task1.getResult().getString("agent.agent1.firstname") + " " + task1.getResult().getString("agent.agent1.lastname") + " " + task1.getResult().getString("agent.agent1.email"));
                                                            }
                                                            if (task1.getResult().getString("agent.agent2.firstname") != "" && task1.getResult().getString("agent.agent2.lastname") != "" && task1.getResult().get("agent.agent2.status_receiver").equals(true)) {
                                                                getNameForMessage.add(task1.getResult().getString("agent.agent2.firstname") + " " + task1.getResult().getString("agent.agent2.lastname") + " " + task1.getResult().getString("agent.agent2.email"));
                                                            }

                                                            for (int i = 0; i < trackNumberModelList.size(); i++) {
                                                                if (trackNumberModelList.get(i).getCorrectTrackNumber() != null) {
                                                                    Date currentDate = Calendar.getInstance().getTime();
                                                                    String checkIn = currentDate.toString();

                                                                    if (trackNumberModelList.get(i).getImage0() != null) {
                                                                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Parcels/" + checkIn + "." + trackNumberModelList.get(i).getImage0().getLastPathSegment());
                                                                        storageReference.putFile(trackNumberModelList.get(i).getImage0())
                                                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                                    @Override
                                                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                                        Log.d("Add image 0", "สำเร็จ");
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.d("Add image 0", e.getMessage());
                                                                                    }
                                                                                });
                                                                    }

                                                                    if (trackNumberModelList.get(i).getImage1() != null) {
                                                                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Parcels/" + checkIn + "." + trackNumberModelList.get(i).getImage1().getLastPathSegment());
                                                                        storageReference.putFile(trackNumberModelList.get(i).getImage1())
                                                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                                    @Override
                                                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                                        Log.d("Add image 1", "สำเร็จ");
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.d("Add image 1", e.getMessage());
                                                                                    }
                                                                                });
                                                                    }

                                                                    if (trackNumberModelList.get(i).getImage2() != null) {
                                                                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Parcels/" + checkIn + "." + trackNumberModelList.get(i).getImage2().getLastPathSegment());
                                                                        storageReference.putFile(trackNumberModelList.get(i).getImage2())
                                                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                                    @Override
                                                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                                        Log.d("Add image 2", "สำเร็จ");
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.d("Add image 2", e.getMessage());
                                                                                    }
                                                                                });
                                                                    }

                                                                    if (trackNumberModelList.get(i).getImage3() != null) {
                                                                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Parcels/" + checkIn + "." + trackNumberModelList.get(i).getImage3().getLastPathSegment());
                                                                        storageReference.putFile(trackNumberModelList.get(i).getImage3())
                                                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                                    @Override
                                                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                                        Log.d("Add image 3", "สำเร็จ");
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.d("Add image 3", e.getMessage());
                                                                                    }
                                                                                });
                                                                    }

                                                                    Map<String, Object> docParcels = new HashMap<>();
                                                                    Map<String, Object> mapImages = new HashMap<>();
                                                                    if (trackNumberModelList.get(i).getImage0() != null) {
                                                                        mapImages.put("image0", "Parcels/" + checkIn + "." + trackNumberModelList.get(i).getImage0().getLastPathSegment());
                                                                    } else {
                                                                        mapImages.put("image0", "");
                                                                        mapImages.put("image1", "");
                                                                        mapImages.put("image2", "");
                                                                        mapImages.put("image3", "");
                                                                    }
                                                                    if (trackNumberModelList.get(i).getImage1() != null) {
                                                                        mapImages.put("image1", "Parcels/" + checkIn + "." + trackNumberModelList.get(i).getImage1().getLastPathSegment());
                                                                    } else {
                                                                        mapImages.put("image1", "");
                                                                        mapImages.put("image2", "");
                                                                        mapImages.put("image3", "");
                                                                    }
                                                                    if (trackNumberModelList.get(i).getImage2() != null) {
                                                                        mapImages.put("image2", "Parcels/" + checkIn + "." + trackNumberModelList.get(i).getImage2().getLastPathSegment());
                                                                    } else {
                                                                        mapImages.put("image2", "");
                                                                        mapImages.put("image3", "");
                                                                    }
                                                                    if (trackNumberModelList.get(i).getImage3() != null) {
                                                                        mapImages.put("image3", "Parcels/" + checkIn + "." + trackNumberModelList.get(i).getImage3().getLastPathSegment());
                                                                    } else {
                                                                        mapImages.put("image3", "");
                                                                    }
                                                                    docParcels.put("images", mapImages);

                                                                    db.collection("parcels")
                                                                            .whereEqualTo("trackNumber", trackNumberModelList.get(i).getTrackNumber())
                                                                            .get()
                                                                            .addOnCompleteListener(task2 -> {
                                                                                if (task2.isSuccessful()) {
                                                                                    DocumentSnapshot documentSnapshot = task2.getResult().getDocuments().get(0);
                                                                                    String docId = documentSnapshot.getId();

                                                                                    db.collection("parcels").document(docId)
                                                                                            .set(docParcels, SetOptions.merge())
                                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void aVoid) {
                                                                                                    Log.d("Add parcels from admin", "สำเร็จ");
                                                                                                }
                                                                                            })
                                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                                @Override
                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                    Log.w("Error admin add parcels", "Error writing document", e);
                                                                                                }
                                                                                            });

                                                                                    db.collection("parcels").document(docId)
                                                                                            .update("checkIn", checkIn)
                                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void unused) {
                                                                                                    Log.d("Add parcels from admin", "สำเร็จ");
                                                                                                }
                                                                                            })
                                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                                @Override
                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                    Log.w("Error admin add parcels", "Error writing document", e);
                                                                                                }
                                                                                            });
                                                                                }
                                                                            });

                                                                } else if (trackNumberModelList.get(i).getWarningTrackNumber() != null) {
                                                                    String owner = selectOwnerName.getText().toString();
                                                                    String[] getName = owner.split(" ");
                                                                    ArrayList<String> ownerName = new ArrayList<>();
                                                                    for (String name : getName) {
                                                                        ownerName.add(name);
                                                                    }
                                                                    Date currentDate = Calendar.getInstance().getTime();
                                                                    String checkIn = currentDate.toString();

                                                                    trackNumberModelList.get(i).setFirstname(ownerName.get(0));
                                                                    trackNumberModelList.get(i).setLastname(ownerName.get(1));
                                                                    trackNumberModelList.get(i).setCheckIn(checkIn);
                                                                    trackNumberModelList.get(i).setDepartment(department);

                                                                    Map<String, Object> docParcels = new HashMap<>();
                                                                    docParcels.put("checkIn", trackNumberModelList.get(i).getCheckIn());
                                                                    docParcels.put("checkOut", trackNumberModelList.get(i).getCheckOut());
                                                                    docParcels.put("department", trackNumberModelList.get(i).getDepartment());

                                                                    Map<String, Object> mapImages = new HashMap<>();
                                                                    if (trackNumberModelList.get(i).getImage0() != null) {
                                                                        mapImages.put("image0", "Parcels/" + checkIn + "." + trackNumberModelList.get(i).getImage0().getLastPathSegment());
                                                                    } else {
                                                                        mapImages.put("image0", "");
                                                                        mapImages.put("image1", "");
                                                                        mapImages.put("image2", "");
                                                                        mapImages.put("image3", "");
                                                                    }
                                                                    if (trackNumberModelList.get(i).getImage1() != null) {
                                                                        mapImages.put("image1", "Parcels/" + checkIn + "." + trackNumberModelList.get(i).getImage1().getLastPathSegment());
                                                                    } else {
                                                                        mapImages.put("image1", "");
                                                                        mapImages.put("image2", "");
                                                                        mapImages.put("image3", "");
                                                                    }
                                                                    if (trackNumberModelList.get(i).getImage2() != null) {
                                                                        mapImages.put("image2", "Parcels/" + checkIn + "." + trackNumberModelList.get(i).getImage2().getLastPathSegment());
                                                                    } else {
                                                                        mapImages.put("image2", "");
                                                                        mapImages.put("image3", "");
                                                                    }
                                                                    if (trackNumberModelList.get(i).getImage3() != null) {
                                                                        mapImages.put("image3", "Parcels/" + checkIn + "." + trackNumberModelList.get(i).getImage3().getLastPathSegment());
                                                                    } else {
                                                                        mapImages.put("image3", "");
                                                                    }
                                                                    docParcels.put("images", mapImages);

                                                                    Map<String, Object> mapOwner = new HashMap<>();
                                                                    mapOwner.put("firstname", trackNumberModelList.get(i).getFirstname());
                                                                    mapOwner.put("lastname", trackNumberModelList.get(i).getLastname());
                                                                    docParcels.put("owner", mapOwner);

                                                                    Map<String, Object> mapReceiver = new HashMap<>();
                                                                    mapReceiver.put("firstname", "");
                                                                    mapReceiver.put("lastname", "");
                                                                    docParcels.put("receiver", mapReceiver);
                                                                    docParcels.put("trackNumber", trackNumberModelList.get(i).getTrackNumber());

                                                                    db.collection("parcels").document()
                                                                            .set(docParcels)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    Log.d("Add parcels from admin", "สำเร็จ");
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Log.w("Error admin add parcels", "Error writing document", e);
                                                                                }
                                                                            });

                                                                    if (trackNumberModelList.get(i).getImage0() != null) {
                                                                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Parcels/" + checkIn + "." + trackNumberModelList.get(i).getImage0().getLastPathSegment());
                                                                        storageReference.putFile(trackNumberModelList.get(i).getImage0())
                                                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                                    @Override
                                                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                                        Log.d("Add image 0", "สำเร็จ");
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.d("Add image 0", e.getMessage());
                                                                                    }
                                                                                });
                                                                    }

                                                                    if (trackNumberModelList.get(i).getImage1() != null) {
                                                                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Parcels/" + checkIn + "." + trackNumberModelList.get(i).getImage1().getLastPathSegment());
                                                                        storageReference.putFile(trackNumberModelList.get(i).getImage1())
                                                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                                    @Override
                                                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                                        Log.d("Add image 1", "สำเร็จ");
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.d("Add image 1", e.getMessage());
                                                                                    }
                                                                                });
                                                                    }

                                                                    if (trackNumberModelList.get(i).getImage2() != null) {
                                                                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Parcels/" + checkIn + "." + trackNumberModelList.get(i).getImage2().getLastPathSegment());
                                                                        storageReference.putFile(trackNumberModelList.get(i).getImage2())
                                                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                                    @Override
                                                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                                        Log.d("Add image 2", "สำเร็จ");
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.d("Add image 2", e.getMessage());
                                                                                    }
                                                                                });
                                                                    }

                                                                    if (trackNumberModelList.get(i).getImage3() != null) {
                                                                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Parcels/" + checkIn + "." + trackNumberModelList.get(i).getImage3().getLastPathSegment());
                                                                        storageReference.putFile(trackNumberModelList.get(i).getImage3())
                                                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                                    @Override
                                                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                                        Log.d("Add image 3", "สำเร็จ");
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.d("Add image 3", e.getMessage());
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            }

                                                            db.collection("users")
                                                                    .whereEqualTo("email", firebaseUser.getEmail())
                                                                    .get()
                                                                    .addOnCompleteListener(task2 -> {
                                                                        if (task2.isSuccessful()) {
                                                                            DocumentSnapshot getDocIdUser = task2.getResult().getDocuments().get(0);
                                                                            String docIdUser = getDocIdUser.getId();

                                                                            db.collection("users").document(docIdUser)
                                                                                    .get()
                                                                                    .addOnCompleteListener(task3 -> {
                                                                                        if (task3.isSuccessful()) {
                                                                                            for (String nameReceiverMessage : getNameForMessage) {
                                                                                                String[] splitReceiverName = nameReceiverMessage.split(" ");
                                                                                                Date date = Calendar.getInstance().getTime();
                                                                                                String getTime = date.toString();
                                                                                                for (int i = 0; i < trackNumberModelList.size(); i++) {
                                                                                                    String trackNumber = trackNumberModelList.get(i).getTrackNumber();
                                                                                                    Map<String, Object> message = new HashMap<>();
                                                                                                    message.put("data", null);
                                                                                                    message.put("dateTime", getTime);
                                                                                                    message.put("read", false);
                                                                                                    message.put("receiver", null);
                                                                                                    message.put("sender", null);
                                                                                                    message.put("type", 2);

                                                                                                    Map<String, Object> dataMap = new HashMap<>();
                                                                                                    dataMap.put("receiver_parcel", null);
                                                                                                    dataMap.put("owner", null);
                                                                                                    dataMap.put("trackNumber", trackNumber);
                                                                                                    message.put("data", dataMap);

                                                                                                    Map<String, Object> receiverParcelsMap = new HashMap<>();
                                                                                                    receiverParcelsMap.put("firstname", "");
                                                                                                    receiverParcelsMap.put("lastname", "");
                                                                                                    dataMap.put("receiver_parcel", receiverParcelsMap);

                                                                                                    Map<String, Object> ownerMap = new HashMap<>();
                                                                                                    ownerMap.put("firstname", getNameOwner[0]);
                                                                                                    ownerMap.put("lastname", getNameOwner[1]);
                                                                                                    dataMap.put("owner", ownerMap);

                                                                                                    Map<String, Object> receiverMap = new HashMap<>();
                                                                                                    receiverMap.put("firstname", splitReceiverName[0]);
                                                                                                    receiverMap.put("lastname", splitReceiverName[1]);
                                                                                                    receiverMap.put("email", splitReceiverName[2]);
                                                                                                    message.put("receiver", receiverMap);

                                                                                                    Map<String, Object> senderMap = new HashMap<>();
                                                                                                    senderMap.put("firstname", task3.getResult().getString("firstname"));
                                                                                                    senderMap.put("lastname", task3.getResult().getString("lastname"));
                                                                                                    message.put("sender", senderMap);

                                                                                                    db.collection("messages")
                                                                                                            .add(message)
                                                                                                            .addOnSuccessListener(unused -> Log.d("Send message of admin", "สำเร็จ"))
                                                                                                            .addOnFailureListener(e -> Log.d("Send message of admin", e.getMessage()));
                                                                                                }
                                                                                            }
                                                                                            trackNumberSet.clear();
                                                                                            trackNumberModelList.clear();
                                                                                            trackNumberAdapter.notifyDataSetChanged();
                                                                                            redTrackNumber.clear();
                                                                                            indexOfTrackNumber.clear();
                                                                                            dialogAdmin.dismiss();
                                                                                            progressDialog.dismiss();
                                                                                        }
                                                                                    });
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }
                                    });
                        }
                    });
                    dialogAdmin.show();
                } else if (pref.getString("Role", "NoData").equals("user")) {
                    Dialog dialogUser = new Dialog(this);
                    dialogUser.setContentView(R.layout.scanner_dialog);
                    dialogUser.setCanceledOnTouchOutside(false);
                    dialogUser.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    Button buttonReceiveMoreParcels = dialogUser.findViewById(R.id.button_receive_more_parcels);
                    Button buttonConfirm = dialogUser.findViewById(R.id.button_confirm);
                    ImageView imageDialogCancel = dialogUser.findViewById(R.id.image_dialog_scanner_cancel);
                    ListView listViewTrackNumber = dialogUser.findViewById(R.id.listview_track_number);

                    db.collection("parcels")
                            .whereEqualTo("trackNumber", result.getContents())
                            .whereEqualTo("checkOut", null)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    for (QueryDocumentSnapshot getDocParcels : task.getResult()) {
                                        Map<String, Object> ownerMap = getDocParcels.getData();
                                        for (Map.Entry<String, Object> entry : ownerMap.entrySet()) {
                                            if (entry.getKey().equals("owner")) {
                                                Map<String, Object> ownerData = (Map<String, Object>) entry.getValue();
                                                for (Map.Entry<String, Object> NameDataOwner : ownerData.entrySet()) {
                                                    if (NameDataOwner.getKey().equals("lastname") && NameDataOwner.getValue() != null) {
                                                        getOwnerSurname = NameDataOwner.getValue().toString();
                                                    }
                                                    if (NameDataOwner.getKey().equals("firstname") && NameDataOwner.getValue() != null) {
                                                        getOwnerName = NameDataOwner.getValue().toString();
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    db.collection("users")
                                            .whereEqualTo("email", firebaseUser.getEmail())
                                            .get()
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    DocumentSnapshot getDocId = task1.getResult().getDocuments().get(0);
                                                    String docId = getDocId.getId();

                                                    db.collection("users").document(docId)
                                                            .get()
                                                            .addOnCompleteListener(task2 -> {
                                                                if (task2.isSuccessful()) {
                                                                    String userName = task2.getResult().getString("firstname");
                                                                    String userSurname = task2.getResult().getString("lastname");

                                                                    if (getOwnerName.equals(userName) && getOwnerSurname.equals(userSurname)) {
                                                                        if (!trackNumberSet.isEmpty()) {
                                                                            if (trackNumberSet.indexOf(result.getContents()) != -1) {
                                                                                Toast.makeText(this, "คุณเพิ่มหมายเลขพัสดุนี้แล้ว", Toast.LENGTH_LONG).show();
                                                                            } else {
                                                                                trackNumberSet.add(result.getContents());
                                                                                TrackNumberModel resultForScan = new TrackNumberModel();
                                                                                resultForScan.setTrackNumber(result.getContents());
                                                                                resultForScan.setRedTrackNumber(false);
                                                                                trackNumberModelList.add(resultForScan);
                                                                                trackNumberAdapter.notifyDataSetChanged();
                                                                            }
                                                                        } else {
                                                                            trackNumberSet.add(result.getContents());
                                                                            TrackNumberModel resultForScan = new TrackNumberModel();
                                                                            resultForScan.setTrackNumber(result.getContents());
                                                                            resultForScan.setRedTrackNumber(false);
                                                                            trackNumberModelList.add(resultForScan);
                                                                            trackNumberAdapter.notifyDataSetChanged();
                                                                        }
                                                                    } else {
                                                                        db.collection("users")
                                                                                .whereEqualTo("firstname", getOwnerName)
                                                                                .whereEqualTo("lastname", getOwnerSurname)
                                                                                .get()
                                                                                .addOnCompleteListener(task3 -> {
                                                                                    if (task3.isSuccessful() && !task3.getResult().isEmpty()) {
                                                                                        DocumentSnapshot getDocIdOwner = task3.getResult().getDocuments().get(0);
                                                                                        String docIdOwner = getDocIdOwner.getId();

                                                                                        db.collection("users").document(docIdOwner)
                                                                                                .get()
                                                                                                .addOnCompleteListener(task4 -> {
                                                                                                    if (task4.isSuccessful()) {
                                                                                                        ArrayList<String> agentNameArrayList = new ArrayList<>();
                                                                                                        if (task4.getResult().get("agent.agent0.firstname") != "" && task4.getResult().get("agent.agent0.lastname") != "") {
                                                                                                            agentNameArrayList.add(task4.getResult().get("agent.agent0.firstname") + " " + task4.getResult().get("agent.agent0.lastname"));
                                                                                                        }
                                                                                                        if (task4.getResult().get("agent.agent1.firstname") != "" && task4.getResult().get("agent.agent1.lastname") != "") {
                                                                                                            agentNameArrayList.add(task4.getResult().get("agent.agent1.firstname") + " " + task4.getResult().get("agent.agent1.lastname"));
                                                                                                        }
                                                                                                        if (task4.getResult().get("agent.agent2.firstname") != "" && task4.getResult().get("agent.agent2.lastname") != "") {
                                                                                                            agentNameArrayList.add(task4.getResult().get("agent.agent2.firstname") + " " + task4.getResult().get("agent.agent2.lastname"));
                                                                                                        }

                                                                                                        if (agentNameArrayList != null) {
                                                                                                            if (agentNameArrayList.indexOf(String.format("%s %s", userName, userSurname)) != -1) {
                                                                                                                if (!trackNumberSet.isEmpty()) {
                                                                                                                    if (trackNumberSet.indexOf(result.getContents()) != -1) {
                                                                                                                        Toast.makeText(this, "คุณเพิ่มหมายเลขพัสดุนี้แล้ว", Toast.LENGTH_LONG).show();
                                                                                                                    } else {
                                                                                                                        trackNumberSet.add(result.getContents());
                                                                                                                        TrackNumberModel resultForScan = new TrackNumberModel();
                                                                                                                        resultForScan.setTrackNumber(result.getContents());
                                                                                                                        resultForScan.setRedTrackNumber(false);
                                                                                                                        trackNumberModelList.add(resultForScan);
                                                                                                                        trackNumberAdapter.notifyDataSetChanged();
                                                                                                                    }
                                                                                                                } else {
                                                                                                                    trackNumberSet.add(result.getContents());
                                                                                                                    TrackNumberModel resultForScan = new TrackNumberModel();
                                                                                                                    resultForScan.setTrackNumber(result.getContents());
                                                                                                                    resultForScan.setRedTrackNumber(false);
                                                                                                                    trackNumberModelList.add(resultForScan);
                                                                                                                    trackNumberAdapter.notifyDataSetChanged();
                                                                                                                }
                                                                                                            } else {
                                                                                                                if (!trackNumberSet.isEmpty()) {
                                                                                                                    if (trackNumberSet.indexOf(result.getContents()) != -1) {
                                                                                                                        Toast.makeText(this, "คุณเพิ่มหมายเลขพัสดุนี้แล้ว", Toast.LENGTH_LONG).show();
                                                                                                                    } else {
                                                                                                                        trackNumberSet.add(result.getContents());
                                                                                                                        TrackNumberModel resultForScan = new TrackNumberModel();
                                                                                                                        resultForScan.setTrackNumber(result.getContents());
                                                                                                                        resultForScan.setRedTrackNumber(true);
                                                                                                                        trackNumberModelList.add(resultForScan);
                                                                                                                        trackNumberAdapter.notifyDataSetChanged();
                                                                                                                    }
                                                                                                                } else {
                                                                                                                    trackNumberSet.add(result.getContents());
                                                                                                                    TrackNumberModel resultForScan = new TrackNumberModel();
                                                                                                                    resultForScan.setTrackNumber(result.getContents());
                                                                                                                    resultForScan.setRedTrackNumber(true);
                                                                                                                    trackNumberModelList.add(resultForScan);
                                                                                                                    trackNumberAdapter.notifyDataSetChanged();
                                                                                                                }
                                                                                                                Toast.makeText(this, "พัสดุนี้ไม่อยู่ในรายการของคุณ", Toast.LENGTH_LONG).show();
                                                                                                            }
                                                                                                        } else {
                                                                                                            Toast.makeText(this, "พัสดุนี้ไม่อยู่ในรายการของคุณ", Toast.LENGTH_LONG).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    } else {
                                                                                        Toast.makeText(this, "ไม่พบข้อมูลเจ้าของพัสดุ", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            });
                                                }
                                            });

                                } else {
                                    Toast.makeText(dialogUser.getContext(), "ขออภัยพัสดุนี้ไม่ได้ถูกบันทึกไว้ภายในระบบหรือถูกเชคเอาท์แล้ว", Toast.LENGTH_LONG).show();
                                }
                            });
                    trackNumberAdapter = new TrackNumberAdapter(dialogUser.getContext(), trackNumberModelList);
                    listViewTrackNumber.setAdapter(trackNumberAdapter);
                    trackNumberAdapter.notifyDataSetChanged();
                    listViewTrackNumber.setClickable(true);

                    listViewTrackNumber.setOnItemClickListener((parent, view, position, id) -> {
                        TextView trackNumber = view.findViewById(R.id.tracknumber);
                        String trackNumberSelect = trackNumber.getText().toString();
                        trackNumberPosition = listViewTrackNumber.getItemAtPosition(position).toString();
                        for (int i = 0; i < trackNumberModelList.size(); i++) {
                            String positionName = trackNumberModelList.get(i).toString();
                            indexOfTrackNumber.put(positionName, i);
                        }
                        new AlertDialog.Builder(this)
                                .setTitle(trackNumberSelect)
                                .setMessage("ท่านต้องการที่จะลบรายการพัสดุนี้ใช่หรือไม่")
                                .setPositiveButtonIcon(getDrawable(R.drawable.ic_baseline_delete_24))
                                .setPositiveButton("ลบหมายเลขติดตามพัสดุ", (deleteTrackNumber, which) -> {
                                    if (redTrackNumber.indexOf(trackNumberSelect) != -1) {
                                        redTrackNumber.remove(trackNumberSelect);
                                        trackNumberModelList.remove(position);
                                        trackNumberSet.remove(trackNumberSelect);
                                        trackNumberAdapter.notifyDataSetChanged();
                                    } else {
                                        trackNumberModelList.remove(position);
                                        trackNumberSet.remove(trackNumberSelect);
                                        trackNumberAdapter.notifyDataSetChanged();
                                    }
                                })
                                .setNeutralButton("ยกเลิก", (dialog, which) -> {
                                    dialog.dismiss();
                                })
                                .show();
                    });

                    imageDialogCancel.setOnClickListener(v -> {
                        trackNumberSet.clear();
                        trackNumberModelList.clear();
                        trackNumberAdapter.notifyDataSetChanged();
                        redTrackNumber.clear();
                        indexOfTrackNumber.clear();
                        dialogUser.dismiss();
                    });

                    buttonReceiveMoreParcels.setOnClickListener(v -> {
                        scanBarcode();
                        dialogUser.dismiss();
                    });

                    buttonConfirm.setOnClickListener(v -> {
                        db.collection("users")
                                .whereEqualTo("email", firebaseUser.getEmail())
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot getDocId = task.getResult().getDocuments().get(0);
                                        String docId = getDocId.getId();
                                        ArrayList<String> receiverMessage = new ArrayList<>();

                                        db.collection("users").document(docId)
                                                .get()
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        redTrackNumber.clear();
                                                        for (int i = 0; i < trackNumberModelList.size(); i++) {
                                                            if (trackNumberModelList.get(i).getRedTrackNumber().equals(true)) {
                                                                Toast.makeText(this, "กรุณาลบหมายเลขติดตามพัสดุที่มีสถานะสีแดง", Toast.LENGTH_SHORT).show();
                                                                redTrackNumber.add(trackNumberModelList.get(i).getTrackNumber());
                                                            }
                                                        }

                                                        if (trackNumberModelList.isEmpty()) {
                                                            Toast.makeText(this, "กรุณาเพิ่มหมายเลขติดตามพัสดุที่ท่านต้องการ", Toast.LENGTH_SHORT).show();
                                                        } else if (redTrackNumber.isEmpty()) {
                                                            progressDialog = new ProgressDialog(this);
                                                            progressDialog.setCanceledOnTouchOutside(false);
                                                            progressDialog.setMessage("กรุณารอสักครู่");
                                                            progressDialog.show();
                                                            String receiverName = task1.getResult().getString("firstname");
                                                            String receiverSurname = task1.getResult().getString("lastname");

                                                            for (int i = 0; i < trackNumberModelList.size(); i++) {
                                                                Date date = Calendar.getInstance().getTime();
                                                                String checkOut = date.toString();
                                                                db.collection("parcels")
                                                                        .whereEqualTo("trackNumber", trackNumberModelList.get(i).getTrackNumber())
                                                                        .get()
                                                                        .addOnCompleteListener(task2 -> {
                                                                            if (task2.isSuccessful()) {
                                                                                DocumentSnapshot getDocParcelId = task2.getResult().getDocuments().get(0);
                                                                                String docParcelId = getDocParcelId.getId();

                                                                                db.collection("parcels").document(docParcelId)
                                                                                        .update("checkOut", checkOut)
                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void unused) {
                                                                                                Log.d("user get parcel", "สำเร็จ");
                                                                                            }
                                                                                        })
                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                Log.d("user get parcel", e.getMessage());
                                                                                            }
                                                                                        });

                                                                                Map<String, Object> parcelMap = new HashMap<>();
                                                                                Map<String, Object> receiverMap = new HashMap<>();
                                                                                receiverMap.put("firstname", receiverName);
                                                                                receiverMap.put("lastname", receiverSurname);
                                                                                parcelMap.put("receiver", receiverMap);

                                                                                db.collection("parcels").document(docParcelId)
                                                                                        .set(parcelMap, SetOptions.merge())
                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                                Log.d("user get parcel", "DocumentSnapshot successfully written!");
                                                                                            }
                                                                                        })
                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                Log.w("user get parcel", "Error writing document", e);
                                                                                            }
                                                                                        });
                                                                                Toast.makeText(this, "รับพัสดุเรียบร้อย", Toast.LENGTH_LONG).show();
                                                                            }
                                                                        });
                                                            }

                                                            db.collection("users")
                                                                    .whereEqualTo("role", "admin")
                                                                    .get()
                                                                    .addOnCompleteListener(task2 -> {
                                                                        if (task2.isSuccessful()) {
                                                                            Date date = Calendar.getInstance().getTime();
                                                                            String getTime = date.toString();
                                                                            for (QueryDocumentSnapshot getNameAdmin : task2.getResult()) {
                                                                                receiverMessage.add(getNameAdmin.getString("firstname") + " " + getNameAdmin.getString("lastname") + " " + getNameAdmin.getString("email"));
                                                                            }
                                                                        }
                                                                    });

                                                            db.collection("users")
                                                                    .whereEqualTo("email", firebaseUser.getEmail())
                                                                    .get()
                                                                    .addOnCompleteListener(task2 -> {
                                                                        if (task2.isSuccessful()) {
                                                                            DocumentSnapshot getDocIdUser = task2.getResult().getDocuments().get(0);
                                                                            String docIdUser = getDocIdUser.getId();
                                                                            db.collection("users").document(docIdUser)
                                                                                    .get()
                                                                                    .addOnCompleteListener(task3 -> {
                                                                                        if (task3.isSuccessful()) {
                                                                                            receiverMessage.add(task3.getResult().getString("firstname") + " " + task3.getResult().getString("lastname") + " " + task3.getResult().getString("email"));
                                                                                            if (task3.getResult().get("agent.agent0.firstname") != "" && task3.getResult().get("agent.agent0.lastname") != "" && task3.getResult().get("agent.agent0.status_receiver").equals(true)) {
                                                                                                receiverMessage.add(task3.getResult().get("agent.agent0.firstname") + " " + task3.getResult().get("agent.agent0.lastname") + " " + task3.getResult().getString("agent.agent0.email"));
                                                                                            }
                                                                                            if (task3.getResult().get("agent.agent1.firstname") != "" && task3.getResult().get("agent.agent1.lastname") != "" && task3.getResult().get("agent.agent1.status_receiver").equals(true)) {
                                                                                                receiverMessage.add(task3.getResult().get("agent.agent1.firstname") + " " + task3.getResult().get("agent.agent1.lastname") + " " + task3.getResult().getString("agent.agent1.email"));
                                                                                            }
                                                                                            if (task3.getResult().get("agent.agent2.firstname") != "" && task3.getResult().get("agent.agent2.lastname") != "" && task3.getResult().get("agent.agent2.status_receiver").equals(true)) {
                                                                                                receiverMessage.add(task3.getResult().get("agent.agent2.firstname") + " " + task3.getResult().get("agent.agent2.lastname") + " " + task3.getResult().getString("agent.agent2.email"));
                                                                                            }

                                                                                            for (int i = 0; i < trackNumberModelList.size(); i++) {
                                                                                                db.collection("parcels")
                                                                                                        .whereEqualTo("trackNumber", trackNumberModelList.get(i).getTrackNumber())
                                                                                                        .get()
                                                                                                        .addOnCompleteListener(task4 -> {
                                                                                                            DocumentSnapshot getDocIdParcel = task4.getResult().getDocuments().get(0);
                                                                                                            String docIdParcel = getDocIdParcel.getId();

                                                                                                            db.collection("parcels").document(docIdParcel)
                                                                                                                    .get()
                                                                                                                    .addOnCompleteListener(task5 -> {
                                                                                                                        if (task5.isSuccessful()) {
                                                                                                                            Date date = Calendar.getInstance().getTime();
                                                                                                                            String getTime = date.toString();
                                                                                                                            for (String receiverMessageName : receiverMessage) {
                                                                                                                                String[] splitName = receiverMessageName.split(" ");
                                                                                                                                Map<String, Object> message = new HashMap<>();
                                                                                                                                message.put("data", null);
                                                                                                                                message.put("dateTime", getTime);
                                                                                                                                message.put("read", false);
                                                                                                                                message.put("receiver", null);
                                                                                                                                message.put("sender", null);
                                                                                                                                message.put("type", 0);

                                                                                                                                Map<String, Object> dataMap = new HashMap<>();
                                                                                                                                dataMap.put("receiver_parcel", null);
                                                                                                                                dataMap.put("owner", null);
                                                                                                                                dataMap.put("trackNumber", task5.getResult().getString("trackNumber"));
                                                                                                                                message.put("data", dataMap);

                                                                                                                                Map<String, Object> receiverParcelsMap = new HashMap<>();
                                                                                                                                receiverParcelsMap.put("firstname", receiverName);
                                                                                                                                receiverParcelsMap.put("lastname", receiverSurname);
                                                                                                                                dataMap.put("receiver_parcel", receiverParcelsMap);

                                                                                                                                Map<String, Object> ownerMap = new HashMap<>();
                                                                                                                                ownerMap.put("firstname", task5.getResult().get("owner.firstname"));
                                                                                                                                ownerMap.put("lastname", task5.getResult().get("owner.lastname"));
                                                                                                                                dataMap.put("owner", ownerMap);

                                                                                                                                Map<String, Object> receiverMap = new HashMap<>();
                                                                                                                                receiverMap.put("firstname", splitName[0]);
                                                                                                                                receiverMap.put("lastname", splitName[1]);
                                                                                                                                receiverMap.put("email",splitName[2]);
                                                                                                                                message.put("receiver", receiverMap);

                                                                                                                                Map<String, Object> senderMap = new HashMap<>();
                                                                                                                                senderMap.put("firstname", receiverName);
                                                                                                                                senderMap.put("lastname", receiverSurname);
                                                                                                                                message.put("sender", senderMap);

                                                                                                                                db.collection("messages")
                                                                                                                                        .add(message)
                                                                                                                                        .addOnSuccessListener(unused -> Log.d("Send message of admin", "สำเร็จ"))
                                                                                                                                        .addOnFailureListener(e -> Log.d("Send message of admin", e.getMessage()));
                                                                                                                            }
                                                                                                                        }
                                                                                                                    });
                                                                                                        });
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                    });
                                                            dialogUser.dismiss();
                                                            progressDialog.dismiss();
                                                        }
                                                    }
                                                });
                                    }
                                });
                    });
                    dialogUser.show();
                }
            } else {
                Toast.makeText(this, "ไม่พบข้อมูล", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    protected void scanBarcode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptuerBarcode.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning code");
        integrator.initiateScan();
    }

    public void getAllNameUser() {
        ownerName.clear();
        db.collection("users")
                .whereNotEqualTo("role", "admin")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot nameUser : task.getResult()) {
                            if (!nameUser.getString("email").equals(firebaseUser.getEmail())) {
                                ownerName.add(nameUser.get("firstname").toString() + " " + nameUser.get("lastname"));
                            }
                        }

                    }
                });
    }

    public void getDepartmentUser() {
        db.collection("users")
                .whereEqualTo("email", firebaseUser.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        String docId = documentSnapshot.getId();
                        db.collection("users").document(docId)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        DocumentSnapshot getDepartment = task1.getResult();
                                        String departmentData = getDepartment.getString("department");
                                        department = departmentData;
                                    }
                                });

                    }
                });
    }

    protected void permissionRequest() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission", "Permission granted");
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(this, "โปรดอนุมัติคำขอเพื่อสแกนบาร์โค้ด", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "Permission granted");
            } else {
                Toast.makeText(this, "โปรดอนุมัติคำขอเพื่อสแกนบาร์โค้ด", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}