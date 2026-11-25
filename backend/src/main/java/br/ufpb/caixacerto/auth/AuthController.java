package br.ufpb.caixacerto.auth;

import br.ufpb.caixacerto.common.exception.BusinessRuleException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthDTOs.AuthResponse> login(@Valid @RequestBody AuthDTOs.LoginRequest request,
                                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String field = bindingResult.getFieldErrors().get(0).getField();
            String message = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            throw new BusinessRuleException(message, field);
        }
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthDTOs.AuthResponse> register(@Valid @RequestBody AuthDTOs.RegisterRequest request,
                                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String field = bindingResult.getFieldErrors().get(0).getField();
            String message = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            throw new BusinessRuleException(message, field);
        }
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthDTOs.AuthResponse> refresh(@Valid @RequestBody AuthDTOs.RefreshRequest request,
                                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new BusinessRuleException("Refresh token é obrigatório.", "refreshToken");
        }
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthDTOs.UserInfo> me(@AuthenticationPrincipal UserDetails userDetails) {
        // O usuário já está autenticado pelo filtro JWT
        // Podemos buscar o user completo se necessário
        return ResponseEntity.ok(AuthDTOs.UserInfo.builder()
                .email(userDetails.getUsername())
                .build());
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        // O userDetails tem o email, precisamos buscar o ID
        // Por simplicidade, o AuthService faria a busca
        // Aqui retornamos 204 No Content
        return ResponseEntity.noContent().build();
    }
}