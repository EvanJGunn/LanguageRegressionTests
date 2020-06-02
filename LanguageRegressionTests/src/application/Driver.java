package application;

import java.util.Scanner;

import database.LocalWord;
import database.MyConnection;
import lrt.SymbolsMainQuestionFactory;
import lrt.Test;

/**
 * The Driver class contains the main loop for execution.
 * @author Evan Gunn
 */
public class Driver {
    // The input scanner.
    private static Scanner scanner;
    // The boolean that controls the input loop for program options.
    private static boolean continueLoop = true;
    
    /**
     * The execute method contains the stages of logic of the program.
     */
    public static void execute() {
        // Introduction
        Logger.getInstance().log("Welcome to the Language Regression Tests program!");
        
        // Set up the scanner that will be used.
        scanner = new Scanner(System.in);
        
        // Connect to the server.
        while (!acquiredConnection());
        
        // Serve options to the user.
        optionsLoop();
        
        // Clean up when ending execution.
        cleanUp();
    }
    
    /**
     * This function will get userinput, and then attempt to connect to a MySQL server.
     * @return Return true if the connection succeeds, false upon failure to connect.
     */
    private static boolean acquiredConnection() {
        Logger.getInstance().log("Please enter your AWS Endpoint:");
        String endpoint = scanner.nextLine();
        
        Logger.getInstance().log("Please enter the port number:");
        String port = scanner.nextLine();
        
        Logger.getInstance().log("Please enter the MySQL schema name:");
        String schema = scanner.nextLine();
        
        Logger.getInstance().log("Please enter your username:");
        String user = scanner.nextLine();
        
        Logger.getInstance().log("Please enter your password:");
        String password = scanner.nextLine();
        
        boolean connected = MyConnection.initializeConnection(endpoint, port, schema, user, password);
        
        // If we are unable to connect, check if the user would like to retry connection.
        if (!connected) {
            Logger.getInstance().log("Could not connect to MySQL server, try again? y/n");
            String answer = scanner.nextLine();
            
            if (!answer.matches("y")) {
                Logger.getInstance().log("Good Bye!");
                System.exit(0);
            }
            return false;
        }
        
        Logger.getInstance().log("Connection successful!");
        return true;
    }
    
    /**
     * Present command options to the user for interaction with their database and regression test creation/execution tools.
     */
    private static void optionsLoop() {
        while (continueLoop) {
            printCommands();
            String input = scanner.nextLine();
            boolean parsed = parseOptions(input);
            if (!parsed) {
                Logger.getInstance().log("Failed to parse input: "+input);
            }
        }
    }
    
    /**
     * Print the line of available commands, should be replaced with a gui in the future.
     */
    private static void printCommands() {
        System.out.print("********************\n"
                + "Please select from the list of commands, and type below:\n"
                + "quit: Exit the program.\n"
                + "insert: Begin a word insertion.\n"
                + "delete: Begin a word deletion, must know the wid (word id).\n"
                + "create: Begin creation of a test, currently creates symbols test.\n"
                + "listall: List all the words by a language.\n"
                + "listhomonyms: List all homonyms for a word's spelling in the database.\n"
                + "checkfor: Check for a vocabulary word's existence in the database, does not account for homonyms.\n"
                + "********************\n");
    }
    
    /**
     * Parse options input and execute user commands.
     * @param input The input from the user.
     * @return Return true if successfully parsed/executed input, return false if input was unparsable.
     */
    private static boolean parseOptions(String input) {
        switch (input) {
            case "quit":
                continueLoop = false;
                break;
            case "insert":
                String word, language, meaning, wtype, main = null, ancillary = null, sourceName = null;
                
                // Get data values for the word table, this is required for all insertions
                Logger.getInstance().log("Please enter the romanization of the word you would like to insert:");
                word = scanner.nextLine();
                Logger.getInstance().log("Please enter the language of the word:");
                language = scanner.nextLine();
                Logger.getInstance().log("Please enter the meaning of the word in 40 characters or less:");
                meaning = scanner.nextLine();
                Logger.getInstance().log("Please enter the type of the word, noun, verb, etc...:");
                wtype = scanner.nextLine();
                
                // Get data values for the symbols table
                String answer = "";
                while (!(answer.matches("y") || answer.matches("n"))) {
                    Logger.getInstance().log("Would you like to enter data for the symbols table? y/n");
                    answer = scanner.nextLine();
                }
                
                if (answer.matches("y")) {
                    Logger.getInstance().log("Please enter the main (kanji, etc...) symbols:");
                    main = scanner.nextLine();
                    
                    // Check if there are secondary symbols
                    answer  = "";
                    while (!(answer.matches("y") || answer.matches("n"))) {
                        Logger.getInstance().log("Would you like to enter ancillary symbols (hiragana, etc...)? y/n");
                        answer = scanner.nextLine();
                    }
                    
                    if (answer.matches("y")) {
                        Logger.getInstance().log("Please enter the ancillary symbols:");
                        ancillary = scanner.nextLine();
                    }
                }
                
                // Get a source value for the wordsource table
                answer = "";
                while (!(answer.matches("y") || answer.matches("n"))) {
                    Logger.getInstance().log("Would you like to enter data for the source table? y/n");
                    answer = scanner.nextLine();
                }
                
                if (answer.matches("y")) {
                    Logger.getInstance().log("Please enter the source in 45 characters or less:");
                    sourceName = scanner.nextLine();
                }
                
                // The MyConnection singleton manages all sql queries/updates
                boolean success = MyConnection.getInstance().insertWord(word, language, meaning, wtype, main, ancillary, sourceName);
                
                if (success) {
                    Logger.getInstance().log("Word successfully inserted into table(s).");
                } else {
                    Logger.getInstance().log("Failed to insert word, all changes rolled back.");
                }
                break;
            case "delete":
                Logger.getInstance().log("Please enter the language of the word you would like to delete:");
                String deletionLanguage = scanner.nextLine();
                Logger.getInstance().log("Please enter the wid of the word you would like to delete:");
                int deletionWID = scanner.nextInt();
                boolean deleted = MyConnection.getInstance().removeWord(deletionWID, deletionLanguage);
                if (deleted) {
                    Logger.getInstance().log("Word successfully deleted!");
                } else {
                    Logger.getInstance().log("Deletion failed.");
                }
                // Progress the line, as scanner.nextInt() leaves a blank line behind.
                scanner.nextLine();
                break;
            case "create":
                Logger.getInstance().log("Please enter the language of the test you would like to create:");
                String testLanguage = scanner.nextLine();
                Test myTest = new Test(10, testLanguage, new SymbolsMainQuestionFactory());
                myTest.administer(scanner);
                break;
            case "listall":
                Logger.getInstance().log("Please enter the language you would like to list:");
                String listLang = scanner.nextLine();
                MyConnection.getInstance().listLanguageWords(listLang);
                break;
            case "listhomonyms":
                Logger.getInstance().log("Please enter the language of the word:");
                String homonymLang = scanner.nextLine();
                Logger.getInstance().log("Please enter the word:");
                String homonym = scanner.nextLine();
                MyConnection.getInstance().listHomonyms(homonym, homonymLang);
                break;
            case "checkfor":
                Logger.getInstance().log("Please enter the romanization of the word you would like to check for:");
                String checkWord = scanner.nextLine();
                Logger.getInstance().log("Please enter the language of the word:");
                String wordLang = scanner.nextLine();
                answer  = "";
                while (!(answer.matches("y") || answer.matches("n"))) {
                    Logger.getInstance().log("Would you like to enter the meaning? Without meaning, a homonym may be returned. y/n");
                    answer = scanner.nextLine();
                }
                meaning = null;
                if (answer.matches("y")) {
                    Logger.getInstance().log("Please enter the meaning:");
                    meaning = scanner.nextLine();
                }
                LocalWord myWord = new LocalWord(checkWord, wordLang, meaning);
                boolean exists = myWord.pull();
                if (!exists) {
                    Logger.getInstance().log("Word does not exist in database.");
                } else {
                    Logger.getInstance().log("Found word:");
                    Logger.getInstance().log(myWord.toString());
                }
                break;
            default:
                return false;
        }
        return true;
    }
    
    /**
     * The clean up function.
     */
    private static void cleanUp() {
        // End the connection to the database.
        MyConnection.closeConnection();
        
        // Close the scanner.
        scanner.close();
        
        // Say goodbye to the user.
        Logger.getInstance().log("Good Bye!");
    }
}
