package intelligence.discoverer.config;

import intelligence.discoverer.crawler.FileCrawler;
import intelligence.discoverer.elastic.FileInfoLogger;
import intelligence.discoverer.scheduler.FileChanger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class Startup {

    @Autowired
    FileCrawler fileCrawler;

    @Autowired
    FileInfoLogger fileInfoLogger;

    @Autowired
    FileChanger fileChanger;


    @Value("${crawler.path}")
    String crawlerpath;

    @Value("${crawler.file}")
    String crawlerfile;

//    @PostConstruct
    public void init() throws IOException {

        fileChanger.changeFile();

        if (Files.exists(Paths.get(crawlerfile))) {
            Stream<String> dirs = Files.lines(Paths.get(crawlerfile));
            List<String> visitedDir = dirs.map(line->line.split("\\|")[2]).distinct().collect(Collectors.toList());
            fileCrawler.setVisitedDir(visitedDir);
        }

        Files.walkFileTree(Paths.get(crawlerpath), fileCrawler);
    }



}
