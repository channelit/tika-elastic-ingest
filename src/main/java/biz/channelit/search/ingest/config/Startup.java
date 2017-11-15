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

@Component
public class Startup {

    @Autowired
    FileCrawler fileCrawler;

    @Autowired
    FileInfoPrinter fileInfoPrinter;


    @PostConstruct
    public void init() throws IOException {
        fileCrawler.setStartDir(Paths.get("/Users"));
        fileInfoPrinter.setFilePath(Paths.get("/Users/hp/workbench/projects/gmu/tika-elastic-ingest/fileInfo.txt"));

        Files.walkFileTree(Paths.get("/Users"), fileCrawler);
    }



}
