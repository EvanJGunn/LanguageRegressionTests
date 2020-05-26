package database;

import java.sql.*;

/**
 * MyConnection manages both the MySQL server connection, and all SQL commands issued to that server.
 * This class is a singleton, as the program will only connect to one server at a time.
 * @author Evan Gunn
 */
public class MyConnection {
    public static MyConnection myConnection = null;
    private Connection connection = null;
    
    /**
     * The connection must be initialized via this initializer method.
     * @param awsEndpoint The AWS RDS MySQL endpoint.
     * @param port The port the MySQL server is on.
     * @param schema The name of the MySQL schema being accessed.
     * @param user The username used to access the MySQL database.
     * @param password The password for the user.
     */
    public static boolean InitializeConnection(String awsEndpoint, String port, String schema, String user, String password) {
        myConnection = new MyConnection();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            myConnection.connection = DriverManager.getConnection("jdbc:mysql://"+awsEndpoint+":"+port+"/"+schema, user, password);
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
        return true;
    }
    
    /**
     * Run an SQL query on the database. All queries are passed as strings, results should be checked for query failure and null returns.
     * @param query A string in the format of an SQL query.
     * @return Returns a ResultSet type, the contents of which depend on the query, or a null value if the query fails.
     */
    public ResultSet runQuery(String query) {
        ResultSet set = null;
        try {
            Statement statement = connection.createStatement();
            set = statement.executeQuery(query);
        } catch (SQLException e) {
            System.out.println(e);
        }
        return set;
    }
    
    /**
     * Close the database connection.
     */
    public static void CloseConnection() {
        if (myConnection != null && myConnection.connection != null) {
            try {
                myConnection.connection.close();
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
    }
}
