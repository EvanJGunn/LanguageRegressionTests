package database;

/**
 * Local Word represents data for a word that is stored locally on the client side.
 * This can include a word that will be inserted, a word that will be updated with new information,
 * or a word that will be deleted.
 * A local word therefore may not contain the same data as an equivalent word that is contained in the database.
 * @author Evan Gunn
 *
 */
public class LocalWord {
    private String romanization, language, meaning, wtype, main, ancillary, sname;
    private int wid = -1;
    
    /**
     * This constructor should be used when attempting to pull unknown data for a word.
     * @param word The word for which you want to pull data.
     * @param language The language of the word.
     */
    public LocalWord(String word, String language) {
        this.romanization = word;
        this.language = language;
        meaning = null;
        wtype = null;
        main = null;
        ancillary = null;
        sname = null;
    }
    
    /**
     * This constructor should be used when attempting to pull unknown data for a specific word,
     * when that word may have homonyms in the database. The combination of (word,meaning) must
     * be unique in the database, functioning somewhat as a second key.
     */
    public LocalWord(String word, String language, String meaning) {
        this.romanization = word;
        this.language = language;
        this.meaning = meaning;
        wtype = null;
        main = null;
        ancillary = null;
        sname = null;
    }
    
    /**
     * This constructor should be used in several contexts:
     * When you want to push a new word/language combination to the database.
     * When you want to update the symbols table with a new combination of symbols for a word already in the database.
     * When you want to update the source table with a new source.
     * All values may be null, but certain actions will only work with non-null values.
     * @param word The romanized word.
     * @param language The language of the word.
     * @param meaning The meaning of the word.
     * @param wtype The type of the word.
     * @param main The main symbols of the word.
     * @param ancillary The ancillary symbols of the word.
     * @param source The name of the source, known to LocalWord as "sname".
     */
    public LocalWord(String word, String language, String meaning, String wtype, String main, String ancillary, String source) {
        this.romanization = word;
        this.language = language;
        this.meaning = meaning;
        this.wtype = wtype;
        this.main = main;
        this.ancillary = ancillary;
        this.sname = source;
    }
    
    /**
     * @return Return the word ID of the LocalWord, this is a unique descriptor of the table.
     */
    public int getWID() {
        return wid;
    }
    
    /**
     * Set the values that the word table would contain in the database.
     * Useful when finishing a partial LocalWord with data from the database.
     */
    public void setWordValues(String word, String language, String meaning, String wtype) {
        this.romanization = word;
        this.language = language;
        this.meaning = meaning;
        this.wtype = wtype;
    }
    
    /**
     * Set the values that the symbol table would contain in the database.
     * Useful when pulling the symbols for a partially finished LocalWord.
     */
    public void setSymbolValues(String main, String ancillary) {
        this.main = main;
        this.ancillary = ancillary;
    }
    
    /**
     * Set the source value that would be contained in the source table.
     * useful when pulling source data for a partially finished word.
     */
    public void setSourceValue(String source) {
        this.sname = source;
    }
    
    /**
     * Get the main symbols string, useful when generating questions.
     */
    public String getMainSymbols() {
        return main;
    }
    
    /**
     * Get the meaning of the romanization.
     */
    public String getMeaning() {
        return meaning;
    }
    
    /**
     * Get the romanization of the word.
     */
    public String getRomanization() {
        return romanization;
    }
    
    /**
     * Get the ancillary symbols string, useful when generating questions.
     */
    public String getAncillarySymbol() {
        return ancillary;
    }
    
    /**
     * The wid is initially -1, which means it has not been set.
     * When printing, this value should be displayed as 'Not Set'.
     * The function facilitates this rule.
     * @return Either the String version of the wid, or 'Not Set'.
     */
    private String widToString() {
        if (wid == -1) {
            return "Not Set";
        }
        return "" + wid;
    }
    
    /**
     * Create a String out of the current values of the local word. Null values appear as 'null' in Java.
     * @return The string version of the local word.
     */
    public String toString() {
        return "Word ID: " + widToString() + ", Romanized Word: " + romanization + ", Language: " + language + ", Meaning: " + meaning
             + ", Type: " + wtype + ", Main Symbols: " + main + ", Ancillary: " + ancillary + ", Source: " + sname;
    }
    
    /**
     * Pull data from the database to complete the LocalWord, may overwrite come of current data.
     * @return True if the pull was successful, false if the pull failed. Pull can succeed partially and return true.
     */
    public boolean pull() {
        // Pull the word ID
        int remoteWID = -1;
        remoteWID = MyConnection.getInstance().checkForWord(romanization, language, meaning);
        if (remoteWID < 0) {
            return false;
        }
        wid = remoteWID;
        
        // Pull from the word table.
        if (!MyConnection.getInstance().setLocalWordTable(this)) return false;
        
        // Attempt to pull from the symbols table.
        MyConnection.getInstance().setLocalSymbolsTable(this);
        
        // Attempt to pull from the source table.
        MyConnection.getInstance().setLocalSourceTable(this);
        return true;
    }
}
