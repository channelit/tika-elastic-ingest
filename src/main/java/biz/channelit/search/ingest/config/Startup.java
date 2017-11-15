package biz.channelit.search.ingest.config;

import biz.channelit.search.ingest.crawler.FileCrawler;
import biz.channelit.search.ingest.crawler.FileInfoPrinter;
import org.springframework.beans.factory.annotation.Autowired;
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


    @PostConstruct
    public void init() throws IOException {

        String fileInfo = "/Users/hp/workbench/projects/gmu/tika-elastic-ingest/fileInfo.txt";
        String startDir = "/Users";

        fileCrawler.setStartDir(Paths.get(startDir));
        fileInfoPrinter.setFilePath(Paths.get(fileInfo));
        if (Files.exists(Paths.get(fileInfo))) {
            Stream<String> dirs = Files.lines(Paths.get(fileInfo));
            List<String> visitedDir = dirs.map(line->line.split("\\|")[2]).distinct().collect(Collectors.toList());
            fileCrawler.setVisitedDir(visitedDir);
        }

        Files.walkFileTree(Paths.get(startDir), fileCrawler);
    }



}
