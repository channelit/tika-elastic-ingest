package intelligence.discoverer.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileCrawler implements FileVisitor<Path> {

    @Autowired
    FileInfoPrinter fileInfoPrinter;

    @Value("${crawler.path}")
    private Path startDir;

    private List<String> visitedDir = new ArrayList<>();

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile()) {
            String fileName = file.getFileName().toString();
            fileInfoPrinter.printToFile(fileName, String.valueOf(attrs.size()), file.getParent().toString(), fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")).toUpperCase() : "UNKNOWN");
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        boolean done = Files.isSameFile(dir, startDir);
        return done ? FileVisitResult.TERMINATE: FileVisitResult.CONTINUE;
    }

    public void setStartDir(Path startDir) {
        this.startDir = startDir;
    }

    public void setVisitedDir(List<String> visitedDir) {
        this.visitedDir = visitedDir;
    }
}
