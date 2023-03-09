package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.config.Entity;
import com.zuehlke.securesoftwaredevelopment.domain.Person;
import com.zuehlke.securesoftwaredevelopment.domain.Rating;
import com.zuehlke.securesoftwaredevelopment.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

@Repository
public class UserRepository {

    private static final Logger LOG = LoggerFactory.getLogger(UserRepository.class);

    private DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public User findUser(String username) {
        String query = "SELECT id, username, password FROM users WHERE username='" + username + "'";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            if (rs.next()) {
                int id = rs.getInt(1);
                String username1 = rs.getString(2);
                String password = rs.getString(3);
                return new User(id, username1, password);
            }
        } catch (SQLException e) {
            LOG.warn("Error while trying to find the user by username!");
        }
        AuditLogger.getAuditLogger(UserRepository.class).audit("User with username"+username+"doesn't exist!");
        return null;
    }

    public boolean validCredentials(String username, String password) {
        String query = "SELECT username FROM users WHERE username='" + username + "' AND password='" + password + "'";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            AuditLogger.getAuditLogger(UserRepository.class).audit("Validation of credentials for the user: "+username+ "is successfull!");

            return rs.next();
        } catch (SQLException e) {
            LOG.warn("Error while validating credentials of the user!");
        }
        AuditLogger.getAuditLogger(UserRepository.class).audit("Validation of credentials for the user: "+username+ "has failed!");
        return false;
    }
//potreban search po userId koji je tipa int
    public User findIdInt(int id){
        String query = "SELECT id, username, password FROM users WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps=connection.prepareStatement(query);){
            ps.setInt(1,id);
            ResultSet rs = ps.executeQuery(query); {
                if (rs.next()) {
                    int id1 = rs.getInt(1);
                    String username1 = rs.getString(2);
                    String password = rs.getString(3);
                    return new User(id1, username1, password);
                }}
        } catch (SQLException e) {
            LOG.warn("Error while getting the list of ratings for the user and the movie!");
        }
        AuditLogger.getAuditLogger(UserRepository.class).audit("User with id"+id+"doesn't exist!");
        return null;
    }

    public void delete(int userId) {
        User before=findIdInt(userId);
        String query = "DELETE FROM users WHERE id = " + userId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
        ) {
            statement.executeUpdate(query);
            User after=findIdInt(userId);
            Entity ent=new Entity("User was deleted",userId+" ",before.getUsername(),after.getUsername());
            AuditLogger.getAuditLogger(UserRepository.class).auditChange(ent);
        } catch (SQLException e) {
            LOG.warn("Error while deleting the user!");
        }
    }
}
