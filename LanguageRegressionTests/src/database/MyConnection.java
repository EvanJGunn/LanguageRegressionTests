package database;

import java.sql.*;
import java.util.ArrayList;

import application.Logger;

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
    private static MyConnection myConnection = null;
    
    /**
     * @return The single instance of the MyConnection class.
     */
    public static MyConnection getInstance() {
        if (myConnection == null) {
            myConnection = new MyConnection();
        }
        return myConnection;
    }
    
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
            Logger.getInstance().log(e.getMessage());
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
            Logger.getInstance().log(e.getMessage());
        }
        return set;
    }
    
    /**
     * Run a SQL update.
     * @param update The SQL update, or insertion.
     * @return Return the number of rows affected
     */
    private int runUpdate(String update) {
        int rowsUpdated = 0;
        try {
            Statement statement = connection.createStatement();
            rowsUpdated = statement.executeUpdate(update);
        } catch (SQLException e) {
            Logger.getInstance().log(e.getMessage());
            return 0;
        }
        return rowsUpdated;
    }
    
    /**
     * getWords is a function meant to assist question factories in creating complete or partial LocalWord instances
     * from the database. An sql query is passed that MUST retrieve all of these fields in the following order:
     * romanization, wlanguage, meaning, wtype, main, ancillary, sname.
     * If any of these fields are not necessary, they should be put as null, i.e. SELECT NULL; This is because
     * A ResultSet's getString() method will return null if it parses NULL.
     * @param sqlQuery The compliant query.
     * @param amount The amount of words that are requested, not necessarily the amount returned.
     * @return Returns either a filled arraylist of localwords, a partial arraylist, or an empty arraylist, depending on success of the query.
     */
    public ArrayList<LocalWord> getWords(String sqlQuery, int amount) {
        ArrayList<LocalWord> myWords = new ArrayList<LocalWord>();
        ResultSet results = runQuery(sqlQuery);
        try {
            while (results.next()) {
                LocalWord newWord = new LocalWord(results.getString(1),results.getString(2),results.getString(3),
                        results.getString(4),results.getString(5),results.getString(6),results.getString(7));
                myWords.add(newWord);
            }
        } catch (SQLException e) {
            Logger.getInstance().log(e.getMessage());
        }
        return myWords;
    }

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
        
        // Begin the transaction
        runQuery("START TRANSACTION;");
        
        // Generate the proper SQL query for insertion into the word table.
        String wordUpdate = "INSERT INTO " + WORD_TABLE + " (wlanguage, meaning, romanization, wtype) "
                          + "VALUES ('" + language + "','" + meaning + "','" + word + "','" + wtype + "');";
        int success = runUpdate(wordUpdate);
        
        int newWID = checkForWord(word, language, meaning);
        
        // Check for insertion success.
        if (success < 1 || newWID < 0) {
            // Rollback the transaction
            runQuery("ROLLBACK;");
            Logger.getInstance().log("Insertion of " + word + " has failed.");
            return false;
        }
        Logger.getInstance().log("Insertion into word table has succeeded.");
        
        // Handle symbols update if necessary.
        if (main != null) {
            if (ancillary == null) ancillary = "NULL";
            String symbolUpdate = "INSERT INTO " + SYMBOL_TABLE + " (wid, main, ancillary) "
                                + "VALUES (" + newWID + ",'" + main + "','" + ancillary +"');";
            success = runUpdate(symbolUpdate);
            
            if (success < 1) {
                runQuery("ROLLBACK;");
                Logger.getInstance().log("Insertion into symbols table of " + main + " has failed.");
                return false;
            }
            Logger.getInstance().log("Insertion into symbols table has succeeded.");
        }
        
        // Handle source update if necessary.
        if (sourceName != null) {
            String sourceUpdate = "INSERT INTO " + SOURCE_TABLE + " (wid, sname) "
                                + "VALUES (" + newWID + ",'" + sourceName + "');";
            success = runUpdate(sourceUpdate);
            
            if (success < 1) {
                runQuery("ROLLBACK;");
                Logger.getInstance().log("Insertion into source table with " + sourceName + " has failed.");
                return false;
            }
            Logger.getInstance().log("Insertion into source table has succeeded.");
        }
        
        runQuery("COMMIT;");
        return true;
    }
    
    /**
     * Remove a word of a language and wid from the database.
     * First attempt to remove symbols data and source data, which have foreign key constraints on word.
     * Then delete word.
     * @param wid
     * @return
     */
    public boolean removeWord(int wid, String language) {
        String newSourceUpdate = "DELETE "
                               + "FROM " + SOURCE_TABLE + " S "
                               + "WHERE S.wid = " + wid + ";";
        String newSymbolsUpdate = "DELETE "
                                + "FROM " + SYMBOL_TABLE + " S "
                                + "WHERE S.wid = " + wid + ";";
        String newWordUpdate = "DELETE "
                             + "FROM " + WORD_TABLE + " W "
                             + "WHERE W.wid = " + wid + ";";
        
        // Run each update in a transaction, and rollback ONLY IF
        // the word update/deletion fails. This is because
        // there may be no entries in the symbols or source table.
        // But if a source/symbols update fails and there were entries,
        // the word table update will also fail, still causing the
        // transaction to be rolled back.
        runQuery("START TRANSACTION;");
        runUpdate(newSourceUpdate);
        runUpdate(newSymbolsUpdate);
        int success = runUpdate(newWordUpdate);
        if (success == 0) {
            runQuery("ROLLBACK;");
            return false;
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
        String newQuery = "SELECT W.romanization, W.wlanguage, W.meaning "
                        + "FROM " + WORD_TABLE + " W "
                        + "WHERE W.wlanguage = '" + language + "';";
        ResultSet result = runQuery(newQuery);
        if (result == null) {
            Logger.getInstance().log("Failed to get query result.");
            return;
        }
        Logger.getInstance().log("Listing:");
        try {
            while (result.next()) {
                LocalWord newWord = new LocalWord(result.getString(1),result.getString(2),result.getString(3));
                newWord.pull();
                Logger.getInstance().log(newWord.toString());
            }
        } catch (SQLException e) {
            Logger.getInstance().log(e.getMessage());
        }
    }
    
    /**
     * Many languages contain homonyms, this function allows the user to check if a word has multiple meanings stored in the database.
     * @param word The romanized spelling of the word.
     * @param language The language of the word.
     */
    public void listHomonyms(String word, String language) {
        String newQuery = "SELECT W.romanization, W.wlanguage, W.meaning "
                        + "FROM " + WORD_TABLE + " W "
                        + "WHERE W.wlanguage = '" + language + "' AND W.romanization = '" + word + "';";
        try {
            ResultSet result = runQuery(newQuery);
            Logger.getInstance().log("Listing:");
            while (result.next()) {
                LocalWord newWord = new LocalWord(result.getString(1),result.getString(2),result.getString(3));
                newWord.pull();
                Logger.getInstance().log(newWord.toString());
            }
        } catch (SQLException e) {
            Logger.getInstance().log(e.getMessage());
        }
    }
    
    /**
     * Check if a word exists in the database. If meaning is not set, then does not account for homonyms,
     * returns the first word wid that matches thw word in the language.
     * If meaning is set, will return the unique word/meaning combination wid.
     * @param word The word for which we are checking.
     * @param language The language of the word.
     * @return If the word exists, return its wid. If the word doesn't exist, return -1. On error, return -2.
     */
    public int checkForWord(String word, String language, String meaning) {
        String newQuery = "";
        if (meaning == null) {
            newQuery = "SELECT W.wid "
                     + "FROM " + WORD_TABLE + " W "
                     + "WHERE W.wlanguage = '" + language + "' AND W.romanization = '" + word + "';";
        } else {
            newQuery = "SELECT W.wid "
                    + "FROM " + WORD_TABLE + " W "
                    + "WHERE W.wlanguage = '" + language + "' AND W.romanization = '" + word + "' AND W.meaning = '" + meaning + "';";
        }
        try {
            ResultSet result = runQuery(newQuery);
            if (result.next()) {
                return result.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            Logger.getInstance().log(e.getMessage());
            return -2;
        }
    }
    
    /**
     * Set a local word with data from the remote databases' word table data.
     * @param myWord The local word we are setting.
     * @return True if set, false upon error or no results to query (i.e. no word in the database).
     */
    public boolean setLocalWordTable(LocalWord myWord) {
        String newQuery = "SELECT W.wlanguage, W.meaning, W.romanization, W.wtype "
                        + "FROM " + WORD_TABLE + " W "
                        + "WHERE W.wid = '" + myWord.getWID() + "';";
        try {
            ResultSet result = runQuery(newQuery);
            if (result.next()) {
                myWord.setWordValues(result.getString(3), result.getString(1), result.getString(2), result.getString(4));
                return true;
            }
            return false;
        } catch (SQLException e) {
            Logger.getInstance().log(e.getMessage());
            return false;
        }
    }
    
    /**
     * Set a local word with data from the remote databases' symbols table.
     * @param myWord The local word we are setting.
     * @return True if set, false upon error or no results to query (i.e. no symbols in the database).
     */
    public boolean setLocalSymbolsTable(LocalWord myWord) {
        String newQuery = "SELECT S.main, S.ancillary "
                        + "FROM " + SYMBOL_TABLE + " S "
                        + "WHERE S.wid = '" + myWord.getWID() + "';";
        try {
            ResultSet result = runQuery(newQuery);
            if (result.next()) {
                myWord.setSymbolValues(result.getString(1), result.getString(2));
                return true;
            }
            return false;
        } catch (SQLException e) {
            Logger.getInstance().log(e.getMessage());
            return false;
        }
    }
    
    /**
     * Set a local word with data from the remote databases' source table.
     * @param myWord The local word we are setting.
     * @return True if set, false upon error or no results to query (i.e. no source in the database).
     */
    public boolean setLocalSourceTable(LocalWord myWord) {
        String newQuery = "SELECT S.sname "
                        + "FROM " + SOURCE_TABLE + " S "
                        + "WHERE S.wid = '" + myWord.getWID() + "';";
        try {
            ResultSet result = runQuery(newQuery);
            if (result.next()) {
                myWord.setSourceValue(result.getString(1));
                return true;
            }
            return false;
        } catch (SQLException e) {
            Logger.getInstance().log(e.getMessage());
            return false;
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
                Logger.getInstance().log(e.getMessage());
            }
        }
    }
}
