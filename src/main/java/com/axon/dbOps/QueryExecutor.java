package com.axon.dbOps;

import java.sql.*;

public class QueryExecutor {
    private Connection conn;

    public QueryExecutor(String url, String username, String password) throws SQLException {
        conn = DriverManager.getConnection(url, username, password);
    }

    // * Get routine for each day
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

    // * Get total number of classes for one subject
    public void getTotalClasses(String subject) {
        String query = """
                SELECT COUNT(*)
                FROM attendance
                WHERE routineid IN(
                	SELECT routineid
                	FROM routine
                	JOIN subjects ON routine.subjectid = subjects.subjectid
                	WHERE subjects.subjectname = ?
                );
                """;
        try(PreparedStatement getClasses = conn.prepareStatement(query)) {
            getClasses.setString(1, subject);
            try(ResultSet rs = getClasses.executeQuery()) {
                int count = rs.next() ? rs.getInt(1) : 0;
                System.out.println("Total classes for " + subject + " so far: " + count);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // * Get total present count for a subject
    public void getPresent(String subject) {
        String query = """
                SELECT COUNT(*)
                FROM attendance
                WHERE routineid IN(
                	SELECT routineid
                	FROM routine
                	JOIN subjects ON routine.subjectid = subjects.subjectid
                	WHERE subjects.subjectname = ?
                ) AND attendancestatus = 'PRESENT';
                """;
        try(PreparedStatement getPresent = conn.prepareStatement(query)) {
            getPresent.setString(1, subject);
            try(ResultSet rs = getPresent.executeQuery()) {
                int count = rs.next() ? rs.getInt(1) : 0;
                System.out.println("Total present for " + subject + " so far: " + count);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: Create all necessary queries as methods for the Service Layer to work with when receiving requests from controller
}
