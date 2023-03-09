package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.config.Entity;
import com.zuehlke.securesoftwaredevelopment.domain.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CommentRepository {

    private static final Logger LOG = LoggerFactory.getLogger(CommentRepository.class);


    private DataSource dataSource;

    public CommentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    private String getComments(Comment comment) {
        List<Comment> list = getAll(String.valueOf(comment.getMovieId()));
        String all=null;
        for(int i=0; i < list.size(); i++){
            all += list.get(i).getComment();

        }
        return all;
    }
    public void create(Comment comment) {
        String beforeAdding = getComments(comment);
        String query = "insert into comments(movieId, userId, comment) values (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstm=connection.prepareStatement(query);
        ) {
            pstm.setInt(1,comment.getMovieId());
            pstm.setInt(2,comment.getUserId());
            pstm.setString(3,comment.getComment());
            pstm.executeUpdate();
            String afterAdding = getComments(comment);

            //Pri vršenju promene
            Entity ent=new Entity("You added comment successfully for the movie",  comment.getMovieId() + "",beforeAdding,afterAdding);
            AuditLogger.getAuditLogger(CommentRepository.class)
                    .auditChange(ent);
        } catch (SQLException e) {
            LOG.warn("Error while creating comment!");
        }
    }

    public List<Comment> getAll(String movieId) {
        List<Comment> commentList = new ArrayList<>();
        String query = "SELECT movieId, userId, comment FROM comments WHERE movieId = " + movieId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                commentList.add(new Comment(rs.getInt(1), rs.getInt(2), rs.getString(3)));
            }
        } catch (SQLException e) {
            LOG.warn("Error while getting comments!");
        }
        return commentList;
    }
}
