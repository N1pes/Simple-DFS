import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class FileHelper {
    public static int getFileLength(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            return (int) fileChannel.size();
        }
    }

    public static void randomWriteToFile(String filePath, long offset, byte[] data) throws IOException {
        Path path = Paths.get(filePath);
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE)) {
            fileChannel.position(offset);
            ByteBuffer buffer = ByteBuffer.wrap(data);
            fileChannel.write(buffer);
        }
    }

    public static ByteBuffer randomReadFromFile(String filePath, long offset, int length) throws IOException {
        Path path = Paths.get(filePath);
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            fileChannel.position(offset);
            ByteBuffer buffer = ByteBuffer.allocate(length);
            int bytesRead = fileChannel.read(buffer);
            if (bytesRead == -1) {
                System.out.println("Reached end of file.");
            }
            buffer.flip();
            return buffer;
        }
    }
}