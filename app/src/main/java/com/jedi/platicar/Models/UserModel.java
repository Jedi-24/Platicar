package com.jedi.platicar.Models;

public class UserModel {
    private String Name, Status, ImageUrl;

    public UserModel(String name, String status, String imageUrl) {
        this.Name = name;
        this.Status = status;
        this.ImageUrl = imageUrl;
    }

    public UserModel() {
    }

    public String getName() {
        return Name;
    }

    public String getStatus() {
        return Status;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public void setStatus(String status) {
        this.Status = status;
    }

    public void setImageUrl(String imageUrl) {
        this.ImageUrl = imageUrl;
    }
}
