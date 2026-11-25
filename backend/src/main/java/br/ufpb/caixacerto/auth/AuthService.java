package br.ufpb.caixacerto.auth;

import br.ufpb.caixacerto.common.exception.BusinessRuleException;
import br.ufpb.caixacerto.user.User;
import br.ufpb.caixacerto.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AuthDTOs.AuthResponse register(AuthDTOs.RegisterRequest request) {
        validateRegistration(request);

        String email = request.getEmail().trim().toLowerCase();
        String salt = generateSalt();
        String passwordHash = passwordEncoder.encode(request.getPassword() + salt);

        User user = User.builder()
                .name(request.getName().trim().replaceAll("\\s+", " "))
                .email(email)
                .passwordHash(passwordHash)
                .passwordSalt(salt)
                .consentGiven(request.getConsent() != null && request.getConsent())
                .build();

        user = userRepository.save(user);
        log.info("Usuário registrado: {}", email);

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthDTOs.AuthResponse login(AuthDTOs.LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessRuleException("E-mail ou senha incorretos.", "email"));

        String rawPasswordWithSalt = request.getPassword() + user.getPasswordSalt();
        if (!passwordEncoder.matches(rawPasswordWithSalt, user.getPasswordHash())) {
            // Mesma mensagem para não revelar se e-mail existe
            throw new BusinessRuleException("E-mail ou senha incorretos.", "email");
        }

        log.info("Login realizado: {}", email);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthDTOs.AuthResponse refreshToken(String refreshToken) {
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessRuleException("Token inválido."));

        if (!jwtTokenProvider.validateToken(refreshToken, user)) {
            throw new BusinessRuleException("Token de atualização expirado ou inválido.");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public void deleteAccount(Long userId) {
        userRepository.deleteById(userId);
        log.info("Conta excluída: userId={}", userId);
    }

    private void validateRegistration(AuthDTOs.RegisterRequest request) {
        if (request.getName() == null || request.getName().trim().length() < 2) {
            throw new BusinessRuleException("Informe seu nome (de 2 a 80 caracteres).", "name");
        }
        if (request.getName().trim().length() > 80) {
            throw new BusinessRuleException("Nome muito longo (máximo 80 caracteres).", "name");
        }

        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
        if (email.isEmpty() || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") || email.length() > 120) {
            throw new BusinessRuleException("Informe um e-mail válido.", "email");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("Já existe uma conta com esse e-mail.", "email");
        }

        String password = request.getPassword();
        if (password == null || password.length() < 6) {
            throw new BusinessRuleException("A senha precisa ter pelo menos 6 caracteres.", "password");
        }
        if (password.length() > 100) {
            throw new BusinessRuleException("Senha muito longa.", "password");
        }

        if (!password.equals(request.getConfirmPassword())) {
            throw new BusinessRuleException("As duas senhas precisam ser iguais.", "confirmPassword");
        }

        if (request.getConsent() == null || !request.getConsent()) {
            throw new BusinessRuleException(
                    "Para criar a conta, marque que você concorda com o uso dos seus dados.", "consent");
        }
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private AuthDTOs.AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return AuthDTOs.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getExpirationMs() / 1000)
                .user(AuthDTOs.UserInfo.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .build())
                .build();
    }
}