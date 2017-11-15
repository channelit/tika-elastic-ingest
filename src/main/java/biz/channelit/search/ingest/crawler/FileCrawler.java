package biz.channelit.search.ingest.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

@Service
public class FileCrawler implements FileVisitor<Path> {

    @Autowired
    FileInfoPrinter fileInfoPrinter;

    private Path startDir;

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile())
        fileInfoPrinter.printToFile(file.getFileName().toString(), String.valueOf(attrs.size()), file.getParent().toString());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.TERMINATE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        boolean done = Files.isSameFile(dir, startDir);
        return done ? FileVisitResult.TERMINATE: FileVisitResult.CONTINUE;
    }

    public void setStartDir(Path startDir) {
        this.startDir = startDir;
    }
}
