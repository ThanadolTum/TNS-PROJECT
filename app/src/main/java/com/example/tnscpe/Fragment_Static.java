package com.example.tnscpe;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Fragment_Static extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private SharedPreferences pref;
    private ProgressDialog progressDialog;
    private ArrayList<String> yearList = new ArrayList<>();
    private ArrayList<String> thisYear = new ArrayList<>();
    private AutoCompleteTextView selectYear;
    private TextView totalParcels, yearSelect, minimizeParcels, maximizeParcels, minimizeMonth, maximizeMonth, averageParcels;
    private BarChart barChart;
    private int Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__static, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        pref = getContext().getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        selectYear = view.findViewById(R.id.year);
        totalParcels = view.findViewById(R.id.total_parcels);
        minimizeParcels = view.findViewById(R.id.minimize_parcels);
        maximizeParcels = view.findViewById(R.id.maximize_parcel);
        minimizeMonth = view.findViewById(R.id.minimize_month);
        maximizeMonth = view.findViewById(R.id.maximize_month);
        averageParcels = view.findViewById(R.id.average_parcel);
        yearSelect = view.findViewById(R.id.year_select);
        barChart = view.findViewById(R.id.bar_chart_parcels);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(getContext(), R.layout.department_item, yearList);
        selectYear.setAdapter(yearAdapter);
        mocData();
        getTotalParcels();
    }

    public void getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 543);
        yearList.add(String.valueOf(calendar.get(Calendar.YEAR)));
    }

    public void mocData() {
        yearList.clear();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 543);
        int currentYear = calendar.get(Calendar.YEAR);
        for (int i = 0; i <= 5; i++) {
            yearList.add(String.valueOf(currentYear - i));
        }
    }

    public void getTotalParcels() {
        if (pref.getString("Role", "NoData").equals("admin")) {
            selectYear.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("RRR", "this is admin");
                    thisYear.clear();
                    Jan = 0;
                    Feb = 0;
                    Mar = 0;
                    Apr = 0;
                    May = 0;
                    Jun = 0;
                    Jul = 0;
                    Aug = 0;
                    Sep = 0;
                    Oct = 0;
                    Nov = 0;
                    Dec = 0;
                    String year = selectYear.getText().toString();
                    yearSelect.setText(year);
                    progressDialog = new ProgressDialog(getContext());
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setMessage("กรุณารอสักครู่");
                    progressDialog.show();
                    db.collection("parcels")
                            .whereNotEqualTo("checkOut", null)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    for (DocumentSnapshot getDocId : task.getResult()) {
                                        String docId = getDocId.getId();

                                        db.collection("parcels").document(docId)
                                                .get()
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        String getCheckOut = task1.getResult().get("checkOut").toString();
                                                        String[] splitCheckOut = getCheckOut.split(" ");
                                                        if (splitCheckOut.length == 6) {
                                                            String getYear = String.valueOf(Integer.parseInt(splitCheckOut[5]) + 543);
                                                            if (getYear.equals(year)) {
                                                                thisYear.add(getYear);
                                                                String getMonth = splitCheckOut[1];
                                                                switch (getMonth) {
                                                                    case "Jan":
                                                                        Jan++;
                                                                        break;
                                                                    case "Feb":
                                                                        Feb++;
                                                                        break;
                                                                    case "Mar":
                                                                        Mar++;
                                                                        break;
                                                                    case "Apr":
                                                                        Apr++;
                                                                    case "May":
                                                                        May++;
                                                                        break;
                                                                    case "Jun":
                                                                        Jun++;
                                                                        break;
                                                                    case "Jul":
                                                                        Jul++;
                                                                        break;
                                                                    case "Aug":
                                                                        Aug++;
                                                                        break;
                                                                    case "Sep":
                                                                        Sep++;
                                                                        break;
                                                                    case "Oct":
                                                                        Oct++;
                                                                        break;
                                                                    case "Nov":
                                                                        Nov++;
                                                                        break;
                                                                    case "Dec":
                                                                        Dec++;
                                                                        break;
                                                                }
                                                            }
                                                        } else if (splitCheckOut.length == 7) {
                                                            String getYear = String.valueOf(Integer.parseInt(splitCheckOut[3]) + 543);
                                                            if (getYear.equals(year)) {
                                                                thisYear.add(getYear);
                                                                String getMonth = splitCheckOut[1];
                                                                switch (getMonth) {
                                                                    case "Jan":
                                                                        Jan++;
                                                                        break;
                                                                    case "Feb":
                                                                        Feb++;
                                                                        break;
                                                                    case "Mar":
                                                                        Mar++;
                                                                        break;
                                                                    case "Apr":
                                                                        Apr++;
                                                                    case "May":
                                                                        May++;
                                                                        break;
                                                                    case "Jun":
                                                                        Jun++;
                                                                        break;
                                                                    case "Jul":
                                                                        Jul++;
                                                                        break;
                                                                    case "Aug":
                                                                        Aug++;
                                                                        break;
                                                                    case "Sep":
                                                                        Sep++;
                                                                        break;
                                                                    case "Oct":
                                                                        Oct++;
                                                                        break;
                                                                    case "Nov":
                                                                        Nov++;
                                                                        break;
                                                                    case "Dec":
                                                                        Dec++;
                                                                        break;
                                                                }
                                                            }
                                                        }
                                                        totalParcels.setText(String.valueOf(thisYear.size()));
                                                        averageParcels.setText(String.valueOf(thisYear.size() / 12));

                                                        ArrayList<Integer> getMonth = new ArrayList<>();
                                                        getMonth.add(Jan);
                                                        getMonth.add(Feb);
                                                        getMonth.add(Mar);
                                                        getMonth.add(Apr);
                                                        getMonth.add(May);
                                                        getMonth.add(Jun);
                                                        getMonth.add(Jul);
                                                        getMonth.add(Aug);
                                                        getMonth.add(Sep);
                                                        getMonth.add(Oct);
                                                        getMonth.add(Nov);
                                                        getMonth.add(Dec);

                                                        int maxLoop = 0;
                                                        int maxIndex = 0;
                                                        int max = -1;
                                                        for (Integer month : getMonth) {
                                                            if (month > max) {
                                                                max = month;
                                                                maxIndex = maxLoop;
                                                            }
                                                            maxLoop++;
                                                            switch (maxIndex) {
                                                                case 0:
                                                                    maximizeMonth.setText("ม.ค.");
                                                                    break;
                                                                case 1:
                                                                    maximizeMonth.setText("ก.พ.");
                                                                    break;
                                                                case 2:
                                                                    maximizeMonth.setText("มี.ค.");
                                                                    break;
                                                                case 3:
                                                                    maximizeMonth.setText("เม.ย.");
                                                                    break;
                                                                case 4:
                                                                    maximizeMonth.setText("พ.ค");
                                                                    break;
                                                                case 5:
                                                                    maximizeMonth.setText("มิ.ย.");
                                                                    break;
                                                                case 6:
                                                                    maximizeMonth.setText("ก.ค.");
                                                                    break;
                                                                case 7:
                                                                    maximizeMonth.setText("ส.ค.");
                                                                    break;
                                                                case 8:
                                                                    maximizeMonth.setText("ก.ย.");
                                                                    break;
                                                                case 9:
                                                                    maximizeMonth.setText("ต.ค.");
                                                                    break;
                                                                case 10:
                                                                    maximizeMonth.setText("พ.ย.");
                                                                    break;
                                                                case 11:
                                                                    maximizeMonth.setText("ธ.ค.");
                                                                    break;
                                                            }
                                                        }

                                                        int min = getMonth.get(0);
                                                        int minLoop = 0;
                                                        int minIndex = 0;
                                                        for (int i = 0; i < getMonth.size(); i++) {
                                                            if (getMonth.get(i) < min) {
                                                                minIndex = minLoop;
                                                            }
                                                            minLoop++;
                                                            switch (minIndex) {
                                                                case 0:
                                                                    minimizeMonth.setText("ม.ค.");
                                                                    break;
                                                                case 1:
                                                                    minimizeMonth.setText("ก.พ.");
                                                                    break;
                                                                case 2:
                                                                    minimizeMonth.setText("มี.ค.");
                                                                    break;
                                                                case 3:
                                                                    minimizeMonth.setText("เม.ย.");
                                                                    break;
                                                                case 4:
                                                                    minimizeMonth.setText("พ.ค.");
                                                                    break;
                                                                case 5:
                                                                    minimizeMonth.setText("มิ.ย.");
                                                                    break;
                                                                case 6:
                                                                    minimizeMonth.setText("ก.ค.");
                                                                    break;
                                                                case 7:
                                                                    minimizeMonth.setText("ส.ค.");
                                                                    break;
                                                                case 8:
                                                                    minimizeMonth.setText("ก.ย.");
                                                                    break;
                                                                case 9:
                                                                    minimizeMonth.setText("ต.ค.");
                                                                    break;
                                                                case 10:
                                                                    minimizeMonth.setText("พ.ย.");
                                                                    break;
                                                                case 11:
                                                                    minimizeMonth.setText("ธ.ค.");
                                                                    break;
                                                            }
                                                        }
                                                        Collections.sort(getMonth, Collections.reverseOrder());
                                                        minimizeParcels.setText(String.valueOf(getMonth.get(11)));
                                                        maximizeParcels.setText(String.valueOf(getMonth.get(0)));

                                                        XAxis xAxis = barChart.getXAxis();
                                                        ArrayList<String> month = new ArrayList<>();
                                                        month.add("มกราคม");
                                                        month.add("กุมภาพันธ์");
                                                        month.add("มีนาคม");
                                                        month.add("เมษายน");
                                                        month.add("พฤษษาคม");
                                                        month.add("มิถุนายน");
                                                        month.add("กรกฎาคม");
                                                        month.add("สิงหาคม");
                                                        month.add("กันยายน");
                                                        month.add("ตุลาคม");
                                                        month.add("พฤศจิกายน");
                                                        month.add("ธันวาคม");

                                                        ArrayList<BarEntry> barEntries = new ArrayList<>();
                                                        barEntries.add(new BarEntry(0, Jan));
                                                        barEntries.add(new BarEntry(1, Feb));
                                                        barEntries.add(new BarEntry(2, Mar));
                                                        barEntries.add(new BarEntry(3, Apr));
                                                        barEntries.add(new BarEntry(4, May));
                                                        barEntries.add(new BarEntry(5, Jun));
                                                        barEntries.add(new BarEntry(6, Jul));
                                                        barEntries.add(new BarEntry(7, Aug));
                                                        barEntries.add(new BarEntry(8, Sep));
                                                        barEntries.add(new BarEntry(9, Oct));
                                                        barEntries.add(new BarEntry(10, Nov));
                                                        barEntries.add(new BarEntry(11, Dec));

                                                        BarDataSet barDataSet = new BarDataSet(barEntries, "จำนวนพัสดุ");
                                                        barDataSet.setColors(ColorTemplate.rgb("3700B3"));
                                                        barDataSet.setValueTextColor(Color.BLACK);
                                                        barDataSet.setValueTextSize(16f);
                                                        barDataSet.setValueFormatter(new MyValueFormatter());

                                                        BarData barData = new BarData(barDataSet);

                                                        barChart.setFitBars(true);
                                                        barChart.setData(barData);
                                                        barChart.animateY(2000);
                                                        barChart.invalidate();

                                                        xAxis.setValueFormatter(new IndexAxisValueFormatter(month));
                                                        xAxis.setPosition(XAxis.XAxisPosition.TOP);
                                                        xAxis.setDrawGridLines(false);
                                                        xAxis.setDrawAxisLine(false);
                                                        xAxis.setLabelCount(0);
                                                        xAxis.setGranularity(1f);
                                                        xAxis.setLabelCount(month.size());
                                                        xAxis.setLabelRotationAngle(270);

                                                        progressDialog.dismiss();
                                                    } else if (!task1.getResult().exists()) {
                                                        Toast.makeText(getContext(), "ไม่พบสถิติการรับพัสดุ", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                } else {
                                    progressDialog.dismiss();
                                    totalParcels.setText("-");
                                    averageParcels.setText("-");
                                    minimizeParcels.setText("-");
                                    maximizeParcels.setText("-");
                                    minimizeMonth.setText("-");
                                    maximizeMonth.setText("-");
                                    Toast.makeText(getContext(), "ไม่พบสถิติการรับพัสดุ", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        } else if (pref.getString("Role", "NoData").equals("user")) {
            selectYear.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    thisYear.clear();
                    Jan = 0;
                    Feb = 0;
                    Mar = 0;
                    Apr = 0;
                    May = 0;
                    Jun = 0;
                    Jul = 0;
                    Aug = 0;
                    Sep = 0;
                    Oct = 0;
                    Nov = 0;
                    Dec = 0;
                    String year = selectYear.getText().toString();
                    yearSelect.setText(year);
                    progressDialog = new ProgressDialog(getContext());
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setMessage("กรุณารอสักครู่");
                    progressDialog.show();
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
                                                    String userSurname = task1.getResult().getString("lastname");

                                                    db.collection("parcels")
                                                            .whereNotEqualTo("checkOut", null)
                                                            .get()
                                                            .addOnCompleteListener(task2 -> {
                                                                if (task2.isSuccessful() && !task2.getResult().isEmpty()) {
                                                                    int checkData = 0;
                                                                    for (QueryDocumentSnapshot getDocParcels : task2.getResult()) {
                                                                        Map<String, Object> ownerMap = getDocParcels.getData();
                                                                        for (Map.Entry<String, Object> entry : ownerMap.entrySet()) {
                                                                            if (entry.getKey().equals("owner")) {
                                                                                Map<String, Object> ownerData = (Map<String, Object>) entry.getValue();
                                                                                for (Map.Entry<String, Object> NameDataOwner : ownerData.entrySet()) {
                                                                                    if (NameDataOwner.getKey().equals("lastname") && NameDataOwner.getValue().equals(userSurname)) {
                                                                                        checkData++;
                                                                                    }
                                                                                    if (NameDataOwner.getKey().equals("firstname") && NameDataOwner.getValue().equals(userName)) {
                                                                                        checkData++;
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                        if (checkData == 2) {
                                                                            checkData = 0;
                                                                            String docIdTarget = getDocParcels.getId();
                                                                            db.collection("parcels").document(docIdTarget)
                                                                                    .get()
                                                                                    .addOnCompleteListener(task3 -> {
                                                                                        if (task3.getResult().exists()) {
                                                                                            String getCheckOut = task3.getResult().get("checkOut").toString();
                                                                                            String[] splitCheckOut = getCheckOut.split(" ");
                                                                                            if (splitCheckOut.length == 6) {
                                                                                                String getYear = String.valueOf(Integer.parseInt(splitCheckOut[5]) + 543);
                                                                                                if (getYear.equals(year)) {
                                                                                                    thisYear.add(getYear);
                                                                                                    String getMonth = splitCheckOut[1];
                                                                                                    switch (getMonth) {
                                                                                                        case "Jan":
                                                                                                            Jan++;
                                                                                                            break;
                                                                                                        case "Feb":
                                                                                                            Feb++;
                                                                                                            break;
                                                                                                        case "Mar":
                                                                                                            Mar++;
                                                                                                            break;
                                                                                                        case "Apr":
                                                                                                            Apr++;
                                                                                                        case "May":
                                                                                                            May++;
                                                                                                            break;
                                                                                                        case "Jun":
                                                                                                            Jun++;
                                                                                                            break;
                                                                                                        case "Jul":
                                                                                                            Jul++;
                                                                                                            break;
                                                                                                        case "Aug":
                                                                                                            Aug++;
                                                                                                            break;
                                                                                                        case "Sep":
                                                                                                            Sep++;
                                                                                                            break;
                                                                                                        case "Oct":
                                                                                                            Oct++;
                                                                                                            break;
                                                                                                        case "Nov":
                                                                                                            Nov++;
                                                                                                            break;
                                                                                                        case "Dec":
                                                                                                            Dec++;
                                                                                                            break;
                                                                                                    }
                                                                                                }
                                                                                            } else if (splitCheckOut.length == 7) {
                                                                                                String getYear = String.valueOf(Integer.parseInt(splitCheckOut[3]) + 543);
                                                                                                if (getYear.equals(year)) {
                                                                                                    thisYear.add(getYear);
                                                                                                    String getMonth = splitCheckOut[1];
                                                                                                    switch (getMonth) {
                                                                                                        case "Jan":
                                                                                                            Jan++;
                                                                                                            break;
                                                                                                        case "Feb":
                                                                                                            Feb++;
                                                                                                            break;
                                                                                                        case "Mar":
                                                                                                            Mar++;
                                                                                                            break;
                                                                                                        case "Apr":
                                                                                                            Apr++;
                                                                                                        case "May":
                                                                                                            May++;
                                                                                                            break;
                                                                                                        case "Jun":
                                                                                                            Jun++;
                                                                                                            break;
                                                                                                        case "Jul":
                                                                                                            Jul++;
                                                                                                            break;
                                                                                                        case "Aug":
                                                                                                            Aug++;
                                                                                                            break;
                                                                                                        case "Sep":
                                                                                                            Sep++;
                                                                                                            break;
                                                                                                        case "Oct":
                                                                                                            Oct++;
                                                                                                            break;
                                                                                                        case "Nov":
                                                                                                            Nov++;
                                                                                                            break;
                                                                                                        case "Dec":
                                                                                                            Dec++;
                                                                                                            break;
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                            totalParcels.setText(String.valueOf(thisYear.size()));
                                                                                            averageParcels.setText(String.valueOf(thisYear.size() / 12));

                                                                                            ArrayList<Integer> getMonth = new ArrayList<>();
                                                                                            getMonth.add(Jan);
                                                                                            getMonth.add(Feb);
                                                                                            getMonth.add(Mar);
                                                                                            getMonth.add(Apr);
                                                                                            getMonth.add(May);
                                                                                            getMonth.add(Jun);
                                                                                            getMonth.add(Jul);
                                                                                            getMonth.add(Aug);
                                                                                            getMonth.add(Sep);
                                                                                            getMonth.add(Oct);
                                                                                            getMonth.add(Nov);
                                                                                            getMonth.add(Dec);

                                                                                            int maxLoop = 0;
                                                                                            int maxIndex = 0;
                                                                                            int max = -1;
                                                                                            for (Integer month : getMonth) {
                                                                                                if (month > max) {
                                                                                                    max = month;
                                                                                                    maxIndex = maxLoop;
                                                                                                }
                                                                                                maxLoop++;
                                                                                                switch (maxIndex) {
                                                                                                    case 0:
                                                                                                        maximizeMonth.setText("ม.ค.");
                                                                                                        break;
                                                                                                    case 1:
                                                                                                        maximizeMonth.setText("ก.พ.");
                                                                                                        break;
                                                                                                    case 2:
                                                                                                        maximizeMonth.setText("มี.ค.");
                                                                                                        break;
                                                                                                    case 3:
                                                                                                        maximizeMonth.setText("เม.ย.");
                                                                                                        break;
                                                                                                    case 4:
                                                                                                        maximizeMonth.setText("พ.ค.");
                                                                                                        break;
                                                                                                    case 5:
                                                                                                        maximizeMonth.setText("มิ.ย.");
                                                                                                        break;
                                                                                                    case 6:
                                                                                                        maximizeMonth.setText("ก.ค.");
                                                                                                        break;
                                                                                                    case 7:
                                                                                                        maximizeMonth.setText("ส.ค.");
                                                                                                        break;
                                                                                                    case 8:
                                                                                                        maximizeMonth.setText("ก.ย.");
                                                                                                        break;
                                                                                                    case 9:
                                                                                                        maximizeMonth.setText("ต.ค.");
                                                                                                        break;
                                                                                                    case 10:
                                                                                                        maximizeMonth.setText("พ.ย.");
                                                                                                        break;
                                                                                                    case 11:
                                                                                                        maximizeMonth.setText("ธ.ค.");
                                                                                                        break;
                                                                                                }
                                                                                            }

                                                                                            int min = getMonth.get(0);
                                                                                            int minLoop = 0;
                                                                                            int minIndex = 0;
                                                                                            for (int i = 0; i < getMonth.size(); i++) {
                                                                                                if (getMonth.get(i) < min) {
                                                                                                    minIndex = minLoop;
                                                                                                }
                                                                                                minLoop++;
                                                                                                switch (minIndex) {
                                                                                                    case 0:
                                                                                                        minimizeMonth.setText("ม.ค.");
                                                                                                        break;
                                                                                                    case 1:
                                                                                                        minimizeMonth.setText("ก.พ.");
                                                                                                        break;
                                                                                                    case 2:
                                                                                                        minimizeMonth.setText("มี.ค.");
                                                                                                        break;
                                                                                                    case 3:
                                                                                                        minimizeMonth.setText("เม.ย.");
                                                                                                        break;
                                                                                                    case 4:
                                                                                                        minimizeMonth.setText("พ.ค.");
                                                                                                        break;
                                                                                                    case 5:
                                                                                                        minimizeMonth.setText("มิ.ย.");
                                                                                                        break;
                                                                                                    case 6:
                                                                                                        minimizeMonth.setText("ก.ค.");
                                                                                                        break;
                                                                                                    case 7:
                                                                                                        minimizeMonth.setText("ส.ค.");
                                                                                                        break;
                                                                                                    case 8:
                                                                                                        minimizeMonth.setText("ก.ย.");
                                                                                                        break;
                                                                                                    case 9:
                                                                                                        minimizeMonth.setText("ต.ค.");
                                                                                                        break;
                                                                                                    case 10:
                                                                                                        minimizeMonth.setText("พ.ย.");
                                                                                                        break;
                                                                                                    case 11:
                                                                                                        minimizeMonth.setText("ธ.ค.");
                                                                                                        break;
                                                                                                }
                                                                                            }
                                                                                            Collections.sort(getMonth, Collections.reverseOrder());
                                                                                            minimizeParcels.setText(String.valueOf(getMonth.get(11)));
                                                                                            maximizeParcels.setText(String.valueOf(getMonth.get(0)));

                                                                                            XAxis xAxis = barChart.getXAxis();
                                                                                            ArrayList<String> month = new ArrayList<>();
                                                                                            month.add("มกราคม");
                                                                                            month.add("กุมภาพันธ์");
                                                                                            month.add("มีนาคม");
                                                                                            month.add("เมษายน");
                                                                                            month.add("พฤษษาคม");
                                                                                            month.add("มิถุนายน");
                                                                                            month.add("กรกฎาคม");
                                                                                            month.add("สิงหาคม");
                                                                                            month.add("กันยายน");
                                                                                            month.add("ตุลาคม");
                                                                                            month.add("พฤศจิกายน");
                                                                                            month.add("ธันวาคม");

                                                                                            ArrayList<BarEntry> barEntries = new ArrayList<>();
                                                                                            barEntries.add(new BarEntry(0, Jan));
                                                                                            barEntries.add(new BarEntry(1, Feb));
                                                                                            barEntries.add(new BarEntry(2, Mar));
                                                                                            barEntries.add(new BarEntry(3, Apr));
                                                                                            barEntries.add(new BarEntry(4, May));
                                                                                            barEntries.add(new BarEntry(5, Jun));
                                                                                            barEntries.add(new BarEntry(6, Jul));
                                                                                            barEntries.add(new BarEntry(7, Aug));
                                                                                            barEntries.add(new BarEntry(8, Sep));
                                                                                            barEntries.add(new BarEntry(9, Oct));
                                                                                            barEntries.add(new BarEntry(10, Nov));
                                                                                            barEntries.add(new BarEntry(11, Dec));

                                                                                            BarDataSet barDataSet = new BarDataSet(barEntries, "จำนวนพัสดุ");
                                                                                            barDataSet.setColors(ColorTemplate.rgb("3700B3"));
                                                                                            barDataSet.setValueTextColor(Color.BLACK);
                                                                                            barDataSet.setValueTextSize(16f);
                                                                                            barDataSet.setValueFormatter(new MyValueFormatter());

                                                                                            BarData barData = new BarData(barDataSet);

                                                                                            barChart.setFitBars(true);
                                                                                            barChart.setData(barData);
                                                                                            barChart.animateY(2000);
                                                                                            barChart.invalidate();

                                                                                            xAxis.setValueFormatter(new IndexAxisValueFormatter(month));
                                                                                            xAxis.setPosition(XAxis.XAxisPosition.TOP);
                                                                                            xAxis.setDrawGridLines(false);
                                                                                            xAxis.setDrawAxisLine(false);
                                                                                            xAxis.setGranularity(1f);
                                                                                            xAxis.setLabelCount(month.size());
                                                                                            xAxis.setLabelRotationAngle(270);

                                                                                            progressDialog.dismiss();
                                                                                        } else {
                                                                                            progressDialog.dismiss();
                                                                                            Toast.makeText(getContext(), "ไม่พบสถิติการรับพัสดุ", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    });
                                                                        } else {
                                                                            progressDialog.dismiss();
                                                                            Toast.makeText(getContext(), "ไม่พบสถิติการรับพัสดุ", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                } else if (task2.getResult().isEmpty()) {
                                                                    progressDialog.dismiss();
                                                                    totalParcels.setText("-");
                                                                    averageParcels.setText("-");
                                                                    minimizeParcels.setText("-");
                                                                    maximizeParcels.setText("-");
                                                                    minimizeMonth.setText("-");
                                                                    maximizeMonth.setText("-");
                                                                    Toast.makeText(getContext(), "ไม่พบสถิติการรับพัสดุ", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                } else if (!task1.getResult().exists()) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(getContext(), "กรุณาลองใหม่ในภายหลัง", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else if (task.getResult().isEmpty()) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getContext(), "กรุณาลองใหม่ในภายหลัง", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }
    }

    public class MyValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return Math.round(value) + "";
        }
    }
}