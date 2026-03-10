package com.payv.notification.presentation.api;

import com.payv.common.presentation.api.AjaxResponses;
import com.payv.iam.infrastructure.security.IamUserDetails;
import com.payv.notification.application.command.NotificationCommandService;
import com.payv.notification.domain.model.NotificationId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notification/notifications")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationCommandService notificationCommandService;

    @PutMapping(path = "/{notificationId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> markRead(@AuthenticationPrincipal IamUserDetails userDetails,
                                                         @PathVariable String notificationId,
                                                         @RequestParam(required = false) Integer page,
                                                         @RequestParam(required = false) Integer size) {
        String ownerUserId = userDetails.getUserId();
        notificationCommandService.markRead(NotificationId.of(notificationId), ownerUserId);
        return AjaxResponses.okRedirect(buildListRedirect(page, size, "read=true"));
    }

    @PutMapping(path = "/read-all", produces = "application/json")
    public ResponseEntity<Map<String, Object>> markAllRead(@AuthenticationPrincipal IamUserDetails userDetails,
                                                            @RequestParam(required = false) Integer page,
                                                            @RequestParam(required = false) Integer size) {
        String ownerUserId = userDetails.getUserId();
        notificationCommandService.markAllRead(ownerUserId);
        return AjaxResponses.okRedirect(buildListRedirect(page, size, "readAll=true"));
    }

    private String buildListRedirect(Integer page, Integer size, String flag) {
        StringBuilder builder = new StringBuilder("/notifications?").append(flag);
        if (page != null && page > 0) {
            builder.append("&page=").append(page);
        }
        if (size != null && size > 0) {
            builder.append("&size=").append(size);
        }
        return builder.toString();
    }
}
