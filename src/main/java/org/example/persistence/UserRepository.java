package org.example.persistence;

import org.example.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UserRepository implements IUserRepository{
    private final DataSource dataSource;

    @Autowired
    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    @Override
    public Optional<User> findOne(Long id) {
        User user = null;
        try(Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE id = ?");
        ){
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String name = resultSet.getString("name");

                user = new User(username, password, name); user.setId(id);
            }
        }
        catch (SQLException e){
            throw new RepositoryException("UserRepository: " + e);
        }
        return Optional.ofNullable(user);
    }


    @Override
    public Optional<User> findOneByUsername(String username) {
        User user = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
        ) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                Long id = resultSet.getLong("id");
                String password = resultSet.getString("password");
                String name = resultSet.getString("name");

                user = new User(username, password, name); user.setId(id);
            }
        } catch (SQLException e) {
            throw new RepositoryException("UserRepository: " + e);
        }
        return Optional.ofNullable(user);
    }

    @Override
    public Iterable<User> getAll() {
        List<User> users=new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users");
        ) {
            try(ResultSet resultSet=preparedStatement.executeQuery()) {
                while (resultSet.next()) {

                    Long id = resultSet.getLong("id");
                    String username = resultSet.getString("username");
                    String password = resultSet.getString("password");
                    String name = resultSet.getString("name");

                    User user = new User(username, password, name); user.setId(id);
                    users.add(user);
                }                                     }

        } catch (SQLException e) {
            System.err.println("Error DB "+e);
            throw new RepositoryException("UserRepository: " + e);
        }
        return users;
    }

    @Override
    public Optional<User> add(User entity) {
        try(Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users (username, password, name) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
        ){
            preparedStatement.setString(1, entity.getUsername());
            preparedStatement.setString(2, entity.getPassword());
            preparedStatement.setString(3, entity.getName());
            int affectedRows = preparedStatement.executeUpdate();
            ResultSet keys = preparedStatement.getGeneratedKeys();
            if (keys.next()) {
                entity.setId(keys.getLong(1));
            }
            return affectedRows == 0 ? Optional.empty() : Optional.of(entity);
        }
        catch (SQLException e){
            throw new RepositoryException("UserRepository: " + e);
        }
    }

    @Override
    public Optional<User> delete(Long aLong) {
        Optional<User> entityOpt = findOne(aLong); // Assuming this method retrieves by ID

        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }

        try(Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM users where id=?");
        ){
            preparedStatement.setLong(1, aLong);
            int affectedRows = preparedStatement.executeUpdate();

            return affectedRows == 0 ? Optional.empty() : entityOpt;
        }
        catch (SQLException e){
            throw new RepositoryException("UserRepository: " + e);
        }
    }

    @Override
    public Optional<User> update(User entity) {
        return Optional.empty();
    }
}
