package data.db;

import data.models.Builder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.stylesheets.DocumentStyle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnector {

    public static DatabaseConnector dbConnector = null;
    private static Connection connection = null;

    static {
        createConnection();
        inflateDB();
    }

    public static DatabaseConnector getInstance(){
        if(dbConnector == null){
            dbConnector = new DatabaseConnector();
        }

        return dbConnector;
    }

    private static void createConnection(){

        try{
            Class.forName("org.sqlite.JDBC");
            //do developer use Builder.buildDatabasePath()+"/pharmacy404.db"
            connection = DriverManager.getConnection("jdbc:sqlite:pharmacy404.db");
        } catch (ClassNotFoundException e) {
            Logger.getLogger(DatabaseConnector.class.getName()).log(Level.SEVERE, "Cannot Load Database");
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
        }
    }

    public static void inflateDB(){
        List<String> tableData = new ArrayList<>();

        try{
            Set<String> tables = getDBTables();
            System.out.println("Locked and loaded tables: " + tables);

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder  = builderFactory.newDocumentBuilder();
            Document document = builder.parse(
                    DatabaseConnector.class.getResourceAsStream("dbTables.xml"));
            NodeList nodeList = document.getElementsByTagName("table");
            int nodeListLength = nodeList.getLength();

            for(int i  = 0; i < nodeListLength; i++){
                Node node = nodeList.item(i);
                Element entry = (Element) node;
                String tableName = entry.getAttribute("name");
                String columnDetails = entry.getAttribute("columns");

                if(!tables.contains(tableName)){
                    String createTableCommand = String.format("CREATE TABLE %s (%s)",
                            tableName, columnDetails);

                    tableData.add(createTableCommand);
                }

            }

            if(tableData.isEmpty()){
                System.out.println("Tables are already loaded");
            }else{
                System.out.println("Inflating new Tables");
                createTables(tableData);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private static Set<String> getDBTables() throws SQLException {
        Set<String> set = new HashSet<>();
        DatabaseMetaData dbmeta = connection.getMetaData();
        readDBTable(set, dbmeta, "TABLE", null);
        return set;
    }

    private static void readDBTable(Set<String> set, DatabaseMetaData dbmeta, String searchCriteria, String schema) throws SQLException {
        ResultSet rs = dbmeta.getTables(null, schema, null, new String[]{searchCriteria});
        while (rs.next()) {
            set.add(rs.getString("TABLE_NAME").toLowerCase());
        }
    }

    private static void createTables(List<String> tableData) throws SQLException {
        Statement statement = connection.createStatement();
        statement.closeOnCompletion();
        for (String command : tableData) {
            System.out.println(command);
            statement.addBatch(command);
        }
        statement.executeBatch();
    }


    public Connection getConnection(){
        return connection;
    }

}