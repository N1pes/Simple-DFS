import java.util.*;

public interface CommandExecutor {

    public void execute(String cmdStr, List<String>options, List<String>path);
    public String readCommand();
    public void responseCommand();

}
