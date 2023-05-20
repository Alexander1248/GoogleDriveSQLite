package ru.alexander.database;

public class SearchingTable {
    private final String[] parameters;
    private final Object[][] data;

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

    public Object[][] getData() {
        return data;
    }
}
