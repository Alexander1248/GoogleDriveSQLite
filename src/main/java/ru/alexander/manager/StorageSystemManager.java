package ru.alexander.manager;

import com.google.api.services.drive.model.File;
import ru.alexander.GoogleDrivePipeline;
import ru.alexander.database.*;
import ru.alexander.database.sql.SQLiteManager;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StorageSystemManager {
    private static String path = "";

    private static String rootID;
    private static String dbName;
    private static DatabaseManager manager;
    private static final String type = "application/octet-stream";

    public static final Map<String, Class<? extends DatabaseManager>> managerTypeMarks = new HashMap<>();

    static {
        managerTypeMarks.put("sqlite", SQLiteManager.class);
    }

    private StorageSystemManager() {}

    public static void connect(String managerType) throws GeneralSecurityException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        rootID = GoogleDrivePipeline.start();
        if (GoogleDrivePipeline.exists("StoSys"))
            rootID = GoogleDrivePipeline.findFile("StoSys");
        else rootID = GoogleDrivePipeline.createFolder(rootID, "StoSys");

        for (Map.Entry<String, Class<? extends DatabaseManager>> classEntry : managerTypeMarks.entrySet()) {
            if (classEntry.getKey().equals(managerType)) {
                manager = (DatabaseManager) classEntry.getValue().getConstructors()[0].newInstance(null);
                path = manager.getDataPath();
            }
        }
        if (manager == null) throw new InstantiationException("Database manager with current type not exists");
    }
    public static void disconnect() {
        GoogleDrivePipeline.close();
        rootID = null;
        manager = null;
    }

    public static void createDatabase(String name) throws SQLException, ClassNotFoundException {
        if (rootID != null) {
            dbName = name;
            manager.connect(dbName, path);
        }
    }
    public static void openDatabase(String name) throws IOException, SQLException, ClassNotFoundException {
        if (rootID != null) {
            FileOutputStream writer = new FileOutputStream(path + "databases/" + dbName);
            writer.write(GoogleDrivePipeline.loadFile(GoogleDrivePipeline.findFile(dbName)));
            writer.flush();
            writer.close();
            manager.connect(dbName, path);
            dbName = name;
        }
        else if (new java.io.File(path + "databases/" + dbName).exists()) {
            manager.connect(dbName, path);
            dbName = name;
        }
    }
    public static void closeDatabase() throws SQLException, IOException {
        if (dbName != null) {
            manager.disconnect();
            if (rootID != null) {
                String file = GoogleDrivePipeline.findFile(dbName);
                if (file != null) GoogleDrivePipeline.removeFile(file);

                FileInputStream writer = new FileInputStream(path + "databases/" + dbName);
                GoogleDrivePipeline.createFile(rootID, dbName, type, writer.readAllBytes());
                writer.close();
            }
            dbName = null;
        }
    }

    public static void removeDatabase(String name) {
        if (rootID != null) {
            String file = GoogleDrivePipeline.findFile(name);
            if (file != null) GoogleDrivePipeline.removeFile(file);
        }
        new java.io.File(path + "databases/" + name).delete();
    }

    public static void addTableInDatabase(String tableName) throws SQLException {
        if (dbName != null) {
            manager.createTable(tableName,
                    new TableParameter(ParameterType.INTEGER, "id", "PRIMARY KEY AUTOINCREMENT"),
                    new TableParameter(ParameterType.TEXT, "name", "UNIQUE NOT NULL"),
                    new TableParameter(ParameterType.INTEGER, "count", "NOT NULL"));
        }
    }
    public static void removeTableFromDatabase(String tableName) throws SQLException {
        if (dbName != null) manager.deleteTable(tableName);
    }

    public static void addRowInTable(String tableName, String name, int count) throws SQLException {
        if (dbName != null)
            manager.insertRow(tableName, new Variable("name", "'" + name + "'"), new Variable("count", count));

    }
    public static void editRowInTable(String tableName, String name, int count) throws SQLException {
        if (dbName != null)
            manager.updateByCondition(tableName, "name = " + name, new Variable("count", count));
    }

    public static SearchingTable searchInTable(String searchingRule) throws SQLException {
        String[] parameters = new String[] { "name", "count" };
        List<Object>[] data = new List[] { new ArrayList<>(), new ArrayList<>() };

        if (dbName != null) {
            String[] tables = manager.listOfTables();
            if (tables != null) {
                for (int j = 0; j < tables.length; j++) {
                    ResultSet resultSet = manager.selectByCondition(tables[j], searchingRule, parameters);
                    while (resultSet.next()) {
                        data[0].add(dbName + "." + tables[j] + "." + resultSet.getObject(parameters[0]));
                        data[1].add(resultSet.getObject(parameters[1]));
                    }
                }
            }

        }
        return new SearchingTable(parameters, new Object[][]{ data[0].toArray(Object[]::new), data[1].toArray(Object[]::new) });
    }

    public static void loadAllDatabasesFromDrive() throws IOException {
        if (rootID != null) {
            List<File> files = GoogleDrivePipeline.getFileList();
            if (files != null) {
                for (int i = 0; i < files.size(); i++) {
                    if (files.get(i).getParents() != null
                            && files.get(i).getParents().contains(rootID)) {

                        FileOutputStream writer = new FileOutputStream(path + "databases/" + files.get(i).getName());
                        writer.write(GoogleDrivePipeline.loadFile(GoogleDrivePipeline.findFile(files.get(i).getName())));
                        writer.flush();
                        writer.close();
                    }
                }
            }
        }
    }
    public static void saveAllDatabasesInDrive() throws IOException {
        if (rootID != null) {
            java.io.File[] databases = new java.io.File("databases").listFiles();

            if (databases != null && databases.length > 0) {
                for (int i = 0; i < databases.length; i++) {

                    String fileID = GoogleDrivePipeline.findFile(databases[i].getName());
                    if (fileID != null) GoogleDrivePipeline.removeFile(fileID);


                    FileInputStream writer = new FileInputStream(path + "databases/" + databases[i].getName());
                    GoogleDrivePipeline.createFile(rootID, databases[i].getName(), type, writer.readAllBytes());
                    writer.close();
                }
            }
        }
    }

    public static SearchingTable searchInAllDatabases(String searchingRule) throws SQLException, ClassNotFoundException, IOException {
        String[] parameters = new String[] { "name", "count" };
        List<Object>[] data = new List[] { new ArrayList<>(), new ArrayList<>() };


        java.io.File[] databases = new java.io.File("databases").listFiles();

        if (databases != null && databases.length > 0) {
            closeDatabase();
            for (int i = 0; i < databases.length; i++) {
                manager.connect(databases[i].getName(), path);

                String[] tables = manager.listOfTables();
                if (tables != null) {
                    for (int j = 0; j < tables.length; j++) {
                        ResultSet resultSet = manager.selectByCondition(tables[j], searchingRule, parameters);
                        while (resultSet.next()) {
                            data[0].add(databases[i].getName() + "." + tables[j] + "." + resultSet.getObject(parameters[0]));
                            data[1].add(resultSet.getObject(parameters[1]));
                        }
                    }
                }

                manager.disconnect();
            }
        }

        return new SearchingTable(parameters, new Object[][]{ data[0].toArray(Object[]::new), data[1].toArray(Object[]::new) });
    }
}
