package com.syberkeep.weclan;

public class UsersModel {

    /**
     *  Make sure that the variables names here are same as the variables names in the firebase database.
     *
     */

    public String full_name;
    public String status;
    public String profile_avatar;
    public String thumbnail;
    
    public UsersModel(){
        
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfile_avatar() {
        return profile_avatar;
    }

    public void setProfile_avatar(String profile_avatar) {
        this.profile_avatar = profile_avatar;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }



}
