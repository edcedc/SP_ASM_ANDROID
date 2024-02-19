package com.csl.ams.Entity.Pallet;

import io.realm.annotations.PrimaryKey;

public class RecordClone {
    public RecordClone(Record record){
        this.barcode = record.getBarcode();
        this.epc = record.getEpc();

        //this.locationName = record.getLocationName();
        this.locationRono = record.getLocationRono();

        //this.categoryName = record.getCategoryName();
        this.categoryRono = record.getCategoryRono();

        this.datetime = record.getDatetime();
        this.loginID = record.getUserid();
    }

    private String barcode;
    private String epc;
    private String locationRono;

    private String categoryRono;


    private String datetime;
    private String loginID;

    public String getUserid() {
        return loginID;
    }

    public void setUserid(String userid) {
        this.loginID = userid;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }


    public String getLocationRono() {
        return locationRono;
    }

    public void setLocationRono(String locationRono) {
        this.locationRono = locationRono;
    }

    public String getCategoryRono() {
        return categoryRono;
    }

    public void setCategoryRono(String categoryRono) {
        this.categoryRono = categoryRono;
    }


}
