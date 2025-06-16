package org.example.domain.validators;

import org.example.business.exception.ValidationException;
import org.example.domain.Report;

/**
 * ReportValidator is a singleton class that implements the Validator interface for validating Report objects.
 * It checks if the latitude and longitude values are within valid ranges and if the warnings list is not empty.
 */
public class ReportValidator implements Validator<Report> {
    private static final ReportValidator instance = new ReportValidator();
    private ReportValidator(){};

    public static ReportValidator getInstance() {
        return instance;
    }


    /**
     * Validates if the latitude value is within the valid range of -90 to 90 degrees.
     *
     * @param lat the latitude value to validate
     * @return true if the latitude is valid, false otherwise
     */
    private Boolean validLat(Double lat) {
        return lat >= -90 && lat <= 90;
    }

    /**
     * Validates if the longitude value is within the valid range of -180 to 180 degrees.
     *
     * @param lng the longitude value to validate
     * @return true if the longitude is valid, false otherwise
     */
    private Boolean validLng(Double lng) {
        return lng >= -180 && lng <= 180;
    }

    /**
     * Validates the properties of a Report object.
     *
     * @param entity the Report object to validate
     * @throws ValidationException if any validation rule is violated
     */
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
