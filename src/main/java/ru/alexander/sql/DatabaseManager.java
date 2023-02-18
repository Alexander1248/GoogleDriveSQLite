package ru.alexander.sql;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static Connection connection;
    private static Statement statement;

    //======================================WORKSPACE COMMANDS======================================
    public static void connect(String dbName) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");

        connection = DriverManager.getConnection("jdbc:sqlite:databases/" + dbName);
        statement = connection.createStatement();
    }
    public static void disconnect() throws SQLException {
        connection.close();
        statement.close();
    }


    //========================================TABLE COMMANDS========================================
    public static void createTable(String tableName, SQLTableParameter... parameters) throws SQLException {
        StringBuilder parStr = new StringBuilder();

        SQLTableParameter parameter = parameters[0];
        parStr.append(parameter.name()).append(" ").append(parameter.type().name()).append(" ").append(parameter.parameters());

        for (int i = 1; i < parameters.length; i++) {
            parameter = parameters[i];
            parStr.append(", ").append(parameter.name()).append(" ");
            parStr.append(parameter.type().name()).append(" ").append(parameter.parameters());
        }

        statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (" + parStr + ");");
    }
    public static void deleteTable(String tableName) throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS " + tableName);
    }


    //=========================================DATA COMMANDS========================================
    public static void insertRow(String tableName, Variable... variables) throws SQLException {
        StringBuilder parStr = new StringBuilder();
        StringBuilder valStr = new StringBuilder();

        parStr.append(variables[0].variable());
        valStr.append(variables[0].value());
        for (int i = 1; i < variables.length; i++) {
            parStr.append(", ").append(variables[i].variable());
            valStr.append(", ").append(variables[i].value());
        }

        statement.executeUpdate("INSERT INTO " + tableName + " (" + parStr + ") VALUES (" + valStr + ");");
    }
    public static ResultSet selectByCondition(String tableName, String selectionRule, String... variables) throws SQLException {
        StringBuilder valStr = new StringBuilder();

        valStr.append(variables[0]);
        for (int i = 1; i < variables.length; i++)
            valStr.append(", ").append(variables[i]);

        if (selectionRule == null || selectionRule.isEmpty()) return statement.executeQuery("SELECT " + valStr + " FROM " + tableName + ";");
        else return statement.executeQuery("SELECT " + valStr + " FROM " + tableName + " WHERE " + selectionRule + ";");
    }
    public static void updateByCondition(String tableName, String selectionRule, Variable... variables) throws SQLException {
        StringBuilder valStr = new StringBuilder();

        valStr.append(variables[0].variable()).append(" = ").append(variables[0].value());
        for (int i = 1; i < variables.length; i++)
            valStr.append(", ").append(variables[i].variable()).append(" = ").append(variables[i].value());

        statement.executeUpdate("UPDATE " + tableName + " SET " + valStr + " WHERE " + selectionRule + ";");
    }
    public static void deleteByCondition(String tableName, String selectionRule) throws SQLException {
        statement.executeUpdate("DELETE FROM " + tableName + " WHERE " + selectionRule + ";");
    }
    public static String[] listOfTables() throws SQLException {
        ResultSet tables = statement.executeQuery("SELECT name FROM sqlite_schema WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY 1;");
        List<String> tbls = new ArrayList<>();
        while (tables.next()) tbls.add(tables.getString("name"));
        return tbls.toArray(String[]::new);
    }

}
