package org.example.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.example.domain.Report;
import org.example.domain.Warning;
import org.example.domain.validators.ReportValidator;
import org.example.domain.validators.ValidationException;
import org.example.domain.validators.Validator;
import org.example.domain.validators.WarningValidator;
import org.example.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private Validator<Report> reportValidator = ReportValidator.getInstance();
    private Validator<Warning> warningValidator = WarningValidator.getInstance();
    @Autowired
    private IReportRepository reportRepository;

    @Autowired
    private IWarningRepository warningRepository;


    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody(required = false) Report report, HttpServletRequest request){
        List<Runnable> cleanupActions = new ArrayList<>();
        try{
            Long userId = (Long) request.getAttribute("user_id");

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing or invalid token!\n"));
            }

            report.setUser_id(userId);
            if(report.getCreated_at() == null){
                report.setCreated_at(System.currentTimeMillis());
            }


            reportValidator.validate(report);
            for(Warning warning : report.getWarnings()){
                warningValidator.validate(warning);
            }
            Optional<Report> optionalReport = reportRepository.add(report);


            if (optionalReport.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "Report could not be created!\n"));
            }

            Report savedReport = optionalReport.get();
            // If successful, add its deletion to the cleanup list
            cleanupActions.add(() -> reportRepository.delete(savedReport.getId()));


            for(Warning warning : report.getWarnings()){
                warning.setReport_id(optionalReport.get().getId());
                Optional<Warning> optionalWarning = warningRepository.add(warning);
                if(optionalWarning.isEmpty()){
                    throw new RuntimeException("A warning for report " + savedReport.getId() + " could not be created!\n");
                }
                Warning savedWarning = optionalWarning.get();

                // If successful, add its deletion to the cleanup list
                cleanupActions.add(() -> warningRepository.delete(savedWarning.getId()));
            }

            URI location = URI.create("/reports/" + optionalReport.get().getId());

            return ResponseEntity
                    .created(location)
                    .body(optionalReport.get());
        }
        catch (ValidationException | RepositoryException exception){
            Collections.reverse(cleanupActions);
            for (Runnable action : cleanupActions) {
                try {
                    action.run();
                } catch (Exception e) {
                    // Log this error, as it means the manual rollback failed
                    System.err.println("CRITICAL: Failed to run cleanup action during rollback: " + e.getMessage());
                }
            }
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", exception.getMessage()));
        }
    }

    @RequestMapping(value = "/getAll",method = RequestMethod.POST)
    public ResponseEntity<?> get_all(HttpServletRequest request){
        try{
            Long user_id = (Long) request.getAttribute("user_id");

            if(user_id == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing or invalid token!\n"));
            }

            List<Report> reports = reportRepository.getAllOfUser(user_id);

            return ResponseEntity.ok(reports);
        }
        catch (Exception e){
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
