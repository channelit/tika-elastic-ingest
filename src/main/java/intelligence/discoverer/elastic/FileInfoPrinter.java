package intelligence.discoverer.elastic;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class FileInfoPrinter {

    private Path filePath;

    @Async
    public void printToFile(String... str) throws IOException {
        Files.write(filePath, Arrays.stream(str).collect(Collectors.joining("|")).concat(System.lineSeparator()).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public void setFilePath(Path filePath) throws IOException {
        if (!Files.exists(filePath))
            Files.createFile(filePath);
        this.filePath = filePath;
    }


}