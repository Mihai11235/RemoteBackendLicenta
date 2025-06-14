package org.example.rest;
import jakarta.servlet.http.HttpServletRequest;
import org.example.business.ReportService;
import org.example.domain.Report;
import org.example.domain.Warning;
import org.example.domain.validators.ReportValidator;
import org.example.domain.validators.Validator;
import org.example.domain.validators.WarningValidator;
import org.example.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/reports")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody(required = false) Report report, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("user_id");
        if(report == null) {
            report = new Report();
        }
        report.setUser_id(userId);

        Report createdReport = reportService.create(report);

        URI location = URI.create("/reports/" + createdReport.getId());

        return ResponseEntity
                .created(location)
                .body(createdReport);
    }

    @RequestMapping(value = "/getAll",method = RequestMethod.POST)
    public ResponseEntity<?> get_all(HttpServletRequest request){
        Long user_id = (Long) request.getAttribute("user_id");
        List<Report> reports = reportService.getAll(user_id);
        return ResponseEntity.ok(reports);
    }
}
