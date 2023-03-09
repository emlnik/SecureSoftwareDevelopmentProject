package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.config.Entity;
import com.zuehlke.securesoftwaredevelopment.domain.Comment;
import com.zuehlke.securesoftwaredevelopment.domain.Person;
import com.zuehlke.securesoftwaredevelopment.domain.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RatingRepository {

    private static final Logger LOG = LoggerFactory.getLogger(RatingRepository.class);


    private DataSource dataSource;

    public RatingRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    //treba mi lista ocena za odgovarajuci film i za odg. osobu
    public List<Rating> getUserRatingForMovie(String movieId,String userId) {
        List<Rating> list = new ArrayList<>();
        String query = "SELECT movieId, userId, rating FROM ratings WHERE movieId = ?" + "and userId =?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps=connection.prepareStatement(query);){

        ps.setString(1,movieId);
        ps.setString(2,userId);

             ResultSet rs = ps.executeQuery(query); {
            while (rs.next()) {
                list.add(new Rating(rs.getInt(1), rs.getInt(2), rs.getInt(3)));
            }}
        } catch (SQLException e) {
            LOG.warn("Error while getting the list of ratings for the user and the movie!");
        }
        return list;
    }
    private String get(Rating r) {
        List<Rating> list =getUserRatingForMovie(String.valueOf(r.getMovieId()),String.valueOf(r.getUserId()));
        String all=null;
        for(int i=0; i < list.size(); i++){

            all += String.valueOf(list.get(i).getRating());

        }
        return all;
    }

    public void createOrUpdate(Rating rating) {

        String before=get(rating);

        String query = "SELECT movieId, userId, rating FROM ratings WHERE movieId = " + rating.getMovieId() + " AND userID = " + rating.getUserId();
        String query2 = "update ratings SET rating = ? WHERE movieId = ? AND userId = ?";
        String query3 = "insert into ratings(movieId, userId, rating) values (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)
        ) {
            if (rs.next()) {
                PreparedStatement preparedStatement = connection.prepareStatement(query2);
                preparedStatement.setInt(1, rating.getRating());
                preparedStatement.setInt(2, rating.getMovieId());
                preparedStatement.setInt(3, rating.getUserId());
                preparedStatement.executeUpdate();
                String after1=get(rating);
                Entity ent=new Entity("Rating is updated",rating.getRating()+"",before,after1);
                AuditLogger.getAuditLogger(RatingRepository.class).auditChange(ent);
            } else { //ovde radi insert
                PreparedStatement preparedStatement = connection.prepareStatement(query3);
                preparedStatement.setInt(1, rating.getMovieId());
                preparedStatement.setInt(2, rating.getUserId());
                preparedStatement.setInt(3, rating.getRating());
                preparedStatement.executeUpdate();
                String after2=get(rating);
                Entity ent=new Entity("Rating is added",rating.getRating()+"",before,after2);
                AuditLogger.getAuditLogger(RatingRepository.class).auditChange(ent);
            }
        } catch (SQLException e) {
            LOG.warn("Error while trying to update or create rating!");
        }
    }

    public List<Rating> getAll(String movieId) {
        List<Rating> ratingList = new ArrayList<>();
        String query = "SELECT movieId, userId, rating FROM ratings WHERE movieId = " + movieId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                ratingList.add(new Rating(rs.getInt(1), rs.getInt(2), rs.getInt(3)));
            }
        } catch (SQLException e) {
            LOG.warn("Error while getting the list of ratings for the movie!");
        }
        return ratingList;
    }
}
