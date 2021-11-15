package com.example.tnscpe;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class HistoryAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<ParcelsModel> parcelsModelList;
    ArrayList<String> imageResultArraylist = new ArrayList<>();
    ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();
    ImageParcelsSliderAdapter parcelsSliderAdapter;
    ProgressDialog progressDialog;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();

    public HistoryAdapter(Context context, ArrayList<ParcelsModel> parcelsModelList) {
        this.context = context;
        this.parcelsModelList = parcelsModelList;
    }

    public class ViewHolderOne extends RecyclerView.ViewHolder {

        TextView checkIn, checkInTime, checkOut, checkOutTime, trackNumber, ownerFname, ownerLname, receiverFname, receiverLname;
        ImageView imageParcel;

        public ViewHolderOne(@NonNull View itemView) {
            super(itemView);
            checkIn = itemView.findViewById(R.id.check_in);
            checkInTime = itemView.findViewById(R.id.check_in_time);
            checkOut = itemView.findViewById(R.id.check_out);
            checkOutTime = itemView.findViewById(R.id.check_out_time);
            trackNumber = itemView.findViewById(R.id.tracknumber);
            ownerFname = itemView.findViewById(R.id.owner_name);
            ownerLname = itemView.findViewById(R.id.owner_surname);
            receiverFname = itemView.findViewById(R.id.receiver_name);
            receiverLname = itemView.findViewById(R.id.receiver_surname);
            imageParcel = itemView.findViewById(R.id.image_parcel_history);
        }
    }

    public class ViewHolderTwo extends RecyclerView.ViewHolder {

        TextView checkIn, checkInTime, checkOut,  checkOutTime, trackNumber, trackNumberInList, ownerFname, ownerLname, receiverFname, receiverLname;
        ImageView imageParcel, dropList;
        ConstraintLayout constraintLayout;

        public ViewHolderTwo(@NonNull View itemView) {
            super(itemView);
            checkIn = itemView.findViewById(R.id.check_in);
            checkInTime = itemView.findViewById(R.id.check_in_time);
            checkOut = itemView.findViewById(R.id.check_out);
            checkOutTime = itemView.findViewById(R.id.check_out_time);
            trackNumber = itemView.findViewById(R.id.tracknumber);
            trackNumberInList = itemView.findViewById(R.id.tracknumber_2);
            ownerFname = itemView.findViewById(R.id.owner_name);
            ownerLname = itemView.findViewById(R.id.owner_surname);
            receiverFname = itemView.findViewById(R.id.receiver_name);
            receiverLname = itemView.findViewById(R.id.receiver_surname);
            imageParcel = itemView.findViewById(R.id.image_parcel_history);
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
            view = LayoutInflater.from(context).inflate(R.layout.listview_parcels_history, parent, false);
            return new ViewHolderTwo(view);
        }
        view = LayoutInflater.from(context).inflate(R.layout.cardview_parcels_history, parent, false);
        return new ViewHolderOne(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        for (int i = 0; i < parcelsModelList.size(); i++) {
            if (parcelsModelList.get(position).getListView().equals(false)) {
                ParcelsModel parcelsModel = parcelsModelList.get(position);
                ViewHolderOne viewHolderOne = (ViewHolderOne) holder;
                String getCheckIn = parcelsModel.getCheckIn();
                String getCheckOut = parcelsModel.getCheckOut();
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
                }
                if (getCheckOut != null) {
                    String[] splitDate = getCheckOut.split(" ");
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
                        String checkOut = String.format("%s %s %s", date, month, year);
                        viewHolderOne.checkOut.setText(checkOut);
                        String[] splitTime = splitDate[3].split(":");
                        String checkOutTime = String.format("%s:%s น.", splitTime[0], splitTime[1]);
                        viewHolderOne.checkOutTime.setText(checkOutTime);
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
                        String checkOut = String.format("%s %s %s", date, month, year);
                        viewHolderOne.checkOut.setText(checkOut);
                        String[] splitTime = splitDate[4].split(":");
                        String checkOutTime = String.format("%s:%s น.", splitTime[0], splitTime[1]);
                        viewHolderOne.checkOutTime.setText(checkOutTime);
                    }
                }
                viewHolderOne.trackNumber.setText(parcelsModel.getTrackNumber());
                viewHolderOne.ownerFname.setText(parcelsModel.getOwner().lastname);
                viewHolderOne.ownerLname.setText(parcelsModel.getOwner().firstname);
                viewHolderOne.receiverFname.setText(parcelsModel.getReceiver().firstname);
                viewHolderOne.receiverLname.setText(parcelsModel.getReceiver().lastname);

                db.collection("parcels")
                        .whereEqualTo("trackNumber", parcelsModel.getTrackNumber())
                        .whereNotEqualTo("images.image0","")
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
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setMessage("กรุณารอสักครู่...");
                    progressDialog.show();
                    db.collection("parcels")
                            .whereEqualTo("trackNumber", trackNumber)
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
                                                } else {
                                                    if (progressDialog.isShowing()) {
                                                        progressDialog.dismiss();
                                                        imageParcelDialog.dismiss();
                                                    }
                                                    Toast.makeText(context, "พัสดุนี้ไม่มีรูปภาพที่บันทึกไว้", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    if (progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                        imageParcelDialog.dismiss();
                                    }
                                    Toast.makeText(context, "กรุณาลองใหม่ในภายหลัง", Toast.LENGTH_SHORT).show();
                                }
                            });
                });
            } else if (parcelsModelList.get(position).getListView().equals(true)) {
                ParcelsModel parcelsModel = parcelsModelList.get(position);
                ViewHolderTwo viewHolderTwo = (ViewHolderTwo) holder;
                String getCheckIn = parcelsModel.getCheckIn();
                String getCheckOut = parcelsModel.getCheckOut();
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
                }
                if (getCheckOut != null) {
                    String[] splitDate = getCheckOut.split(" ");
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
                        String checkOut = String.format("%s %s %s", date, month, year);
                        viewHolderTwo.checkOut.setText(checkOut);
                        String[] splitTime = splitDate[3].split(":");
                        String checkOutTime = String.format("%s:%s น.", splitTime[0], splitTime[1]);
                        viewHolderTwo.checkOutTime.setText(checkOutTime);
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
                        String checkOut = String.format("%s %s %s", date, month, year);
                        viewHolderTwo.checkOut.setText(checkOut);
                        String[] splitTime = splitDate[4].split(":");
                        String checkOutTime = String.format("%s:%s น.", splitTime[0], splitTime[1]);
                        viewHolderTwo.checkOutTime.setText(checkOutTime);
                    }
                }
                viewHolderTwo.trackNumber.setText(parcelsModel.getTrackNumber());
                viewHolderTwo.trackNumberInList.setText(parcelsModel.getTrackNumber());
                viewHolderTwo.constraintLayout.setVisibility(parcelsModel.getDetailInListView() ? View.VISIBLE : View.GONE);
                viewHolderTwo.ownerFname.setText(parcelsModel.getOwner().firstname);
                viewHolderTwo.ownerLname.setText(parcelsModel.getOwner().lastname);
                viewHolderTwo.receiverFname.setText(parcelsModel.getReceiver().firstname);
                viewHolderTwo.receiverLname.setText(parcelsModel.getReceiver().lastname);
                db.collection("parcels")
                        .whereEqualTo("trackNumber", parcelsModel.getTrackNumber())
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
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    progressDialog = new ProgressDialog(context);
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
                                                } else {
                                                    if (progressDialog.isShowing()) {
                                                        progressDialog.dismiss();
                                                        imageParcelDialog.dismiss();
                                                    }
                                                    Toast.makeText(context, "พัสดุนี้ไม่มีรูปภาพที่บันทึกไว้", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    if (progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                        imageParcelDialog.dismiss();
                                    }
                                    Toast.makeText(context, "กรุณาลองใหม่ในภายหลัง", Toast.LENGTH_SHORT).show();
                                }
                            });
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return parcelsModelList.size();
    }
}