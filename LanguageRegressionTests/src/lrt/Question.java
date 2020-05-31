package lrt;

/**
 * Store a question and the answer to the question.
 * @author Evan Gunn
 *
 */
public class Question {
    private String question, answer, userAnswer = null;

    public Question(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    /**
     * Set the answer that the user has given.
     * @param newAnswer The user's answer.
     */
    public void setuserAnswer(String newAnswer) {
        userAnswer = newAnswer;
    }

    /**
     * @return The user's answer.
     */
    public String getUserAnswer() {
        return userAnswer;
    }
}
