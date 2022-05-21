package com.lambda.easydqc.utils;

import com.lambda.easydqc.entity.DataSource;
import lombok.Data;

import java.io.File;
import java.sql.*;
import java.util.*;

public class CatalogUtils {

    public static final Map<String, Boolean> connectorSupport = new HashMap<>();
    public static final List<String> supportConnectorNameList = new LinkedList<>();
    public static final Map<String, String> connector2driverClass = new HashMap<>();
    static {
        supportConnectorNameList.add("oracle");
        connector2driverClass.put("oracle", "oracle.jdbc.OracleDriver");
        supportConnectorNameList.add("pg");
        connector2driverClass.put("pg", "org.postgresql.Driver");
        supportConnectorNameList.add("mysql");
        connector2driverClass.put("mysql", "com.mysql.cj.jdbc.Driver");
        // supportConnectorNameList.add("hive");
        // supportConnectorNameList.add("presto");
        // supportConnectorNameList.add("trino");

        for (String connectorName : supportConnectorNameList) {
            connectorSupport.put(connectorName, true);
        }
    }

    public static File checkCatalogNaive(Properties properties) {
        boolean isUseSpecifiedConfPath = false;
        String confPath = null;
        if (properties.getProperty("configPath") != null) {
            isUseSpecifiedConfPath = true;
            confPath = properties.getProperty("configPath");
        }
        String catalogName = properties.getProperty("catalogName");
        if (catalogName == null) {
            System.out.println("catalog name should be specified");
            return null;
        }
        String catalogFilePath = null;
        if (isUseSpecifiedConfPath) {
            catalogFilePath = confPath + "/" + catalogName + ".properties";
        } else {
            catalogFilePath = catalogName + ".properties";
        }
        File catalogFile = new File(catalogFilePath);
        if (catalogFile.isDirectory()) {
            System.out.println("catalog [" + catalogFilePath + "] should be a file, not a dir");
            return null;
        }
        if (!catalogFile.exists()) {
            System.out.println("catalog [" + catalogFilePath + "] should exist");
            return null;
        }
        return catalogFile;
    }


    /**
     * 验证相应的准确性
     * @param dataSource
     */
    public static int checkDataSource(DataSource dataSource) {
        String dataSourceType = dataSource.getType();
        if (!connectorSupport.getOrDefault(dataSourceType, false)) {
            System.out.println("un-support connector: " + dataSourceType + ", it should be in : " +
                    String.join(",", supportConnectorNameList));
            return -1;
        }
        return 0;
    }

    public static Connection getConnection(DataSource dataSource) throws ClassNotFoundException, SQLException {
        Properties properties = new Properties();
        properties.put("user", dataSource.getUsername());
        properties.put("password", dataSource.getPassword());
        String jdbcUrl = dataSource.getJdbcUrl();
        if (jdbcUrl == null) {
            return null;
        }
        Class.forName(connector2driverClass.get(dataSource.getType()));
        Connection connection = DriverManager.getConnection(jdbcUrl, properties);
        return connection;
    }



    public static void printSchemaAndData(String sql, ResultSet resultSet) throws SQLException {
        printSchemaAndData(sql, resultSet, 20);
    }

    public static void printSchemaAndData(String sql, ResultSet resultSet, int positionLength) throws SQLException {
        System.out.println("execute query: " + sql);
        boolean flag = true;
        while (resultSet.next()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            if (flag) {
                System.out.print("|");
                for (int i = 0; i < columnCount; i++) {
                    System.out.print(stringReplicate("-", positionLength) + "|");
                }
                System.out.println("");

                System.out.print("|");
                for (int i = 0; i < columnCount; i++) {
                    System.out.print(center(metaData.getColumnLabel(i + 1), positionLength));
                    System.out.print("|");
                }
                System.out.println("");

                System.out.print("|");
                for (int i = 0; i < columnCount; i++) {
                    System.out.print(stringReplicate("-", positionLength) + "|");
                }
                System.out.println("");
                flag = false;
            }
            System.out.print("|");
            for (int i = 0; i < columnCount; i++) {
                System.out.print(center(resultSet.getString(metaData.getColumnLabel(i + 1)), positionLength));
                System.out.print("|");
            }
            System.out.println("");

            System.out.print("|");
            for (int i = 0; i < columnCount; i++) {
                System.out.print(stringReplicate("-", positionLength) + "|");
            }
            System.out.println("");
        }
        System.out.println("\n");
    }

    private static String center(String str, int size) {
        if (str == null) {
            str = "";
        }
        int length = str.length();
        int rest = size - length;
        int left = (int) ((size - length) / 2);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < left; i++) {
            sb.append(" ");
        }
        sb.append(str);
        for (int i = 0; i < rest - left; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }


    private static String stringReplicate(String str, int replicationNum) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < replicationNum; i++) {
            stringBuffer.append(str);
        }
        return stringBuffer.toString();
    }
}
