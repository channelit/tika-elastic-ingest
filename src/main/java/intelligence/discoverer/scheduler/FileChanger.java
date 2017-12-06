package intelligence.discoverer.scheduler;

import intelligence.discoverer.elastic.FileInfoPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class FileChanger {

    @Autowired
    FileInfoPrinter fileInfoPrinter;

    @Value("${crawler.file}")
    String crawlerfile;

    @Scheduled(fixedDelay = 60000, initialDelay = 0)
    public void changeFile() throws IOException {
        String suffix = new SimpleDateFormat("yyyyMMddHH'.txt'").format(new Date());;
        fileInfoPrinter.setFilePath(Paths.get(crawlerfile.replace(".txt", suffix)));
    }

}
