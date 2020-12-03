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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private ArrayList<GalleryImage> galleryList;
    private ArrayList<CheckBox> checkBoxList;
    private Context context;
    private CheckBoxSelect callback;

    public GalleryAdapter(Context context, ArrayList<GalleryImage> galleryList, CheckBoxSelect callback) {
        this.galleryList = galleryList;
        this.checkBoxList = new ArrayList<CheckBox>();
        this.context = context;
        this.callback = callback;
    }

    public List<Integer> getCheckedIds()
    {
        List<Integer> ids = new ArrayList<Integer>();
        for (int i=0;i<checkBoxList.size();i++)
        {
            if (checkBoxList.get(i).isChecked())
                ids.add(i);
        }
        return ids;
    }

    private void updateNoChecked()
    {
        int count = 0;
        for (CheckBox checkBox : this.checkBoxList)
        {
            if (checkBox.isChecked())
                count++;
        }
        this.callback.updateNoSelected(count);
    }

    public void toggleAllCheckBoxes()
    {
        for (CheckBox checkBox : this.checkBoxList)
        {
            checkBox.setChecked(! checkBox.isChecked());
        }
    }


    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.gallery_image, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GalleryAdapter.ViewHolder viewHolder, int i) {
        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Bitmap bitmap = galleryList.get(i).getImage();
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), 1200, false);
        viewHolder.img.setImageBitmap(scaledBitmap);
    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView img;
        private CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);

            this.img = view.findViewById(R.id.image);
            this.checkBox = view.findViewById(R.id.select);
            GalleryAdapter.this.checkBoxList.add(this.checkBox);

            this.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    GalleryAdapter.this.updateNoChecked();
                }
            });
        }
    }
}