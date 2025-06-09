package org.example.domain.validators;

import org.example.domain.Warning;

public class WarningValidator implements Validator<Warning> {
    private static final WarningValidator instance = new WarningValidator();
    private WarningValidator(){};

    public static WarningValidator getInstance() {
        return instance;
    }

    private Boolean validLat(Double lat) {
        return lat >= -90 && lat <= 90;
    }

    private Boolean validLng(Double lon) {
        return lon >= -180 && lon <= 180;
    }

    @Override
    public void validate(Warning entity) throws ValidationException {
        String errors = "";
        if(!validLat(entity.getLat()) || !validLng(entity.getLng())) {
            errors += "Lat and Lng are not valid!\n";
        }
        if(entity.getText() == null || entity.getText().isEmpty()) {
            errors += "Text cannot be empty!\n";
        }
        if(!errors.isEmpty()){
            throw new ValidationException("WarningValidator: " + errors);
        }
    }
}