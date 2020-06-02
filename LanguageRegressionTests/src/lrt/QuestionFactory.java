package lrt;

import java.util.ArrayList;

/**
 * My abstract question factory. All concrete question factories extend
 * this class, and implement their own version of the method generateQuestions.
 * @author Evan Gunn
 *
 */
public abstract class QuestionFactory {
    /**
     * Generate questions.
     * @param amount The amount of questions to generate.
     * @return An ArrayList of the questions generated.
     */
    public abstract ArrayList<Question> generateQuestions(int amount, String language);
}
