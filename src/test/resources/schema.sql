-- Drop tables in reverse order of dependency to avoid foreign key errors
DROP TABLE IF EXISTS warnings;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS Users;

-- Create Users table
CREATE TABLE Users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       name VARCHAR(255)
);

-- Create reports table
CREATE TABLE reports (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         start_lat FLOAT NOT NULL,
                         start_lng FLOAT NOT NULL,
                         end_lat FLOAT NOT NULL,
                         end_lng FLOAT NOT NULL,
                         created_at BIGINT,
                         user_id BIGINT NOT NULL,
                         FOREIGN KEY (user_id) REFERENCES Users(id)
);

-- Create warnings table
CREATE TABLE warnings (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          report_id BIGINT NOT NULL,
                          text VARCHAR(1000) NOT NULL,
                          lat FLOAT NOT NULL,
                          lng FLOAT NOT NULL,
                          created_at BIGINT,
                          FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE
);