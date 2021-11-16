package com.example.tnscpe;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
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
import java.util.Random;

public class Fragment_HomePage extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton buttonAddParcel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyParcels;
    private Button randomKey, buttonSearchType;
    private SharedPreferences pref;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = mAuth.getCurrentUser();
    private MaterialButtonToggleGroup buttonToggleGroupListview;
    private EditText searchParcels;
    private ProgressDialog progressDialog;
    private HashSet<String> docListParcelsId = new HashSet<>();
    private ArrayList<String> newTrackNumberArrayList = new ArrayList<>();
    private ArrayList<ParcelsModel> parcelsModelArrayList;
    private ArrayList<ParcelsModel> copyParcelsModelArrayList;
    private AddTrackNumberAdapter addTrackNumberAdapter;
    private ParcelsAdapter parcelsAdapter;
    private ParcelsAdapterAdmin parcelsAdapterAdmin;
    private String userName;
    private String userSurname;
    private String typeForSearch = "";
    private MenuBuilder menuBuilder;

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__home_page, container, false);
        db = FirebaseFirestore.getInstance();
        pref = getContext().getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        recyclerView = view.findViewById(R.id.recycler_view_home_page);
        buttonAddParcel = view.findViewById(R.id.button_add_parcel);
        swipeRefreshLayout = view.findViewById(R.id.layout_refresh_home_page);
        buttonToggleGroupListview = view.findViewById(R.id.button_layout_listview);
        searchParcels = view.findViewById(R.id.search_parcels);
        emptyParcels = view.findViewById(R.id.on_empty_parcels);
        randomKey = view.findViewById(R.id.button_random_key);
        buttonSearchType = view.findViewById(R.id.button_search_type);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        parcelsModelArrayList = new ArrayList<>();

        menuBuilder = new MenuBuilder(getContext());
        MenuInflater inflaterMenu = new MenuInflater(getContext());
        inflaterMenu.inflate(R.menu.menu_search_item, menuBuilder);

        buttonSearchType.setOnClickListener(v -> {
            MenuPopupHelper popupHelper = new MenuPopupHelper(getContext(), menuBuilder, v);
            menuBuilder.setCallback(new MenuBuilder.Callback() {
                @Override
                public boolean onMenuItemSelected(@NonNull MenuBuilder menu, @NonNull MenuItem item) {
                    if (item.getItemId() == R.id.search_track_number) {
                        typeForSearch = "Track Number";
                        buttonSearchType.setText("เลขพัสดุ");
                        return true;
                    } else if (item.getItemId() == R.id.search_name) {
                        typeForSearch = "Name";
                        buttonSearchType.setText("เจ้าของ");
                        return true;
                    } else if (item.getItemId() == R.id.search_date) {
                        typeForSearch = "Date";
                        buttonSearchType.setText("วันที่");
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

        if (pref.getString("Role", "NoData").equals("user")) {
            buttonAddParcel.setVisibility(View.VISIBLE);
            randomKey.setVisibility(View.GONE);
            parcelsAdapter = new ParcelsAdapter(getContext(), parcelsModelArrayList);
            recyclerView.setAdapter(parcelsAdapter);

            swipeRefreshLayout.setOnRefreshListener(() -> {
                parcelsModelArrayList.clear();
                buttonToggleGroupListview.clearChecked();
                searchParcels.clearFocus();
                typeForSearch = "";
                buttonSearchType.setText("ตัวเลือกค้นหา");
                getDataParcel();
                parcelsAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            });

            buttonToggleGroupListview.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    if (checkedId == R.id.button_expandable_view) {
                        for (int i = 0; i < parcelsModelArrayList.size(); i++) {
                            parcelsModelArrayList.get(i).setListView(true);
                            parcelsAdapter.notifyDataSetChanged();
                        }
                    }
                } else if (!isChecked) {
                    for (int i = 0; i < parcelsModelArrayList.size(); i++) {
                        parcelsModelArrayList.get(i).setListView(false);
                        parcelsAdapter.notifyDataSetChanged();
                    }
                }
            });

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy < 0) {
                        buttonAddParcel.hide();
                    } else {
                        buttonAddParcel.show();
                    }
                }
            });

            searchParcels.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() != 0 && searchParcels.isFocused()) {
                        if (!typeForSearch.isEmpty()) {
                            parcelsModelArrayList.clear();
                            parcelsAdapter.notifyDataSetChanged();
                            String getSearch = searchParcels.getText().toString();
                            if (typeForSearch.equals("Track Number")) {
                                String searchTrackNumber = getSearch.toUpperCase();
                                for (int i = 0; i < copyParcelsModelArrayList.size(); i++) {
                                    if (copyParcelsModelArrayList.get(i).getTrackNumber().contains(searchTrackNumber)) {
                                        parcelsModelArrayList.add(copyParcelsModelArrayList.get(i));
                                        parcelsAdapter.notifyDataSetChanged();
                                    }
                                }
                            } else if (typeForSearch.equals("Name")) {
                                for (int i = 0; i < copyParcelsModelArrayList.size(); i++) {
                                    if (copyParcelsModelArrayList.get(i).getOwner().getFirstname().contains(getSearch)) {
                                        parcelsModelArrayList.add(copyParcelsModelArrayList.get(i));
                                        parcelsAdapter.notifyDataSetChanged();
                                    } else if (copyParcelsModelArrayList.get(i).getOwner().getLastname().contains(getSearch)) {
                                        parcelsModelArrayList.add(copyParcelsModelArrayList.get(i));
                                        parcelsAdapter.notifyDataSetChanged();
                                    }
                                }
                            } else if (typeForSearch.equals("Date")) {
                                for (int i = 0; i < copyParcelsModelArrayList.size(); i++) {
                                    if (copyParcelsModelArrayList.get(i).getCheckIn() != null) {
                                        String[] getTime = copyParcelsModelArrayList.get(i).getCheckIn().split(" ");
                                        if (getTime.length == 6) {
                                            String date = getTime[2];
                                            String month = null;
                                            String year = String.valueOf(Integer.parseInt(getTime[5]) + 543);
                                            String getMonth = getTime[1];
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
                                            if (getSearch.contains(date) || getSearch.contains(month) || getSearch.contains(year)) {
                                                parcelsModelArrayList.add(copyParcelsModelArrayList.get(i));
                                                parcelsAdapter.notifyDataSetChanged();
                                            }
                                        } else if (getTime.length == 7) {
                                            String date = getTime[2];
                                            String month = null;
                                            String year = String.valueOf(Integer.parseInt(getTime[3]) + 543);
                                            String getMonth = getTime[1];
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
                                            if (getSearch.contains(date) || getSearch.contains(month) || getSearch.contains(year)) {
                                                parcelsModelArrayList.add(copyParcelsModelArrayList.get(i));
                                                parcelsAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            searchParcels.setError("กรุณาเลือกประเภทที่ต้องการค้นหา");
                        }
                    } else if (s.length() == 0 && searchParcels.isFocused()) {
                        parcelsModelArrayList.clear();
                        parcelsAdapter.notifyDataSetChanged();
                        if (parcelsModelArrayList.size() == 0) {
                            getDataParcel();
                        }
                        buttonToggleGroupListview.clearChecked();
                    }
                }
            });

        } else if (pref.getString("Role", "NoData").equals("admin")) {
            buttonAddParcel.setVisibility(View.GONE);
            randomKey.setVisibility(View.VISIBLE);
            parcelsAdapterAdmin = new ParcelsAdapterAdmin(getContext(), parcelsModelArrayList);
            recyclerView.setAdapter(parcelsAdapterAdmin);

            swipeRefreshLayout.setOnRefreshListener(() -> {
                parcelsModelArrayList.clear();
                buttonToggleGroupListview.clearChecked();
                searchParcels.clearFocus();
                typeForSearch = "";
                buttonSearchType.setText("ตัวเลือกค้นหา");
                getDataParcel();
                parcelsAdapterAdmin.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            });

            buttonToggleGroupListview.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    if (checkedId == R.id.button_expandable_view) {
                        for (int i = 0; i < parcelsModelArrayList.size(); i++) {
                            parcelsModelArrayList.get(i).setListView(true);
                            parcelsAdapterAdmin.notifyDataSetChanged();
                        }
                    }
                } else if (!isChecked) {
                    for (int i = 0; i < parcelsModelArrayList.size(); i++) {
                        parcelsModelArrayList.get(i).setListView(false);
                        parcelsAdapterAdmin.notifyDataSetChanged();
                    }
                }
            });

            randomKey.setOnClickListener(v -> {
                Dialog dialogRandomKey = new Dialog(getContext());
                dialogRandomKey.setContentView(R.layout.random_key_admin);
                dialogRandomKey.setCanceledOnTouchOutside(false);
                ImageView cancelDialogRandomKey = dialogRandomKey.findViewById(R.id.image_random_key_cancel);
                TextInputEditText randomKeyValue = dialogRandomKey.findViewById(R.id.random_key);
                ImageButton buttonRandomNewKey = dialogRandomKey.findViewById(R.id.button_random_new_key);

                cancelDialogRandomKey.setOnClickListener(v1 -> {
                    db.collection("users")
                            .whereEqualTo("role", "admin")
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot getDataAdmin : task.getResult()) {
                                        db.collection("users").document(getDataAdmin.getId())
                                                .get()
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        if (task1.getResult().get("key_code") != null) {
                                                            db.collection("users").document(getDataAdmin.getId())
                                                                    .update("key_code", null)
                                                                    .addOnSuccessListener(unused -> {
                                                                        Log.d("Random key", "is null");
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Log.d("Random key", e.getMessage());
                                                                    });
                                                            dialogRandomKey.dismiss();
                                                        } else {
                                                            dialogRandomKey.dismiss();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                });

                String randomString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
                StringBuilder keyResult = new StringBuilder();
                for (int i = 0; i <= 5; i++) {
                    Random randomKey = new Random();
                    keyResult.append(randomString.charAt(randomKey.nextInt(randomString.length())));
                }
                db.collection("users")
                        .whereEqualTo("role", "admin")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot admin : task.getResult()) {
                                    db.collection("users").document(admin.getId())
                                            .update("key_code", keyResult.toString())
                                            .addOnSuccessListener(unused -> {
                                                Log.d("Random key", "add key");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.d("Random key", e.getMessage());
                                            });
                                }
                            }
                        });
                randomKeyValue.setText(keyResult.toString());

                buttonRandomNewKey.setOnClickListener(v1 -> {
                    randomKeyValue.setText("");
                    String randomNewString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
                    StringBuilder newKeyResult = new StringBuilder();
                    for (int i = 0; i <= 5; i++) {
                        Random randomKey = new Random();
                        newKeyResult.append(randomNewString.charAt(randomKey.nextInt(randomNewString.length())));
                    }
                    db.collection("users")
                            .whereEqualTo("role", "admin")
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot admin : task.getResult()) {
                                        db.collection("users").document(admin.getId())
                                                .update("key_code", newKeyResult.toString())
                                                .addOnSuccessListener(unused -> {
                                                    Log.d("Random key", "add key");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.d("Random key", e.getMessage());
                                                });
                                    }
                                }
                            });
                    randomKeyValue.setText(newKeyResult.toString());
                });
                dialogRandomKey.show();
            });

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy < 0) {
                        randomKey.setVisibility(View.INVISIBLE);
                    } else {
                        randomKey.setVisibility(View.VISIBLE);
                    }
                }
            });

            searchParcels.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() != 0 && searchParcels.isFocused()) {
                        if (!typeForSearch.isEmpty()) {
                            parcelsModelArrayList.clear();
                            parcelsAdapterAdmin.notifyDataSetChanged();
                            String getSearch = searchParcels.getText().toString();
                            if (typeForSearch.equals("Track Number")) {
                                String searchTrackNumber = getSearch.toUpperCase();
                                for (int i = 0; i < copyParcelsModelArrayList.size(); i++) {
                                    if (copyParcelsModelArrayList.get(i).getTrackNumber().contains(searchTrackNumber)) {
                                        parcelsModelArrayList.add(copyParcelsModelArrayList.get(i));
                                        parcelsAdapterAdmin.notifyDataSetChanged();
                                    }
                                }
                            } else if (typeForSearch.equals("Name")) {
                                for (int i = 0; i < copyParcelsModelArrayList.size(); i++) {
                                    if (copyParcelsModelArrayList.get(i).getOwner().getFirstname().contains(getSearch)) {
                                        parcelsModelArrayList.add(copyParcelsModelArrayList.get(i));
                                        parcelsAdapterAdmin.notifyDataSetChanged();
                                    } else if (copyParcelsModelArrayList.get(i).getOwner().getLastname().contains(getSearch)) {
                                        parcelsModelArrayList.add(copyParcelsModelArrayList.get(i));
                                        parcelsAdapterAdmin.notifyDataSetChanged();
                                    }
                                }
                            } else if (typeForSearch.equals("Date")) {
                                for (int i = 0; i < copyParcelsModelArrayList.size(); i++) {
                                    if (copyParcelsModelArrayList.get(i).getCheckIn() != null) {
                                        String[] getTime = copyParcelsModelArrayList.get(i).getCheckIn().split(" ");
                                        if (getTime.length == 6) {
                                            String date = getTime[2];
                                            String month = null;
                                            String year = String.valueOf(Integer.parseInt(getTime[5]) + 543);
                                            String getMonth = getTime[1];
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
                                            if (getSearch.contains(date) || getSearch.contains(month) || getSearch.contains(year)) {
                                                parcelsModelArrayList.add(copyParcelsModelArrayList.get(i));
                                                parcelsAdapterAdmin.notifyDataSetChanged();
                                            }
                                        } else if (getTime.length == 7) {
                                            String date = getTime[2];
                                            String month = null;
                                            String year = String.valueOf(Integer.parseInt(getTime[3]) + 543);
                                            String getMonth = getTime[1];
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
                                            if (getSearch.contains(date) || getSearch.contains(month) || getSearch.contains(year)) {
                                                parcelsModelArrayList.add(copyParcelsModelArrayList.get(i));
                                                parcelsAdapterAdmin.notifyDataSetChanged();
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            searchParcels.setError("กรุณาเลือกประเภทที่ต้องการค้นหา");
                        }
                    } else if (s.length() == 0 && searchParcels.isFocused()) {
                        parcelsModelArrayList.clear();
                        parcelsAdapterAdmin.notifyDataSetChanged();
                        if (parcelsModelArrayList.size() == 0) {
                            getDataParcel();
                        }
                        buttonToggleGroupListview.clearChecked();
                    }
                }
            });
        }

        buttonAddParcel.setOnClickListener(v -> {
            userAddTrackNumber();
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDataParcel();
    }

    private void getDataParcel() {
        if (pref.getString("Role", "NoData").equals("user")) {
            db.collection("users")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot getDocIDUser : task.getResult()) {
                                db.collection("users").document(getDocIDUser.getId())
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                if (task1.getResult().getString("email").equals(firebaseUser.getEmail())) {
                                                    userName = task1.getResult().getString("firstname");
                                                    userSurname = task1.getResult().getString("lastname");
                                                    db.collection("parcels")
                                                            .whereEqualTo("checkOut", null)
                                                            .whereEqualTo("owner.firstname", userName)
                                                            .whereEqualTo("owner.lastname", userSurname)
                                                            .get()
                                                            .addOnCompleteListener(task2 -> {
                                                                if (task2.isSuccessful() && task2.getResult() != null) {
                                                                    docListParcelsId.clear();
                                                                    for (QueryDocumentSnapshot getDocParcel : task2.getResult()) {
                                                                        docListParcelsId.add(getDocParcel.getId());
                                                                    }

                                                                    if (!docListParcelsId.isEmpty()) {
                                                                        progressDialog = new ProgressDialog(getContext());
                                                                        progressDialog.setCanceledOnTouchOutside(false);
                                                                        progressDialog.setMessage("กำลังดึงข้อมูล");
                                                                        progressDialog.show();
                                                                        emptyParcels.setVisibility(View.GONE);
                                                                        searchParcels.setVisibility(View.VISIBLE);
                                                                        buttonToggleGroupListview.setVisibility(View.VISIBLE);
                                                                        buttonSearchType.setVisibility(View.VISIBLE);
                                                                        for (String docIdCurrentUser : docListParcelsId) {
                                                                            db.collection("parcels").document(docIdCurrentUser)
                                                                                    .addSnapshotListener((value, error) -> {
                                                                                        if (error != null) {
                                                                                            Log.d("Error home page", error.getMessage());
                                                                                            if (progressDialog.isShowing()) {
                                                                                                progressDialog.dismiss();
                                                                                            }
                                                                                        }

                                                                                        if (value.exists()) {
                                                                                            parcelsModelArrayList.add(value.toObject(ParcelsModel.class));
                                                                                            copyParcelsModelArrayList = new ArrayList<>(parcelsModelArrayList);
                                                                                        }
                                                                                        parcelsAdapter.notifyDataSetChanged();
                                                                                        progressDialog.dismiss();
                                                                                    });
                                                                        }
                                                                    } else {
                                                                        searchParcels.setVisibility(View.GONE);
                                                                        buttonToggleGroupListview.setVisibility(View.GONE);
                                                                        buttonSearchType.setVisibility(View.GONE);
                                                                        emptyParcels.setVisibility(View.VISIBLE);
                                                                    }
                                                                } else {
                                                                    Toast.makeText(getContext(), "ไม่พบข้อมูลพัสดุของคุณ", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            }
                                        });
                            }
                        }
                    });
        } else if (pref.getString("Role", "NoData").equals("admin")) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("กำลังดึงข้อมูล");
            progressDialog.show();
            db.collection("parcels")
                    .whereEqualTo("checkOut", null)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.d("Error home page", error.getMessage());
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        }

                        if (!value.isEmpty()) {
                            emptyParcels.setVisibility(View.GONE);
                            buttonToggleGroupListview.setVisibility(View.VISIBLE);
                            searchParcels.setVisibility(View.VISIBLE);
                            randomKey.setVisibility(View.VISIBLE);
                            buttonSearchType.setVisibility(View.VISIBLE);
                            for (DocumentChange documentChange : value.getDocumentChanges()) {
                                if (documentChange.getType().equals(DocumentChange.Type.ADDED)) {
                                    parcelsModelArrayList.add(documentChange.getDocument().toObject(ParcelsModel.class));
                                    copyParcelsModelArrayList = new ArrayList<>(parcelsModelArrayList);
                                }
                                parcelsAdapterAdmin.notifyDataSetChanged();
                                progressDialog.dismiss();
                            }
                        } else {
                            emptyParcels.setVisibility(View.VISIBLE);
                            searchParcels.setVisibility(View.GONE);
                            buttonToggleGroupListview.setVisibility(View.GONE);
                            buttonSearchType.setVisibility(View.GONE);
                            progressDialog.dismiss();
                        }
                    });
        }

    }

    public void userAddTrackNumber() {
        Dialog dialogAddParcel = new Dialog(getContext());
        dialogAddParcel.setCanceledOnTouchOutside(false);
        dialogAddParcel.setContentView(R.layout.add_parcel_dialog);
        ImageView closeDialogAddParcel = dialogAddParcel.findViewById(R.id.close_add_parcel_dialog);
        ListView trackNumberList = dialogAddParcel.findViewById(R.id.track_number);
        EditText trackNumberNewParcel = dialogAddParcel.findViewById(R.id.add_tracknumber_new_parcel);
        ImageButton buttonAddTrackNumber = dialogAddParcel.findViewById(R.id.button_add_track_number);
        Button buttonSaveNewParcel = dialogAddParcel.findViewById(R.id.button_confirm_add_parcel);

        addTrackNumberAdapter = new AddTrackNumberAdapter(dialogAddParcel.getContext(), newTrackNumberArrayList);
        trackNumberList.setAdapter(addTrackNumberAdapter);
        addTrackNumberAdapter.notifyDataSetChanged();
        trackNumberList.setClickable(true);

        closeDialogAddParcel.setOnClickListener(v1 -> {
            newTrackNumberArrayList.clear();
            addTrackNumberAdapter.notifyDataSetChanged();
            dialogAddParcel.dismiss();
        });

        buttonAddTrackNumber.setOnClickListener(v -> {
            if (trackNumberNewParcel.getText().toString().isEmpty()) {
                Toast.makeText(dialogAddParcel.getContext(), "กรุณากรอกเลขติดตามพัสดุที่ท่านต้องการ", Toast.LENGTH_SHORT).show();
            } else {
                String trackNumber = trackNumberNewParcel.getText().toString().trim().toUpperCase();
                db.collection("parcels")
                        .whereEqualTo("trackNumber", trackNumber)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult().isEmpty()) {
                                if (newTrackNumberArrayList.indexOf(trackNumber) != -1) {
                                    trackNumberNewParcel.setError("กรุณากรอกหมายเลขติดตามพัสดุอีกครั้ง");
                                    Toast.makeText(getContext(), "ท่านเพิ่มหมายเลขติดตามพัสดุนี้แล้ว", Toast.LENGTH_SHORT).show();
                                } else {
                                    newTrackNumberArrayList.add(trackNumber);
                                    addTrackNumberAdapter.notifyDataSetChanged();
                                    trackNumberNewParcel.setText("");
                                }
                            } else {
                                trackNumberNewParcel.setError("กรุณากรอกหมายเลขติดตามพัสดุอีกครั้ง");
                                Toast.makeText(getContext(), "หมายเลขติดตามนี้มีอยู่แล้ว", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        trackNumberList.setOnItemClickListener((parent, view, position, id) -> {
            TextView trackNumber = view.findViewById(R.id.tracknumber);
            String trackNumberSelect = trackNumber.getText().toString();
            new AlertDialog.Builder(dialogAddParcel.getContext())
                    .setTitle(trackNumberSelect)
                    .setMessage("ท่านต้องการลบหมายเลขติดตามพัสดุนี้ใช่หรือไม่")
                    .setPositiveButton("ยืนยัน", (deleteTrackNumber, which) -> {
                        newTrackNumberArrayList.remove(position);
                        addTrackNumberAdapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("ยกเลิก", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });

        buttonSaveNewParcel.setOnClickListener(v1 -> {
            if (newTrackNumberArrayList.size() == 0) {
                trackNumberNewParcel.setError("กรุณาเพิ่มหมายเลขติดตามพัสดุที่ท่านต้องการ");
            } else {
                db.collection("users")
                        .whereEqualTo("email", firebaseUser.getEmail())
                        .get()
                        .addOnCompleteListener(task1 -> {
                            DocumentSnapshot getDocId = task1.getResult().getDocuments().get(0);
                            String docId = getDocId.getId();

                            db.collection("users").document(docId)
                                    .get()
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            String userName = task2.getResult().getString("firstname");
                                            String userSurname = task2.getResult().getString("lastname");
                                            String department = task2.getResult().getString("department");

                                            for (String trackNumber : newTrackNumberArrayList) {
                                                Map<String, Object> docParcel = new HashMap<>();
                                                docParcel.put("checkIn", null);
                                                docParcel.put("checkOut", null);
                                                docParcel.put("department", department);

                                                Map<String, Object> mapImages = new HashMap<>();
                                                mapImages.put("image0", "");
                                                mapImages.put("image1", "");
                                                mapImages.put("image2", "");
                                                mapImages.put("image3", "");
                                                docParcel.put("images", mapImages);

                                                Map<String, Object> mapOwner = new HashMap<>();
                                                mapOwner.put("firstname", userName);
                                                mapOwner.put("lastname", userSurname);
                                                docParcel.put("owner", mapOwner);

                                                Map<String, Object> mapReceiver = new HashMap<>();
                                                mapReceiver.put("firstname", "");
                                                mapReceiver.put("lastname", "");
                                                docParcel.put("receiver", mapReceiver);

                                                docParcel.put("trackNumber", trackNumber);

                                                db.collection("parcels").document()
                                                        .set(docParcel)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                dialogAddParcel.dismiss();
                                                                Toast.makeText(getContext(), "เพิ่มหมายเลขติดตามพัสดุเสร็จสิ้น", Toast.LENGTH_SHORT).show();
                                                                Log.d("user add new track number", "สำเร็จ");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                if (dialogAddParcel.isShowing()) {
                                                                    dialogAddParcel.dismiss();
                                                                }
                                                                Log.d("user add new track number", e.getMessage());
                                                            }
                                                        });
                                            }
                                            newTrackNumberArrayList.clear();
                                            addTrackNumberAdapter.notifyDataSetChanged();
                                            buttonToggleGroupListview.clearChecked();
                                            searchParcels.setText("");
                                            searchParcels.clearFocus();
                                            parcelsAdapter.notifyDataSetChanged();
                                        }
                                    });
                        });
            }
        });
        dialogAddParcel.show();
    }
}