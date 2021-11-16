package com.example.tnscpe;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ReceiverAdapter extends BaseAdapter {

    Context context;
    ArrayList<AgentModel> agentModels;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ProgressDialog progressDialog;

    public ReceiverAdapter(Context context, ArrayList<AgentModel> agentModels) {
        this.context = context;
        this.agentModels = agentModels;
    }

    @Override
    public int getCount() {
        return agentModels.size();
    }

    @Override
    public Object getItem(int position) {
        return agentModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.receiver_list, parent, false);
        AgentModel agentModel = agentModels.get(position);
        TextView receiverName = view.findViewById(R.id.receiver_name);
        Switch switchNotification = view.findViewById(R.id.switch_notification);
        ImageView deleteReceiver = view.findViewById(R.id.delete_receiver);
        TextView displayName = view.findViewById(R.id.button_display_name);

        receiverName.setText(agentModel.getFirstname() + " " + agentModel.getLastname());
        String subName = agentModel.getFirstname().substring(0, 1);
        String subSurname = agentModel.getLastname().substring(0, 1);
        displayName.setText(subName + subSurname);

        if (agentModel.getStatus_receiver() != null) {
            if (agentModel.getStatus_receiver().equals(true)) {
                switchNotification.setChecked(true);
            } else {
                switchNotification.setChecked(false);
            }
        }

        switchNotification.setOnClickListener(v -> {
            if (switchNotification.isChecked()) {
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
                                                if (task1.getResult().get("agent.agent0.firstname").equals(agentModel.getFirstname()) && task1.getResult().get("agent.agent0.lastname").equals(agentModel.getLastname())) {
                                                    if (task1.getResult().get("agent.agent0.status_receiver").equals(false)) {
                                                        db.collection("users").document(docIdUser)
                                                                .update("agent.agent0.status_receiver",true)
                                                                .addOnCompleteListener(task2 -> {
                                                                   if (task2.isSuccessful()) {
                                                                       Log.d("Change status receiver",agentModel.getFirstname() + " " + agentModel.getLastname());
                                                                       switchNotification.setChecked(true);
                                                                       agentModel.setStatus_receiver(true);
                                                                       notifyDataSetChanged();
                                                                   }
                                                                });
                                                    }
                                                }
                                            }
                                        });
                            }
                        });
            } else {
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
                                                if (task1.getResult().get("agent.agent0.firstname").equals(agentModel.getFirstname()) && task1.getResult().get("agent.agent0.lastname").equals(agentModel.getLastname())) {
                                                    if (task1.getResult().get("agent.agent0.status_receiver").equals(true)) {
                                                        db.collection("users").document(docIdUser)
                                                                .update("agent.agent0.status_receiver",false)
                                                                .addOnCompleteListener(task2 -> {
                                                                    if (task2.isSuccessful()) {
                                                                        Log.d("Change status receiver",agentModel.getFirstname() + " " + agentModel.getLastname());
                                                                        switchNotification.setChecked(false);
                                                                        agentModel.setStatus_receiver(false);
                                                                        notifyDataSetChanged();
                                                                    }
                                                                });
                                                    }
                                                }
                                            }
                                        });
                            }
                        });
            }
        });

        deleteReceiver.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("ลบตัวแทนรับพัสดุ")
                    .setMessage("แน่ใจหรือไม่ที่จะลบตัวแทนพัสดุท่านนี้")
                    .setPositiveButton("ยืนยัน", ((dialog, which) -> {
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
                                                        progressDialog = new ProgressDialog(context);
                                                        progressDialog.setCanceledOnTouchOutside(false);
                                                        progressDialog.setMessage("กรุณารอสักครู่...");
                                                        progressDialog.show();
                                                        if (task1.getResult().get("agent.agent0.firstname").equals(agentModel.getFirstname()) && task1.getResult().get("agent.agent0.lastname").equals(agentModel.getLastname())) {
                                                            db.collection("users").document(docId)
                                                                    .update("agent.agent0.firstname", "", "agent.agent0.lastname", "", "agent.agent0.status_receiver", false, "agent.agent0.email","")
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            agentModels.remove(agentModels.get(position));
                                                                            notifyDataSetChanged();
                                                                            Toast.makeText(context, "ลบตัวแทนพัสดุสำเร็จ", Toast.LENGTH_SHORT).show();
                                                                            Log.d("Delete agent", "สำเร็จ");
                                                                            progressDialog.dismiss();
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.d("Delete agent", e.getMessage());
                                                                            progressDialog.dismiss();
                                                                        }
                                                                    });
                                                        } else if (task1.getResult().get("agent.agent1.firstname").equals(agentModel.getFirstname()) && task1.getResult().get("agent.agent1.lastname").equals(agentModel.getLastname())) {
                                                            db.collection("users").document(docId)
                                                                    .update("agent.agent1.firstname", "", "agent.agent1.lastname", "", "agent.agent1.status_receiver", false, "agent.agent0.email","")
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            agentModels.remove(agentModels.get(position));
                                                                            notifyDataSetChanged();
                                                                            Toast.makeText(context, "ลบตัวแทนพัสดุสำเร็จ", Toast.LENGTH_SHORT).show();
                                                                            Log.d("Delete agent", "สำเร็จ");
                                                                            progressDialog.dismiss();
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.d("Delete agent", e.getMessage());
                                                                            progressDialog.dismiss();
                                                                        }
                                                                    });
                                                        } else if (task1.getResult().get("agent.agent2.firstname").equals(agentModel.getFirstname()) && task1.getResult().get("agent.agent2.lastname").equals(agentModel.getLastname())) {
                                                            db.collection("users").document(docId)
                                                                    .update("agent.agent2.firstname", "", "agent.agent2.lastname", "", "agent.agent2.status_receiver", false, "agent.agent0.email","")
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            agentModels.remove(agentModels.get(position));
                                                                            notifyDataSetChanged();
                                                                            Toast.makeText(context, "ลบตัวแทนพัสดุสำเร็จ", Toast.LENGTH_SHORT).show();
                                                                            Log.d("Delete agent", "สำเร็จ");
                                                                            progressDialog.dismiss();
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.d("Delete agent", e.getMessage());
                                                                            progressDialog.dismiss();
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }))
                    .setNegativeButton("ยกเลิก", ((dialog, which) -> {
                        dialog.dismiss();
                    }))
                    .show();
        });
        return view;
    }
}
