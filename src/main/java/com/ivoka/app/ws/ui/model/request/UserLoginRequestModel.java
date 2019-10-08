package com.ivoka.app.ws.ui.model.request;

import lombok.Data;

@Data
public class UserLoginRequestModel {
    private String email;
    private String password;
}
