package com.example.tapmove;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private ArrayList<GalleryImage> galleryList;
    private Context context;
    private CheckBoxSelect callback;
    private boolean allChecked;

    public GalleryAdapter(Context context, ArrayList<GalleryImage> galleryList, CheckBoxSelect callback) {
        this.galleryList = galleryList;
        this.context = context;
        this.callback = callback;
        this.allChecked = false;
    }

    private void updateNoChecked()
    {
        this.callback.updateNoSelected();
    }

    public boolean toggleAllCheckBoxes()
    {
        for (GalleryImage galleryImage : this.galleryList)
            galleryImage.setSelected(! this.allChecked);
        this.allChecked = ! this.allChecked;
        notifyDataSetChanged();
        return this.allChecked;
    }


    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.gallery_image, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(GalleryAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        final GalleryImage galleryImage = galleryList.get(i);
        Bitmap bitmap = galleryImage.getImage();
        viewHolder.img.setImageBitmap(bitmap);

        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.e("CHECK", galleryImage.getFileName()+" is selected "+b);
                galleryImage.setSelected(b);
                updateNoChecked();
            }
        });

        viewHolder.checkBox.setChecked(galleryImage.isSelected());
    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView img;
        private final CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);

            this.img = view.findViewById(R.id.image);
            this.checkBox = view.findViewById(R.id.select);
        }
    }
}