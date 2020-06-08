package com.cigi.iems.domain.dto.request;

import com.cigi.iems.domain.model.Role;
import lombok.Getter;

@Getter
public class UserRequest {
    private String username;
    private String email;
    private Role.RoleName role;
}
