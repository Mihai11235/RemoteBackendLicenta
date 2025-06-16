package org.example.domain.validators;

import org.example.business.exception.ValidationException;
import org.example.domain.User;

/**
 * UserValidator is a singleton class that implements the Validator interface for User entities.
 * It validates the properties of a User object to ensure they meet specific criteria.
 */
public class UserValidator implements Validator<User> {
    private static final UserValidator instance = new UserValidator();
    private UserValidator(){};

    public static UserValidator getInstance() {
        return instance;
    }

    /**
     * Validates the properties of a User object.
     *
     * @param entity the User object to validate
     * @throws ValidationException if any validation rule is violated
     */
    @Override
    public void validate(User entity) throws ValidationException {
        String errors = "";

        if(entity == null || entity.getName() == null || !entity.getName().matches("^[A-Z][ a-zA-Z]*$")){
            errors += "Name must be capitalized and must contain only letters!\n";
        }
        if(entity == null || entity.getUsername() == null || !entity.getUsername().matches("^[a-zA-Z][a-zA-Z0-9]*$")){
            errors += "Username must be alphanumeric and start with a letter!\n";
        }
        if(entity == null || entity.getPassword() == null){
            errors += "Password cannot be null!\n";
        }
        if(!errors.isEmpty()){
            throw new ValidationException("UserValidator: " + errors);
        }
    }
}

