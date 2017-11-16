package biz.channelit.search.ingest.config;

import biz.channelit.search.ingest.crawler.FileCrawler;
import biz.channelit.search.ingest.crawler.FileInfoPrinter;
import biz.channelit.search.ingest.scheduler.FileChanger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class Startup {

    @Autowired
    FileCrawler fileCrawler;

    @Autowired
    FileInfoPrinter fileInfoPrinter;

    @Autowired
    FileChanger fileChanger;


    @Value("${crawler.path}")
    String crawlerpath;

    @Value("${crawler.file}")
    String crawlerfile;

    @PostConstruct
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
