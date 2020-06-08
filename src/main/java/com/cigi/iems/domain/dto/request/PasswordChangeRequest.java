package com.cigi.iems.domain.dto.request;

import lombok.Getter;

@Getter
public class PasswordChangeRequest {
    private String email;
    private String password;
}
