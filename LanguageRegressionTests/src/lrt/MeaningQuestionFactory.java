package lrt;

import java.util.ArrayList;

import database.LocalWord;
import database.MyConnection;

/**
 * Generate questions about the meaning of a romanized
 * word. These questions are naturally more difficult
 * to answer correctly, as the meaning entered must
 * match perfectly to the meaning stored in the database.
 * @author Evan Gunn
 *
 */
public class MeaningQuestionFactory extends QuestionFactory {
    public MeaningQuestionFactory(String queryModifier) {
        super(queryModifier);
    }

    @Override
    public ArrayList<Question> generateQuestions(int amount, String language) {
        ArrayList<Question> myQuestions = new ArrayList<Question>();
        
        // Create and run a query on the database to get required data for questions.
        String myQuery = "SELECT DISTINCT W.romanization, NULL, W.meaning, NULL, NULL, NULL, NULL "
                       + "FROM word W, symbols S, wordsource N "
                       + "WHERE W.wlanguage = '" + language + "' " + queryModification
                       + "ORDER BY RAND() "
                       + "LIMIT " + String.valueOf(amount) + " ;";
        ArrayList<LocalWord> myWords = MyConnection.getInstance().getWords(myQuery, amount);
        
        // Convert the words to questions
        for (int i = 0; i < myWords.size(); i++) {
            myQuestions.add(new Question("What is the meaning of the " + language + " romanized word: " + myWords.get(i).getRomanization(), myWords.get(i).getMeaning()));
        }
        return myQuestions;
    }
}
