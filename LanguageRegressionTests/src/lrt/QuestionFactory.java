package lrt;

import java.util.ArrayList;

/**
 * My abstract question factory. All concrete question factories extend
 * this class, and implement their own version of the method generateQuestions.
 * @author Evan Gunn
 *
 */
public abstract class QuestionFactory {
    protected String queryModification = null;
    public QuestionFactory(String queryModifier) {
        queryModification = queryModifier;
    }
    /**
     * Generate questions.
     * @param amount The amount of questions to generate.
     * @param queryModifier A modification to the query's where statement, can be left blank "".
     * @return An ArrayList of the questions generated.
     */
    public abstract ArrayList<Question> generateQuestions(int amount, String language);
}
