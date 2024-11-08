import java.util.*;

public class CommandParser {
    private String          cmdString;
    private List<String>    options = new ArrayList<>();
    private List<String>    paths = new ArrayList<>();
    private String          rawCommandString;

    public CommandParser() {
    }

    public void parse(String rawCommandString) {
        List<String> res = new ArrayList<>();
        StringTokenizer itr = new StringTokenizer(rawCommandString);
        while (itr.hasMoreTokens()) {
            res.add(itr.nextToken());
        }
        if (res.size() < 2) {
            System.err.println("CommandParser(): Invalid command " + rawCommandString);
            return;
        }
        this.cmdString = res.get(0);
        for (int i = 1; i < res.size(); i++)
            paths.add(res.get(i));
    }

    public void clear() {
        cmdString = "";
        paths.clear();
    }

    public String command() {
        return this.cmdString;
    }

    public List<String> paths() { return this.paths; }

}
