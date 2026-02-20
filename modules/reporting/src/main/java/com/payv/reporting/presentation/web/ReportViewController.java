package com.payv.reporting.presentation.web;

import com.payv.iam.infrastructure.security.IamUserDetails;
import com.payv.reporting.application.query.ReportingQueryService;
import com.payv.reporting.application.query.model.MonthlyReportView;
import com.payv.reporting.presentation.dto.request.ReportConditionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.YearMonth;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportViewController {

    private final ReportingQueryService reportingQueryService;

    @GetMapping
    public String report(@AuthenticationPrincipal IamUserDetails userDetails,
                         @ModelAttribute("condition") ReportConditionRequest condition,
                         Model model) {
        String ownerUserId = userDetails.getUserId();
        YearMonth month = condition.resolvedMonth();

        MonthlyReportView report = reportingQueryService.getMonthlyReport(ownerUserId, month);

        model.addAttribute("report", report);
        model.addAttribute("selectedMonth", month.toString());
        return "reporting/report";
    }
}
