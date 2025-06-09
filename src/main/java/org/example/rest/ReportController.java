package org.example.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.example.domain.Report;
import org.example.domain.Warning;
import org.example.domain.validators.ReportValidator;
import org.example.domain.validators.ValidationException;
import org.example.domain.validators.Validator;
import org.example.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private Validator<Report> reportValidator = ReportValidator.getInstance();
    @Autowired
    private IReportRepository reportRepository;

    @Autowired
    private IWarningRepository warningRepository;


    Boolean missing_fields(Report report){
        return report.getStart_lat() == null || report.getStart_lng() == null || report.getEnd_lat() == null || report.getEnd_lng() == null || report.getWarnings() == null || report.getWarnings().isEmpty();
    }

    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.DEFAULT,
            readOnly = false)
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody(required = false) Report report, HttpServletRequest request){
        try{
            Long userId = (Long) request.getAttribute("user_id");

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing or invalid token"));
            }

            report.setUser_id(userId);


            if(missing_fields(report)){
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "Missing report fields!"));
            }

            reportValidator.validate(report);
            Optional<Report> optionalReport = reportRepository.add(report);


            if (optionalReport.isEmpty()) {
    //            return ResponseEntity
    //                    .badRequest()
    //                    .body(Map.of("error", "Report could not be created"));
                throw new RuntimeException("Report could not be created");
            }

            for(Warning warning : report.getWarnings()){
                warning.setReport_id(optionalReport.get().getId());
                warningRepository.add(warning);
            }


            URI location = URI.create("/reports/" + optionalReport.get().getId());

            return ResponseEntity
                    .created(location)
                    .body(optionalReport.get());
        }
        catch (ValidationException | RepositoryException exception){
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
                        .body(Map.of("error", "You are not logged in!"));
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
