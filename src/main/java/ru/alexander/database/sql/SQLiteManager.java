package ru.alexander.database.sql;


import ru.alexander.database.DatabaseManager;
import ru.alexander.database.TableParameter;
import ru.alexander.database.Variable;

import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SQLiteManager implements DatabaseManager {
    private Connection connection;
    private Statement statement;

    private static final byte[] pass = {
            39, 121, 15, 54, -95, 25, -5, 85, -86, 61, -28, 118, -27, -97, -51, -20, -86, -78, 86, -90,
            63, -55, -96, -36, 23, 65, 58, -27, -45, 28, -6, -30, 92, -27, -82, -113, 50, 104, 55, -52,
            -100, 103, 43, 53, -74, -102, 64, 42, -122, 91, -14, 120, 17, 14, -72, -51, 82, 112, 109, -81,
            62, 82, 52, 84, 56, 81, -34, 82, -105, -98, 15, 39, -59, -36, 27, 66, 27, -47, -56, 65,
            -101, 87, -37, 98, 127, -27, -76, -11, 95, 45, -61, 107, -116, 56, -51, 50, 86, 76, -8, -23
    };

    public SQLiteManager() {}

    //======================================WORKSPACE COMMANDS======================================
    public void connect(String dbName, String path) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");

        connection = DriverManager.getConnection("jdbc:sqlite:" + path + "databases/" + dbName,
                Base64.getEncoder().encodeToString(dbName.getBytes()),
                Base64.getEncoder().encodeToString(pass));
        statement = connection.createStatement();
    }
    public void disconnect() throws SQLException {
        connection.close();
        statement.close();
    }


    //========================================TABLE COMMANDS========================================
    public void createTable(String tableName, TableParameter... parameters) throws SQLException {
        StringBuilder parStr = new StringBuilder();

        TableParameter parameter = parameters[0];
        parStr.append(parameter.name()).append(" ").append(parameter.type().name()).append(" ").append(parameter.parameters());

        for (int i = 1; i < parameters.length; i++) {
            parameter = parameters[i];
            parStr.append(", ").append(parameter.name()).append(" ");
            parStr.append(parameter.type().name()).append(" ").append(parameter.parameters());
        }

        statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (" + parStr + ");");
    }
    public void deleteTable(String tableName) throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS " + tableName);
    }


    //=========================================DATA COMMANDS========================================
    public void insertRow(String tableName, Variable... variables) throws SQLException {
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
    public ResultSet selectByCondition(String tableName, String selectionRule, String... variables) throws SQLException {
        StringBuilder valStr = new StringBuilder();

        valStr.append(variables[0]);
        for (int i = 1; i < variables.length; i++)
            valStr.append(", ").append(variables[i]);

        if (selectionRule == null || selectionRule.isEmpty()) return statement.executeQuery("SELECT " + valStr + " FROM " + tableName + ";");
        else return statement.executeQuery("SELECT " + valStr + " FROM " + tableName + " WHERE " + selectionRule + ";");
    }
    public void updateByCondition(String tableName, String selectionRule, Variable... variables) throws SQLException {
        StringBuilder valStr = new StringBuilder();

        valStr.append(variables[0].variable()).append(" = ").append(variables[0].value());
        for (int i = 1; i < variables.length; i++)
            valStr.append(", ").append(variables[i].variable()).append(" = ").append(variables[i].value());

        statement.executeUpdate("UPDATE " + tableName + " SET " + valStr + " WHERE " + selectionRule + ";");
    }
    public void deleteByCondition(String tableName, String selectionRule) throws SQLException {
        statement.executeUpdate("DELETE FROM " + tableName + " WHERE " + selectionRule + ";");
    }
    public String[] listOfTables() throws SQLException {
        ResultSet tables = statement.executeQuery("SELECT name FROM sqlite_schema WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY 1;");
        List<String> tbls = new ArrayList<>();
        while (tables.next()) tbls.add(tables.getString("name"));
        return tbls.toArray(String[]::new);
    }

    @Override
    public String getDataPath() {
        return "";
    }
}
