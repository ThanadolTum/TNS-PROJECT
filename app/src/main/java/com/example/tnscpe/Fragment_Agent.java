package com.example.tnscpe;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Fragment_Agent extends Fragment {

    private AutoCompleteTextView selectReceiver;
    private TextView addReceiver, countOfReceiver;
    private ListView listViewReceiver;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private HashSet<String> arrayListReceiverName = new HashSet<>();
    private ArrayList<AgentModel> agentModels = new ArrayList<>();
    private ReceiverAdapter receiverAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    public Fragment_Agent() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        getAgent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__agent, container, false);
        selectReceiver = view.findViewById(R.id.search_agent);
        addReceiver = view.findViewById(R.id.button_add_receiver);
        countOfReceiver = view.findViewById(R.id.count_of_receiver);
        listViewReceiver = view.findViewById(R.id.list_receiver);
        swipeRefreshLayout = view.findViewById(R.id.layout_refresh_add_agent);
        receiverAdapter = new ReceiverAdapter(getContext(), agentModels);
        listViewReceiver.setAdapter(receiverAdapter);
        receiverAdapter.notifyDataSetChanged();
        getAllNameUser();
        swipeRefreshLayout.setOnRefreshListener(() -> {
            agentModels.clear();
            getAllNameUser();
            getAgent();
            swipeRefreshLayout.setRefreshing(false);
        });

        addReceiver.setOnClickListener(v -> {
            if (selectReceiver.getText().toString().isEmpty()) {
                selectReceiver.setError("กรุณาเลือกตัวแทนรับพัสดุของคุณ");
            } else {
                String receiverName = selectReceiver.getText().toString();
                String[] nameReceiver = receiverName.split(" ");
                new AlertDialog.Builder(getContext())
                        .setTitle("เพิ่มผู้รับ")
                        .setMessage("คุณต้องการเพิ่ม" + " " + receiverName + " " + "เป็นตัวแทนผู้รับพัสดุใช่หรือไม่")
                        .setPositiveButton("ยืนยัน", (dialog, which) -> {
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

                                                            db.collection("messages")
                                                                    .whereEqualTo("type", 1)
                                                                    .whereEqualTo("receiver.firstname", nameReceiver[0])
                                                                    .whereEqualTo("receiver.lastname", nameReceiver[1])
                                                                    .get()
                                                                    .addOnCompleteListener(task2 -> {
                                                                        if (task2.isSuccessful() && !task2.getResult().isEmpty()) {
                                                                            Toast.makeText(getContext(), "ท่านส่งคำขอเพิ่มผู้แทนพัสดุแล้ว กรุณาลองใหม่อีกครั้ง", Toast.LENGTH_LONG).show();
                                                                        } else {
                                                                            String userName = task1.getResult().getString("firstname");
                                                                            String userSurname = task1.getResult().getString("lastname");
                                                                            ArrayList<String> allNameUser = new ArrayList<>(arrayListReceiverName);
                                                                            if (allNameUser.indexOf(receiverName) < 0) {
                                                                                Toast.makeText(getContext(), "ไม่พบชื่อผู้ใช้งานที่ท่านเลือก กรุณาลองใหม่อีกครั้ง", Toast.LENGTH_LONG).show();
                                                                            } else if (task1.getResult().get("agent.agent0.firstname").equals(nameReceiver[0]) && task1.getResult().get("agent.agent0.lastname").equals(nameReceiver[1])) {
                                                                                Toast.makeText(getContext(), "ท่านเพิ่มตัวแทนรับพัสดุคนนี้ไปแล้ว กรุณาลองใหม่อีกครั้ง", Toast.LENGTH_LONG).show();
                                                                            } else if (task1.getResult().get("agent.agent1.firstname").equals(nameReceiver[0]) && task1.getResult().get("agent.agent1.lastname").equals(nameReceiver[1])) {
                                                                                Toast.makeText(getContext(), "ท่านเพิ่มตัวแทนรับพัสดุคนนี้ไปแล้ว กรุณาลองใหม่อีกครั้ง", Toast.LENGTH_LONG).show();
                                                                            } else if (task1.getResult().get("agent.agent2.firstname").equals(nameReceiver[0]) && task1.getResult().get("agent.agent2.lastname").equals(nameReceiver[1])) {
                                                                                Toast.makeText(getContext(), "ท่านเพิ่มตัวแทนรับพัสดุคนนี้ไปแล้ว กรุณาลองใหม่อีกครั้ง", Toast.LENGTH_LONG).show();
                                                                            } else {
                                                                                if (task1.getResult().get("agent.agent0.firstname") == "" && task1.getResult().get("agent.agent0.lastname") == "") {
                                                                                    if (userName.equals(nameReceiver[0]) && userSurname.equals(nameReceiver[1])) {
                                                                                        Toast.makeText(getContext(), "ไม่สามารถเพิ่มตัวแทนที่ท่านเลือกได้ กรุณาลองใหม่อีกครั้ง", Toast.LENGTH_LONG).show();
                                                                                    } else {
                                                                                        progressDialog = new ProgressDialog(getContext());
                                                                                        progressDialog.setCanceledOnTouchOutside(false);
                                                                                        progressDialog.setMessage("กรุณารอสักครู่...");
                                                                                        progressDialog.show();
                                                                                        Date date = Calendar.getInstance().getTime();
                                                                                        String getDate = date.toString();

                                                                                        Map<String, Object> messageTypeOne = new HashMap<>();
                                                                                        messageTypeOne.put("data", null);
                                                                                        messageTypeOne.put("dateTime", getDate);
                                                                                        messageTypeOne.put("read", false);
                                                                                        messageTypeOne.put("receiver", null);
                                                                                        messageTypeOne.put("sender", null);
                                                                                        messageTypeOne.put("type", 1);

                                                                                        Map<String, Object> dataMap = new HashMap<>();
                                                                                        dataMap.put("receiver_parcel", null);
                                                                                        dataMap.put("owner", null);
                                                                                        dataMap.put("trackNumber", "");
                                                                                        messageTypeOne.put("data", dataMap);

                                                                                        Map<String, Object> receiverParcelsMap = new HashMap<>();
                                                                                        receiverParcelsMap.put("firstname", "");
                                                                                        receiverParcelsMap.put("lastname", "");
                                                                                        dataMap.put("receiver_parcel", receiverParcelsMap);

                                                                                        Map<String, Object> ownerMap = new HashMap<>();
                                                                                        ownerMap.put("firstname", "");
                                                                                        ownerMap.put("lastname", "");
                                                                                        dataMap.put("owner", ownerMap);

                                                                                        Map<String, Object> receiverMap = new HashMap<>();
                                                                                        receiverMap.put("firstname", nameReceiver[0]);
                                                                                        receiverMap.put("lastname", nameReceiver[1]);
                                                                                        messageTypeOne.put("receiver", receiverMap);

                                                                                        Map<String, Object> senderMap = new HashMap<>();
                                                                                        senderMap.put("firstname", userName);
                                                                                        senderMap.put("lastname", userSurname);
                                                                                        messageTypeOne.put("sender", senderMap);

                                                                                        db.collection("messages")
                                                                                                .add(messageTypeOne)
                                                                                                .addOnSuccessListener(documentReference -> progressDialog.dismiss())
                                                                                                .addOnFailureListener(e -> Log.d("Add messages", e.getMessage()));
                                                                                        Toast.makeText(getContext(), "เพิ่มตัวแทนรับพัสดุสำเร็จ รอการตอบรับ", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                } else if (task1.getResult().get("agent.agent1.firstname") == "" && task1.getResult().get("agent.agent1.lastname") == "") {
                                                                                    if (userName.equals(nameReceiver[0]) && userSurname.equals(nameReceiver[1])) {
                                                                                        Toast.makeText(getContext(), "ไม่สามารถเพิ่มตัวแทนที่ท่านเลือกได้ กรุณาลองใหม่อีกครั้ง", Toast.LENGTH_LONG).show();
                                                                                    } else {
                                                                                        progressDialog = new ProgressDialog(getContext());
                                                                                        progressDialog.setCanceledOnTouchOutside(false);
                                                                                        progressDialog.setMessage("กรุณารอสักครู่...");
                                                                                        progressDialog.show();
                                                                                        Date date = Calendar.getInstance().getTime();
                                                                                        String getDate = date.toString();

                                                                                        Map<String, Object> messageTypeOne = new HashMap<>();
                                                                                        messageTypeOne.put("data", null);
                                                                                        messageTypeOne.put("dateTime", getDate);
                                                                                        messageTypeOne.put("read", false);
                                                                                        messageTypeOne.put("receiver", null);
                                                                                        messageTypeOne.put("sender", null);
                                                                                        messageTypeOne.put("type", 1);

                                                                                        Map<String, Object> dataMap = new HashMap<>();
                                                                                        dataMap.put("receiver_parcel", null);
                                                                                        dataMap.put("trackNumber", "");
                                                                                        messageTypeOne.put("data", dataMap);

                                                                                        Map<String, Object> receiverParcelsMap = new HashMap<>();
                                                                                        receiverParcelsMap.put("firstname", "");
                                                                                        receiverParcelsMap.put("lastname", "");
                                                                                        dataMap.put("receiver_parcel", receiverParcelsMap);

                                                                                        Map<String, Object> receiverMap = new HashMap<>();
                                                                                        receiverMap.put("firstname", nameReceiver[0]);
                                                                                        receiverMap.put("lastname", nameReceiver[1]);
                                                                                        messageTypeOne.put("receiver", receiverMap);

                                                                                        Map<String, Object> senderMap = new HashMap<>();
                                                                                        senderMap.put("firstname", userName);
                                                                                        senderMap.put("lastname", userSurname);
                                                                                        messageTypeOne.put("sender", senderMap);

                                                                                        db.collection("messages")
                                                                                                .add(messageTypeOne)
                                                                                                .addOnSuccessListener(documentReference -> progressDialog.dismiss())
                                                                                                .addOnFailureListener(e -> Log.d("Add messages", e.getMessage()));
                                                                                        Toast.makeText(getContext(), "เพิ่มตัวแทนรับพัสดุสำเร็จ รอการตอบรับ", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                } else if (task1.getResult().get("agent.agent2.firstname") == "" && task1.getResult().get("agent.agent2.lastname") == "") {
                                                                                    if (userName.equals(nameReceiver[0]) && userSurname.equals(nameReceiver[1])) {
                                                                                        Toast.makeText(getContext(), "ไม่สามารถเพิ่มตัวแทนที่ท่านเลือกได้ กรุณาลองใหม่อีกครั้ง", Toast.LENGTH_LONG).show();
                                                                                    } else {
                                                                                        progressDialog = new ProgressDialog(getContext());
                                                                                        progressDialog.setCanceledOnTouchOutside(false);
                                                                                        progressDialog.setMessage("กรุณารอสักครู่...");
                                                                                        progressDialog.show();
                                                                                        Date date = Calendar.getInstance().getTime();
                                                                                        String getDate = date.toString();

                                                                                        Map<String, Object> messageTypeOne = new HashMap<>();
                                                                                        messageTypeOne.put("data", null);
                                                                                        messageTypeOne.put("dateTime", getDate);
                                                                                        messageTypeOne.put("read", false);
                                                                                        messageTypeOne.put("receiver", null);
                                                                                        messageTypeOne.put("sender", null);
                                                                                        messageTypeOne.put("type", 1);

                                                                                        Map<String, Object> dataMap = new HashMap<>();
                                                                                        dataMap.put("receiver_parcel", null);
                                                                                        dataMap.put("trackNumber", "");
                                                                                        messageTypeOne.put("data", dataMap);

                                                                                        Map<String, Object> receiverParcelsMap = new HashMap<>();
                                                                                        receiverParcelsMap.put("firstname", "");
                                                                                        receiverParcelsMap.put("lastname", "");
                                                                                        dataMap.put("receiver_parcel", receiverParcelsMap);

                                                                                        Map<String, Object> receiverMap = new HashMap<>();
                                                                                        receiverMap.put("firstname", nameReceiver[0]);
                                                                                        receiverMap.put("lastname", nameReceiver[1]);
                                                                                        messageTypeOne.put("receiver", receiverMap);

                                                                                        Map<String, Object> senderMap = new HashMap<>();
                                                                                        senderMap.put("firstname", userName);
                                                                                        senderMap.put("lastname", userSurname);
                                                                                        messageTypeOne.put("sender", senderMap);

                                                                                        db.collection("messages")
                                                                                                .add(messageTypeOne)
                                                                                                .addOnSuccessListener(documentReference -> progressDialog.dismiss())
                                                                                                .addOnFailureListener(e -> Log.d("Add messages", e.getMessage()));
                                                                                    }
                                                                                } else {
                                                                                    Toast.makeText(getContext(), "คุณเพิ่มตัวแทนรับพัสดุครบจำนวนที่กำหนดแล้ว", Toast.LENGTH_LONG).show();
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }
                                    });
                        })
                        .setNegativeButton("ยกเลิก", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
        });
        return view;
    }

    public void getAllNameUser() {
        arrayListReceiverName.clear();
        db.collection("users")
                .whereNotEqualTo("role", "admin")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot nameUser : task.getResult()) {
                            if (!nameUser.getString("email").equals(firebaseUser.getEmail())) {
                                arrayListReceiverName.add(nameUser.get("firstname").toString() + " " + nameUser.get("lastname"));
                            }
                        }
                        ArrayList<String> arrayListReceiver = new ArrayList<>(arrayListReceiverName);
                        ArrayAdapter<String> adapterReceiver = new ArrayAdapter<>(getContext(), R.layout.department_item, arrayListReceiver);
                        selectReceiver.setAdapter(adapterReceiver);
                    }
                });
    }

    public void getAgent() {
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
                                        if (task1.getResult().get("agent.agent0.firstname") != "" && task1.getResult().get("agent.agent0.lastname") != null) {
                                            AgentModel agentModel = new AgentModel();
                                            agentModel.setFirstname(task1.getResult().get("agent.agent0.firstname").toString());
                                            agentModel.setLastname(task1.getResult().get("agent.agent0.lastname").toString());
                                            if (task1.getResult().get("agent.agent0.status_receiver").equals(true)) {
                                                agentModel.setStatus_receiver(true);
                                            } else {
                                                agentModel.setStatus_receiver(false);
                                            }
                                            agentModels.add(agentModel);
                                            receiverAdapter.notifyDataSetChanged();
                                        }

                                        if (task1.getResult().get("agent.agent1.firstname") != "" && task1.getResult().get("agent.agent1.lastname") != null) {
                                            AgentModel agentModel = new AgentModel();
                                            agentModel.setFirstname(task1.getResult().get("agent.agent1.firstname").toString());
                                            agentModel.setLastname(task1.getResult().get("agent.agent1.lastname").toString());
                                            if (task1.getResult().get("agent.agent1.status_receiver").equals(true)) {
                                                agentModel.setStatus_receiver(true);
                                            } else {
                                                agentModel.setStatus_receiver(false);
                                            }
                                            agentModels.add(agentModel);
                                            receiverAdapter.notifyDataSetChanged();
                                        }

                                        if (task1.getResult().get("agent.agent2.firstname") != "" && task1.getResult().get("agent.agent2.lastname") != null) {
                                            AgentModel agentModel = new AgentModel();
                                            agentModel.setFirstname(task1.getResult().get("agent.agent2.firstname").toString());
                                            agentModel.setLastname(task1.getResult().get("agent.agent2.lastname").toString());
                                            if (task1.getResult().get("agent.agent2.status_receiver").equals(true)) {
                                                agentModel.setStatus_receiver(true);
                                            } else {
                                                agentModel.setStatus_receiver(false);
                                            }
                                            agentModels.add(agentModel);
                                            receiverAdapter.notifyDataSetChanged();
                                        }
                                        countOfReceiver.setText(String.valueOf(agentModels.size()));
                                    }
                                });
                    }
                });
    }
}