package com.csl.ams.Event.RFID;

public class ScanRFIDResult {
    private String epc;

    public ScanRFIDResult(String epc) {
        this.epc = epc;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

}
