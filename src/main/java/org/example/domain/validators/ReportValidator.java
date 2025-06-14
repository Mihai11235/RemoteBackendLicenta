package org.example.domain.validators;

import org.example.business.exception.ValidationException;
import org.example.domain.Report;

public class ReportValidator implements Validator<Report> {
    private static final ReportValidator instance = new ReportValidator();
    private ReportValidator(){};

    public static ReportValidator getInstance() {
        return instance;
    }

    private Boolean validLat(Double lat) {
        return lat >= -90 && lat <= 90;
    }

    private Boolean validLng(Double lng) {
        return lng >= -180 && lng <= 180;
    }

    @Override
    public void validate(Report entity) throws ValidationException {
        if (entity == null) {
            throw new ValidationException("Report cannot be null.");
        }
        String errors = "";

        if(entity.getStart_lat() == null || entity.getStart_lng() == null || entity.getEnd_lat() == null || entity.getEnd_lng() == null){
            errors += "Missing report fields.\n";
        }
        else if(!validLat(entity.getStart_lat()) || !validLat(entity.getEnd_lat())
        || !validLng(entity.getStart_lng()) || !validLng(entity.getEnd_lng())) {
            errors += "Invalid lat/lng coordinates!\n";
        }

        if(entity.getWarnings() == null) {
            errors += "Warnings cannot be null!\n";
        }
        else if(entity.getWarnings().isEmpty()) {
            errors += "No warnings found!\n";
        }
        if(!errors.isEmpty()){
            throw new ValidationException("ReportValidator: " + errors);
        }
    }
}
