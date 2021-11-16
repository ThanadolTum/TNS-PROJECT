package com.example.tnscpe;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.integration.android.IntentIntegrator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class Fragment_ScanBarcode extends Fragment {

    private Button scanBarcode;
    private FloatingActionButton buttonAddParcelManual;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private SharedPreferences pref;
    private ArrayList<TrackNumberModel> trackNumberModelList = new ArrayList<>();
    private TrackNumberAdapter trackNumberAdapter;
    private ArrayList<String> trackNumberSet = new ArrayList<>();
    private ArrayList<String> redTrackNumber = new ArrayList<>();
    private HashSet<String> ownerName = new HashSet<>();
    private ActivityResultLauncher<Intent> activityResultAddImageParcel;
    private Dialog dialogAddImage;
    private Map<String, Integer> indexOfTrackNumber = new HashMap<>();
    private String trackNumberPosition;
    private String department;
    private ProgressDialog progressDialog;

    public Fragment_ScanBarcode() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__scan_barcode, container, false);
        scanBarcode = view.findViewById(R.id.button_scan_barcode);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        dialogAddImage = new Dialog(getContext());
        getAllNameUser();
        getDepartmentUser();
        pref = getActivity().getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        if (pref.getString("Role", "NoData").equals("user")) {
            buttonAddParcelManual = view.findViewById(R.id.button_add_notification);
            buttonAddParcelManual.setVisibility(View.INVISIBLE);
        } else if (pref.getString("Role", "NoData").equals("admin")) {
            buttonAddParcelManual = view.findViewById(R.id.button_add_notification);
            buttonAddParcelManual.setVisibility(View.VISIBLE);
        }
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
                        Toast.makeText(getContext(), "สามารถเลือกรูปภาพสูงสุด 4 รูปภาพ", Toast.LENGTH_SHORT).show();
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
        scanBarcode.setOnClickListener(v -> {
            scanBarcode();
        });
        buttonAddParcelManual.setOnClickListener(v -> {
            Dialog dialogAdmin = new Dialog(getContext());
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
                    trackNumberAdapter.setUsername(getName[0]);
                    trackNumberAdapter.setUserSurname(getName[1]);
                    trackNumberAdapter.notifyDataSetChanged();
                }
            });

            trackNumberAdapter = new TrackNumberAdapter(dialogAdmin.getContext(), trackNumberModelList);
            listViewTrackNumber.setAdapter(trackNumberAdapter);
            trackNumberAdapter.notifyDataSetChanged();
            listViewTrackNumber.setClickable(true);

            buttonAddTrackNumber.setOnClickListener(v1 -> {
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
                                                            Toast.makeText(getContext(), "คุณเพิ่มหมายเลขพัสดุนี้แล้ว", Toast.LENGTH_LONG).show();
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
                                            Toast.makeText(getContext(), "คุณเพิ่มหมายเลขพัสดุนี้แล้ว", Toast.LENGTH_LONG).show();
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

            listViewTrackNumber.setOnItemClickListener((parent, view1, position, id) -> {
                TextView trackNumber = view1.findViewById(R.id.tracknumber);
                String trackNumberSelect = trackNumber.getText().toString();
                trackNumberPosition = listViewTrackNumber.getItemAtPosition(position).toString();
                for (int i = 0; i < trackNumberModelList.size(); i++) {
                    String positionName = trackNumberModelList.get(i).toString();
                    indexOfTrackNumber.put(positionName, i);
                }
                new AlertDialog.Builder(getContext())
                        .setTitle(trackNumberSelect)
                        .setMessage("หากต้องการเพิ่มรูปภาพ สามารถเพิ่มรูปภาพได้สูงสุด 4 รูปภาพ")
                        .setPositiveButtonIcon(getContext().getDrawable(R.drawable.ic_baseline_image_search_24))
                        .setPositiveButton("เพิ่มรูปภาพ", (addImage, which) -> {
                            Intent intentAddImages = new Intent();
                            intentAddImages.setType("image/*");
                            intentAddImages.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            intentAddImages.setAction(Intent.ACTION_GET_CONTENT);
                            activityResultAddImageParcel.launch(intentAddImages);
                        })
                        .setNegativeButtonIcon(getContext().getDrawable(R.drawable.ic_baseline_delete_24))
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

            imageDialogCancel.setOnClickListener(v1 -> {
                trackNumberSet.clear();
                trackNumberModelList.clear();
                trackNumberAdapter.notifyDataSetChanged();
                indexOfTrackNumber.clear();
                redTrackNumber.clear();
                dialogAdmin.dismiss();
            });

            buttonAddParcel.setVisibility(View.GONE);

            buttonSendNotification.setOnClickListener(v1 -> {
                redTrackNumber.clear();
                for (int i = 0; i < trackNumberModelList.size(); i++) {
                    if (trackNumberModelList.get(i).getRedTrackNumber().equals(true)) {
                        Toast.makeText(dialogAdmin.getContext(), "กรุณาลบหมายเลขติดตามพัสดุที่มีสถานะสีแดง", Toast.LENGTH_SHORT).show();
                        redTrackNumber.add(trackNumberModelList.get(i).getTrackNumber());
                    }
                }

                if (selectOwnerName.getText().toString().isEmpty()) {
                    Toast.makeText(dialogAdmin.getContext(), "กรุณาเลือกเจ้าของพัสดุ", Toast.LENGTH_SHORT).show();
                } else if (trackNumberModelList.isEmpty()) {
                    Toast.makeText(dialogAdmin.getContext(), "กรุณาเพิ่มหมายเลขติดตามพัสดุที่ท่านต้องการ", Toast.LENGTH_SHORT).show();
                } else if (redTrackNumber.isEmpty()) {
                    progressDialog = new ProgressDialog(dialogAdmin.getContext());
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setMessage("กรุณารอสักครู่");
                    progressDialog.show();
                    ArrayList<String> getNameForMessage = new ArrayList<>();
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
                                                                                            dataMap.put("trackNumber", trackNumberModelList.get(i).getTrackNumber());
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
                                                                                            receiverMap.put("email",splitReceiverName[2]);
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
                                                                                    indexOfTrackNumber.clear();
                                                                                    redTrackNumber.clear();
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
        });
        return view;
    }

    protected void scanBarcode() {
        IntentIntegrator integrator = new IntentIntegrator(getActivity());
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
}