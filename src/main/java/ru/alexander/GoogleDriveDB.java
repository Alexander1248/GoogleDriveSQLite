package ru.alexander;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.model.File;
import ru.alexander.sql.DatabaseManager;
import ru.alexander.sql.SearchingTable;

import java.io.*;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GoogleDriveDB {
    private static String rootID;
    private static String dbName;

    public static void connect() throws GeneralSecurityException, IOException {
        rootID = GoogleDrivePipeline.start();
    }
    public static void disconnect() {
        GoogleDrivePipeline.close();
        rootID = null;
    }

    public static void createDatabase(String name) throws SQLException, ClassNotFoundException {
        if (rootID != null) {
            dbName = name;
            DatabaseManager.connect(dbName);
        }
    }
    public static void openDatabase(String name) throws IOException, SQLException, ClassNotFoundException {
        if (rootID != null) {
            dbName = name;
            PrintWriter writer = new PrintWriter("databases/" + dbName);
            writer.write(GoogleDrivePipeline.loadFile(GoogleDrivePipeline.findFile(dbName)).toString());
            writer.flush();
            writer.close();
            DatabaseManager.connect(dbName);
        }
    }
    public static void closeDatabase() throws SQLException, IOException {
        if (rootID != null) {
            DatabaseManager.disconnect();

            String file = GoogleDrivePipeline.findFile(dbName);
            if (file != null) GoogleDrivePipeline.removeFile(file);

            FileInputStream inputStream = new FileInputStream("databases/" + dbName);
            GoogleDrivePipeline.createFile(rootID, dbName, new ByteArrayContent("text/plain", inputStream.readAllBytes()));
            inputStream.close();

            dbName = null;
        }
    }

    public static void addTableInDatabase() {
        
    }


    public static void loadAllDatabasesFromDrive() throws IOException {
        if (rootID != null) {
            List<File> files = GoogleDrivePipeline.getFileList();
            if (files != null) {
                for (File file : files)
                    if (!file.getId().equals(rootID)) {
                        PrintWriter writer = new PrintWriter("databases/" + file.getName());
                        writer.write(GoogleDrivePipeline.loadFile(file.getId()).toString());
                        writer.flush();
                        writer.close();
                    }
            }
        }
    }
    public static SearchingTable searchInAllDatabases(String searchingRule) throws SQLException, ClassNotFoundException {
        String[] parameters = new String[] { "item", "count" };
        List<Object>[] data = new List[] { new ArrayList<>(), new ArrayList<>() };


        java.io.File[] databases = new java.io.File("databases").listFiles();

        if (databases != null && databases.length > 0) {
            DatabaseManager.disconnect();
            for (int i = 0; i < databases.length; i++) {
                DatabaseManager.connect(databases[i].getName());

                String[] tables = DatabaseManager.listOfTables();
                if (tables != null) {
                    for (int j = 0; j < tables.length; i++) {
                        ResultSet resultSet = DatabaseManager.selectByCondition(tables[j], searchingRule, parameters);
                        while (resultSet.next()) {
                            data[0].add(resultSet.getObject(parameters[0]));
                            data[1].add(resultSet.getObject(parameters[1]));
                        }
                    }
                }

                DatabaseManager.disconnect();
            }
        }

        return new SearchingTable(parameters, new Object[][]{ data[0].toArray(Object[]::new), data[1].toArray(Object[]::new) });
    }
}
