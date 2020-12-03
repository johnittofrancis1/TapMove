package com.example.tapmove;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.Serializable;

public class GalleryImage implements Serializable {
    private final String fileName;
    private final String parentPath;

    public GalleryImage(String parentPath, String fileName)
    {
        this.fileName = fileName;
        this.parentPath = parentPath;
    }

    public String getParentPath(){
        return this.parentPath;
    }

    public String getFileName() {
        return this.fileName;
    }

    public Bitmap getImage()
    {
        return BitmapFactory.decodeFile(this.parentPath+"/"+this.fileName);
    }

}
