package com.example.tnscpe;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;

public class TrackNumberAdapter extends BaseAdapter {

    Context context;
    ArrayList<TrackNumberModel> trackNumberList;
    String userName;
    String userSurname;
    SharedPreferences pref;

    public String setUsername(String userName) {
        return this.userName = userName;
    }

    public String setUserSurname(String userSurname) {
        return this.userSurname = userSurname;
    }

    public TrackNumberAdapter(Context context, ArrayList<TrackNumberModel> trackNumberList) {
        this.context = context;
        this.trackNumberList = trackNumberList;
    }

    @Override
    public int getCount() {
        return trackNumberList.size();
    }

    @Override
    public Object getItem(int position) {
        return trackNumberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.track_number_item, parent, false);
        TrackNumberModel trackNumberModel = trackNumberList.get(position);
        TextView trackNumberView = view.findViewById(R.id.tracknumber);
        ImageView statusIconTrackNumber = view.findViewById(R.id.track_number_status_icon);
        ImageView addImageParcel = view.findViewById(R.id.add_image_parcel_icon);
        ImageView deleteTrackNumber = view.findViewById(R.id.delete_track_number_icon);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        pref = context.getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        if (pref.getString("Role", "NoData").equals("admin")) {
            db.collection("parcels")
                    .whereEqualTo("trackNumber", trackNumberModel.getTrackNumber())
                    .get()
                    .addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            int checkData = 0;
                            int checkDataIncorrect = 0;
                            for (QueryDocumentSnapshot getDocParcels : task2.getResult()) {
                                Map<String, Object> ownerMap = getDocParcels.getData();
                                for (Map.Entry<String, Object> entry : ownerMap.entrySet()) {
                                    if (entry.getKey().equals("owner")) {
                                        Map<String, Object> ownerData = (Map<String, Object>) entry.getValue();
                                        for (Map.Entry<String, Object> NameDataOwner : ownerData.entrySet()) {
                                            if (NameDataOwner.getKey().equals("lastname") && NameDataOwner.getValue().equals(userSurname)) {
                                                checkData++;
                                            } else if (NameDataOwner.getKey().equals("lastname") && !NameDataOwner.getValue().equals(userSurname)) {
                                                checkDataIncorrect++;
                                            }
                                            if (NameDataOwner.getKey().equals("firstname") && NameDataOwner.getValue().equals(userName)) {
                                                checkData++;
                                            } else if (NameDataOwner.getKey().equals("firstname") && !NameDataOwner.getValue().equals(userName)) {
                                                checkDataIncorrect++;
                                            }
                                        }
                                    }

                                    if (entry.getKey().equals("receiver")) {
                                        Map<String, Object> receiverData = (Map<String, Object>) entry.getValue();
                                        for (Map.Entry<String, Object> NameDataReceiver : receiverData.entrySet()) {
                                            if (NameDataReceiver.getKey().equals("lastname") && NameDataReceiver.getValue().equals(userSurname)) {
                                                checkData++;
                                            } else if (NameDataReceiver.getKey().equals("lastname") && !NameDataReceiver.getValue().equals(userSurname)) {
                                                checkDataIncorrect++;
                                            }
                                            if (NameDataReceiver.getKey().equals("firstname") && NameDataReceiver.getValue().equals(userName)) {
                                                checkData++;
                                            } else if (NameDataReceiver.getKey().equals("firstname") && !NameDataReceiver.getValue().equals(userName)) {
                                                checkDataIncorrect++;
                                            }
                                        }
                                    }
                                }
                            }

                            if (checkData >= 2) {
                                trackNumberView.setText(trackNumberModel.getTrackNumber());
                                statusIconTrackNumber.setImageResource(R.drawable.ic_baseline_check_circle_correct);
                                addImageParcel.setImageResource(R.drawable.icon_add_image);
                                deleteTrackNumber.setImageResource(R.drawable.ic_baseline_delete_24);
                                trackNumberModel.setRedTrackNumber(false);
                                trackNumberModel.setCorrectTrackNumber(true);
                            } else if (checkDataIncorrect == 4) {
                                trackNumberView.setText(trackNumberModel.getTrackNumber());
                                statusIconTrackNumber.setImageResource(R.drawable.icon_cancel);
                                addImageParcel.setImageResource(R.drawable.icon_add_image);
                                deleteTrackNumber.setImageResource(R.drawable.ic_baseline_delete_24);
                                trackNumberModel.setRedTrackNumber(true);
                            } else {
                                trackNumberView.setText(trackNumberModel.getTrackNumber());
                                statusIconTrackNumber.setImageResource(R.drawable.ic_action_warning_image);
                                addImageParcel.setImageResource(R.drawable.icon_add_image);
                                deleteTrackNumber.setImageResource(R.drawable.ic_baseline_delete_24);
                                trackNumberModel.setRedTrackNumber(false);
                                trackNumberModel.setWarningTrackNumber(true);
                            }
                        }

                    });
        } else if (pref.getString("Role", "NoData").equals("user")) {
            addImageParcel.setVisibility(View.GONE);
            if (trackNumberList.get(position).getRedTrackNumber().equals(true)) {
                statusIconTrackNumber.setImageResource(R.drawable.icon_cancel);
                trackNumberView.setText(trackNumberModel.getTrackNumber());
                deleteTrackNumber.setImageResource(R.drawable.ic_baseline_delete_24);
            } else {
                statusIconTrackNumber.setImageResource(R.drawable.ic_baseline_check_circle_correct);
                trackNumberView.setText(trackNumberModel.getTrackNumber());
                deleteTrackNumber.setImageResource(R.drawable.ic_baseline_delete_24);
            }
        }
        return view;
    }
}
