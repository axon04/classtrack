package com.axon;

import com.axon.dbOps.Populator;
import com.axon.dbOps.QueryExecutor;

import java.sql.*;

public class Main {
    public static void main(String[] args) {

        String url = "jdbc:postgresql://localhost:5432/testdb";
        String username = "postgres";
        String password = System.getenv("DBPASS");

        // Uncomment this to create tables first
//        CreateTables createTables = new CreateTables(url, username, password);

        // Then comment the createTables and uncomment these below
        // one by one, and run, to set up ta database
        try{
            Populator populator = new Populator(url, username, password);
//            populator.populateSubjects();
//            populator.populateSlots();
//            populator.populateRoutine();
//            populator.populateAttendance();
            QueryExecutor queryExecutor = new QueryExecutor(url, username, password);
                queryExecutor.getRoutineForDay("FRI");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}