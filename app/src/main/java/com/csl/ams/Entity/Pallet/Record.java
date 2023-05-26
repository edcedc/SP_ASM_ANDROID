package com.csl.ams.Entity.Pallet;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Record extends RealmObject {
    @PrimaryKey
    private String barcode;
    private String epc;
    //private String type;
    private String datetime;
    private String userid;
    private String companyid;

    private String locationRono;
    private String locationName;
    private String categoryRono;
    private String categoryName;

    public String getLocationRono() {
        return locationRono;
    }

    public void setLocationRono(String locationRono) {
        this.locationRono = locationRono;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getCategoryRono() {
        return categoryRono;
    }

    public void setCategoryRono(String categoryRono) {
        this.categoryRono = categoryRono;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
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

    //public String getType() {
    //    return type;
   // }

    //public void setType(String type) {
     //   this.type = type;
    //}

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
