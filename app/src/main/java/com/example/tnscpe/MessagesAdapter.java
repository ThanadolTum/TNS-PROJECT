package com.example.tnscpe;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<MessagesModel> messagesModelArrayList;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userName;
    String userSurname;
    String userEmail;

    public MessagesAdapter(Context context, ArrayList<MessagesModel> messagesModelArrayList) {
        this.context = context;
        this.messagesModelArrayList = messagesModelArrayList;

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
                                        userName = task1.getResult().getString("firstname");
                                        userSurname = task1.getResult().getString("lastname");
                                        userEmail = task1.getResult().getString("email");
                                    }
                                });
                    }
                });
    }

    public static class ViewHolderOne extends RecyclerView.ViewHolder {

        private final Button displayName;
        private final Button buttonConfirmAgent;
        private final Button buttonCancelAgent;
        private final TextView senderName;
        private final TextView dateTime;

        public ViewHolderOne(@NonNull View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.button_display_name);
            buttonConfirmAgent = itemView.findViewById(R.id.button_confirm_agent);
            buttonCancelAgent = itemView.findViewById(R.id.button_cancel_agent);
            senderName = itemView.findViewById(R.id.sender_name);
            dateTime = itemView.findViewById(R.id.date_time);
        }
    }

    public static class ViewHolderTwo extends RecyclerView.ViewHolder {

        private final TextView trackNumber;
        private final TextView dateTime;

        public ViewHolderTwo(@NonNull View itemView) {
            super(itemView);
            trackNumber = itemView.findViewById(R.id.tracknumber);
            dateTime = itemView.findViewById(R.id.date_time);
        }
    }

    public static class ViewHolderThree extends RecyclerView.ViewHolder {

        private final TextView trackNumber;
        private final TextView ownerName;
        private final TextView receiverName;
        private final TextView dateTime;

        public ViewHolderThree(@NonNull View itemView) {
            super(itemView);
            trackNumber = itemView.findViewById(R.id.tracknumber);
            ownerName = itemView.findViewById(R.id.owner_name);
            receiverName = itemView.findViewById(R.id.receiver_name);
            dateTime = itemView.findViewById(R.id.date_time);
        }
    }

    @Override
    public int getItemViewType(int position) {
//        for (int i = 0; i < messagesModelArrayList.size(); i++) {
            if (messagesModelArrayList.get(position).getType() == 1) {
                return 1;
            } else if (messagesModelArrayList.get(position).getType() == 2) {
                return 2;
            }
//        }
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.notification_add_agent, parent, false);
            return new ViewHolderOne(view);
        } else if (viewType == 2) {
            view = LayoutInflater.from(context).inflate(R.layout.notification_check_in, parent, false);
            return new ViewHolderTwo(view);
        }
        view = LayoutInflater.from(context).inflate(R.layout.notification_get_parcels, parent, false);
        return new ViewHolderThree(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (messagesModelArrayList.get(position).getType().equals(0)) {
                ViewHolderThree viewHolderThree = (ViewHolderThree) holder;
                MessagesModel messagesModel = messagesModelArrayList.get(position);
                viewHolderThree.trackNumber.setText(messagesModel.getData().getTrackNumber());
                viewHolderThree.ownerName.setText(messagesModel.getData().getOwner().getFirstname()+" "+messagesModel.getData().getOwner().getLastname());
                viewHolderThree.receiverName.setText(messagesModel.getData().getReceiver_parcel().getFirstname() + " " + messagesModel.getData().getReceiver_parcel().getLastname());
                String getDateTime = messagesModel.getDateTime();
                if (getDateTime != null) {
                    String[] splitDate = getDateTime.split(" ");
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
                        String[] splitTime = splitDate[3].split(":");
                        String dateTime = String.format("%s %s %s %s:%s น.", date, month, year, splitTime[0], splitTime[1]);
                        viewHolderThree.dateTime.setText(dateTime);
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
                        String[] splitTime = splitDate[4].split(":");
                        String dateTime = String.format("%s %s %s %s:%s น.", date, month, year, splitTime[0], splitTime[1]);
                        viewHolderThree.dateTime.setText(dateTime);
                    }
                } else {
                    viewHolderThree.dateTime.setText("-");
                }
            }
        if (messagesModelArrayList.get(position).getType().equals(1)) {
            ViewHolderOne viewHolderOne = (ViewHolderOne) holder;
            MessagesModel messagesModel = messagesModelArrayList.get(position);
            viewHolderOne.senderName.setText(messagesModel.getSender().getFirstname() + " " + messagesModel.getSender().getLastname());
            String receiverName = messagesModel.getReceiver().getFirstname().substring(0,1);
            String receiverSurname = messagesModel.getReceiver().getLastname().substring(0,1);
            viewHolderOne.displayName.setText(receiverName+receiverSurname);
            String getDateTime = messagesModel.getDateTime();
            if (getDateTime != null) {
                String[] splitDate = getDateTime.split(" ");
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
                    String[] splitTime = splitDate[3].split(":");
                    String dateTime = String.format("%s %s %s %s:%s น.", date, month, year, splitTime[0], splitTime[1]);
                    viewHolderOne.dateTime.setText(dateTime);
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
                    String[] splitTime = splitDate[4].split(":");
                    String dateTime = String.format("%s %s %s %s:%s น.", date, month, year, splitTime[0], splitTime[1]);
                    viewHolderOne.dateTime.setText(dateTime);
                }
            } else {
                viewHolderOne.dateTime.setText("-");
            }

            db.collection("users")
                    .whereEqualTo("firstname", messagesModel.getSender().getFirstname())
                    .whereEqualTo("lastname", messagesModel.getSender().getLastname())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot getDocIdSender = task.getResult().getDocuments().get(0);
                            String docIdSender = getDocIdSender.getId();

                            db.collection("users").document(docIdSender)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            viewHolderOne.buttonConfirmAgent.setOnClickListener(v -> {
                                                if (task1.getResult().get("agent.agent0.firstname") == "" && task1.getResult().get("agent.agent0.lastname") == "") {
                                                    db.collection("users").document(docIdSender)
                                                            .update("agent.agent0.firstname", userName, "agent.agent0.lastname", userSurname, "agent.agent0.status_receiver", true , "agent.agent0.email", userEmail)
                                                            .addOnSuccessListener(unused -> {
                                                                Toast.makeText(context, "ตอบรับคำขอแล้ว", Toast.LENGTH_SHORT).show();
                                                                db.collection("messages")
                                                                        .whereEqualTo("type", 1)
                                                                        .whereEqualTo("receiver.firstname", userName)
                                                                        .whereEqualTo("receiver.lastname", userSurname)
                                                                        .get()
                                                                        .addOnCompleteListener(task2 -> {
                                                                            if (task2.isSuccessful() && task2.getResult() != null) {
                                                                                DocumentSnapshot getDocIdMessage = task2.getResult().getDocuments().get(0);
                                                                                String docIdMessage = getDocIdMessage.getId();

                                                                                db.collection("messages").document(docIdMessage)
                                                                                        .delete()
                                                                                        .addOnSuccessListener(unused1 -> {
                                                                                            Log.d("Delete messages", "ลบข้อความสำเร็จ");
                                                                                        })
                                                                                        .addOnFailureListener(e -> Log.d("Delete messages", e.getMessage()));
                                                                            } else {
                                                                                Log.d("Delete messages", "ไม่พบข้อความ");
                                                                            }
                                                                        });
                                                                messagesModelArrayList.remove(messagesModelArrayList.get(position));
                                                                notifyItemRemoved(position);
                                                                notifyItemRangeChanged(position,messagesModelArrayList.size());
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.d("Confirm agent", e.getMessage());
                                                            });

                                                } else if (task1.getResult().get("agent.agent1.firstname") == "" && task1.getResult().get("agent.agent1.lastname") == "") {
                                                    db.collection("users").document(docIdSender)
                                                            .update("agent.agent1.firstname", userName, "agent.agent1.lastname", userSurname, "agent.agent1.status_receiver", true, "agent.agent1.email", userEmail)
                                                            .addOnSuccessListener(unused -> {
                                                                Toast.makeText(context, "ตอบรับคำขอแล้ว", Toast.LENGTH_SHORT).show();
                                                                db.collection("messages")
                                                                        .whereEqualTo("type", 1)
                                                                        .whereEqualTo("receiver.firstname", userName)
                                                                        .whereEqualTo("receiver.lastname", userSurname)
                                                                        .get()
                                                                        .addOnCompleteListener(task2 -> {
                                                                            if (task2.isSuccessful() && task2.getResult() != null) {
                                                                                DocumentSnapshot getDocIdMessage = task2.getResult().getDocuments().get(0);
                                                                                String docIdMessage = getDocIdMessage.getId();

                                                                                db.collection("messages").document(docIdMessage)
                                                                                        .delete()
                                                                                        .addOnSuccessListener(unused1 -> {
                                                                                            Log.d("Delete messages", "ลบข้อความสำเร็จ");
                                                                                        })
                                                                                        .addOnFailureListener(e -> Log.d("Delete messages", e.getMessage()));
                                                                            } else {
                                                                                Log.d("Delete messages", "ไม่พบข้อความ");
                                                                            }
                                                                        });
                                                                messagesModelArrayList.remove(messagesModelArrayList.get(position));
                                                                notifyItemRemoved(position);
                                                                notifyItemRangeChanged(position,messagesModelArrayList.size());
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.d("Confirm agent", e.getMessage());
                                                            });

                                                } else if (task1.getResult().get("agent.agent2.firstname") == "" && task1.getResult().get("agent.agent2.lastname") == "") {
                                                    db.collection("users").document(docIdSender)
                                                            .update("agent.agent2.firstname", userName, "agent.agent2.lastname", userSurname, "agent.agent2.status_receiver", true, "agent.agent2.email",userEmail)
                                                            .addOnSuccessListener(unused -> {
                                                                Toast.makeText(context, "ตอบรับคำขอแล้ว", Toast.LENGTH_SHORT).show();
                                                                db.collection("messages")
                                                                        .whereEqualTo("type", 1)
                                                                        .whereEqualTo("receiver.firstname", userName)
                                                                        .whereEqualTo("receiver.lastname", userSurname)
                                                                        .get()
                                                                        .addOnCompleteListener(task2 -> {
                                                                            if (task2.isSuccessful() && task2.getResult() != null) {
                                                                                DocumentSnapshot getDocIdMessage = task2.getResult().getDocuments().get(0);
                                                                                String docIdMessage = getDocIdMessage.getId();

                                                                                db.collection("messages").document(docIdMessage)
                                                                                        .delete()
                                                                                        .addOnSuccessListener(unused1 -> {
                                                                                            Log.d("Delete messages", "ลบข้อความสำเร็จ");
                                                                                        })
                                                                                        .addOnFailureListener(e -> Log.d("Delete messages", e.getMessage()));
                                                                            } else {
                                                                                Log.d("Delete messages", "ไม่พบข้อความ");
                                                                            }
                                                                        });
                                                                messagesModelArrayList.remove(messagesModelArrayList.get(position));
                                                                notifyItemRemoved(position);
                                                                notifyItemRangeChanged(position,messagesModelArrayList.size());
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.d("Confirm agent", e.getMessage());
                                                            });

                                                } else {
                                                    Toast.makeText(context, "ไม่มีคำขอนี้แล้วหรือผู้ใช้งานเพิ่มตัวแทนครบแล้ว", Toast.LENGTH_SHORT).show();
                                                    messagesModelArrayList.remove(messagesModelArrayList.get(position));
                                                }
                                            });
                                        }
                                    });
                        }
                    });
            viewHolderOne.buttonCancelAgent.setOnClickListener(v -> {
                Toast.makeText(context, "ปฏิเสธคำขอแล้ว", Toast.LENGTH_SHORT).show();
                db.collection("messages")
                        .whereEqualTo("type", 1)
                        .whereEqualTo("receiver.firstname", userName)
                        .whereEqualTo("receiver.lastname", userSurname)
                        .get()
                        .addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful() && task2.getResult() != null) {
                                DocumentSnapshot getDocIdMessage = task2.getResult().getDocuments().get(0);
                                String docIdMessage = getDocIdMessage.getId();

                                db.collection("messages").document(docIdMessage)
                                        .delete()
                                        .addOnSuccessListener(unused1 -> {
                                            Log.d("Delete messages", "ลบข้อความสำเร็จ");
                                        })
                                        .addOnFailureListener(e -> Log.d("Delete messages", e.getMessage()));
                            } else {
                                Log.d("Delete messages", "ไม่พบข้อความ");
                            }
                        });
                messagesModelArrayList.remove(messagesModelArrayList.get(position));
                notifyItemRemoved(position);
                notifyItemRangeChanged(position,messagesModelArrayList.size());
            });
        }
        if (messagesModelArrayList.get(position).getType().equals(2)) {
            ViewHolderTwo viewHolderTwo = (ViewHolderTwo) holder;
            MessagesModel messagesModel = messagesModelArrayList.get(position);
            viewHolderTwo.trackNumber.setText(messagesModel.getData().getTrackNumber());
            String getDateTime = messagesModel.getDateTime();
            if (getDateTime != null) {
                String[] splitDate = getDateTime.split(" ");
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
                    String[] splitTime = splitDate[3].split(":");
                    String dateTime = String.format("%s %s %s %s:%s น.", date, month, year, splitTime[0], splitTime[1]);
                    viewHolderTwo.dateTime.setText(dateTime);
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
                    String[] splitTime = splitDate[4].split(":");
                    String dateTime = String.format("%s %s %s %s:%s น.", date, month, year, splitTime[0], splitTime[1]);
                    viewHolderTwo.dateTime.setText(dateTime);
                }
            } else {
                viewHolderTwo.dateTime.setText("-");
            }
        }
    }

    @Override
    public int getItemCount() {
        return messagesModelArrayList.size();
    }
}
