package lrt;

import java.util.ArrayList;

import database.LocalWord;
import database.MyConnection;

/**
 * This question factory utilizes the connection the the database to retrieve
 * data about the symbols table. It uses that data to generate questions
 * about readings of symbols. For example, the hiragana reading of japanese
 * kanji. The selection is random.
 * @author Evan Gunn
 *
 */
public class SymbolsMainQuestionFactory extends QuestionFactory {
    public SymbolsMainQuestionFactory(String queryModifier) {
        super(queryModifier);
    }

    @Override
    public ArrayList<Question> generateQuestions(int amount, String language) {
        ArrayList<Question> myQuestions = new ArrayList<Question>();
        
        // Create and run a query on the database to get required data for questions.
        String myQuery = "SELECT NULL, NULL, NULL, NULL, S.main, S.ancillary, NULL "
                       + "FROM symbols S, word W, wordsource N "
                       + "WHERE S.wid = W.wid AND W.wlanguage = '" + language + "' " + queryModification
                       + "ORDER BY RAND() "
                       + "LIMIT " + String.valueOf(amount) + " ;";
        ArrayList<LocalWord> myWords = MyConnection.getInstance().getWords(myQuery, amount);
        
        // Convert the words to questions
        for (int i = 0; i < myWords.size(); i++) {
            myQuestions.add(new Question("What is the reading of the symbol(s): " + myWords.get(i).getMainSymbols(), myWords.get(i).getAncillarySymbol()));
        }
        return myQuestions;
    }
}
