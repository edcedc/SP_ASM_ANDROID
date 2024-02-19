package com.csl.ams.Event.SystemUI;

public class NetworkInventoryDoneEvent {
    String type;

    public NetworkInventoryDoneEvent() {
    }

    public NetworkInventoryDoneEvent(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
