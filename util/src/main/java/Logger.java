
public class Logger {
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RESET = "\u001B[0m";


    public static final int DEBUG_LEVEL = 3;
    public static final int LOG_LEVEL = 2;
    public static final int ERROR_LEVEL = 1;

    private static int level = LOG_LEVEL;

    public static void setLevel(int l) {
        level = l;
    }

    public static void debug(String message) {
        if (level < DEBUG_LEVEL)
            return;
        System.out.println(ANSI_YELLOW + "[DEBUG]" +
                " - " + message + ANSI_RESET);
    }

    public static void log(String message) {
        if (level < LOG_LEVEL)
            return;
        System.out.println(ANSI_BLUE + "[log]" +
                " - " + message + ANSI_RESET);
    }

    public static void error(String message) {
        System.out.println(ANSI_RED + "[error]" +
                " - " + message + ANSI_RESET);
    }

    public static void success(String message) {
        System.out.println(ANSI_GREEN + "[success]" +
                " - " + message + ANSI_RESET);
    }

}
