package my.bot.web.auth;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import my.bot.web.dto.AuthStatus;
import my.bot.web.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    @Value("${app.admin.password:admin1234}")
    private String adminPassword;

    @Value("${app.admin.allowed-user-ids:}")
    private String allowedUserIds;

    @GetMapping("/status")
    public AuthStatus status(HttpSession session) {
        boolean authenticated = Boolean.TRUE.equals(session.getAttribute(AdminSession.AUTHENTICATED));
        String userId = authenticated ? (String) session.getAttribute(AdminSession.USER_ID) : null;
        return new AuthStatus(authenticated, userId);
    }

    @PostMapping("/login")
    public AuthStatus login(@RequestBody LoginRequest request, HttpSession session) {
        String userId = normalizeUserId(request.userId());
        if (!isAllowedUser(userId) || request.password() == null || !request.password().equals(adminPassword)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid user or password");
        }
        session.setAttribute(AdminSession.AUTHENTICATED, true);
        session.setAttribute(AdminSession.USER_ID, userId);
        return new AuthStatus(true, userId);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        session.invalidate();
    }

    private String normalizeUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        return userId.trim();
    }

    private boolean isAllowedUser(String userId) {
        Set<String> allowedIds = Arrays.stream(allowedUserIds.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toSet());
        return allowedIds.isEmpty() || allowedIds.contains(userId);
    }
}
