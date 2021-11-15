package com.example.tnscpe;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;

public class ImageParcelsSliderAdapter extends SliderViewAdapter<ImageParcelsSliderAdapter.ViewHolder> {

    ArrayList<Bitmap> images;

    public ImageParcelsSliderAdapter(ArrayList<Bitmap> image) {
        this.images = image;
    }

    public class ViewHolder extends SliderViewAdapter.ViewHolder {

        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_parcel_item);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_parcels_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.imageView.setImageBitmap(images.get(position));
    }

    @Override
    public int getCount() {
        return images.size();
    }
}
