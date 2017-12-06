package intelligence.discoverer.elastic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CompletableFuture;

@Service
public class FileProcessor {

    @Autowired
    FileInfoPrinter fileInfoPrinter;


    @Async
    public CompletableFuture<String> processFile(Path file, BasicFileAttributes attrs) throws IOException {
        String fileName = file.getFileName().toString();
        fileInfoPrinter.printToFile(fileName, String.valueOf(attrs.size()), file.getParent().toString(), fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")).toUpperCase() : "UNKNOWN");


        String results = "";

        return CompletableFuture.completedFuture(results);
    }
}
