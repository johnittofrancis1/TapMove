package com.example.tapmove;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.Serializable;

public class GalleryImage implements Serializable {
    private final String fileName;
    private final String parentPath;
    private boolean isSelected;

    public GalleryImage(String parentPath, String fileName)
    {
        this.fileName = fileName;
        this.parentPath = parentPath;
        this.isSelected = false;
    }

    public String getParentPath(){
        return this.parentPath;
    }

    public String getFileName() {
        return this.fileName;
    }

    public Bitmap getImage()
    {
        Bitmap image = BitmapFactory.decodeFile(this.parentPath+"/"+this.fileName);
        return Bitmap.createScaledBitmap(image, image.getWidth(), 600, false);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
