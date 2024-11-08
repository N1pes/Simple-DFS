import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

// helper class for master to maintain a virtual filesystem

public class FileTree {
    String              name;
    List<FileTree>      children;
    boolean             isDirectory;

    FileTree(String name) {
        this.name = name;
        this.children = new ArrayList<>();
        this.isDirectory = false;
    }

    FileTree(String name, boolean isDirectory) {
        this.name = name;
        this.children = new ArrayList<>();
        this.isDirectory = isDirectory;
    }

    void add(FileTree f) {
        children.add(f);
    }

    void remove(String victim) {
        for (FileTree child : children) {
            if (victim.equals(child.name)) {
                this.children.remove(child);
                return;
            }
        }
        System.err.println("FileTree::remove::() " + victim + " not found");
    }

    FileTree getDirectChild(String name) {
        for (FileTree f : children) {
            if (f.name.equals(name)) {
                return f;
            }
        }
        return null;
    }

    FileTree getChild(String path) {
        List<String> splitPaths = new ArrayList<>();
        StringTokenizer itr = new StringTokenizer(path, "/");
        FileTree child, curr;
        child = curr = this;
        while (itr.hasMoreTokens()) {
            splitPaths.add(itr.nextToken());
        }

        for (String p : splitPaths) {
            child = curr.getDirectChild(p);
            Logger.log("child: " + child.name + " curr: " + curr.name);
            curr = child;

        }
        return child;
    }

    boolean existDirectChild(String name) {
        for (FileTree f : children) {
            if (f.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public StringBuilder listChildren() {
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_BLUE = "\u001B[34m";
        Logger.debug("Listing " + name);
        StringBuilder result = new StringBuilder();
        for (FileTree f: children) {
            if (f.isDirectory) {
                System.out.print(ANSI_BLUE + f.name + " " + ANSI_RESET);
                result.append(ANSI_BLUE).append(f.name).append(" ").append(ANSI_RESET);
            }
            else {
                System.out.print(f.name + " ");
                result.append(f.name).append(" ");
            }

        }
        result.append("\n");
        return result;
    }
}