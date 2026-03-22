package fr.sam.mathrush.models;

public class MathQuestion {

    private final String display;
    private final int answer;
    private final String difficulty;

    public MathQuestion(String display, int answer, String difficulty) {
        this.display = display;
        this.answer = answer;
        this.difficulty = difficulty;
    }

    public String getDisplay() { return display; }
    public int getAnswer() { return answer; }
    public String getDifficulty() { return difficulty; }
}
