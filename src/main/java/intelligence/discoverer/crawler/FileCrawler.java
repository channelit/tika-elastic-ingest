package intelligence.discoverer.crawler;

import intelligence.discoverer.elastic.FileInfoLogger;
import intelligence.discoverer.elastic.FileProcessor;
import intelligence.discoverer.scheduler.FileChanger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileCrawler implements FileVisitor<Path> {

    @Value("${crawler.path}")
    private Path startDir;

    private List<String> visitedDir = new ArrayList<>();

    @Autowired
    FileCrawler fileCrawler;

    @Autowired
    FileProcessor fileProcessor;

    @Autowired
    FileChanger fileChanger;

    @Autowired
    FileInfoLogger fileInfoLogger;

    @Value("${crawler.path}")
    String crawlerpath;

    @Value("${crawler.file}")
    String crawlerfile;

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile()) {
            if (!fileInfoLogger.fileIndexed(file.getFileName().toString())) {
                fileProcessor.processFile(file, attrs);
            }
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

    public void startCrawler() throws IOException {
        fileChanger.changeFile();

        if (Files.exists(Paths.get(crawlerfile))) {
            Stream<String> dirs = Files.lines(Paths.get(crawlerfile));
            List<String> visitedDir = dirs.map(line->line.split("\\|")[2]).distinct().collect(Collectors.toList());
            fileCrawler.setVisitedDir(visitedDir);
        }

        Files.walkFileTree(Paths.get(crawlerpath), fileCrawler);
    }
}
