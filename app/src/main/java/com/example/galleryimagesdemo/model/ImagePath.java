package com.example.galleryimagesdemo.model;

public class ImagePath {

    String imagePath;

    boolean isSynced = false;

    public ImagePath() {

    }

    public ImagePath(String imagePath, boolean isSynced) {
        this.imagePath = imagePath;
        this.isSynced = isSynced;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
