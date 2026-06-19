package pl.edu.uj.projzes.itl.api;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.uj.projzes.itl.api.dto.UpdateUserRoleRequest;
import pl.edu.uj.projzes.itl.api.dto.UserResponse;
import pl.edu.uj.projzes.itl.application.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/management/users")
@PreAuthorize("hasAuthority('USER_MANAGE')")
public class ManagementUserController {

    private final UserService userService;

    public ManagementUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> listUsers() {
        return userService.getAllUsers().stream()
                .map(UserResponse::from)
                .toList();
    }

    @PatchMapping("/{id}/role")
    public UserResponse updateRole(@PathVariable UUID id,
                                   @Valid @RequestBody UpdateUserRoleRequest request) {
        String performedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        return UserResponse.from(userService.updateRole(id, request.role(), performedBy));
    }
}
