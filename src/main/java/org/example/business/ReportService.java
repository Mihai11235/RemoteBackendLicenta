package org.example.business;
import org.example.business.exception.DataAccessException;
import org.example.business.exception.InvalidCredentialsException;
import org.example.business.exception.ServiceException;
import org.example.domain.Report;
import org.example.domain.Warning;
import org.example.domain.validators.ReportValidator;
import org.example.domain.validators.Validator;
import org.example.domain.validators.WarningValidator;
import org.example.persistence.IReportRepository;
import org.example.persistence.IWarningRepository;
import org.example.persistence.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ReportService {
    private Validator<Report> reportValidator = ReportValidator.getInstance();
    private Validator<Warning> warningValidator = WarningValidator.getInstance();

    @Autowired
    private IReportRepository reportRepository;
    @Autowired
    private IWarningRepository warningRepository;

    public Report create(Report report) {
        List<Runnable> cleanupActions = new ArrayList<>();

        if (report.getUser_id() == null) {
            throw new InvalidCredentialsException("Missing or invalid token!\n");
        }

        if (report.getCreated_at() == null) {
            report.setCreated_at(System.currentTimeMillis());
        }

        reportValidator.validate(report);
        for (Warning warning : report.getWarnings()) {
            warningValidator.validate(warning);
        }

        try {
            Report savedReport = reportRepository.add(report)
                    .orElseThrow(() -> new DataAccessException("Report could not be created!\n", null));

            // If successful, add its deletion to the cleanup list
            cleanupActions.add(() -> reportRepository.delete(savedReport.getId()));


            for (Warning warning : report.getWarnings()) {
                warning.setReport_id(savedReport.getId());
                Warning savedWarning = warningRepository.add(warning)
                        .orElseThrow(() -> new DataAccessException("A warning for report " + savedReport.getId() + " could not be created!\n", null));

                // If successful, add its deletion to the cleanup list
                cleanupActions.add(() -> warningRepository.delete(savedWarning.getId()));
            }

            return savedReport;
        } catch (RepositoryException | DataAccessException exception) {
            Collections.reverse(cleanupActions);
            for (Runnable action : cleanupActions) {
                try {
                    action.run();
                } catch (Exception e) {
                    System.err.println("CRITICAL: Failed to run cleanup action during rollback: " + e.getMessage());
                }
            }
            throw new DataAccessException("Error while creating report!", exception);
        }
    }

    public List<Report> getAll(Long user_id) {
        try{
            if(user_id == null){
                throw new InvalidCredentialsException("Missing or invalid token!\n");
            }

            return reportRepository.getAllOfUser(user_id);
        }
        catch (RepositoryException exception){
            throw new DataAccessException("Error while fetching reports!", exception);
        }
    }
}
