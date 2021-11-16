package com.example.tnscpe;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class Fragment_Notification extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView emptyMessages, textViewNotification;
    private SharedPreferences pref;
    private ArrayList<MessagesModel> messagesModelArrayList;
    private MessagesAdapter messagesAdapter;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = mAuth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__notification, container, false);
        pref = getContext().getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        swipeRefreshLayout = view.findViewById(R.id.layout_refresh_notification);
        recyclerView = view.findViewById(R.id.recycler_view_notification);
        emptyMessages = view.findViewById(R.id.on_empty_messags);
        textViewNotification = view.findViewById(R.id.text_notification);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messagesModelArrayList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(getContext(), messagesModelArrayList);
        recyclerView.setAdapter(messagesAdapter);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            messagesModelArrayList.clear();
            getNotification();
            swipeRefreshLayout.setRefreshing(false);
        });
        getNotification();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    protected void getNotification() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("กำลังดึงข้อมูล");
        progressDialog.show();

        db.collection("users")
                .whereEqualTo("email", firebaseUser.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot getDocIdUser = task.getResult().getDocuments().get(0);
                        String docIdUser = getDocIdUser.getId();

                        db.collection("users").document(docIdUser)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        String userName = task1.getResult().getString("firstname");
                                        String userSurname = task1.getResult().getString("lastname");

                                        db.collection("messages")
                                                .whereEqualTo("receiver.firstname", userName)
                                                .whereEqualTo("receiver.lastname", userSurname)
                                                .get()
                                                .addOnCompleteListener(task2 -> {
                                                    if (task2.isSuccessful() && task2.getResult() != null) {
                                                        ArrayList<String> docIdMessage = new ArrayList<>();
                                                        for (QueryDocumentSnapshot getDocIdMessage : task2.getResult()) {
                                                            docIdMessage.add(getDocIdMessage.getId());
                                                        }

                                                        if (!docIdMessage.isEmpty()) {
                                                            emptyMessages.setVisibility(View.GONE);
                                                            for (String docId : docIdMessage) {
                                                                db.collection("messages").document(docId)
                                                                        .addSnapshotListener((value, error) -> {
                                                                            if (error != null) {
                                                                                if (progressDialog.isShowing()) {
                                                                                    progressDialog.dismiss();
                                                                                }
                                                                                Log.d("Find Message", error.getMessage());
                                                                            }

                                                                            if (value.exists()) {
                                                                                messagesModelArrayList.add(value.toObject(MessagesModel.class));
                                                                            }
                                                                            messagesAdapter.notifyDataSetChanged();
                                                                            progressDialog.dismiss();
                                                                        });

                                                                db.collection("messages").document(docId)
                                                                        .get()
                                                                        .addOnCompleteListener(task3 -> {
                                                                           if (task3.isSuccessful()) {
                                                                               if (task3.getResult().get("read").equals(false)) {
                                                                                   db.collection("messages").document(docId)
                                                                                           .update("read",true)
                                                                                           .addOnSuccessListener(unused -> {Log.d("Update status read",task3.getResult().getId());});
                                                                               }
                                                                           }
                                                                        });
                                                            }
                                                        } else {
                                                            emptyMessages.setVisibility(View.VISIBLE);
                                                            textViewNotification.setVisibility(View.GONE);
                                                            progressDialog.dismiss();
                                                        }
                                                    } else {
                                                        Toast.makeText(getContext(), "ไม่มีการแจ้งเตือนถึงคุณ", Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

}