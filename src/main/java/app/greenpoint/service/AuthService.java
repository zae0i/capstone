package app.greenpoint.service;

import app.greenpoint.domain.AppUser;
import app.greenpoint.dto.JwtResponseDto;
import app.greenpoint.dto.LoginRequestDto;
import app.greenpoint.dto.UserSignupRequestDto;
import app.greenpoint.jwt.JwtTokenProvider;
import app.greenpoint.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Transactional
    public AppUser signup(UserSignupRequestDto signupRequest) {
        if (appUserRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email address already in use."); // Replace with a custom exception later
        }

        AppUser user = AppUser.builder()
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .nickname(signupRequest.getNickname())
                .region(signupRequest.getRegion())
                .role(AppUser.Role.USER)
                .build();

        return appUserRepository.save(user);
    }

    public JwtResponseDto login(LoginRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return new JwtResponseDto(jwt);
    }
}
