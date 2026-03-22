package foundIT.demo.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import foundIT.demo.model.admin;
import foundIT.demo.service.adminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
public class authController {

    private static final Logger logger = LoggerFactory.getLogger(authController.class);

    private final adminService aService;

    /** Stores the admin service used to authenticate staff sign-ins. */
    public authController(adminService aService)
    {
        this.aService = aService;
    }

    /** Authenticates a sign-in request and returns the staff profile payload on success. */
    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> signIn(@Valid @RequestBody SignInRequest request)
    {
        final var authenticatedAdmin = authenticateAdmin(request);

        if (authenticatedAdmin.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }

        final var admin = authenticatedAdmin.get();
        final Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("message", "Sign in successful");
        responseBody.put("id", emptyIfNull(admin.getId()));
        responseBody.put("username", emptyIfNull(admin.getUsername()));
        responseBody.put("firstName", emptyIfNull(admin.getFirstName()));
        responseBody.put("lastName", emptyIfNull(admin.getLastName()));

        return ResponseEntity.ok(responseBody);
    }

    /** Converts backend connectivity failures into a consistent API response. */
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleServiceUnavailable()
    {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Sign in is temporarily unavailable. Database connection failed."));
    }

    /** Captures the username and password required for sign-in. */
    public record SignInRequest(
            @NotBlank(message = "Username is required") String username,
            @NotBlank(message = "Password is required") String password)
    {
    }

    /** Wraps service-layer authentication so database failures can be logged and normalized. */
    private java.util.Optional<admin> authenticateAdmin(SignInRequest request)
    {
        try
        {
            return aService.authenticate(request.username(), request.password());
        }
        catch (Exception exception)
        {
            logger.error("Unable to reach the database during sign in", exception);
            throw new ServiceUnavailableException();
        }
    }

    /** Replaces null response values with empty strings for the frontend. */
    private String emptyIfNull(String value)
    {
        return value == null ? "" : value;
    }

    // Makes a custom Exception for any errors thrown on sign in
    private static final class ServiceUnavailableException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
    }
}
