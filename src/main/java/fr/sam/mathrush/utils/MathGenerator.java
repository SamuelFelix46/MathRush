package fr.sam.mathrush.utils;

import fr.sam.mathrush.models.MathQuestion;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.concurrent.ThreadLocalRandom;

public class MathGenerator {

    private final FileConfiguration config;

    public MathGenerator(FileConfiguration config) {
        this.config = config;
    }

    /**
     * Génère une question selon la difficulté.
     * En mode "hard" avec combo-operations, peut générer des expressions à 2 opérations.
     */
    public MathQuestion generate(String difficulty) {
        boolean combo = difficulty.equals("hard")
                && config.getBoolean("difficulty.hard.combo-operations", false);

        if (combo && ThreadLocalRandom.current().nextBoolean()) {
            return generateCombo(difficulty);
        }
        return generateSimple(difficulty);
    }

    private MathQuestion generateSimple(String diff) {
        int type = ThreadLocalRandom.current().nextInt(4);
        int a, b, answer;
        String symbol;

        switch (type) {
            case 0:
                a = rng(diff, "addition");
                b = rng(diff, "addition");
                answer = a + b;
                symbol = "+";
                break;
            case 1:
                a = rng(diff, "subtraction");
                b = rng(diff, "subtraction");
                if (a < b) { int t = a; a = b; b = t; }
                answer = a - b;
                symbol = "-";
                break;
            case 2:
                a = rng(diff, "multiplication");
                b = rng(diff, "multiplication");
                answer = a * b;
                symbol = "×";
                break;
            default:
                b = rng(diff, "division");
                if (b == 0) b = 2;
                answer = rng(diff, "division");
                a = answer * b;
                symbol = "÷";
                break;
        }

        return new MathQuestion(a + " " + symbol + " " + b, answer, diff);
    }

    /**
     * Ex: (12 + 5) × 3  ou  8 × 4 - 7
     */
    private MathQuestion generateCombo(String diff) {
        ThreadLocalRandom r = ThreadLocalRandom.current();

        // Première opération simple
        int a = rng(diff, "addition");
        int b = rng(diff, "addition");
        if (a < b) { int t = a; a = b; b = t; }

        boolean addFirst = r.nextBoolean();
        int intermediate = addFirst ? (a + b) : (a - b);
        String firstOp = addFirst ? "+" : "-";

        // Seconde opération
        int c = rng("easy", "multiplication");
        if (c == 0) c = 2;
        boolean multSecond = r.nextBoolean();

        int answer;
        String display;

        if (multSecond) {
            answer = intermediate * c;
            display = "(" + a + " " + firstOp + " " + b + ") × " + c;
        } else {
            int d = rng("easy", "addition");
            answer = intermediate + d;
            display = a + " " + firstOp + " " + b + " + " + d;
        }

        return new MathQuestion(display, answer, diff);
    }

    private int rng(String diff, String op) {
        var list = config.getIntegerList("difficulty." + diff + "." + op);
        int min = list.size() >= 1 ? list.get(0) : 1;
        int max = list.size() >= 2 ? list.get(1) : 20;
        if (max <= min) max = min + 1;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
