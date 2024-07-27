package com.axon.dbOps;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class Populator {
    private Connection conn;

    public Populator(String url, String username, String password) throws SQLException {
        conn = DriverManager.getConnection(url, username, password);
    }

    // * Insert data into subjects
    public void populateSubjects() {
        String insert = "INSERT INTO Subjects (SubjectName) VALUES (?)";
        String[] subjects = {"MCAN-301", "MCAN-302", "MCAN-303", "MCAN-E304F", "MCAN-E305G", "MCAN-E394F", "MCAN-381", "AAT", "SST", "BREAK", "OFF"};
        try(PreparedStatement insertSubjects = conn.prepareStatement(insert)) {
            for(String subject: subjects) {
                insertSubjects.setString(1, subject);
                insertSubjects.addBatch();
            }
            insertSubjects.executeBatch();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    // * Insert data into class slots
    public void populateSlots() {
        String insert = "INSERT INTO Slots(StartTime, EndTime) VALUES (?, ?)";
        try(PreparedStatement insertSlots = conn.prepareStatement(insert)) {
            for(int i = 10; i < 17; i++) {
                String startTime = i + ":00:00";
                String endTime = (i + 1) + ":00:00";
                insertSlots.setTime(1, Time.valueOf(startTime));
                insertSlots.setTime(2, Time.valueOf(endTime));
                insertSlots.addBatch();
            }
            insertSlots.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // * Insert data into class routine
    public void populateRoutine() {
        String getSub = "SELECT * FROM Subjects";
        String getSlot = "SELECT * FROM Slots";
        String insert = "INSERT INTO Routine (Day, SlotID, SubjectID) VALUES (?, ?, ?)";

        HashMap<String, Integer> subjects = new HashMap<>();
        HashMap<Time, Integer> slots = new HashMap<>();

        try(PreparedStatement getSubjects = conn.prepareStatement(getSub);
            PreparedStatement getSlots = conn.prepareStatement(getSlot);
            ResultSet rsSubs = getSubjects.executeQuery();
            ResultSet rsSlots = getSlots.executeQuery();
            BufferedReader br = new BufferedReader(new FileReader("src/main/resources/data/routine.txt"));
            PreparedStatement setRoutine = conn.prepareStatement(insert)) {
            while(rsSubs.next()) {
                subjects.put(rsSubs.getString("subjectname"), rsSubs.getInt("subjectid"));
            }
            while(rsSlots.next()) {
                slots.put(rsSlots.getTime("starttime"), rsSlots.getInt("slotid"));
            }

            // ***** Insert rows into routine table *****
            String line;
            while((line = br.readLine()) != null) {
                String[] values = line.split(",");
                setRoutine.setString(1, values[0]);
                setRoutine.setInt(2, slots.get(Time.valueOf(values[1])));
                setRoutine.setInt(3, subjects.get(values[2]));
                setRoutine.addBatch();
            }
            setRoutine.executeBatch();
            
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    //  Class for using in hashmap
    private static class RoutineKey {
        private final String day;
        private final Time startTime;

        public RoutineKey(String day, Time startTime) {
            this.day = day;
            this.startTime = startTime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RoutineKey that = (RoutineKey) o;
            return day.equals(that.day) && startTime.equals(that.startTime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(day, startTime);
        }
    }

    private Map<RoutineKey, Integer> routineMap = new HashMap<>();

    //  Method to load routine in HashMap
    private void loadRoutine() {
        String routineQuery = """
                SELECT RoutineID, Day, StartTime
                FROM Routine
                JOIN Slots ON Routine.SlotID = Slots.SlotID;
                """;
        try(PreparedStatement getRoutine = conn.prepareStatement(routineQuery);
            ResultSet rsRoutine = getRoutine.executeQuery()) {
            while(rsRoutine.next()) {
                RoutineKey key = new RoutineKey(rsRoutine.getString("Day"), rsRoutine.getTime("StartTime"));
                routineMap.put(key, rsRoutine.getInt("RoutineID"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // * Insert data into attendance
    public void populateAttendance() {
        //  1. Get the routine inside a hashmap
        //  2. Create records in text file
        //  3. Load the routine
        //  4. Add batch to prepareStatement and execute
        loadRoutine();
        String insertQuery = "INSERT INTO Attendance(RoutineID, Date, AttendanceStatus) VALUES (?, ?, ?)";
        try(PreparedStatement setAttendance = conn.prepareStatement(insertQuery);
            BufferedReader br = new BufferedReader(new FileReader("src/main/resources/data/attendance.txt"))) {
            String line;
            while((line = br.readLine()) != null) {
                String[] values = line.split(",");
                int routineId = routineMap.get(new RoutineKey(values[0], Time.valueOf(values[1])));
                setAttendance.setInt(1, routineId);
                setAttendance.setDate(2, Date.valueOf(values[2]));
                setAttendance.setString(3, values[3]);
                setAttendance.addBatch();
            }
            setAttendance.executeBatch();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

    }
}
