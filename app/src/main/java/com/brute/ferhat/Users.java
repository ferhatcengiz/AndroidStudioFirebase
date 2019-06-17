package com.brute.ferhat;

public class Users
{
    public String uid, username, country, profileimage, fullname;

    public Users()
    {

    }

    public Users(String uid, String username, String country, String profileimage, String fullname) {
        this.uid = uid;
        this.username = username;
        this.country = country;
        this.profileimage = profileimage;
        this.fullname = fullname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String fullname) {
        this.username = username;
    }
    public String getCountry() {
        return country;
    }

    public void setCountry(String fullname) {
        this.country = country;
    }

}