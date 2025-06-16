package org.example.business;
import org.example.business.exception.DataAccessException;
import org.example.business.exception.InvalidCredentialsException;
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

/**
 * Service class responsible for managing reports and associated warnings.
 * Provides functionality to create reports and retrieve them by user.
 */
@Service
public class ReportService {
    private Validator<Report> reportValidator = ReportValidator.getInstance();
    private Validator<Warning> warningValidator = WarningValidator.getInstance();

    @Autowired
    private IReportRepository reportRepository;
    @Autowired
    private IWarningRepository warningRepository;

    /**
     * Creates a new report with its associated warnings after validation.
     * Performs rollback if any part of the creation fails.
     *
     * @param report The report to be saved, including its warnings.
     * @return The created report.
     * @throws InvalidCredentialsException If the report has no user ID.
     * @throws DataAccessException If saving the report or any warning fails.
     */
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

    /**
     * Retrieves all reports associated with a given user.
     *
     * @param user_id The ID of the user whose reports are to be retrieved.
     * @return A list of reports submitted by the user.
     * @throws InvalidCredentialsException If the user ID is null.
     * @throws DataAccessException If repository access fails.
     */
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
