package com.hgxx.whiteboard.models;

import android.graphics.Bitmap;

/**
 * Created by ly on 24/05/2017.
 */

public class PresentationInfo {
    private Bitmap thumb;
    private String presentationName;
    private String uploadTime;
    private String size;

    public PresentationInfo(String name){
        this.presentationName = name;
    }

    public Bitmap getThumb() {
        return thumb;
    }

    public void setThumb(Bitmap thumb) {
        this.thumb = thumb;
    }

    public String getPresentationName() {
        return presentationName;
    }

    public void setPresentationName(String presentationName) {
        this.presentationName = presentationName;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
