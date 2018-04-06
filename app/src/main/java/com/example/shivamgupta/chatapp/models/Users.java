package com.example.shivamgupta.chatapp.models;

/**
 * Created by Shivam Gupta on 02-04-2018.
 */

public class Users {
    private String Name;
    private String Image;
    private String Status;
    private String Thumb_image;

    public String getThumb_image() {
        return Thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        Thumb_image = thumb_image;
    }

    public Users(){}

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public Users(String name, String image, String status , String thumb_image) {

        Name = name;
        Image = image;
        Status = status;
        Thumb_image = thumb_image;
    }
}
