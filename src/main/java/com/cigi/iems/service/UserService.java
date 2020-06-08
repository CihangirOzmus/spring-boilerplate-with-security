package com.cigi.iems.service;

import com.cigi.iems.domain.dto.response.UserIdentityAvailability;
import com.cigi.iems.security.UserPrincipal;
import com.cigi.iems.domain.dto.request.UserRequest;
import com.cigi.iems.domain.dto.response.UserProfile;
import com.cigi.iems.domain.dto.response.UserSummary;
import org.springframework.http.ResponseEntity;

public interface UserService {
    UserSummary getCurrentUser(UserPrincipal currentUser);
    UserProfile getUserProfile(String username);
    UserIdentityAvailability checkUsernameAvailability(String username);
    UserIdentityAvailability checkUserEmailAvailability(String email);
    ResponseEntity<?> createUser(UserRequest userRequest);
}
