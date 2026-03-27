package org.group45.choreday.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.group45.choreday.models.UserModel;
import org.group45.choreday.utils.DatabaseConnectionPool;

public class SignUpService {

    public UserModel signUp(String fullName, String studentId, String password) {
        String sql = "INSERT INTO users (full_name, student_id, password) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, fullName);
            stmt.setString(2, studentId);
            stmt.setString(3, password);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                return UserModel.builder()
                        .fullName(fullName)
                        .studentId(studentId)
                        .password(password)
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
