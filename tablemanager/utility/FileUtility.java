package tablemanager.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileUtility {

    // Create a new file at the given path
    public static Path createFile(String fileName) throws IOException {
        if (!fileName.toLowerCase().endsWith(".txt")) {
            fileName += ".txt";
        }

        Path path = Paths.get(fileName);

        if (Files.exists(path)) {
            throw new IOException("File already exists: " + fileName);
        }

        return Files.createFile(path);
    }

    // Check if a file exists and is a regular file
    public static boolean fileExists(String fileName) {
        Path path = Paths.get(fileName);
        return Files.exists(path) && Files.isRegularFile(path);
    }

    // Validate an existing file path
    public static Path validateFile(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        if (!fileExists(fileName)) {
            throw new IOException("File not found: " + fileName);
        }
        return path;
    }

    // Read all lines from a file
    public static List<String> readFile(String fileName) throws IOException {
        Path path = validateFile(fileName);
        return Files.readAllLines(path);
    }

    // Write lines to a file (overwrites existing content)
    public static void writeFile(String fileName, List<String> lines) throws IOException {
        Path path = Paths.get(fileName);
        Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
