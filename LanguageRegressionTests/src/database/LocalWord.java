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
    private String romanization, language, meaning, wtype, main, ancillary, tname;
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
        tname = null;
    }
    
    /**
     * This constructor should be used in several contexts:
     * When you want to push a new word/language combination to the database.
     * When you want to update the symbols table with a new combination of symbols for a word already in the database.
     * When you want to update the source table with a new source.
     * All values may be null, but certain actions will only work with non-null values.
     * @param word The word, which you may wish to push.
     * @param language The language of the word.
     * @param meaning The meaning of the word.
     * @param wtype The type of the word.
     * @param main The main symbols of the word.
     * @param ancillary The ancillary symbols of the word.
     * @param source The name of the source.
     */
    public LocalWord(String word, String language, String meaning, String wtype, String main, String ancillary, String source) {
        this.romanization = word;
        this.language = language;
        this.meaning = meaning;
        this.wtype = wtype;
        this.main = main;
        this.ancillary = ancillary;
        this.tname = source;
    }
    
    public void setWID(int wid) {
        this.wid = wid;
    }
    
    public int getWID() {
        return wid;
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
             + ", Type: " + wtype + ", Main Symbols: " + main + ", Ancillary: " + ancillary + ", Source: " + tname;
    }
    
    // TODO Complete push function
    /**
     * 
     * @return
     */
    public boolean push() {
        return true;
    }
    
    // TODO Complete pull function
    /**
     * 
     * @return 
     */
    public boolean pull() {
        return true;
    }
}
