package intelligence.discoverer.elastic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CompletableFuture;

@Service
public class FileProcessor {

    @Autowired
    FileInfoLogger fileInfoLogger;

    @Autowired
    FileIndexer fileIndexer;


//    @Async
    public CompletableFuture<String> processFile(Path file, BasicFileAttributes attrs) throws IOException {
        String fileName = file.getFileName().toString();
        fileInfoLogger.printToFile(fileName, String.valueOf(attrs.size()), file.getParent().toString(), fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")).toUpperCase() : "UNKNOWN");
        String results = fileIndexer.indexFile(file, attrs);
        return CompletableFuture.completedFuture(results);
    }

}
