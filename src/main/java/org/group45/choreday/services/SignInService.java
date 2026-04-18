package org.group45.choreday.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.group45.choreday.models.UserModel;
import org.group45.choreday.utils.DatabaseConnectionPool;

public class SignInService {

    public UserModel signIn(String studentId, String password) {
        String sql = "SELECT * FROM users WHERE student_id = ? AND password = ?";
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return UserModel.builder()
                        .studentId(rs.getString("student_id"))
                        .fullName(rs.getString("full_name"))
                        .password(rs.getString("password"))
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
