package com.cigi.iems.controller;

import com.cigi.iems.domain.dto.request.LoginRequest;
import com.cigi.iems.domain.dto.request.PasswordChangeRequest;
import com.cigi.iems.domain.dto.request.SignUpRequest;
import com.cigi.iems.domain.dto.response.ApiResponse;
import com.cigi.iems.domain.dto.response.JwtAuthenticationResponse;
import com.cigi.iems.domain.model.PasswordResetToken;
import com.cigi.iems.domain.model.Role;
import com.cigi.iems.domain.model.User;
import com.cigi.iems.domain.repository.PasswordResetTokenRepository;
import com.cigi.iems.domain.repository.RoleRepository;
import com.cigi.iems.domain.repository.UserRepository;
import com.cigi.iems.exception.ResourceNotFoundException;
import com.cigi.iems.exception.UserAlreadyInUseException;
import com.cigi.iems.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
	
	private AuthenticationManager authenticationManager;
	
	private UserRepository userRepository;
	
	private RoleRepository roleRepository;
	
	private PasswordEncoder passwordEncoder;
	
	private PasswordResetTokenRepository passwordResetTokenRepository;
	
	private JavaMailSender javaMailSender;
	
	private JwtTokenProvider tokenProvider;
	
	public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, PasswordResetTokenRepository passwordResetTokenRepository, JavaMailSender javaMailSender, JwtTokenProvider tokenProvider) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.passwordResetTokenRepository = passwordResetTokenRepository;
		this.javaMailSender = javaMailSender;
		this.tokenProvider = tokenProvider;
	}

    @PostMapping("/signin")
	public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						loginRequest.getEmail(),
						loginRequest.getPassword()
				)
		);
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		String jwt = tokenProvider.generateToken(authentication);
		return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
	}

    @GetMapping("/signout")
    public ResponseEntity<ApiResponse> signOutUser(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
			return ResponseEntity.ok(new ApiResponse(true, "User logged out."));
		}
		return ResponseEntity.badRequest().body(new ApiResponse(false, "User was not logged in."));
	}

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signUpUser(@Valid @RequestBody SignUpRequest signUpRequest) {
		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			throw new UserAlreadyInUseException("User is already in use!");
		}
		
		User user = User.builder()
				.email(signUpRequest.getEmail())
				.password(passwordEncoder.encode(signUpRequest.getPassword()))
                .roles(Collections.singleton(roleRepository.findByName(Role.RoleName.ROLE_USER)))
				.build();
		
		User result = userRepository.save(user);
		
		URI location = ServletUriComponentsBuilder
				.fromCurrentContextPath().path("/users/{username}")
				.buildAndExpand(result.getUsername()).toUri();
		
		return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully."));
	}
	
	@GetMapping("/forgotPassword")
	@Transactional
	public ResponseEntity<ApiResponse> sendMailToResetPassword(@RequestParam(value = "email") String email) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User does not found."));
		passwordResetTokenRepository.findByUser(user).ifPresent((token) -> passwordResetTokenRepository.delete(token));
		sendMailToUser(user, email);
		return ResponseEntity.ok().body(new ApiResponse(true, "Please check your mail address to reset your password."));
	}
	
	@PostMapping("/resetPassword")
	public ResponseEntity<ApiResponse> resetPassword(@RequestParam(value = "token") String token, @RequestBody PasswordChangeRequest request) {
		PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token).orElseThrow(() -> new ResourceNotFoundException("Token does not found"));
		Instant now = Instant.now();
		if (now.isBefore(passwordResetToken.getExpiryDate())) {
			userRepository.findByEmail(request.getEmail()).ifPresent((user -> {
				user.setPassword(passwordEncoder.encode(request.getPassword()));
				userRepository.save(user);
				passwordResetTokenRepository.delete(passwordResetToken);
			}));
			
			return ResponseEntity.ok().body(new ApiResponse(true, "Password is updated"));
		}
		return ResponseEntity.badRequest().body(new ApiResponse(false, "Please try again"));
	}
	
	private PasswordResetToken generatePasswordResetToken(User user) {
		PasswordResetToken token = new PasswordResetToken();
		token.setUser(user);
		token.setToken(UUID.randomUUID().toString());
		token.setExpiryDate();
		return passwordResetTokenRepository.save(token);
	}
	
	private void sendMailToUser(User user, String to) {
		SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
		simpleMailMessage.setTo(to);
		simpleMailMessage.setFrom("cihangir@thinkerfox.com");
		simpleMailMessage.setSubject("Password Reset Mail");
		simpleMailMessage.setText("http://localhost:8080/user/resetPassword?token=" + generatePasswordResetToken(user).getToken());
		javaMailSender.send(simpleMailMessage);
	}
}
