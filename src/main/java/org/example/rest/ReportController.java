package org.example.rest;
import jakarta.servlet.http.HttpServletRequest;
import org.example.business.ReportService;
import org.example.domain.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.*;

/**
 * REST controller for handling report-related endpoints.
 * Provides endpoints to create reports and retrieve all reports of a user.
 */
@RestController
@RequestMapping("/reports")
public class ReportController {
    @Autowired
    private ReportService reportService;

    /**
     * Creates a new report for the authenticated user.
     *
     * @param report  the report data (can be null)
     * @param request the HTTP request containing the user ID
     * @return ResponseEntity with the created report and its location URI
     */
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

    /**
     * Retrieves all reports created by the authenticated user.
     *
     * @param request the HTTP request containing the user ID
     * @return ResponseEntity with a list of the user's reports
     */
    @RequestMapping(value = "/getAll",method = RequestMethod.POST)
    public ResponseEntity<?> get_all(HttpServletRequest request){
        Long user_id = (Long) request.getAttribute("user_id");
        List<Report> reports = reportService.getAll(user_id);
        return ResponseEntity.ok(reports);
    }
}
