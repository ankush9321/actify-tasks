package com.finledger.controller;

import java.time.YearMonth;
import java.util.Collections;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.finledger.service.InterestService;

@RestController
@RequestMapping("/api/admin/interest")
@PreAuthorize("hasRole('ADMIN')")
public class InterestController {
    private final InterestService interest;
    public InterestController(InterestService interest) { this.interest=interest; }
    @PostMapping("/run")
    public Map<String,String> run(@RequestParam(required=false) String month) {
        YearMonth period = month == null ? YearMonth.now().minusMonths(1) : YearMonth.parse(month);
        interest.postMonthlyInterest(period);
        return Collections.singletonMap("result", "Interest run completed for " + period);
    }
}
