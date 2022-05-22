package com.lambda.easydqc;

import com.lambda.easydqc.entity.DataSource;
import com.lambda.easydqc.utils.CatalogUtils;
import com.lambda.easydqc.utils.FileUtils;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class JDBCUtils {
    public static final Map<String, String> arg2key = new HashMap<>();
    static {
        arg2key.put("-ck", "isCheckCatalog");
        arg2key.put("--check", "isCheckCatalog");
        arg2key.put("-cn", "catalogName");
        arg2key.put("--catalog_name", "catalogName");
        arg2key.put("-cp", "configPath");
        arg2key.put("--conf_path", "configPath");
        arg2key.put("-q", "query");
        arg2key.put("--query", "query");
        arg2key.put("-cw", "columnWidth");
        arg2key.put("--column_width", "columnWidth");
        arg2key.put("-i", "initial");
        arg2key.put("--initial", "initial");
    }

    public static void printHelp() {
        String help                 =       "-h; --help                         print user guide";
        String check                =       "-ck; --check                       check catalog availability";
        String catalog              =       "-cn; --catalog_name                specify catalog name";
        String conf                 =       "-cp; --conf_path                   if the config path is not included in class path, " +
                                                                                "specify catalog config directory/path";
        String query                =       "-q; --query                        sql to query";
        String lineWidth            =       "-cw; --column_width                column width";

        // TODO 描述catalog
        String descCatalog          =       "-dc; --describe_catalog            describe catalog. mysql -> databases;" +
                                                                                "pg -> databases; oracle -> users";

        // TODO init sqlite数据库
        String initial              =       "-i; --initial                      initial sqlite db with a specified path";


        System.out.println(help);
        System.out.println(check);
        System.out.println(catalog);
        System.out.println(conf);
        System.out.println(query);
        System.out.println(lineWidth);
        System.out.println(initial);
    }

    public static Properties parseArgs(String[] args) {
        Properties properties = new Properties();
        String key = null;
//        String querySQL = "";
        List<String> querySQLList = new LinkedList<>();
        for (String arg : args) {
            arg = arg.trim();
            if (key == null) {
                if (arg.equals("-h") || arg.equals("--help")) {
                    printHelp();
                    break;
                }
                key = arg2key.get(arg);
                if (key == null) {
                    System.out.println("invalid arg, " + arg);
                    printHelp();
                    break;
                }
                if (key.equals("isCheckCatalog")) {
                    properties.put(key, "true");
                    key = null;
                }
            } else {
//                if (key.equals("query")) {
//                    arg = arg.trim();
//                    if (arg.startsWith("\"") || arg.startsWith("'") || querySQLList.isEmpty()) {
//                        querySQLList.add(arg);
//                        if (arg.endsWith(";")) {
//                            properties.put("query", String.join(" ", querySQLList));
//                            key = null;
//                        }
//                        continue;
//                    }
//                    if (arg.endsWith("\"") || arg.endsWith("'") || arg.endsWith(";")) {
//                        querySQLList.add(arg);
//                        properties.put("query", String.join(" ", querySQLList));
//                        key = null;
//                        continue;
//                    }
//                    querySQLList.add(arg);
//                } else {
                    properties.put(key, arg.trim());
                    key = null;
//                }
            }
        }
//        String query = properties.getProperty("query");
//        if (query != null) {
//            if (query.startsWith("\"") || query.startsWith("'")) {
//                query = query.substring(1, query.length());
//            }
//            if (query.endsWith("\"") || query.endsWith("'")) {
//                query = query.substring(0, query.length()-1);
//            }
//            if (query.endsWith(";")) {
//                query = query.substring(0, query.length()-1);
//            }
//            query = query + ";";
//            properties.put("query", query);
//        }
        return properties;
    }




    /**
     *  connector.name=oracle
     * # The correct syntax of the connection-url varies by Oracle version and
     * # configuration. The following example URL connects to an Oracle SID named
     * # "orcl".
     * connection-url=jdbc:oracle:thin:@example.net:1521:orcl
     * connection-user=root
     * connection-password=secret
     * @param properties
     * @return
     * @throws IOException
     */
    public static int checkCatalogAvailability(Properties properties) throws IOException, SQLException, ClassNotFoundException {
        File catalogFile = CatalogUtils.checkCatalogNaive(properties);
        if (catalogFile == null) {
            return -1;
        }
        String catalogName = properties.getProperty("catalogName");
        Properties catalogProperties = FileUtils.loadProperties(catalogFile);
        DataSource dataSource = DataSource.builder()
                .jdbcUrl(catalogProperties.getProperty("connection-url"))
                .type(catalogProperties.getProperty("connector.name"))
                .name(catalogName)
                .password(catalogProperties.getProperty("connection-password"))
                .username(catalogProperties.getProperty("connection-user"))
                .build();
        int datasourceCheckResultCode = CatalogUtils.checkDataSource(dataSource);
        if (datasourceCheckResultCode != 0) {
            return datasourceCheckResultCode;
        }
        Connection connection = CatalogUtils.getConnection(dataSource);
        if (connection == null) {
            System.out.println("catalog properties file error");
            return -1;
        }
        Statement statement = connection.createStatement();
        statement.executeQuery("select 1");
        statement.close();
        connection.close();
        System.out.println("connection check successfully");
        return 0;
    }


    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        Properties properties = parseArgs(args);
        if (properties.getOrDefault("isCheckCatalog", "false").equals("true")) {
            int exitCode = checkCatalogAvailability(properties);
            System.exit(exitCode);
        }
        if (properties.getProperty("query") != null) {
            int exitCOde = querySQL(properties);
            System.exit(exitCOde);
        }
        printHelp();
    }

    private static int querySQL(Properties properties) throws IOException, SQLException, ClassNotFoundException {
        System.out.println("query sql: " + properties.getProperty("query"));
        File catalogFile = CatalogUtils.checkCatalogNaive(properties);
        if (catalogFile == null) {
            return -1;
        }
        String catalogName = properties.getProperty("catalogName");
        Properties catalogProperties = FileUtils.loadProperties(catalogFile);
        DataSource dataSource = DataSource.builder()
                .jdbcUrl(catalogProperties.getProperty("connection-url"))
                .type(catalogProperties.getProperty("connector.name"))
                .name(catalogName)
                .password(catalogProperties.getProperty("connection-password"))
                .username(catalogProperties.getProperty("connection-user"))
                .build();
        int datasourceCheckResultCode = CatalogUtils.checkDataSource(dataSource);
        if (datasourceCheckResultCode != 0) {
            return datasourceCheckResultCode;
        }
        Connection connection = CatalogUtils.getConnection(dataSource);
        if (connection == null) {
            System.out.println("catalog properties file error");
            return -1;
        }
        Statement statement = connection.createStatement();
        try {
            ResultSet resultSet = statement.executeQuery(properties.getProperty("query"));
            String columnWidth = properties.getProperty("columnWidth");
            if (columnWidth == null) {
                CatalogUtils.printSchemaAndData(properties.getProperty("query"), resultSet);
            } else {
                CatalogUtils.printSchemaAndData(properties.getProperty("query"), resultSet, Integer.parseInt(columnWidth));
            }
            System.out.println("query successfully");
        } catch (SQLException e) {
            throw e;
        } finally {
            statement.close();
            connection.close();
        }
        return 0;
    }
}
