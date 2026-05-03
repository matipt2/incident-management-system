package pl.edu.uj.projzes.itl.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.uj.projzes.itl.api.dto.MeResponse;
import pl.edu.uj.projzes.itl.domain.user.CurrentUser;
import pl.edu.uj.projzes.itl.infrastructure.web.UserContextHolder;

@RestController
@RequestMapping("/api/me")
public class MeController {

    @GetMapping("/resources")
    public ResponseEntity<MeResponse> getResources() {
        CurrentUser user = UserContextHolder.get();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(new MeResponse(user.userId(), user.username(), user.role(), user.role().getPermissions()));
    }
}
