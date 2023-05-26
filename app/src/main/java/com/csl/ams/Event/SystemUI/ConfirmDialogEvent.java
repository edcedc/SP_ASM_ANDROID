package com.csl.ams.Event.SystemUI;

public class ConfirmDialogEvent {
    public ConfirmDialogEvent(String title, String message) {
        this.title = title;
        this.message = message;
    }

    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;

}
