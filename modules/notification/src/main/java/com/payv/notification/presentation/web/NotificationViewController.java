package com.payv.notification.presentation.web;

import com.payv.iam.infrastructure.security.IamUserDetails;
import com.payv.notification.application.query.NotificationQueryService;
import com.payv.notification.presentation.dto.request.NotificationListConditionRequest;
import com.payv.notification.presentation.dto.request.NotificationListNoticeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationViewController {

    private final NotificationQueryService notificationQueryService;

    @GetMapping
    public String list(@AuthenticationPrincipal IamUserDetails userDetails,
                       @ModelAttribute("condition") NotificationListConditionRequest condition,
                       @ModelAttribute("notice") NotificationListNoticeRequest notice,
                       Model model) {
        String ownerUserId = userDetails.getUserId();

        model.addAttribute("result", notificationQueryService.list(ownerUserId, condition.getPage(), condition.getSize()));
        model.addAttribute("unreadCount", notificationQueryService.unreadCount(ownerUserId));
        model.addAttribute("notice", notice);
        return "notification/list";
    }
}
