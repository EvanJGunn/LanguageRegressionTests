package application;

import java.util.Scanner;

import database.MyConnection;

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
        System.out.println("Welcome to the Language Regression Tests program!");
        
        // Set up the scanner that will be used.
        scanner = new Scanner(System.in);
        
        // Connect to the server.
        while(!acquiredConnection());
        
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
        System.out.println("Please enter your AWS Endpoint:");
        String endpoint = scanner.nextLine();
        
        System.out.println("Please enter the port number:");
        String port = scanner.nextLine();
        
        System.out.println("Please enter the MySQL schema name:");
        String schema = scanner.nextLine();
        
        System.out.println("Please enter your username:");
        String user = scanner.nextLine();
        
        System.out.println("Please enter your password:");
        String password = scanner.nextLine();
        
        boolean connected = MyConnection.InitializeConnection(endpoint, port, schema, user, password);
        
        // If we are unable to connect, check if the user would like to retry connection.
        if (!connected) {
            System.out.println("Could not connect to sql server, try again? y/n");
            String answer = scanner.nextLine();
            
            if (!answer.matches("y")) {
                System.out.println("Good Bye!");
                System.exit(0);
            }
            return false;
        }
        
        System.out.println("Connection successful!");
        return true;
    }
    
    /**
     * Present command options to the user for interaction with their database and regression test creation/execution tools.
     */
    private static void optionsLoop() {
        while(continueLoop) {
            printCommands();
            String input = scanner.nextLine();
            boolean parsed = parseOptions(input);
            if(!parsed) {
                System.out.println("Failed to parse input: "+input);
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
                + "********************\n");
    }
    
    /**
     * Parse options input and execute user commands.
     * @param input The input from the user.
     * @return Return true if successfully parsed/executed input, return false if input was unparsable.
     */
    private static boolean parseOptions(String input) {
        switch(input){
            case "quit":
                continueLoop = false;
                break;
            default:
                return false;
        }
        return true;
    }
    
    /**
     * The clean up function currently closes the connection to the database.
     */
    private static void cleanUp() {
        // End the connection to the database.
        MyConnection.CloseConnection();
        
        // Say goodbye to the user.
        System.out.println("Good Bye!");
    }
}
