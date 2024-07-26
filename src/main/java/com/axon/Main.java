package com.axon;

import java.io.File;
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
            Inserter inserter = new Inserter(url, username, password);
//            inserter.populateSubjects();
//            inserter.populateSlots();
//            inserter.populateRoutine();
            System.out.println(password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}