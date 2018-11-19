package com.viktoriia.photori.db;

import org.intellij.lang.annotations.Language;
import org.postgresql.util.PSQLException;

import java.sql.*;

public class DatabaseConnector {

    private Connection connection;

    public DatabaseConnector() throws ClassNotFoundException, SQLException {

        Class.forName("org.postgresql.Driver");

        connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/photori",
                "postgres",
                "pass129049p");

    }

    public void sql(@Language("SQL") String sql) {
        try {
            System.out.println(sql);
            Statement statement = connection.createStatement(
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE
            );
            try {
                statement.executeUpdate(sql);
            } catch (PSQLException ignored) {
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public String[][] getSql(@Language("SQL") String sql) {
        try {
            System.out.println(sql);
            Statement statement = connection.createStatement(
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE
            );
            ResultSet resultSet = statement.executeQuery(sql);

            int col = resultSet.getMetaData().getColumnCount();
            resultSet.last();
            int row = resultSet.getRow();
            String[][] resultTable = new String[row + 1][col];

            resultSet.first();
            int j = 0;
            for (int i = 1; i <= col; i++) {
                resultTable[j][i - 1] = resultSet.getMetaData().getColumnName(i);
            }

            resultSet.beforeFirst();
            while (resultSet.next()) {
                j++;
                for (int i = 1; i <= col; i++) {
                    resultTable[j][i - 1] = resultSet.getString(i);
                }
            }

            resultSet.close();
            statement.close();

            return resultTable;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new String[][]{};
    }

    public boolean isConnected() {
        return connection != null;
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
