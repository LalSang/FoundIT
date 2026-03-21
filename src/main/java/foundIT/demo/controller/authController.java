package foundIT.demo.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import foundIT.demo.service.adminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
public class authController {

    private final adminService aService;

    public authController(adminService aService)
    {
        this.aService = aService;
    }

    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> signIn(@Valid @RequestBody SignInRequest request)
    {
        var authenticatedAdmin = aService.authenticate(request.username(), request.password());

        if (authenticatedAdmin.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }

        var admin = authenticatedAdmin.get();
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("message", "Sign in successful");
        responseBody.put("id", emptyIfNull(admin.getId()));
        responseBody.put("username", emptyIfNull(admin.getUsername()));
        responseBody.put("firstName", emptyIfNull(admin.getFirstName()));
        responseBody.put("lastName", emptyIfNull(admin.getLastName()));

        return ResponseEntity.ok(responseBody);
    }

    public record SignInRequest(
            @NotBlank(message = "Username is required") String username,
            @NotBlank(message = "Password is required") String password)
    {
    }

    private String emptyIfNull(String value)
    {
        return value == null ? "" : value;
    }
}
