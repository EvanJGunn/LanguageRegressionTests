package lrt;

import java.util.ArrayList;
import java.util.Scanner;

import application.Logger;

/**
 * The Test class provides a container for a list of questions, and
 * the ability to list those questions and retrieve user response.
 * @author Evan Gunn
 *
 */
public class Test {
    private ArrayList<Question> questions = null;
    
    public Test(int questionCount, String language, QuestionFactory questionFactory) {
        questions = questionFactory.generateQuestions(questionCount, language);
    }
    
    /**
     * Administer the test to the user. Display the list of questions,
     * retrieve entered answers. Tell the user if their answer was correct.
     * @param scanner
     */
    public void administer(Scanner scanner) {
        if (questions == null || questions.size() == 0) {
            Logger.getInstance().log("Test does not contain any questions.");
            return;
        }
        // Display the questions, get response, tell the user if they were correct or incorrect.
        for (int i = 0; i < questions.size(); i++) {
            Logger.getInstance().log("-------------------------------------------------------------");
            Logger.getInstance().log(questions.get(i).getQuestion());
            
            String answered = scanner.nextLine();
            questions.get(i).setuserAnswer(answered);
            
            if (questions.get(i).correctAnswer()) Logger.getInstance().log("Correct!");
            if (!questions.get(i).correctAnswer()) Logger.getInstance().log("Incorrect.");
            Logger.getInstance().log("The correct answer was: " + questions.get(i).getAnswer());
        }
    }
}
