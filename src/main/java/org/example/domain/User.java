package org.example.domain;

/**
 * Represents an application user with login credentials and a display name.
 */
public class User extends Entity<Long>{
    String username; // The unique username of the user
    String password; // The user's hashed password
    String name;  // The user's full name or display name

    /**
     * Default constructor.
     */
    public User() {}


    /**
     * Constructs a user with specified username, password, and name.
     *
     * @param username the user's username
     * @param password the user's password (hashed)
     * @param name     the user's full/display name
     */
    public User(String username, String password, String name) {
        this.username = username;
        this.password = password;
        this.name = name;
    }

    // Standard getters/setters for each field

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a string representation of the user (includes password).
     *
     * @return a formatted string with user details
     */
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
