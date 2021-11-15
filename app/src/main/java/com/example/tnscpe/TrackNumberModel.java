package com.example.tnscpe;

import android.net.Uri;

public class TrackNumberModel {
    String checkIn;
    String checkOut;
    String department;
    Uri image0;
    Uri image1;
    Uri image2;
    Uri image3;
    String firstname;
    String lastname;
    String trackNumber;
    Boolean correctTrackNumber;
    Boolean warningTrackNumber;
    Boolean redTrackNumber;

    public TrackNumberModel() { }

    public Boolean getRedTrackNumber() {
        return redTrackNumber;
    }

    public void setRedTrackNumber(Boolean redTrackNumber) {
        this.redTrackNumber = redTrackNumber;
    }

    public Boolean getCorrectTrackNumber() {
        return correctTrackNumber;
    }

    public void setCorrectTrackNumber(Boolean correctTrackNumber) {
        this.correctTrackNumber = correctTrackNumber;
    }

    public Boolean getWarningTrackNumber() {
        return warningTrackNumber;
    }

    public void setWarningTrackNumber(Boolean warningTrackNumber) {
        this.warningTrackNumber = warningTrackNumber;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(String checkIn) {

        this.checkIn = checkIn;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Uri getImage0() {
        return image0;
    }

    public void setImage0(Uri image0) {
        this.image0 = image0;
    }

    public Uri getImage1() {
        return image1;
    }

    public void setImage1(Uri image1) {
        this.image1 = image1;
    }

    public Uri getImage2() {
        return image2;
    }

    public void setImage2(Uri image2) {
        this.image2 = image2;
    }

    public Uri getImage3() {
        return image3;
    }

    public void setImage3(Uri image3) {
        this.image3 = image3;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }
}
