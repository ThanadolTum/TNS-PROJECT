package com.example.tnscpe;

import android.app.AlertDialog;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class ParcelsAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<ParcelsModel> parcelsModelList;
    ArrayList<ParcelsModel> parcelsModelsFilter = new ArrayList<>();
    ArrayList<String> imageResultArraylist = new ArrayList<>();
    ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();
    ImageParcelsSliderAdapter parcelsSliderAdapter;
    ProgressDialog progressDialog;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();

    public ParcelsAdapter(Context context, ArrayList<ParcelsModel> parcelsModelsList) {
        this.context = context;
        this.parcelsModelList = parcelsModelsList;
    }

    public class ViewHolderOne extends RecyclerView.ViewHolder {

        TextView trackNumber;
        TextView ownerFname;
        TextView ownerLname;
        TextView checkIn;
        TextView checkInTime;
        Button buttonEditParcel;
        ImageView imageParcel;

        public ViewHolderOne(@NonNull View itemView) {
            super(itemView);
            checkIn = itemView.findViewById(R.id.check_in);
            checkInTime = itemView.findViewById(R.id.check_in_time);
            trackNumber = itemView.findViewById(R.id.tracknumber);
            ownerLname = itemView.findViewById(R.id.owner_name);
            ownerFname = itemView.findViewById(R.id.owner_surname);
            buttonEditParcel = itemView.findViewById(R.id.button_edit_parcel);
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
            view = LayoutInflater.from(context).inflate(R.layout.listview_parcels_home_page, parent, false);
            return new ViewHolderTwo(view);
        }
        view = LayoutInflater.from(context).inflate(R.layout.cardview_parcels_home_page, parent, false);
        return new ViewHolderOne(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        parcelsModelsFilter.addAll(position,parcelsModelList);
        if (parcelsModelList.get(position).getListView().equals(false)) {
            ViewHolderOne viewHolderOne = (ViewHolderOne) holder;
            ParcelsModel parcelsModel = parcelsModelList.get(position);
            viewHolderOne.trackNumber.setText(parcelsModel.getTrackNumber());
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
                            month = "??????????????????";
                            break;
                        case "Feb":
                            month = "??????????????????????????????";
                            break;
                        case "Mar":
                            month = "??????????????????";
                            break;
                        case "Apr":
                            month = "??????????????????";
                            break;
                        case "May":
                            month = "?????????????????????";
                            break;
                        case "Jun":
                            month = "????????????????????????";
                            break;
                        case "Jul":
                            month = "?????????????????????";
                            break;
                        case "Aug":
                            month = "?????????????????????";
                            break;
                        case "Sep":
                            month = "?????????????????????";
                            break;
                        case "Oct":
                            month = "??????????????????";
                            break;
                        case "Nov":
                            month = "???????????????????????????";
                            break;
                        case "Dec":
                            month = "?????????????????????";
                            break;
                    }
                    String checkIn = String.format("%s %s %s", date, month, year);
                    viewHolderOne.checkIn.setText(checkIn);
                    String[] splitTime = splitDate[3].split(":");
                    String checkInTime = String.format("%s:%s ???.", splitTime[0], splitTime[1]);
                    viewHolderOne.checkInTime.setText(checkInTime);
                } else if (splitDate.length == 7) {
                    String date = splitDate[2];
                    String month = null;
                    String year = String.valueOf(Integer.parseInt(splitDate[3]) + 543);
                    String getMonth = splitDate[1];
                    switch (getMonth) {
                        case "Jan":
                            month = "??????????????????";
                            break;
                        case "Feb":
                            month = "??????????????????????????????";
                            break;
                        case "Mar":
                            month = "??????????????????";
                            break;
                        case "Apr":
                            month = "??????????????????";
                            break;
                        case "May":
                            month = "?????????????????????";
                            break;
                        case "Jun":
                            month = "????????????????????????";
                            break;
                        case "Jul":
                            month = "?????????????????????";
                            break;
                        case "Aug":
                            month = "?????????????????????";
                            break;
                        case "Sep":
                            month = "?????????????????????";
                            break;
                        case "Oct":
                            month = "??????????????????";
                            break;
                        case "Nov":
                            month = "???????????????????????????";
                            break;
                        case "Dec":
                            month = "?????????????????????";
                            break;
                    }
                    String checkIn = String.format("%s %s %s", date, month, year);
                    viewHolderOne.checkIn.setText(checkIn);
                    String[] splitTime = splitDate[4].split(":");
                    String checkInTime = String.format("%s:%s ???.", splitTime[0], splitTime[1]);
                    viewHolderOne.checkInTime.setText(checkInTime);
                }
            } else {
                viewHolderOne.checkIn.setText("-");
                viewHolderOne.checkInTime.setText("-");
            }

            viewHolderOne.ownerFname.setText(parcelsModel.getOwner().getFirstname());
            viewHolderOne.ownerLname.setText(parcelsModel.getOwner().getLastname());

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
                progressDialog = new ProgressDialog(context);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("??????????????????????????????????????????...");
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
                                                    Toast.makeText(context, "?????????????????????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        });
            });
            viewHolderOne.buttonEditParcel.setOnClickListener(v -> {
                db.collection("users")
                        .whereEqualTo("email", firebaseUser.getEmail())
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot getDocIdUser = task.getResult().getDocuments().get(0);
                                String docId = getDocIdUser.getId();

                                db.collection("users").document(docId)
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                String userName = task1.getResult().getString("firstname");
                                                String userSurname = task1.getResult().getString("lastname");

                                                if (userName.equals(parcelsModel.getOwner().getFirstname()) && userSurname.equals(parcelsModel.getOwner().getLastname())) {
                                                    db.collection("parcels")
                                                            .whereEqualTo("trackNumber", parcelsModel.getTrackNumber())
                                                            .get()
                                                            .addOnCompleteListener(task2 -> {
                                                                if (task2.isSuccessful()) {
                                                                    DocumentSnapshot getDocIdParcel = task2.getResult().getDocuments().get(0);
                                                                    String docIdParcel = getDocIdParcel.getId();

                                                                    db.collection("parcels").document(docIdParcel)
                                                                            .get()
                                                                            .addOnCompleteListener(task3 -> {
                                                                                if (task3.isSuccessful()) {
                                                                                    if (task3.getResult().get("checkIn") != null) {
                                                                                        Toast.makeText(context, "????????????????????????????????????????????????????????????????????? ????????????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_LONG).show();
                                                                                    } else {
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
                                                                                                    .addOnCompleteListener(task4 -> {
                                                                                                        if (task4.isSuccessful()) {
                                                                                                            DocumentSnapshot documentSnapshot = task4.getResult().getDocuments().get(0);
                                                                                                            String docIdParcels = documentSnapshot.getId();

                                                                                                            new AlertDialog.Builder(context)
                                                                                                                    .setTitle(parcelsModel.getTrackNumber())
                                                                                                                    .setMessage("????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????")
                                                                                                                    .setPositiveButton("??????????????????", (dialog, which) -> {
                                                                                                                        progressDialog = new ProgressDialog(context);
                                                                                                                        progressDialog.setCanceledOnTouchOutside(false);
                                                                                                                        progressDialog.setMessage("??????????????????????????????????????????...");
                                                                                                                        progressDialog.show();
                                                                                                                        db.collection("parcels").document(docIdParcels)
                                                                                                                                .delete()
                                                                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                                    @Override
                                                                                                                                    public void onSuccess(Void unused) {
                                                                                                                                        Toast.makeText(context, "??????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                                                                                                                                        editTrackNumberDialog.dismiss();
                                                                                                                                        progressDialog.dismiss();
                                                                                                                                    }
                                                                                                                                })
                                                                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                                                                    @Override
                                                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                                                        Toast.makeText(context, "???????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                                                                                                                                        Log.d("Error delete parcel", e.getMessage());
                                                                                                                                    }
                                                                                                                                });
                                                                                                                        parcelsModelList.remove(position);
                                                                                                                        notifyItemRemoved(position);
                                                                                                                        notifyItemRangeChanged(position, parcelsModelList.size());
                                                                                                                    })
                                                                                                                    .setNegativeButton("??????????????????", (dialog, which) -> {
                                                                                                                        dialog.dismiss();
                                                                                                                    })
                                                                                                                    .show();
                                                                                                        } else {
                                                                                                            Toast.makeText(context, "??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    });
                                                                                        });

                                                                                        buttonEditTrackNumber.setOnClickListener(v1 -> {
                                                                                            if (editTrackNumber.getText().toString().isEmpty()) {
                                                                                                editTrackNumber.setError("?????????????????????????????????????????????????????????????????????????????????");
                                                                                            } else {
                                                                                                String trackNumber = editTrackNumber.getText().toString().trim().toUpperCase();
                                                                                                db.collection("parcels")
                                                                                                        .whereEqualTo("trackNumber", parcelsModel.getTrackNumber())
                                                                                                        .get()
                                                                                                        .addOnCompleteListener(task5 -> {
                                                                                                            if (task5.isSuccessful()) {
                                                                                                                DocumentSnapshot documentSnapshot = task5.getResult().getDocuments().get(0);
                                                                                                                String docIdParcels = documentSnapshot.getId();
                                                                                                                new AlertDialog.Builder(context)
                                                                                                                        .setTitle(trackNumber)
                                                                                                                        .setMessage("???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????")
                                                                                                                        .setPositiveButton("??????????????????", (dialog, which) -> {
                                                                                                                            progressDialog = new ProgressDialog(context);
                                                                                                                            progressDialog.setCanceledOnTouchOutside(false);
                                                                                                                            progressDialog.setMessage("??????????????????????????????????????????...");
                                                                                                                            progressDialog.show();
                                                                                                                            db.collection("parcels").document(docIdParcels)
                                                                                                                                    .update("trackNumber", trackNumber)
                                                                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                                        @Override
                                                                                                                                        public void onSuccess(Void unused) {
                                                                                                                                            Toast.makeText(context, "??????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                                                                                                                                            editTrackNumberDialog.dismiss();
                                                                                                                                            progressDialog.dismiss();
                                                                                                                                        }
                                                                                                                                    });
                                                                                                                        })
                                                                                                                        .setNegativeButton("??????????????????", (dialog, which) -> {
                                                                                                                            dialog.dismiss();
                                                                                                                        })
                                                                                                                        .show();
                                                                                                            } else {
                                                                                                                Toast.makeText(context, "??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            });
                                                } else {
                                                    Toast.makeText(context, "????????????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        });
            });
        } else if (parcelsModelList.get(position).getListView().equals(true)) {
            ViewHolderTwo viewHolderTwo = (ViewHolderTwo) holder;
            ParcelsModel parcelsModel = parcelsModelList.get(position);
            viewHolderTwo.trackNumber.setText(parcelsModel.getTrackNumber());
            viewHolderTwo.trackNumberInList.setText(parcelsModel.getTrackNumber());
            viewHolderTwo.constraintLayout.setVisibility(parcelsModel.getDetailInListView() ? View.VISIBLE : View.GONE);
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
                            month = "??????????????????";
                            break;
                        case "Feb":
                            month = "??????????????????????????????";
                            break;
                        case "Mar":
                            month = "??????????????????";
                            break;
                        case "Apr":
                            month = "??????????????????";
                            break;
                        case "May":
                            month = "?????????????????????";
                            break;
                        case "Jun":
                            month = "????????????????????????";
                            break;
                        case "Jul":
                            month = "?????????????????????";
                            break;
                        case "Aug":
                            month = "?????????????????????";
                            break;
                        case "Sep":
                            month = "?????????????????????";
                            break;
                        case "Oct":
                            month = "??????????????????";
                            break;
                        case "Nov":
                            month = "???????????????????????????";
                            break;
                        case "Dec":
                            month = "?????????????????????";
                            break;
                    }
                    String checkIn = String.format("%s %s %s", date, month, year);
                    viewHolderTwo.checkIn.setText(checkIn);
                    String[] splitTime = splitDate[3].split(":");
                    String checkInTime = String.format("%s:%s ???.", splitTime[0], splitTime[1]);
                    viewHolderTwo.checkInTime.setText(checkInTime);
                } else if (splitDate.length == 7) {
                    String date = splitDate[2];
                    String month = null;
                    String year = String.valueOf(Integer.parseInt(splitDate[3]) + 543);
                    String getMonth = splitDate[1];
                    switch (getMonth) {
                        case "Jan":
                            month = "??????????????????";
                            break;
                        case "Feb":
                            month = "??????????????????????????????";
                            break;
                        case "Mar":
                            month = "??????????????????";
                            break;
                        case "Apr":
                            month = "??????????????????";
                            break;
                        case "May":
                            month = "?????????????????????";
                            break;
                        case "Jun":
                            month = "????????????????????????";
                            break;
                        case "Jul":
                            month = "?????????????????????";
                            break;
                        case "Aug":
                            month = "?????????????????????";
                            break;
                        case "Sep":
                            month = "?????????????????????";
                            break;
                        case "Oct":
                            month = "??????????????????";
                            break;
                        case "Nov":
                            month = "???????????????????????????";
                            break;
                        case "Dec":
                            month = "?????????????????????";
                            break;
                    }
                    String checkIn = String.format("%s %s %s", date, month, year);
                    viewHolderTwo.checkIn.setText(checkIn);
                    String[] splitTime = splitDate[4].split(":");
                    String checkInTime = String.format("%s:%s ???.", splitTime[0], splitTime[1]);
                    viewHolderTwo.checkInTime.setText(checkInTime);
                }
            } else {
                viewHolderTwo.checkIn.setText("-");
                viewHolderTwo.checkInTime.setText("-");
            }
            viewHolderTwo.ownerFname.setText(parcelsModel.getOwner().getFirstname());
            viewHolderTwo.ownerLname.setText(parcelsModel.getOwner().getLastname());
            db.collection("parcels")
                    .whereEqualTo("trackNumber", parcelsModel.getTrackNumber())
                    .whereNotEqualTo("images.image0", "")
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
                progressDialog = new ProgressDialog(context);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("??????????????????????????????????????????...");
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
                                                    Toast.makeText(context, "?????????????????????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        });
            });
            viewHolderTwo.buttonEditParcel.setOnClickListener(v -> {
                db.collection("users")
                        .whereEqualTo("email", firebaseUser.getEmail())
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot getDocIdUser = task.getResult().getDocuments().get(0);
                                String docId = getDocIdUser.getId();

                                db.collection("users").document(docId)
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                String userName = task1.getResult().getString("firstname");
                                                String userSurname = task1.getResult().getString("lastname");

                                                if (userName.equals(parcelsModel.getOwner().getFirstname()) && userSurname.equals(parcelsModel.getOwner().getLastname())) {
                                                    db.collection("parcels")
                                                            .whereEqualTo("trackNumber", parcelsModel.getTrackNumber())
                                                            .get()
                                                            .addOnCompleteListener(task2 -> {
                                                                if (task2.isSuccessful()) {
                                                                    DocumentSnapshot getDocIdParcel = task2.getResult().getDocuments().get(0);
                                                                    String docIdParcel = getDocIdParcel.getId();

                                                                    db.collection("parcels").document(docIdParcel)
                                                                            .get()
                                                                            .addOnCompleteListener(task3 -> {
                                                                                if (task3.isSuccessful()) {
                                                                                    if (task3.getResult().get("checkIn") != null) {
                                                                                        Toast.makeText(context, "????????????????????????????????????????????????????????????????????? ????????????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_LONG).show();
                                                                                    } else {
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
                                                                                                    .addOnCompleteListener(task4 -> {
                                                                                                        if (task4.isSuccessful()) {
                                                                                                            DocumentSnapshot documentSnapshot = task4.getResult().getDocuments().get(0);
                                                                                                            String docIdParcels = documentSnapshot.getId();

                                                                                                            new AlertDialog.Builder(context)
                                                                                                                    .setTitle(parcelsModel.getTrackNumber())
                                                                                                                    .setMessage("????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????")
                                                                                                                    .setPositiveButton("??????????????????", (dialog, which) -> {
                                                                                                                        progressDialog = new ProgressDialog(context);
                                                                                                                        progressDialog.setCanceledOnTouchOutside(false);
                                                                                                                        progressDialog.setMessage("??????????????????????????????????????????...");
                                                                                                                        progressDialog.show();
                                                                                                                        db.collection("parcels").document(docIdParcels)
                                                                                                                                .delete()
                                                                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                                    @Override
                                                                                                                                    public void onSuccess(Void unused) {
                                                                                                                                        Toast.makeText(context, "??????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                                                                                                                                        editTrackNumberDialog.dismiss();
                                                                                                                                        progressDialog.dismiss();
                                                                                                                                    }
                                                                                                                                })
                                                                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                                                                    @Override
                                                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                                                        Toast.makeText(context, "???????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                                                                                                                                        Log.d("Error delete parcel", e.getMessage());
                                                                                                                                    }
                                                                                                                                });
                                                                                                                        parcelsModelList.remove(position);
                                                                                                                        notifyItemRemoved(position);
                                                                                                                        notifyItemRangeChanged(position, parcelsModelList.size());
                                                                                                                    })
                                                                                                                    .setNegativeButton("??????????????????", (dialog, which) -> {
                                                                                                                        dialog.dismiss();
                                                                                                                    })
                                                                                                                    .show();
                                                                                                        } else {
                                                                                                            Toast.makeText(context, "??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    });
                                                                                        });

                                                                                        buttonEditTrackNumber.setOnClickListener(v1 -> {
                                                                                            if (editTrackNumber.getText().toString().isEmpty()) {
                                                                                                editTrackNumber.setError("?????????????????????????????????????????????????????????????????????????????????");
                                                                                            } else {
                                                                                                String trackNumber = editTrackNumber.getText().toString().trim().toUpperCase();
                                                                                                db.collection("parcels")
                                                                                                        .whereEqualTo("trackNumber", parcelsModel.getTrackNumber())
                                                                                                        .get()
                                                                                                        .addOnCompleteListener(task5 -> {
                                                                                                            if (task5.isSuccessful()) {
                                                                                                                DocumentSnapshot documentSnapshot = task5.getResult().getDocuments().get(0);
                                                                                                                String docIdParcels = documentSnapshot.getId();
                                                                                                                new AlertDialog.Builder(context)
                                                                                                                        .setTitle(trackNumber)
                                                                                                                        .setMessage("???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????")
                                                                                                                        .setPositiveButton("??????????????????", (dialog, which) -> {
                                                                                                                            progressDialog = new ProgressDialog(context);
                                                                                                                            progressDialog.setCanceledOnTouchOutside(false);
                                                                                                                            progressDialog.setMessage("??????????????????????????????????????????...");
                                                                                                                            progressDialog.show();
                                                                                                                            db.collection("parcels").document(docIdParcels)
                                                                                                                                    .update("trackNumber", trackNumber)
                                                                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                                        @Override
                                                                                                                                        public void onSuccess(Void unused) {
                                                                                                                                            Toast.makeText(context, "??????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                                                                                                                                            editTrackNumberDialog.dismiss();
                                                                                                                                            progressDialog.dismiss();
                                                                                                                                        }
                                                                                                                                    });
                                                                                                                        })
                                                                                                                        .setNegativeButton("??????????????????", (dialog, which) -> {
                                                                                                                            dialog.dismiss();
                                                                                                                        })
                                                                                                                        .show();
                                                                                                            } else {
                                                                                                                Toast.makeText(context, "??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            });
                                                } else {
                                                    Toast.makeText(context, "????????????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                                                }
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
