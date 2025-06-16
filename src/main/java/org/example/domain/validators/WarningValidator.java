package org.example.domain.validators;

import org.example.business.exception.ValidationException;
import org.example.domain.Warning;

/**
 * WarningValidator is a singleton class that implements the Validator interface for validating Report objects.
 * It checks if the latitude and longitude values are within valid ranges and if the warnings list is not empty.
 */
public class WarningValidator implements Validator<Warning> {
    private static final WarningValidator instance = new WarningValidator();
    private WarningValidator(){};

    public static WarningValidator getInstance() {
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
     * @param lon the longitude value to validate
     * @return true if the longitude is valid, false otherwise
     */
    private Boolean validLng(Double lon) {
        return lon >= -180 && lon <= 180;
    }

    /**
     * Validates a Warning entity.
     * Checks if latitude and longitude are within valid ranges and if the text is not empty.
     *
     * @param entity the Warning entity to validate
     * @throws ValidationException if validation fails
     */
    @Override
    public void validate(Warning entity) throws ValidationException {
        String errors = "";
        if(!validLat(entity.getLat()) || !validLng(entity.getLng())) {
            errors += "Invalid lat/lng coordinates!\n";
        }
        if(entity.getText() == null || entity.getText().isEmpty()) {
            errors += "Text cannot be empty!\n";
        }
        if(!errors.isEmpty()){
            throw new ValidationException("WarningValidator: " + errors);
        }
    }
}