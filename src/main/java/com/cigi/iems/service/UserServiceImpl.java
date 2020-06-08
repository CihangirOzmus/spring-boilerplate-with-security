package com.cigi.iems.service;

import com.cigi.iems.domain.dto.request.UserRequest;
import com.cigi.iems.domain.dto.response.ApiResponse;
import com.cigi.iems.domain.dto.response.UserIdentityAvailability;
import com.cigi.iems.domain.dto.response.UserProfile;
import com.cigi.iems.domain.dto.response.UserSummary;
import com.cigi.iems.domain.model.Role;
import com.cigi.iems.domain.model.User;
import com.cigi.iems.domain.repository.RoleRepository;
import com.cigi.iems.domain.repository.UserRepository;
import com.cigi.iems.exception.UserAlreadyInUseException;
import com.cigi.iems.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserSummary getCurrentUser(UserPrincipal currentUser) {
        return new UserSummary(currentUser.getId(), currentUser.getUsername(), currentUser.getEmail());
    }

    @Override
    public UserProfile getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new UserProfile(user.getId(), user.getUsername(), user.getEmail());
    }

    @Override
    public UserIdentityAvailability checkUsernameAvailability(String username) {
        return new UserIdentityAvailability(!userRepository.existsByUsername(username));
    }

    @Override
    public UserIdentityAvailability checkUserEmailAvailability(String email) {
        return new UserIdentityAvailability(!userRepository.existsByEmail(email));
    }

    @Override
    public ResponseEntity<?> createUser(UserRequest userRequest) {
        if (userRepository.existsByUsername(userRequest.getUsername()) || userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserAlreadyInUseException("User is already in use!");
        }

        User user = User.builder()
                .username(userRequest.getUsername())
                .email(userRequest.getEmail())
                .roles(Collections.singleton(roleRepository.findByName(Role.RoleName.ROLE_USER)))
                .roles(Collections.singleton(roleRepository.findByName(userRequest.getRole())))
                .build();

        userRepository.save(user);
        return ResponseEntity.ok().body(new ApiResponse(true, "User created."));
    }
}
