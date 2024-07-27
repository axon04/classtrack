package com.axon.dbOps;

import java.sql.*;

public class QueryExecutor {
    private Connection conn;

    public QueryExecutor(String url, String username, String password) throws SQLException {
        conn = DriverManager.getConnection(url, username, password);
    }

    public void getRoutineForDay(String day) {
        String query = """
                SELECT routineid, day, starttime, subjectname
                FROM routine
                JOIN slots ON routine.slotid = slots.slotid
                JOIN subjects ON routine.subjectid = subjects.subjectid
                WHERE routine.day = ?
                ORDER BY starttime ASC;
                """;
        try(PreparedStatement getRoutine = conn.prepareStatement(query)) {
            getRoutine.setString(1, day);
            try(ResultSet rs = getRoutine.executeQuery()) {
                while(rs.next()) {
                    System.out.println(rs.getInt("RoutineID") + "\t" + rs.getString("Day") + "\t" + rs.getTime("StartTime") + "\t" + rs.getString("SubjectName"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    

    // TODO: Create all necessary queries as methods for the Service Layer to work with when receiving requests from controller
}
