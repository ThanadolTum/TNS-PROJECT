package com.example.tnscpe;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import android.widget.TextView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class Fragment_History extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyParcels;
    private Button buttonSearchType;
    private EditText searchParcels;
    private MaterialButtonToggleGroup buttonToggleGroupListview;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = mAuth.getCurrentUser();
    private ProgressDialog progressDialog;
    private HashSet<String> docListParcelsId = new HashSet<>();
    private ArrayList<ParcelsModel> parcelsModelArrayList;
    private ArrayList<ParcelsModel> copyParcelsModelArrayList;
    private HistoryAdapter historyAdapter;
    private SharedPreferences pref;
    private String typeForSearch = "";
    private MenuBuilder menuBuilder;

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__history, container, false);
        db = FirebaseFirestore.getInstance();
        pref = getContext().getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        recyclerView = view.findViewById(R.id.recycler_view_history);
        swipeRefreshLayout = view.findViewById(R.id.layout_refresh_history);
        emptyParcels = view.findViewById(R.id.on_empty_parcels);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        parcelsModelArrayList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(getContext(), parcelsModelArrayList);
        recyclerView.setAdapter(historyAdapter);
        buttonToggleGroupListview = view.findViewById(R.id.button_layout_listview);
        buttonToggleGroupListview.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.button_expandable_view) {
                    for (int i = 0; i < parcelsModelArrayList.size(); i++) {
                        parcelsModelArrayList.get(i).setListView(true);
                        historyAdapter.notifyDataSetChanged();
                    }
                }
            } else if (!isChecked) {
                for (int i = 0; i < parcelsModelArrayList.size(); i++) {
                    parcelsModelArrayList.get(i).setListView(false);
                    historyAdapter.notifyDataSetChanged();
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            parcelsModelArrayList.clear();
            buttonToggleGroupListview.clearChecked();
            searchParcels.clearFocus();
            typeForSearch = "";
            buttonSearchType.setText("ตัวเลือกค้นหา");
            getDataParcelsHistory();
            historyAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        });

        buttonSearchType = view.findViewById(R.id.button_search_type);
        searchParcels = view.findViewById(R.id.search_parcels);

        if (pref.getString("Role", "NoData").equals("user")) {
            menuBuilder = new MenuBuilder(getContext());
            MenuInflater inflaterMenu = new MenuInflater(getContext());
            inflaterMenu.inflate(R.menu.menu_search_history_item, menuBuilder);

            buttonSearchType.setOnClickListener(v -> {
                MenuPopupHelper popupHelper = new MenuPopupHelper(getContext(), menuBuilder, v);
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(@NonNull MenuBuilder menu, @NonNull MenuItem item) {
                        if (item.getItemId() == R.id.search_receiver_name) {
                            typeForSearch = "Receiver name";
                            buttonSearchType.setText("ชื่อผู้รับ");
                            return true;
                        } else if (item.getItemId() == R.id.search_track_number) {
                            typeForSearch = "Track number";
                            buttonSearchType.setText("เลขพัสดุ");
                            return true;
                        } else if (item.getItemId() == R.id.search_date_of_admin) {
                            typeForSearch = "Date of admin";
                            buttonSearchType.setText("เจ้าหน้าที่");
                            return true;
                        } else if (item.getItemId() == R.id.search_date_of_receiver) {
                            typeForSearch = "Date of receiver";
                            buttonSearchType.setText("ผู้รับ");
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
        } else if (pref.getString("Role", "NoData").equals("admin")) {
            menuBuilder = new MenuBuilder(getContext());
            MenuInflater inflaterMenu = new MenuInflater(getContext());
            inflaterMenu.inflate(R.menu.menu_search_history_admin_item, menuBuilder);

            buttonSearchType.setOnClickListener(v -> {
                MenuPopupHelper popupHelper = new MenuPopupHelper(getContext(), menuBuilder, v);
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(@NonNull MenuBuilder menu, @NonNull MenuItem item) {
                        if (item.getItemId() == R.id.search_owner_name) {
                            typeForSearch = "Owner name";
                            buttonSearchType.setText("เจ้าของ");
                            return true;
                        } else if (item.getItemId() == R.id.search_receiver_name) {
                            typeForSearch = "Receiver name";
                            buttonSearchType.setText("ชื่อผู้รับ");
                            return true;
                        } else if (item.getItemId() == R.id.search_track_number) {
                            typeForSearch = "Track number";
                            buttonSearchType.setText("เลขพัสดุ");
                            return true;
                        } else if (item.getItemId() == R.id.search_date_of_admin) {
                            typeForSearch = "Date of admin";
                            buttonSearchType.setText("เจ้าหน้าที่");
                            return true;
                        } else if (item.getItemId() == R.id.search_date_of_receiver) {
                            typeForSearch = "Date of receiver";
                            buttonSearchType.setText("ผู้รับ");
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
        }

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
                        historyAdapter.notifyDataSetChanged();
                        String getSearch = searchParcels.getText().toString();
                        if (typeForSearch.equals("Track number")) {
                            String searchTrackNumber = getSearch.toUpperCase();
                            for (int i = 0; i < copyParcelsModelArrayList.size(); i++) {
                                if (copyParcelsModelArrayList.get(i).getTrackNumber().contains(searchTrackNumber)) {
                                    parcelsModelArrayList.add(copyParcelsModelArrayList.get(i));
                                    historyAdapter.notifyDataSetChanged();
                                }
                            }
                        } else if (typeForSearch.equals("Owner name")) {
                            for (int i = 0; i < copyParcelsModelArrayList.size(); i++) {
                                if (copyParcelsModelArrayList.get(i).getOwner().getFirstname().contains(getSearch)) {
                                    parcelsModelArrayList.add(copyParcelsModelArrayList.get(i));
                                    historyAdapter.notifyDataSetChanged();
                                } else if (copyParcelsModelArrayList.get(i).getOwner().getLastname().contains(getSearch)) {
                                    parcelsModelArrayList.add(copyParcelsModelArrayList.get(i));
                                    historyAdapter.notifyDataSetChanged();
                                }
                            }
                        } else if (typeForSearch.equals("Date of admin")) {
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
                                            historyAdapter.notifyDataSetChanged();
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
                                            historyAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            }
                        } else if (typeForSearch.equals("Receiver name")) {
                            for (int i = 0; i < copyParcelsModelArrayList.size(); i++) {
                                if (copyParcelsModelArrayList.get(i).getReceiver().getFirstname().contains(getSearch)) {
                                    parcelsModelArrayList.add(copyParcelsModelArrayList.get(i));
                                    historyAdapter.notifyDataSetChanged();
                                } else if (copyParcelsModelArrayList.get(i).getReceiver().getLastname().contains(getSearch)) {
                                    parcelsModelArrayList.add(copyParcelsModelArrayList.get(i));
                                    historyAdapter.notifyDataSetChanged();
                                }
                            }
                        } else if (typeForSearch.equals("Date of receiver")) {
                            for (int i = 0; i < copyParcelsModelArrayList.size(); i++) {
                                if (copyParcelsModelArrayList.get(i).getCheckOut() != null) {
                                    String[] getTime = copyParcelsModelArrayList.get(i).getCheckOut().split(" ");
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
                                            historyAdapter.notifyDataSetChanged();
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
                                            historyAdapter.notifyDataSetChanged();
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
                    historyAdapter.notifyDataSetChanged();
                    if (parcelsModelArrayList.size() == 0) {
                        getDataParcelsHistory();
                    }
                    buttonToggleGroupListview.clearChecked();
                }
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDataParcelsHistory();
    }

    private void getDataParcelsHistory() {
        if (pref.getString("Role", "NoData").equals("user")) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("กำลังดึงข้อมูล");
            progressDialog.show();
            db.collection("users")
                    .whereEqualTo("email", firebaseUser.getEmail())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            String userDocId = documentSnapshot.getId();

                            db.collection("users").document(userDocId)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot getUserData = task1.getResult();
                                            String userName = Objects.requireNonNull(getUserData.get("firstname")).toString();
                                            String userSurname = Objects.requireNonNull(getUserData.get("lastname")).toString();

                                            db.collection("parcels")
                                                    .whereNotEqualTo("checkOut", null)
                                                    .get()
                                                    .addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful() && task2.getResult() != null) {
                                                            docListParcelsId.clear();
                                                            for (QueryDocumentSnapshot getParcelsDocId : task2.getResult()) {
                                                                if (getParcelsDocId.get("owner.firstname").equals(userName) && getParcelsDocId.get("owner.lastname").equals(userSurname)) {
                                                                    docListParcelsId.add(getParcelsDocId.getId());
                                                                }
                                                            }
                                                            if (!docListParcelsId.isEmpty()) {
                                                                emptyParcels.setVisibility(View.GONE);
                                                                buttonToggleGroupListview.setVisibility(View.VISIBLE);
                                                                searchParcels.setVisibility(View.VISIBLE);
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
                                                                                } else if (!value.exists()) {
                                                                                    if (progressDialog.isShowing()) {
                                                                                        progressDialog.dismiss();
                                                                                    }
                                                                                }
                                                                                historyAdapter.notifyDataSetChanged();
                                                                                progressDialog.dismiss();
                                                                            });
                                                                }
                                                            } else {
                                                                emptyParcels.setVisibility(View.VISIBLE);
                                                                buttonToggleGroupListview.setVisibility(View.GONE);
                                                                searchParcels.setVisibility(View.GONE);
                                                                buttonSearchType.setVisibility(View.GONE);
                                                                progressDialog.dismiss();
                                                            }
                                                        } else {
                                                            emptyParcels.setVisibility(View.VISIBLE);
                                                            buttonToggleGroupListview.setVisibility(View.GONE);
                                                            searchParcels.setVisibility(View.GONE);
                                                            buttonSearchType.setVisibility(View.GONE);
                                                            progressDialog.dismiss();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    });
        } else if (pref.getString("Role", "NoData").equals("admin")) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("กำลังดึงข้อมูล");
            progressDialog.show();
            db.collection("parcels")
                    .whereNotEqualTo("checkOut", null)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.d("Error history page", error.getMessage());
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        }

                        if (!value.isEmpty()) {
                            emptyParcels.setVisibility(View.GONE);
                            buttonToggleGroupListview.setVisibility(View.VISIBLE);
                            searchParcels.setVisibility(View.VISIBLE);
                            buttonSearchType.setVisibility(View.VISIBLE);
                            for (DocumentChange documentChange : value.getDocumentChanges()) {
                                if (documentChange.getType().equals(DocumentChange.Type.ADDED)) {
                                    parcelsModelArrayList.add(documentChange.getDocument().toObject(ParcelsModel.class));
                                    copyParcelsModelArrayList = new ArrayList<>(parcelsModelArrayList);
                                }
                            }
                            historyAdapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                        } else {
                            emptyParcels.setVisibility(View.VISIBLE);
                            buttonToggleGroupListview.setVisibility(View.GONE);
                            searchParcels.setVisibility(View.GONE);
                            buttonSearchType.setVisibility(View.GONE);
                            progressDialog.dismiss();
                        }
                    });
        }

    }
}