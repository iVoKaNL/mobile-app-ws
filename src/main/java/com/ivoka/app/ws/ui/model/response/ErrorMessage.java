package com.ivoka.app.ws.ui.model.response;

import lombok.Data;

import java.util.Date;

@Data
public class ErrorMessage {
    private Date timeStamp;
    private String message;

    public ErrorMessage() { }

    public ErrorMessage(Date timeStamp, String message) {
        this.timeStamp = timeStamp;
        this.message = message;
    }
}
