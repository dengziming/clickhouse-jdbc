package ru.yandex.clickhouse;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ClickHouseConnectionTest {
    @Test
    public void testGetSetCatalog() throws SQLException {
        ClickHouseDataSource dataSource = new ClickHouseDataSource(
                "jdbc:clickhouse://localhost:8123/default?option1=one%20two&option2=y");
        String[] dbNames = new String[]{"get_set_catalog_test1", "get_set_catalog_test2"};
        try {
            ClickHouseConnectionImpl connection = (ClickHouseConnectionImpl) dataSource.getConnection();
            assertEquals(connection.getUrl(), "jdbc:clickhouse://localhost:8123/default?option1=one%20two&option2=y");
            assertEquals(connection.getCatalog(), "default");
            assertEquals(connection.getProperties().getDatabase(), "default");

            for (String db : dbNames) {
                connection.createStatement().executeUpdate("CREATE DATABASE " + db);
                connection.createStatement().executeUpdate(
                        "CREATE TABLE " + db + ".some_table ENGINE = TinyLog()"
                                + " AS SELECT 'value_" + db + "' AS field");

                connection.setCatalog(db);
                assertEquals(connection.getCatalog(), db);
                assertEquals(connection.getProperties().getDatabase(), db);
                assertEquals(connection.getUrl(),
                        "jdbc:clickhouse://localhost:8123/" + db + "?option1=one%20two&option2=y");

                ResultSet resultSet = connection.createStatement().executeQuery("SELECT field FROM some_table");
                assertTrue(resultSet.next());
                assertEquals(resultSet.getString(1), "value_" + db);
            }
        } finally {
            Connection connection = dataSource.getConnection();
            for (String db : dbNames) {
                connection.createStatement().executeUpdate("DROP DATABASE IF EXISTS " + db);
            }
        }
    }
}
