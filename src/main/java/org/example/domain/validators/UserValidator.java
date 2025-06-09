package org.example.domain.validators;

import org.example.domain.User;

public class UserValidator implements Validator<User> {
    private static final UserValidator instance = new UserValidator();
    private UserValidator(){};

    public static UserValidator getInstance() {
        return instance;
    }

    @Override
    public void validate(User entity) throws ValidationException {
        String errors = "";

        if(entity == null || entity.getName() == null || !entity.getName().matches("^[A-Z][ a-zA-Z]*$")){
            errors += "Name must be capitalized and must contain only letters!\n";
        }
        if(entity == null || !entity.getUsername().matches("^[a-zA-Z][a-zA-Z0-9]*$")){
            errors += "Username must be alphanumeric and start with a letter!\n";
        }
        if(!errors.isEmpty()){
            throw new ValidationException("UserValidator: " + errors);
        }
    }
}

