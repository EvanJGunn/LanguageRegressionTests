package database;

import java.sql.*;

/**
 * MyConnection manages both the MySQL server connection, and all SQL commands issued to that server.
 * This class is a singleton, as the program will only connect to one server at a time.
 * @author Evan Gunn
 */
public class MyConnection {
    private static String WORD_TABLE = "word";
    private static String SOURCE_TABLE = "wordsource";
    private static String SYMBOL_TABLE = "symbols";
    private Connection connection = null;
    
    // The singleton's instance
    public static MyConnection myConnection = null;
    
    /**
     * The connection must be initialized via this initializer method.
     * @param awsEndpoint The AWS RDS MySQL endpoint.
     * @param port The port the MySQL server is on.
     * @param schema The name of the MySQL schema being accessed.
     * @param user The username used to access the MySQL database.
     * @param password The password for the user.
     */
    public static boolean initializeConnection(String awsEndpoint, String port, String schema, String user, String password) {
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
    private ResultSet runQuery(String query) {
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
     * 
     * @param update The SQL update, or insertion.
     * @return Return the number of rows affected
     */
    private int runUpdate(String update) {
        int rowsUpdated = 0;
        try {
            Statement statement = connection.createStatement();
            rowsUpdated = statement.executeUpdate(update);
        } catch (SQLException e) {
            System.out.println(e);
            return 0;
        }
        return rowsUpdated;
    }
    
    /**
     * Get the number of rows in a table
     * @return Return the number of rows in a table, or if an error occurs, return -1.
     */
    private int getRowCount(String tableName) {
        ResultSet result = runQuery("SELECT COUNT(*) FROM " + tableName + ";");
        int count = 0;
        try {
            result.next();
            count = result.getInt(1);
        } catch (SQLException e) {
            System.out.println(e);
            count = -1;
        }
        return count;
    }

    // TODO This function has a lot of parameters, in the future it may need to be redone, or split up
    /**
     * Insert a word, its symbols, and its source into the associated tables.
     * @param word The romanized word.
     * @param language The language of the word.
     * @param meaning The meaning of the word.
     * @param wtype The type of word.
     * @param main The main symbols, may be null.
     * @param ancillary The ancillary symbols, may be null.
     * @param sourceName The name of the source of the word, may be null.
     * @return Return true for a successful commit, false if the transaction was rolled back,
     */
    public boolean insertWord(String word, String language, String meaning,
            String wtype, String main, String ancillary, String sourceName) {
        // Generate a new ID for the new word by adding 1 to the count of the word table.
        int newWordID = getRowCount(WORD_TABLE) + 1;
        
        if (newWordID == -1) {
            System.out.println("Failed to acquire word count from database, word not inserted.");
            return false;
        }
        
        // Begin the transaction
        runQuery("START TRANSACTION;");
        
        // Generate the proper SQL query for insertion into the word table.
        String wordUpdate = "INSERT INTO " + WORD_TABLE + " (wlanguage, wid, meaning, romanization, wtype) "
                         + "VALUES ('" + language + "','" + newWordID + "','" + meaning + "','" + word + "','" + wtype + "');";
        int success = runUpdate(wordUpdate);
        
        // Check for insertion success.
        if (success < 1) {
            // Rollback the transaction
            runQuery("ROLLBACK;");
            System.out.println("Insertion of " + word + " has failed.");
            return false;
        }
        System.out.println("Insertion into word table has succeeded.");
        
        // Handle symbols update if necessary.
        if (main != null) {
            if(ancillary == null) ancillary = "NULL";
            String symbolUpdate = "INSERT INTO " + SYMBOL_TABLE + " (wid, main, ancillary) "
                                + "VALUES ('" + newWordID + "','" + main + "','" + ancillary +"');";
            success = runUpdate(symbolUpdate);
            
            if (success < 1) {
                runQuery("ROLLBACK;");
                System.out.println("Insertion into symbols table of " + main + " has failed.");
                return false;
            }
            System.out.println("Insertion into symbols table has succeeded.");
        }
        
        // Handle source update if necessary.
        if (sourceName != null) {
            String sourceUpdate = "INSERT INTO " + SOURCE_TABLE + " (wid, tname) "
                                + "VALUES ('" + newWordID + "','" + sourceName + "');";
            success = runUpdate(sourceUpdate);
            
            if (success < 1) {
                runQuery("ROLLBACK;");
                System.out.println("Insertion into source table with " + sourceName + " has failed.");
                return false;
            }
            System.out.println("Insertion into source table has succeeded.");
        }
        
        runQuery("COMMIT;");
        return true;
    }
    
    /**
     * List all the words entered for a language, along with any data in the symbols and wordsource tables.
     * This command is currently for debug, obviously listing 
     * @param language Lists words based on language
     */
    public void listLanguageWords(String language) {
        String newQuery = "SELECT * "
                        + "FROM word W "
                        + "WHERE W.wlanguage = '" + language + "';";
        ResultSet result = runQuery(newQuery);
        if (result == null) {
            System.out.println("Failed to get query result.");
            return;
        }
        System.out.println("Listing wid, romanization, and language, all other fields will be null:");
        try {
            while (result.next()) {
                LocalWord newWord = new LocalWord(result.getString(4),result.getString(1));
                newWord.setWID(result.getInt(2));
                // TODO possibly add a pull here to complete word data
                System.out.println(newWord.toString());
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    
    /**
     * Check if a word exists in the database.
     * @param word The word for which we are checking.
     * @param language The language of the word.
     * @return If the word exists, return its wid. If the word doesn't exist, return -1. On error, return -2.
     */
    public int checkForWord(String word, String language) {
        String newQuery = "SELECT * "
                        + "FROM word W "
                        + "WHERE W.wlanguage = '" + language + "' AND W.romanization = '" + word + "';";
        try {
            ResultSet result = runQuery(newQuery);
            if (result.next()) {
                return result.getInt(2);
            }
            return -1;
        } catch (SQLException e) {
            System.out.println(e);
            return -2;
        }
    }
    
    /**
     * Close the database connection.
     */
    public static void closeConnection() {
        if (myConnection != null && myConnection.connection != null) {
            try {
                myConnection.connection.close();
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
    }
}
