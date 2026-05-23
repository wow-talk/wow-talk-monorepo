package io.wowtalk.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.wowtalk.user.domain.User;
import io.wowtalk.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Guest", description = "게스트 사용자 API")
@RestController
@RequestMapping("/api/v1/guests")
public class GuestController {

    private final UserService userService;

    public GuestController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "게스트 사용자 생성", description = "로그인 전 사용할 임시 사용자 식별자를 발급합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuestResponse create(@Valid @RequestBody(required = false) CreateGuestRequest request) {
        String displayName = request == null ? null : request.displayName();
        return GuestResponse.from(userService.createGuest(displayName));
    }

    public record CreateGuestRequest(
            @Size(max = 50, message = "표시 이름은 50자 이하여야 합니다.")
            String displayName
    ) {
    }

    public record GuestResponse(
            String userId,
            String userType,
            String displayName
    ) {
        private static GuestResponse from(User user) {
            return new GuestResponse(
                    user.userId().value(),
                    user.userType().name(),
                    user.displayName()
            );
        }
    }
}
