package com.cigi.iems.domain.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentityAvailability {
    private Boolean available;
}
