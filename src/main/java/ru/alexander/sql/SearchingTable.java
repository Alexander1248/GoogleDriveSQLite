package ru.alexander.sql;

import java.util.Arrays;
import java.util.List;

public class SearchingTable {
    private String[] parameters;
    private Object[][] data;

    public SearchingTable(String[] parameters, Object[][] data) {
        this.parameters = parameters;
        this.data = data;
    }

    public String[] getParameters() {
        return parameters;
    }

    public Object[] getDataByParameter(String parameter) {
        for (int i = 0; i < parameters.length; i++)
            if (parameters[i].equals(parameter))
                return data[i];
        return null;
    }
}
