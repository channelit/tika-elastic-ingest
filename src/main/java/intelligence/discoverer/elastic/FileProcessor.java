package intelligence.discoverer.elastic;

import intelligence.discoverer.tika.Extractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CompletableFuture;

@Service
public class FileProcessor {

    @Autowired
    FileInfoPrinter fileInfoPrinter;

    @Autowired
    FileIndexer fileIndexer;


    @Async
    public CompletableFuture<String> processFile(Path file, BasicFileAttributes attrs) throws IOException {
        String fileName = file.getFileName().toString();
        fileInfoPrinter.printToFile(fileName, String.valueOf(attrs.size()), file.getParent().toString(), fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")).toUpperCase() : "UNKNOWN");
        fileIndexer.indexFile(file, attrs);
        String results = fileIndexer.indexFile(file, attrs);
        return CompletableFuture.completedFuture(results);
    }

    public void processFileLocal(MultipartFile file) throws IOException {
        Extractor.extractFileContent(file.getInputStream());
    }

}
