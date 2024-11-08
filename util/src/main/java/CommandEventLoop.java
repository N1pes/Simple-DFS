import java.util.ArrayList;
import java.util.List;

public class CommandEventLoop extends Thread {

    CommandParser   commandParser;
    CommandExecutor commandExecutor;

    CommandEventLoop(CommandExecutor commandExecutor) {
        this.commandParser = new CommandParser();
        this.commandExecutor = commandExecutor;
    }

    @Override
    public void run() {
        loop();
    }

    public void loop() {
        while (true) {
            String command = commandExecutor.readCommand();
            System.out.println("CommandEventLoop() read command: " + command);
            List<String> options = new ArrayList<>();
            List<String> paths;
            commandParser.parse(command);
            paths = commandParser.paths();
            commandExecutor.execute(commandParser.command(), new ArrayList<>(), paths);
            commandExecutor.responseCommand();
            commandParser.clear();
        }
    }
}
