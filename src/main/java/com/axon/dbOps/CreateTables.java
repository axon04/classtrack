package com.axon.dbOps;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateTables {
    public CreateTables(String url, String username, String password) {
        try(Connection conn = DriverManager.getConnection(url, username, password)) {

            // Create table ClassSlots
            String createTableSlots = """
                    CREATE TABLE Slots (
                        SlotID SERIAL PRIMARY KEY,
                        StartTime TIME NOT NULL,
                        EndTime TIME NOT NULL
                    )
                    """;
            try(PreparedStatement createStmt = conn.prepareStatement(createTableSlots)) {
                createStmt.execute();
            }

            // Create table Subjects
            String createTableSubjects = """
                    CREATE TABLE Subjects (
                      SubjectID SERIAL PRIMARY KEY,
                      SubjectName VARCHAR(50) NOT NULL
                    );
                    """;
            try(PreparedStatement createStmt = conn.prepareStatement(createTableSubjects)) {
                createStmt.execute();
            }

            // Create table ClassRoutine
            String createTableRoutine = """
                    CREATE TABLE Routine (
                      RoutineID SERIAL PRIMARY KEY,
                      Day VARCHAR(3) NOT NULL CHECK (Day IN ('SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT')),
                      SlotID INT NOT NULL,
                      SubjectID INT NOT NULL,
                      FOREIGN KEY (SlotID) REFERENCES Slots(SlotID),
                      FOREIGN KEY (SubjectID) REFERENCES Subjects(SubjectID)
                    );
                    """;
            try(PreparedStatement createStmt = conn.prepareStatement(createTableRoutine)) {
                createStmt.execute();
            }

            // Create table Attendance
            String createTableAttendance = """
                    CREATE TABLE Attendance (
                      AttendanceID SERIAL PRIMARY KEY,
                      RoutineID INT NOT NULL,
                      Date DATE NOT NULL,
                      AttendanceStatus VARCHAR(10) NOT NULL DEFAULT 'NOCLASS' CHECK (AttendanceStatus IN ('PRESENT', 'ABSENT', 'NOCLASS')),
                      FOREIGN KEY (RoutineID) REFERENCES Routine(RoutineID)
                    );
                    """;
            try(PreparedStatement createStmt = conn.prepareStatement(createTableAttendance)) {
                createStmt.execute();
            }

        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}
