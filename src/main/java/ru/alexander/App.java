package ru.alexander;

import ru.alexander.sql.DatabaseManager;
import ru.alexander.sql.SQLParameterType;
import ru.alexander.sql.SQLTableParameter;
import ru.alexander.sql.Variable;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class App {
    public static void main(String[] args) throws GeneralSecurityException, IOException, SQLException, ClassNotFoundException {
//        String rootID = GoogleDrivePipeline.start();

        DatabaseManager.connect("memory");
        DatabaseManager.deleteTable("test");
        DatabaseManager.createTable("test",
                new SQLTableParameter(SQLParameterType.INTEGER, "id", "PRIMARY KEY AUTOINCREMENT"),
                new SQLTableParameter(SQLParameterType.TEXT, "name", "NOT NULL"),
                new SQLTableParameter(SQLParameterType.TEXT, "surname", "NOT NULL"));
        DatabaseManager.insertRow("test",
                new Variable("name", "'Alexander'"),
                new Variable("surname", "'Izmailov'"));
        DatabaseManager.insertRow("test",
                new Variable("name", "'Andrew'"),
                new Variable("surname", "'Surname'"));
        DatabaseManager.insertRow("test",
                new Variable("name", "'Имя'"),
                new Variable("surname", "'Фамилия'"));
        ResultSet test = DatabaseManager.selectByCondition("test", null, "*");
        while (test.next()) System.out.println(test.getString("name") + " " + test.getString("surname"));

        DatabaseManager.disconnect();
    }
}
