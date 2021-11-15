package com.example.tnscpe;

import android.icu.util.LocaleData;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class ParcelsModel {
    String checkIn;
    String checkOut;
    String trackNumber;
    ParcelsOwnerModel owner = new ParcelsOwnerModel();
    ParcelsReceiverModel receiver = new ParcelsReceiverModel();
    ParcelsImage images = new ParcelsImage();
    Boolean ListView = false;
    Boolean detailInListView = false;

    public ParcelsModel() {
    }

    public Boolean getDetailInListView() {
        return detailInListView;
    }

    public void setDetailInListView(Boolean detailInListView) {
        this.detailInListView = detailInListView;
    }

    public Boolean getListView() {
        return ListView;
    }

    public void setListView(Boolean listView) {
        ListView = listView;
    }

    public ParcelsImage getImages() {
        return images;
    }

    public void setImages(ParcelsImage images) {
        this.images = images;
    }

    public ParcelsReceiverModel getReceiver() {
        return receiver;
    }

    public void setReceiver(ParcelsReceiverModel receiver) {
        this.receiver = receiver;
    }

    public ParcelsOwnerModel getOwner() {
        return owner;
    }

    public void setOwner(ParcelsOwnerModel owner) {
        this.owner = owner;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }

    public static class ParcelsOwnerModel {
        String firstname;
        String lastname;

        public ParcelsOwnerModel() {
        }

        public ParcelsOwnerModel(String firstname, String lastname) {
            this.firstname = firstname;
            this.lastname = lastname;
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
    }

    public static class ParcelsReceiverModel {
        String firstname;
        String lastname;

        public ParcelsReceiverModel() {
        }

        public ParcelsReceiverModel(String firstname, String lastname) {
            this.firstname = firstname;
            this.lastname = lastname;
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
    }

    public static class ParcelsImage {
        String image0;
        String image1;
        String image2;
        String image3;

        public ParcelsImage() {
        }

        public String getImage0() {
            return image0;
        }

        public void setImage0(String image0) {
            this.image0 = image0;
        }

        public String getImage1() {
            return image1;
        }

        public void setImage1(String image1) {
            this.image1 = image1;
        }

        public String getImage2() {
            return image2;
        }

        public void setImage2(String image2) {
            this.image2 = image2;
        }

        public String getImage3() {
            return image3;
        }

        public void setImage3(String image3) {
            this.image3 = image3;
        }
    }
}

