// PlayerData.java

public class PlayerData {
    private int maxAnswers = 4;
    private int answerCounter = 0;

    public void increaseAnswerCounter() {
        if(answerCounter < maxAnswers) {
            answerCounter++;
        }
    }

    public int getAnswerCounter() {
        return answerCounter;
    }

    public void resetAnswerCounter() {
        answerCounter = 0;
    }
}