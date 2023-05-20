package ru.alexander.database;


import java.sql.*;

public interface DatabaseManager {
    //======================================WORKSPACE COMMANDS======================================
    void connect(String dbName, String path) throws SQLException, ClassNotFoundException;
    void disconnect() throws SQLException;


    //========================================TABLE COMMANDS========================================
    void createTable(String tableName, TableParameter... parameters) throws SQLException;
    void deleteTable(String tableName) throws SQLException;

    String[] listOfTables() throws SQLException;


    //=========================================DATA COMMANDS========================================
    void insertRow(String tableName, Variable... variables) throws SQLException;
    ResultSet selectByCondition(String tableName, String selectionRule, String... variables) throws SQLException;
    void updateByCondition(String tableName, String selectionRule, Variable... variables) throws SQLException;
    void deleteByCondition(String tableName, String selectionRule) throws SQLException;

    String getDataPath();

}
