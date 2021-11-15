package com.example.tnscpe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ParcelsAdapterAdmin extends RecyclerView.Adapter {

    Context context;
    ArrayList<ParcelsModel> parcelsModelList;
    ArrayList<String> imageResultArraylist = new ArrayList<>();
    ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();
    ImageParcelsSliderAdapter parcelsSliderAdapter;
    ProgressDialog progressDialog;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    SharedPreferences pref;

    public ParcelsAdapterAdmin(Context context, ArrayList<ParcelsModel> parcelsModelsList) {
        this.context = context;
        this.parcelsModelList = parcelsModelsList;
    }

    public class ViewHolderOne extends RecyclerView.ViewHolder {

        TextView checkIn, checkInTime, trackNumber, ownerFname, ownerLname;
        Button buttonEditParcel, buttonCheckOutParcel;
        ImageView imageParcel;

        public ViewHolderOne(@NonNull View itemView) {
            super(itemView);
            checkIn = itemView.findViewById(R.id.check_in);
            checkInTime = itemView.findViewById(R.id.check_in_time);
            trackNumber = itemView.findViewById(R.id.tracknumber);
            ownerLname = itemView.findViewById(R.id.owner_name);
            ownerFname = itemView.findViewById(R.id.owner_surname);
            buttonEditParcel = itemView.findViewById(R.id.button_edit_parcel);
            buttonCheckOutParcel = itemView.findViewById(R.id.button_check_out_parcel);
            imageParcel = itemView.findViewById(R.id.image_parcel);
        }
    }

    public class ViewHolderTwo extends RecyclerView.ViewHolder {

        TextView trackNumber;
        TextView trackNumberInList;
        TextView ownerFname;
        TextView ownerLname;
        TextView checkIn;
        TextView checkInTime;
        Button buttonEditParcel;
        Button buttonCheckOutParcel;
        ImageView imageParcel;
        ImageView dropList;
        ConstraintLayout constraintLayout;

        public ViewHolderTwo(@NonNull View itemView) {
            super(itemView);
            checkIn = itemView.findViewById(R.id.check_in);
            checkInTime = itemView.findViewById(R.id.check_in_time);
            trackNumber = itemView.findViewById(R.id.tracknumber);
            ownerLname = itemView.findViewById(R.id.owner_surname);
            ownerFname = itemView.findViewById(R.id.owner_name);
            buttonEditParcel = itemView.findViewById(R.id.button_edit_parcel);
            buttonCheckOutParcel = itemView.findViewById(R.id.button_check_out_parcel);
            imageParcel = itemView.findViewById(R.id.image_parcel);
            trackNumberInList = itemView.findViewById(R.id.tracknumber_2);
            constraintLayout = itemView.findViewById(R.id.parcels_listview_layout);
            dropList = itemView.findViewById(R.id.drop_list);
            dropList.setOnClickListener(v -> {
                ParcelsModel parcelsModel = parcelsModelList.get(getAdapterPosition());
                parcelsModel.setDetailInListView(!parcelsModel.detailInListView);
                notifyItemChanged(getAdapterPosition());
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        for (int i = 0; i < parcelsModelList.size(); i++) {
            if (parcelsModelList.get(i).getListView().equals(true)) {
                return 0;
            }
        }
        return 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(context).inflate(R.layout.listview_parcels_home_page_admin, parent, false);
            return new ViewHolderTwo(view);
        }
        view = LayoutInflater.from(context).inflate(R.layout.cardview_parcels_home_page_admin, parent, false);
        return new ViewHolderOne(view);
    }

    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (parcelsModelList.get(position).getListView().equals(false)) {
            ParcelsModel parcelsModel = parcelsModelList.get(position);
            pref = context.getSharedPreferences("CurrentUser",Context.MODE_PRIVATE);
            ViewHolderOne viewHolderOne = (ViewHolderOne) holder;
            viewHolderOne.trackNumber.setText(parcelsModel.getTrackNumber());
            viewHolderOne.ownerFname.setText(parcelsModel.getOwner().getFirstname());
            viewHolderOne.ownerLname.setText(parcelsModel.getOwner().getLastname());
            String getCheckIn = parcelsModel.getCheckIn();
            if (getCheckIn != null) {
                String[] splitDate = getCheckIn.split(" ");
                if (splitDate.length == 6) {
                    String date = splitDate[2];
                    String month = null;
                    String year = String.valueOf(Integer.parseInt(splitDate[5]) + 543);
                    String getMonth = splitDate[1];
                    switch (getMonth) {
                        case "Jan":
                            month = "มกราคม";
                            break;
                        case "Feb":
                            month = "กุมภาพันธ์";
                            break;
                        case "Mar":
                            month = "มีนาคม";
                            break;
                        case "Apr":
                            month = "เมษายน";
                            break;
                        case "May":
                            month = "พฤษภาคม";
                            break;
                        case "Jun":
                            month = "มิถุนายน";
                            break;
                        case "Jul":
                            month = "กรกฎาคม";
                            break;
                        case "Aug":
                            month = "สิงหาคม";
                            break;
                        case "Sep":
                            month = "กันยายน";
                            break;
                        case "Oct":
                            month = "ตุลาคม";
                            break;
                        case "Nov":
                            month = "พฤศจิกายน";
                            break;
                        case "Dec":
                            month = "ธันวาคม";
                            break;
                    }
                    String checkIn = String.format("%s %s %s", date, month, year);
                    viewHolderOne.checkIn.setText(checkIn);
                    String[] splitTime = splitDate[3].split(":");
                    String checkInTime = String.format("%s:%s น.", splitTime[0], splitTime[1]);
                    viewHolderOne.checkInTime.setText(checkInTime);
                } else if (splitDate.length == 7) {
                    String date = splitDate[2];
                    String month = null;
                    String year = String.valueOf(Integer.parseInt(splitDate[3]) + 543);
                    String getMonth = splitDate[1];
                    switch (getMonth) {
                        case "Jan":
                            month = "มกราคม";
                            break;
                        case "Feb":
                            month = "กุมภาพันธ์";
                            break;
                        case "Mar":
                            month = "มีนาคม";
                            break;
                        case "Apr":
                            month = "เมษายน";
                            break;
                        case "May":
                            month = "พฤษภาคม";
                            break;
                        case "Jun":
                            month = "มิถุนายน";
                            break;
                        case "Jul":
                            month = "กรกฎาคม";
                            break;
                        case "Aug":
                            month = "สิงหาคม";
                            break;
                        case "Sep":
                            month = "กันยายน";
                            break;
                        case "Oct":
                            month = "ตุลาคม";
                            break;
                        case "Nov":
                            month = "พฤศจิกายน";
                            break;
                        case "Dec":
                            month = "ธันวาคม";
                            break;
                    }
                    String checkIn = String.format("%s %s %s", date, month, year);
                    viewHolderOne.checkIn.setText(checkIn);
                    String[] splitTime = splitDate[4].split(":");
                    String checkInTime = String.format("%s:%s น.", splitTime[0], splitTime[1]);
                    viewHolderOne.checkInTime.setText(checkInTime);
                }
            } else {
                viewHolderOne.checkIn.setText("-");
                viewHolderOne.checkInTime.setText("-");
            }
            db.collection("parcels")
                    .whereEqualTo("trackNumber", parcelsModel.getTrackNumber())
                    .whereNotEqualTo("images.image0", "")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            String docId = documentSnapshot.getId();

                            db.collection("parcels").document(docId)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Map<String, Object> getData = task1.getResult().getData();
                                            for (Map.Entry<String, Object> dataResult : getData.entrySet()) {
                                                if (dataResult.getKey().equals("images")) {
                                                    Map<String, Object> getImage = (Map<String, Object>) dataResult.getValue();
                                                    for (Map.Entry<String, Object> imageResult : getImage.entrySet()) {
                                                        if (imageResult.getKey().equals("image0") && imageResult.getValue() != "") {
                                                            StorageReference storageReference;
                                                            storageReference = storage.getReference().child(imageResult.getValue().toString());
                                                            try {
                                                                File file = File.createTempFile("Parcels", "jpg");
                                                                storageReference.getFile(file)
                                                                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                                            @Override
                                                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                                                                viewHolderOne.imageParcel.setImageBitmap(bitmap);
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
                                                            viewHolderOne.imageParcel.setImageResource(R.drawable.parcel);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                        }
                    });
            viewHolderOne.imageParcel.setOnClickListener(v -> {
                imageResultArraylist.clear();
                bitmapArrayList.clear();
                String trackNumber = parcelsModel.getTrackNumber();
                Dialog imageParcelDialog = new Dialog(context);
                imageParcelDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                imageParcelDialog.setContentView(R.layout.image_parcels_slider);
                imageParcelDialog.show();
                SliderView imageSlider = imageParcelDialog.findViewById(R.id.image_parcel_slider);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                progressDialog = new ProgressDialog(context);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("กรุณารอสักครู่...");
                progressDialog.show();
                db.collection("parcels")
                        .whereEqualTo("trackNumber", trackNumber)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                String docId = documentSnapshot.getId();
                                db.collection("parcels").document(docId)
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Map<String, Object> getData = task1.getResult().getData();
                                                for (Map.Entry<String, Object> dataResult : getData.entrySet()) {
                                                    if (dataResult.getKey().equals("images")) {
                                                        Map<String, Object> getImage = (Map<String, Object>) dataResult.getValue();
                                                        for (Map.Entry<String, Object> imageResult : getImage.entrySet()) {
                                                            if (imageResult.getKey().equals("image0") && imageResult.getValue() != "") {
                                                                imageResultArraylist.add(imageResult.getValue().toString());
                                                            }
                                                            if (imageResult.getKey().equals("image1") && imageResult.getValue() != "") {
                                                                imageResultArraylist.add(imageResult.getValue().toString());
                                                            }
                                                            if (imageResult.getKey().equals("image2") && imageResult.getValue() != "") {
                                                                imageResultArraylist.add(imageResult.getValue().toString());
                                                            }
                                                            if (imageResult.getKey().equals("image3") && imageResult.getValue() != "") {
                                                                imageResultArraylist.add(imageResult.getValue().toString());
                                                            }
                                                        }
                                                    }
                                                }
                                                if (!imageResultArraylist.isEmpty()) {
                                                    for (String result : imageResultArraylist) {
                                                        StorageReference storageReference;
                                                        storageReference = storage.getReference().child(result);

                                                        try {
                                                            File file = File.createTempFile("Parcels", "jpg");
                                                            storageReference.getFile(file)
                                                                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                                                            bitmapArrayList.add(bitmap);
                                                                            parcelsSliderAdapter = new ImageParcelsSliderAdapter(bitmapArrayList);
                                                                            imageSlider.setSliderAdapter(parcelsSliderAdapter);
                                                                            imageSlider.setIndicatorAnimation(IndicatorAnimationType.WORM);
                                                                            imageSlider.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION);
                                                                            progressDialog.dismiss();
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.d("get image in card home page", e.getMessage());
                                                                            if (progressDialog.isShowing()) {
                                                                                progressDialog.dismiss();
                                                                                imageParcelDialog.dismiss();
                                                                            }
                                                                        }
                                                                    });
                                                        } catch (IOException e) {
                                                            Log.d("get image in card home page", e.getMessage());
                                                            if (progressDialog.isShowing()) {
                                                                progressDialog.dismiss();
                                                                imageParcelDialog.dismiss();
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    if (progressDialog.isShowing()) {
                                                        progressDialog.dismiss();
                                                        imageParcelDialog.dismiss();
                                                    }
                                                    Toast.makeText(context, "พัสดุนี้ไม่มีรูปภาพที่บันทึกไว้", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        });
            });
            viewHolderOne.buttonEditParcel.setOnClickListener(v -> {
                Dialog editTrackNumberDialog = new Dialog(context);
                editTrackNumberDialog.setCanceledOnTouchOutside(false);
                editTrackNumberDialog.setContentView(R.layout.edit_parcel_dialog);
                editTrackNumberDialog.show();
                ImageView imageCloseEditParcelDialog = editTrackNumberDialog.findViewById(R.id.close_edit_parcel_dialog);
                Button buttonDeleteParcel = editTrackNumberDialog.findViewById(R.id.button_delete_parcels);
                Button buttonEditTrackNumber = editTrackNumberDialog.findViewById(R.id.button_confirm_edit_tracknumber);
                EditText editTrackNumber = editTrackNumberDialog.findViewById(R.id.edit_tracknumber);
                TextView oldTrackNumber = editTrackNumberDialog.findViewById(R.id.old_trackNumber);
                oldTrackNumber.setText(parcelsModel.getTrackNumber());

                imageCloseEditParcelDialog.setOnClickListener(v1 -> {
                    editTrackNumberDialog.dismiss();
                });

                buttonDeleteParcel.setOnClickListener(v1 -> {
                    db.collection("parcels")
                            .whereEqualTo("trackNumber", parcelsModel.getTrackNumber())
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                    String docId = documentSnapshot.getId();
                                    new AlertDialog.Builder(context)
                                            .setTitle(parcelsModel.getTrackNumber())
                                            .setMessage("ท่านแน่ใจแล้วใช่หรือไม่ที่จะลบรายการพัสดุนี้")
                                            .setPositiveButton("ยืนยัน", (dialog, which) -> {
                                                progressDialog = new ProgressDialog(context);
                                                progressDialog.setCanceledOnTouchOutside(false);
                                                progressDialog.setMessage("กรุณารอสักครู่...");
                                                progressDialog.show();
                                                db.collection("parcels").document(docId)
                                                        .delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Toast.makeText(context, "ลบรายการพัสดุเรียบร้อย", Toast.LENGTH_SHORT).show();
                                                                editTrackNumberDialog.dismiss();
                                                                progressDialog.dismiss();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(context, "กรุณาลองใหม่ในภายหลัง", Toast.LENGTH_SHORT).show();
                                                                Log.d("Error delete parcel", e.getMessage());
                                                            }
                                                        });
                                                parcelsModelList.remove(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position, parcelsModelList.size());
                                            })
                                            .setNegativeButton("ยกเลิก", (dialog, which) -> {
                                                dialog.dismiss();
                                            })
                                            .show();
                                } else {
                                    Toast.makeText(context, "กรุณาลองอีกครั้งในภายหลังหรือติดต่อเจ้าหน้าที่", Toast.LENGTH_SHORT).show();
                                }
                            });
                });

                buttonEditTrackNumber.setOnClickListener(v1 -> {
                    if (editTrackNumber.getText().toString().isEmpty()) {
                        editTrackNumber.setError("กรุณากรอกหมายเลขติดตามพัสดุ");
                    } else {
                        String trackNumber = editTrackNumber.getText().toString().trim().toUpperCase();
                        db.collection("parcels")
                                .whereEqualTo("trackNumber", parcelsModel.getTrackNumber())
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                        String docId = documentSnapshot.getId();
                                        new AlertDialog.Builder(context)
                                                .setTitle(trackNumber)
                                                .setMessage("ท่านแน่ใจใช่หรือไม่ที่จะเปลี่ยนหมายเลขติดตามพัสดุ")
                                                .setPositiveButton("ยืนยัน", (dialog, which) -> {
                                                    progressDialog = new ProgressDialog(context);
                                                    progressDialog.setCanceledOnTouchOutside(false);
                                                    progressDialog.setMessage("กรุณารอสักครู่...");
                                                    progressDialog.show();
                                                    db.collection("parcels").document(docId)
                                                            .update("trackNumber", trackNumber)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Toast.makeText(context, "แก้ไขหมายเลขพัสดุเรียบร้อย", Toast.LENGTH_SHORT).show();
                                                                    editTrackNumberDialog.dismiss();
                                                                    progressDialog.dismiss();
                                                                }
                                                            });
                                                })
                                                .setNegativeButton("ยกเลิก", (dialog, which) -> {
                                                    dialog.dismiss();
                                                })
                                                .show();
                                    } else {
                                        Toast.makeText(context, "กรุณาลองอีกครั้งในภายหลังหรือติดต่อเจ้าหน้าที่", Toast.LENGTH_SHORT).show();
                                    }

                                });
                    }
                });
            });
            viewHolderOne.buttonCheckOutParcel.setOnClickListener(v -> {
                Date date = Calendar.getInstance().getTime();
                String checkOut = date.toString();
                String ownerName = parcelsModel.getOwner().getFirstname();
                String ownerSurname = parcelsModel.getOwner().getLastname();
                ArrayList<String> receiver = new ArrayList<>();
                ArrayList<String> checkReceiver = new ArrayList<>();
                Dialog dialogCheckOutParcel = new Dialog(context);
                dialogCheckOutParcel.setCanceledOnTouchOutside(false);
                dialogCheckOutParcel.setContentView(R.layout.check_out_parcel_admin);
                dialogCheckOutParcel.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                TextInputEditText receiverPassword = dialogCheckOutParcel.findViewById(R.id.receiver_password);
                TextInputEditText receiverEmail = dialogCheckOutParcel.findViewById(R.id.receiver_email);
                Button buttonConfirmPassword = dialogCheckOutParcel.findViewById(R.id.button_confirm_receiver_password);
                ImageView dialogCancel = dialogCheckOutParcel.findViewById(R.id.image_receiver_password_cancel);
                dialogCheckOutParcel.show();

                dialogCancel.setOnClickListener(v1 -> {
                    receiver.clear();
                    checkReceiver.clear();
                    dialogCheckOutParcel.dismiss();
                });

                buttonConfirmPassword.setOnClickListener(v1 -> {
                    if (receiverEmail.getText().toString().isEmpty() && receiverPassword.getText().toString().isEmpty()) {
                        receiverEmail.setError("กรุณากรอกอีเมลผู้รับพัสดุ");
                        receiverPassword.setError("กรุณากรอกรหัสผ่านผู้รับพัสดุ");
                    } else if (receiverEmail.getText().toString().isEmpty()) {
                        receiverEmail.setError("กรุณากรอกอีเมลผู้รับพัสดุ");
                    } else if (receiverPassword.getText().toString().isEmpty()) {
                        receiverPassword.setError("กรุณากรอกรหัสผ่านผู้รับพัสดุ");
                    } else {
                        String emailReceiver = receiverEmail.getText().toString();
                        String passwordReceiver = receiverPassword.getText().toString();
                        receiver.clear();
                        checkReceiver.clear();

                        db.collection("users")
                                .whereEqualTo("role", "admin")
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot getDocIdAdmin : task.getResult()) {
                                            db.collection("users").document(getDocIdAdmin.getId())
                                                    .addSnapshotListener((value, error) -> {
                                                        if (error != null) {
                                                            Log.d("Get admin", error.getMessage());
                                                        }

                                                        if (value.exists()) {
                                                            receiver.add(value.getString("firstname") + " " + value.getString("lastname") + " " + value.getString("email"));
                                                        } else {
                                                            Log.d("Get admin", error.getMessage());
                                                        }
                                                    });
                                        }
                                    }
                                });

                        db.collection("users")
                                .whereEqualTo("firstname", ownerName)
                                .whereEqualTo("lastname", ownerSurname)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null) {
                                        DocumentSnapshot getDocIdOwner = task.getResult().getDocuments().get(0);
                                        String docIdOwner = getDocIdOwner.getId();

                                        db.collection("users").document(docIdOwner)
                                                .get()
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        receiver.add(task1.getResult().getString("firstname") + " " + task1.getResult().getString("lastname") + " " + task1.getResult().getString("email"));
                                                        checkReceiver.add(task1.getResult().getString("firstname") + " " + task1.getResult().getString("lastname"));

                                                        if (task1.getResult().getString("agent.agent0.firstname") != "" && task1.getResult().getString("agent.agent0.lastname") != "" && task1.getResult().get("agent.agent0.status_receiver").equals(true)) {
                                                            receiver.add(task1.getResult().getString("agent.agent0.firstname") + " " + task1.getResult().getString("agent.agent0.lastname") + " " + task1.getResult().getString("agent.agent0.email"));
                                                            checkReceiver.add(task1.getResult().getString("agent.agent0.firstname") + " " + task1.getResult().getString("agent.agent0.lastname"));
                                                        }
                                                        if (task1.getResult().getString("agent.agent1.firstname") != "" && task1.getResult().getString("agent.agent1.lastname") != "" && task1.getResult().get("agent.agent1.status_receiver").equals(true)) {
                                                            receiver.add(task1.getResult().getString("agent.agent1.firstname") + " " + task1.getResult().getString("agent.agent1.lastname") + " " + task1.getResult().getString("agent.agent1.email"));
                                                            checkReceiver.add(task1.getResult().getString("agent.agent1.firstname") + " " + task1.getResult().getString("agent.agent1.lastname"));
                                                        }
                                                        if (task1.getResult().getString("agent.agent2.firstname") != "" && task1.getResult().getString("agent.agent2.lastname") != "" && task1.getResult().get("agent.agent2.status_receiver").equals(true)) {
                                                            receiver.add(task1.getResult().getString("agent.agent2.firstname") + " " + task1.getResult().getString("agent.agent2.lastname") + " " + task1.getResult().getString("agent.agent2.email"));
                                                            checkReceiver.add(task1.getResult().getString("agent.agent2.firstname") + " " + task1.getResult().getString("agent.agent2.lastname"));
                                                        }
                                                    }
                                                });
                                    }
                                });

                        db.collection("users")
                                .whereEqualTo("email", emailReceiver)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                        DocumentSnapshot getDocId = task.getResult().getDocuments().get(0);
                                        String docId = getDocId.getId();

                                        db.collection("users").document(docId)
                                                .get()
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        byte[] decodePassword = Base64.decode(task1.getResult().getString("password"), Base64.DEFAULT);
                                                        String passwordDecode = new String(decodePassword);

                                                        if (emailReceiver.equals(task1.getResult().getString("email")) && passwordReceiver.equals(passwordDecode)) {
                                                            if (checkReceiver.indexOf(task1.getResult().getString("firstname") + " " + task1.getResult().getString("lastname")) != -1) {
                                                                db.collection("users")
                                                                        .whereEqualTo("email", pref.getString("CurrentEmailUser","Nodata"))
                                                                        .get()
                                                                        .addOnCompleteListener(task2 -> {
                                                                            if (task2.isSuccessful()) {
                                                                                DocumentSnapshot getDocIdUser = task2.getResult().getDocuments().get(0);
                                                                                String docIdUser = getDocIdUser.getId();

                                                                                db.collection("users").document(docIdUser)
                                                                                        .get()
                                                                                        .addOnCompleteListener(task3 -> {
                                                                                            if (task3.isSuccessful()) {
                                                                                                db.collection("parcels")
                                                                                                        .whereEqualTo("trackNumber", parcelsModel.getTrackNumber())
                                                                                                        .get()
                                                                                                        .addOnCompleteListener(task4 -> {
                                                                                                            if (task4.isSuccessful() && task4.getResult() != null) {
                                                                                                                DocumentSnapshot getDocIdParcel = task4.getResult().getDocuments().get(0);
                                                                                                                String docIdParcel = getDocIdParcel.getId();

                                                                                                                db.collection("parcels").document(docIdParcel)
                                                                                                                        .update("checkOut", checkOut, "receiver.firstname", task1.getResult().getString("firstname"), "receiver.lastname", task1.getResult().getString("lastname"))
                                                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onSuccess(Void unused) {
                                                                                                                                progressDialog = new ProgressDialog(context);
                                                                                                                                progressDialog.setCanceledOnTouchOutside(false);
                                                                                                                                progressDialog.setMessage("กรุณารอสักครู่...");
                                                                                                                                progressDialog.show();
                                                                                                                                for (String sendMessages : receiver) {
                                                                                                                                    String[] receiverName = sendMessages.split(" ");
                                                                                                                                    Map<String, Object> messages = new HashMap<>();
                                                                                                                                    messages.put("data", null);
                                                                                                                                    messages.put("dateTime", checkOut);
                                                                                                                                    messages.put("read", false);
                                                                                                                                    messages.put("receiver", null);
                                                                                                                                    messages.put("sender", null);
                                                                                                                                    messages.put("type", 0);

                                                                                                                                    Map<String, Object> data = new HashMap<>();
                                                                                                                                    data.put("owner", null);
                                                                                                                                    data.put("receiver_parcel", null);
                                                                                                                                    data.put("trackNumber", parcelsModel.getTrackNumber());
                                                                                                                                    messages.put("data", data);

                                                                                                                                    Map<String, Object> owner = new HashMap<>();
                                                                                                                                    owner.put("firstname", ownerName);
                                                                                                                                    owner.put("lastname", ownerSurname);
                                                                                                                                    data.put("owner", owner);

                                                                                                                                    Map<String, Object> receiver_parcels = new HashMap<>();
                                                                                                                                    receiver_parcels.put("firstname", task1.getResult().getString("firstname"));
                                                                                                                                    receiver_parcels.put("lastname", task1.getResult().getString("lastname"));
                                                                                                                                    data.put("receiver_parcel", receiver_parcels);

                                                                                                                                    Map<String, Object> receiverMap = new HashMap<>();
                                                                                                                                    receiverMap.put("firstname", receiverName[0]);
                                                                                                                                    receiverMap.put("lastname", receiverName[1]);
                                                                                                                                    receiverMap.put("email", receiverName[2]);
                                                                                                                                    messages.put("receiver", receiverMap);

                                                                                                                                    Map<String, Object> sender = new HashMap<>();
                                                                                                                                    sender.put("firstname", task3.getResult().getString("firstname"));
                                                                                                                                    sender.put("lastname", task3.getResult().getString("lastname"));
                                                                                                                                    messages.put("sender", sender);

                                                                                                                                    db.collection("messages").document()
                                                                                                                                            .set(messages, SetOptions.merge())
                                                                                                                                            .addOnSuccessListener(unused1 -> Log.d("Check out send message", "สำเร็จ"))
                                                                                                                                            .addOnFailureListener(e -> Log.d("Check out send message", e.getMessage()));
                                                                                                                                }
                                                                                                                                parcelsModelList.remove(position);
                                                                                                                                notifyItemRemoved(position);
                                                                                                                                notifyItemRangeChanged(position, parcelsModelList.size());
                                                                                                                                dialogCheckOutParcel.dismiss();
                                                                                                                                progressDialog.dismiss();
                                                                                                                            }
                                                                                                                        });
                                                                                                            } else {
                                                                                                                Toast.makeText(context, "ขออภัยไม่พบข้อมูลพัสดุที่ท่านต้องการ", Toast.LENGTH_LONG).show();
                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        });
                                                                            }
                                                                        });
                                                            } else {
                                                                receiverPassword.setError("ไม่พบข้อมูลผู้ใช้งานที่ถูกต้อง");
                                                            }
                                                        } else {
                                                            receiverPassword.setError("รหัสผ่านไม่ถูกต้อง");
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(context, "ไม่พบข้อมูลใช้งาน", Toast.LENGTH_LONG).show();
                                        receiverEmail.setError("กรุณาตรวจสอบอีเมล");
                                        receiverPassword.setError("กรุณาตรวจสอบรหัสผ่าน");
                                    }
                                });
                    }
                });

            });
        } else if (parcelsModelList.get(position).getListView().equals(true)) {
            ParcelsModel parcelsModel = parcelsModelList.get(position);
            ViewHolderTwo viewHolderTwo = (ViewHolderTwo) holder;
            HashSet<String> passwordVerify = new HashSet<>();
            ArrayList<String> agentName = new ArrayList<>();
            viewHolderTwo.trackNumber.setText(parcelsModel.getTrackNumber());
            viewHolderTwo.trackNumberInList.setText(parcelsModel.getTrackNumber());
            viewHolderTwo.constraintLayout.setVisibility(parcelsModel.getDetailInListView() ? View.VISIBLE : View.GONE);
            viewHolderTwo.ownerFname.setText(parcelsModel.getOwner().getFirstname());
            viewHolderTwo.ownerLname.setText(parcelsModel.getOwner().getLastname());
            String getCheckIn = parcelsModel.getCheckIn();
            if (getCheckIn != null) {
                String[] splitDate = getCheckIn.split(" ");
                if (splitDate.length == 6) {
                    String date = splitDate[2];
                    String month = null;
                    String year = String.valueOf(Integer.parseInt(splitDate[5]) + 543);
                    String getMonth = splitDate[1];
                    switch (getMonth) {
                        case "Jan":
                            month = "มกราคม";
                            break;
                        case "Feb":
                            month = "กุมภาพันธ์";
                            break;
                        case "Mar":
                            month = "มีนาคม";
                            break;
                        case "Apr":
                            month = "เมษายน";
                            break;
                        case "May":
                            month = "พฤษภาคม";
                            break;
                        case "Jun":
                            month = "มิถุนายน";
                            break;
                        case "Jul":
                            month = "กรกฎาคม";
                            break;
                        case "Aug":
                            month = "สิงหาคม";
                            break;
                        case "Sep":
                            month = "กันยายน";
                            break;
                        case "Oct":
                            month = "ตุลาคม";
                            break;
                        case "Nov":
                            month = "พฤศจิกายน";
                            break;
                        case "Dec":
                            month = "ธันวาคม";
                            break;
                    }
                    String checkIn = String.format("%s %s %s", date, month, year);
                    viewHolderTwo.checkIn.setText(checkIn);
                    String[] splitTime = splitDate[3].split(":");
                    String checkInTime = String.format("%s:%s น.", splitTime[0], splitTime[1]);
                    viewHolderTwo.checkInTime.setText(checkInTime);
                } else if (splitDate.length == 7) {
                    String date = splitDate[2];
                    String month = null;
                    String year = String.valueOf(Integer.parseInt(splitDate[3]) + 543);
                    String getMonth = splitDate[1];
                    switch (getMonth) {
                        case "Jan":
                            month = "มกราคม";
                            break;
                        case "Feb":
                            month = "กุมภาพันธ์";
                            break;
                        case "Mar":
                            month = "มีนาคม";
                            break;
                        case "Apr":
                            month = "เมษายน";
                            break;
                        case "May":
                            month = "พฤษภาคม";
                            break;
                        case "Jun":
                            month = "มิถุนายน";
                            break;
                        case "Jul":
                            month = "กรกฎาคม";
                            break;
                        case "Aug":
                            month = "สิงหาคม";
                            break;
                        case "Sep":
                            month = "กันยายน";
                            break;
                        case "Oct":
                            month = "ตุลาคม";
                            break;
                        case "Nov":
                            month = "พฤศจิกายน";
                            break;
                        case "Dec":
                            month = "ธันวาคม";
                            break;
                    }
                    String checkIn = String.format("%s %s %s", date, month, year);
                    viewHolderTwo.checkIn.setText(checkIn);
                    String[] splitTime = splitDate[4].split(":");
                    String checkInTime = String.format("%s:%s น.", splitTime[0], splitTime[1]);
                    viewHolderTwo.checkInTime.setText(checkInTime);
                }
            } else {
                viewHolderTwo.checkIn.setText("-");
                viewHolderTwo.checkInTime.setText("-");
            }
            db.collection("parcels")
                    .whereEqualTo("trackNumber", parcelsModel.getTrackNumber())
                    .whereNotEqualTo("images.image0", "")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            String docId = documentSnapshot.getId();

                            db.collection("parcels").document(docId)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Map<String, Object> getData = task1.getResult().getData();
                                            for (Map.Entry<String, Object> dataResult : getData.entrySet()) {
                                                if (dataResult.getKey().equals("images")) {
                                                    Map<String, Object> getImage = (Map<String, Object>) dataResult.getValue();
                                                    for (Map.Entry<String, Object> imageResult : getImage.entrySet()) {
                                                        if (imageResult.getKey().equals("image0") && imageResult.getValue() != "") {
                                                            StorageReference storageReference;
                                                            storageReference = storage.getReference().child(imageResult.getValue().toString());
                                                            try {
                                                                File file = File.createTempFile("Parcels", "jpg");
                                                                storageReference.getFile(file)
                                                                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                                            @Override
                                                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                                                                viewHolderTwo.imageParcel.setImageBitmap(bitmap);
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
                                                            viewHolderTwo.imageParcel.setImageResource(R.drawable.parcel);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                        }
                    });
            viewHolderTwo.imageParcel.setOnClickListener(v -> {
                imageResultArraylist.clear();
                bitmapArrayList.clear();
                String trackNumber = parcelsModel.getTrackNumber();
                Dialog imageParcelDialog = new Dialog(context);
                imageParcelDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                imageParcelDialog.setContentView(R.layout.image_parcels_slider);
                imageParcelDialog.show();
                SliderView imageSlider = imageParcelDialog.findViewById(R.id.image_parcel_slider);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                progressDialog = new ProgressDialog(context);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("กรุณารอสักครู่...");
                progressDialog.show();
                db.collection("parcels")
                        .whereEqualTo("trackNumber", trackNumber)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                String docId = documentSnapshot.getId();
                                db.collection("parcels").document(docId)
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Map<String, Object> getData = task1.getResult().getData();
                                                for (Map.Entry<String, Object> dataResult : getData.entrySet()) {
                                                    if (dataResult.getKey().equals("images")) {
                                                        Map<String, Object> getImage = (Map<String, Object>) dataResult.getValue();
                                                        for (Map.Entry<String, Object> imageResult : getImage.entrySet()) {
                                                            if (imageResult.getKey().equals("image0") && imageResult.getValue() != "") {
                                                                imageResultArraylist.add(imageResult.getValue().toString());
                                                            }
                                                            if (imageResult.getKey().equals("image1") && imageResult.getValue() != "") {
                                                                imageResultArraylist.add(imageResult.getValue().toString());
                                                            }
                                                            if (imageResult.getKey().equals("image2") && imageResult.getValue() != "") {
                                                                imageResultArraylist.add(imageResult.getValue().toString());
                                                            }
                                                            if (imageResult.getKey().equals("image3") && imageResult.getValue() != "") {
                                                                imageResultArraylist.add(imageResult.getValue().toString());
                                                            }
                                                        }
                                                    }
                                                }
                                                if (!imageResultArraylist.isEmpty()) {
                                                    for (String result : imageResultArraylist) {
                                                        StorageReference storageReference;
                                                        storageReference = storage.getReference().child(result);

                                                        try {
                                                            File file = File.createTempFile("Parcels", "jpg");
                                                            storageReference.getFile(file)
                                                                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                                                            bitmapArrayList.add(bitmap);
                                                                            parcelsSliderAdapter = new ImageParcelsSliderAdapter(bitmapArrayList);
                                                                            imageSlider.setSliderAdapter(parcelsSliderAdapter);
                                                                            imageSlider.setIndicatorAnimation(IndicatorAnimationType.WORM);
                                                                            imageSlider.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION);
                                                                            progressDialog.dismiss();
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.d("get image in card home page", e.getMessage());
                                                                            if (progressDialog.isShowing()) {
                                                                                progressDialog.dismiss();
                                                                                imageParcelDialog.dismiss();
                                                                            }
                                                                        }
                                                                    });
                                                        } catch (IOException e) {
                                                            Log.d("get image in card home page", e.getMessage());
                                                            if (progressDialog.isShowing()) {
                                                                progressDialog.dismiss();
                                                                imageParcelDialog.dismiss();
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    if (progressDialog.isShowing()) {
                                                        progressDialog.dismiss();
                                                        imageParcelDialog.dismiss();
                                                    }
                                                    Toast.makeText(context, "พัสดุนี้ไม่มีรูปภาพที่บันทึกไว้", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        });
            });
            viewHolderTwo.buttonEditParcel.setOnClickListener(v -> {
                Dialog editTrackNumberDialog = new Dialog(context);
                editTrackNumberDialog.setCanceledOnTouchOutside(false);
                editTrackNumberDialog.setContentView(R.layout.edit_parcel_dialog);
                editTrackNumberDialog.show();
                ImageView imageCloseEditParcelDialog = editTrackNumberDialog.findViewById(R.id.close_edit_parcel_dialog);
                Button buttonDeleteParcel = editTrackNumberDialog.findViewById(R.id.button_delete_parcels);
                Button buttonEditTrackNumber = editTrackNumberDialog.findViewById(R.id.button_confirm_edit_tracknumber);
                EditText editTrackNumber = editTrackNumberDialog.findViewById(R.id.edit_tracknumber);
                TextView oldTrackNumber = editTrackNumberDialog.findViewById(R.id.old_trackNumber);
                oldTrackNumber.setText(parcelsModel.getTrackNumber());

                imageCloseEditParcelDialog.setOnClickListener(v1 -> {
                    editTrackNumberDialog.dismiss();
                });

                buttonDeleteParcel.setOnClickListener(v1 -> {
                    db.collection("parcels")
                            .whereEqualTo("trackNumber", parcelsModel.getTrackNumber())
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("RRR", "มา");
                                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                    String docId = documentSnapshot.getId();
                                    new AlertDialog.Builder(context)
                                            .setTitle(parcelsModel.getTrackNumber())
                                            .setMessage("ท่านแน่ใจแล้วใช่หรือไม่ที่จะลบรายการพัสดุนี้")
                                            .setPositiveButton("ยืนยัน", (dialog, which) -> {
                                                progressDialog = new ProgressDialog(context);
                                                progressDialog.setCanceledOnTouchOutside(false);
                                                progressDialog.setMessage("กรุณารอสักครู่...");
                                                progressDialog.show();
                                                db.collection("parcels").document(docId)
                                                        .delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Toast.makeText(context, "ลบรายการพัสดุเรียบร้อย", Toast.LENGTH_SHORT).show();
                                                                editTrackNumberDialog.dismiss();
                                                                progressDialog.dismiss();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(context, "กรุณาลองใหม่ในภายหลัง", Toast.LENGTH_SHORT).show();
                                                                Log.d("Error delete parcel", e.getMessage());
                                                            }
                                                        });
                                                parcelsModelList.remove(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position, parcelsModelList.size());
                                            })
                                            .setNegativeButton("ยกเลิก", (dialog, which) -> {
                                                dialog.dismiss();
                                            })
                                            .show();
                                } else {
                                    Toast.makeText(context, "กรุณาลองอีกครั้งในภายหลังหรือติดต่อเจ้าหน้าที่", Toast.LENGTH_SHORT).show();
                                }
                            });
                });

                buttonEditTrackNumber.setOnClickListener(v1 -> {
                    if (editTrackNumber.getText().toString().isEmpty()) {
                        editTrackNumber.setError("กรุณากรอกหมายเลขติดตามพัสดุ");
                    } else {
                        String trackNumber = editTrackNumber.getText().toString().trim().toUpperCase();
                        db.collection("parcels")
                                .whereEqualTo("trackNumber", parcelsModel.getTrackNumber())
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                        String docId = documentSnapshot.getId();
                                        new AlertDialog.Builder(context)
                                                .setTitle(trackNumber)
                                                .setMessage("ท่านแน่ใจใช่หรือไม่ที่จะเปลี่ยนหมายเลขติดตามพัสดุ")
                                                .setPositiveButton("ยืนยัน", (dialog, which) -> {
                                                    progressDialog = new ProgressDialog(context);
                                                    progressDialog.setCanceledOnTouchOutside(false);
                                                    progressDialog.setMessage("กรุณารอสักครู่...");
                                                    progressDialog.show();
                                                    db.collection("parcels").document(docId)
                                                            .update("trackNumber", trackNumber)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Toast.makeText(context, "แก้ไขหมายเลขพัสดุเรียบร้อย", Toast.LENGTH_SHORT).show();
                                                                    editTrackNumberDialog.dismiss();
                                                                    progressDialog.dismiss();
                                                                }
                                                            });
                                                })
                                                .setNegativeButton("ยกเลิก", (dialog, which) -> {
                                                    dialog.dismiss();
                                                })
                                                .show();
                                    } else {
                                        Toast.makeText(context, "กรุณาลองอีกครั้งในภายหลังหรือติดต่อเจ้าหน้าที่", Toast.LENGTH_SHORT).show();
                                    }

                                });
                    }
                });
            });
            viewHolderTwo.buttonCheckOutParcel.setOnClickListener(v -> {
                Date date = Calendar.getInstance().getTime();
                String checkOut = date.toString();
                String ownerName = parcelsModel.getOwner().getFirstname();
                String ownerSurname = parcelsModel.getOwner().getLastname();
                ArrayList<String> receiver = new ArrayList<>();
                ArrayList<String> checkReceiver = new ArrayList<>();
                Dialog dialogCheckOutParcel = new Dialog(context);
                dialogCheckOutParcel.setCanceledOnTouchOutside(false);
                dialogCheckOutParcel.setContentView(R.layout.check_out_parcel_admin);
                dialogCheckOutParcel.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                TextInputEditText receiverEmail = dialogCheckOutParcel.findViewById(R.id.receiver_email);
                TextInputEditText receiverPassword = dialogCheckOutParcel.findViewById(R.id.receiver_password);
                Button buttonConfirmPassword = dialogCheckOutParcel.findViewById(R.id.button_confirm_receiver_password);
                ImageView dialogCancel = dialogCheckOutParcel.findViewById(R.id.image_receiver_password_cancel);
                dialogCheckOutParcel.show();

                dialogCancel.setOnClickListener(v1 -> {
                    receiver.clear();
                    checkReceiver.clear();
                    dialogCheckOutParcel.dismiss();
                });

                buttonConfirmPassword.setOnClickListener(v1 -> {
                    if (receiverEmail.getText().toString().isEmpty() && receiverPassword.getText().toString().isEmpty()) {
                        receiverEmail.setError("กรุณากรอกอีเมลผู้รับพัสดุ");
                        receiverPassword.setError("กรุณากรอกรหัสผ่านผู้รับพัสดุ");
                    } else if (receiverEmail.getText().toString().isEmpty()) {
                        receiverEmail.setError("กรุณากรอกอีเมลผู้รับพัสดุ");
                    } else if (receiverPassword.getText().toString().isEmpty()) {
                        receiverPassword.setError("กรุณากรอกรหัสผ่านผู้รับพัสดุ");
                    } else {
                        String emailReceiver = receiverEmail.getText().toString();
                        String passwordReceiver = receiverPassword.getText().toString();
                        receiver.clear();
                        checkReceiver.clear();

                        db.collection("users")
                                .whereEqualTo("role", "admin")
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot getDocIdAdmin : task.getResult()) {
                                            db.collection("users").document(getDocIdAdmin.getId())
                                                    .addSnapshotListener((value, error) -> {
                                                        if (error != null) {
                                                            Log.d("Get admin", error.getMessage());
                                                        }
                                                        if (value.exists()) {
                                                            receiver.add(value.getString("firstname") + " " + value.getString("lastname") + " " + value.getString("email"));
                                                        } else {
                                                            Log.d("Get admin", error.getMessage());
                                                        }
                                                    });
                                        }
                                    }
                                });

                        db.collection("users")
                                .whereEqualTo("firstname", ownerName)
                                .whereEqualTo("lastname", ownerSurname)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null) {
                                        DocumentSnapshot getDocIdOwner = task.getResult().getDocuments().get(0);
                                        String docIdOwner = getDocIdOwner.getId();

                                        db.collection("users").document(docIdOwner)
                                                .get()
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        receiver.add(task1.getResult().getString("firstname") + " " + task1.getResult().getString("lastname") + " " + task1.getResult().getString("email"));
                                                        checkReceiver.add(task1.getResult().getString("firstname") + " " + task1.getResult().getString("lastname"));

                                                        if (task1.getResult().getString("agent.agent0.firstname") != "" && task1.getResult().getString("agent.agent0.lastname") != "" && task1.getResult().get("agent.agent0.status_receiver").equals(true)) {
                                                            receiver.add(task1.getResult().getString("agent.agent0.firstname") + " " + task1.getResult().getString("agent.agent0.lastname") + " " + task1.getResult().getString("agent.agent0.email"));
                                                            checkReceiver.add(task1.getResult().getString("agent.agent0.firstname") + " " + task1.getResult().getString("agent.agent0.lastname"));
                                                        }
                                                        if (task1.getResult().getString("agent.agent1.firstname") != "" && task1.getResult().getString("agent.agent1.lastname") != "" && task1.getResult().get("agent.agent1.status_receiver").equals(true)) {
                                                            receiver.add(task1.getResult().getString("agent.agent1.firstname") + " " + task1.getResult().getString("agent.agent1.lastname") + " " + task1.getResult().getString("agent.agent1.email"));
                                                            checkReceiver.add(task1.getResult().getString("agent.agent1.firstname") + " " + task1.getResult().getString("agent.agent1.lastname"));
                                                        }
                                                        if (task1.getResult().getString("agent.agent2.firstname") != "" && task1.getResult().getString("agent.agent2.lastname") != "" && task1.getResult().get("agent.agent2.status_receiver").equals(true)) {
                                                            receiver.add(task1.getResult().getString("agent.agent2.firstname") + " " + task1.getResult().getString("agent.agent2.lastname") + " " + task1.getResult().getString("agent.agent2.email"));
                                                            checkReceiver.add(task1.getResult().getString("agent.agent2.firstname") + " " + task1.getResult().getString("agent.agent2.lastname"));
                                                        }
                                                    }
                                                });
                                    }
                                });

                        db.collection("users")
                                .whereEqualTo("email", emailReceiver)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                        DocumentSnapshot getDocId = task.getResult().getDocuments().get(0);
                                        String docId = getDocId.getId();

                                        db.collection("users").document(docId)
                                                .get()
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        byte[] decodePassword = Base64.decode(task1.getResult().getString("password"), Base64.DEFAULT);
                                                        String passwordDecode = new String(decodePassword);

                                                        if (emailReceiver.equals(task1.getResult().getString("email")) && passwordReceiver.equals(passwordDecode)) {
                                                            if (checkReceiver.indexOf(task1.getResult().getString("firstname") + " " + task1.getResult().getString("lastname")) != -1) {
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
                                                                                                db.collection("parcels")
                                                                                                        .whereEqualTo("trackNumber", parcelsModel.getTrackNumber())
                                                                                                        .get()
                                                                                                        .addOnCompleteListener(task4 -> {
                                                                                                            if (task4.isSuccessful() && task4.getResult() != null) {
                                                                                                                DocumentSnapshot getDocIdParcel = task4.getResult().getDocuments().get(0);
                                                                                                                String docIdParcel = getDocIdParcel.getId();

                                                                                                                db.collection("parcels").document(docIdParcel)
                                                                                                                        .update("checkOut", checkOut, "receiver.firstname", task1.getResult().getString("firstname"), "receiver.lastname", task1.getResult().getString("lastname"))
                                                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onSuccess(Void unused) {
                                                                                                                                progressDialog = new ProgressDialog(context);
                                                                                                                                progressDialog.setCanceledOnTouchOutside(false);
                                                                                                                                progressDialog.setMessage("กรุณารอสักครู่...");
                                                                                                                                progressDialog.show();
                                                                                                                                for (String sendMessages : receiver) {
                                                                                                                                    String[] receiverName = sendMessages.split(" ");
                                                                                                                                    Map<String, Object> messages = new HashMap<>();
                                                                                                                                    messages.put("data", null);
                                                                                                                                    messages.put("dateTime", checkOut);
                                                                                                                                    messages.put("read", false);
                                                                                                                                    messages.put("receiver", null);
                                                                                                                                    messages.put("sender", null);
                                                                                                                                    messages.put("type", 0);

                                                                                                                                    Map<String, Object> data = new HashMap<>();
                                                                                                                                    data.put("owner", null);
                                                                                                                                    data.put("receiver_parcel", null);
                                                                                                                                    data.put("trackNumber", parcelsModel.getTrackNumber());
                                                                                                                                    messages.put("data", data);

                                                                                                                                    Map<String, Object> owner = new HashMap<>();
                                                                                                                                    owner.put("firstname", ownerName);
                                                                                                                                    owner.put("lastname", ownerSurname);
                                                                                                                                    data.put("owner", owner);

                                                                                                                                    Map<String, Object> receiver_parcels = new HashMap<>();
                                                                                                                                    receiver_parcels.put("firstname", task1.getResult().getString("firstname"));
                                                                                                                                    receiver_parcels.put("lastname", task1.getResult().getString("lastname"));
                                                                                                                                    data.put("receiver_parcel", receiver_parcels);

                                                                                                                                    Map<String, Object> receiverMap = new HashMap<>();
                                                                                                                                    receiverMap.put("firstname", receiverName[0]);
                                                                                                                                    receiverMap.put("lastname", receiverName[1]);
                                                                                                                                    receiverMap.put("email", receiverName[2]);
                                                                                                                                    messages.put("receiver", receiverMap);

                                                                                                                                    Map<String, Object> sender = new HashMap<>();
                                                                                                                                    sender.put("firstname", task3.getResult().getString("firstname"));
                                                                                                                                    sender.put("lastname", task3.getResult().getString("lastname"));
                                                                                                                                    messages.put("sender", sender);

                                                                                                                                    db.collection("messages").document()
                                                                                                                                            .set(messages, SetOptions.merge())
                                                                                                                                            .addOnSuccessListener(unused1 -> Log.d("Check out send message", "สำเร็จ"))
                                                                                                                                            .addOnFailureListener(e -> Log.d("Check out send message", e.getMessage()));
                                                                                                                                }
                                                                                                                                parcelsModelList.remove(position);
                                                                                                                                notifyItemRemoved(position);
                                                                                                                                notifyItemRangeChanged(position, parcelsModelList.size());
                                                                                                                                dialogCheckOutParcel.dismiss();
                                                                                                                                progressDialog.dismiss();
                                                                                                                            }
                                                                                                                        });
                                                                                                            } else {
                                                                                                                Toast.makeText(context, "ขออภัยไม่พบข้อมูลพัสดุที่ท่านต้องการ", Toast.LENGTH_LONG).show();
                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        });
                                                                            }
                                                                        });
                                                            } else {
                                                                Toast.makeText(context, "ไม่พบข้อมูลผู้ใช้งานที่ถูกต้อง", Toast.LENGTH_LONG).show();
                                                            }
                                                        } else {
                                                            receiverPassword.setError("รหัสผ่านไม่ถูกต้อง");
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(context, "ไม่พบข้อมูลใช้งาน", Toast.LENGTH_LONG).show();
                                        receiverEmail.setError("กรุณาตรวจสอบอีเมล");
                                        receiverPassword.setError("กรุณาตรวจสอบรหัสผ่าน");
                                    }
                                });
                    }
                });

            });
        }
    }

    @Override
    public int getItemCount() {
        return parcelsModelList.size();
    }


}
