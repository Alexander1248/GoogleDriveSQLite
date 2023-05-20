package ru.alexander;

import junit.framework.TestCase;
import ru.alexander.manager.StorageSystemManager;
import ru.alexander.database.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;

public class Tests extends TestCase {

    public void testCloudStoring() throws GeneralSecurityException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException, SQLException, ClassNotFoundException {
        StorageSystemManager.connect("sqlite");
        StorageSystemManager.removeDatabase("message");
        StorageSystemManager.createDatabase("message");

        StorageSystemManager.addTableInDatabase("message1");
        StorageSystemManager.addTableInDatabase("message2");
        StorageSystemManager.addTableInDatabase("message3");

        StorageSystemManager.addRowInTable("message1", "m1", 8);
        StorageSystemManager.addRowInTable("message1", "m2", 9);
        StorageSystemManager.addRowInTable("message2", "m7", 31);
        StorageSystemManager.addRowInTable("message3", "m135", 165);
        StorageSystemManager.closeDatabase();
        StorageSystemManager.loadAllDatabasesFromDrive();

        SearchingTable searchingTable = StorageSystemManager.searchInAllDatabases("name like '%m1%'");
        String data = Arrays.deepToString(searchingTable.getData());
        System.out.println(data);
        assertEquals(data, "[[message.message1.m1, message.message3.m135], [8, 165]]");

    }

}
