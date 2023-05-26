package com.csl.ams.Event.Barcode;

public class ScanBarcodeResult {
    private String barcode;

    public ScanBarcodeResult(String barcode) {
        this.barcode = barcode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
