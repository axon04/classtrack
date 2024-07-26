package com.axon;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;


public class Inserter {
    private Connection conn;

    public Inserter(String url, String username, String password) throws SQLException {
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

    // TODO: inserting attendance yet to be implemented
    public void populateAttendance() {
        String routineQuery = "SELECT * FROM routine";
    }
}
