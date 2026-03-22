import java.util.Arrays;
import java.util.List;

public class MathRushTabCompleter {
    private static final List<String> SUBS = Arrays.asList( "defnum", "def", "run", "defreward", "supprreward", "time", "stats", "top", "reload" );

    public List<String> getCompletions(String command, String[] args) {
        switch (command) {
            case "defnum":
                // Your existing code
                break;
            case "def":
                if (args.length == 2) return Arrays.asList("5", "10", "30", "60", "120");
                break;
            // Other cases...
        }
        return null;
    }
}