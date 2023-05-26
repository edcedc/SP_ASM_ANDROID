package com.csl.ams.Event;

public class RFIDEPCReadEvent {
    public RFIDEPCReadEvent(String epc) {
        setEpc(epc);
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    private String epc;

}
