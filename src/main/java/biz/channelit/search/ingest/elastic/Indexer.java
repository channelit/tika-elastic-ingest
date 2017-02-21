package biz.channelit.search.ingest.elastic;

import biz.channelit.search.ingest.corenlp.CoreNlpNer;
import biz.channelit.search.ingest.opennlp.OpenNlpNer;
import biz.channelit.search.ingest.tika.Extractor;
import edu.stanford.nlp.dcoref.CorefChain;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by hp on 2/14/17.
 */

@Service
public class Indexer {

    @Autowired
    TransportClient client;

    List<File> files;

    @Value("${crawler.path}")
    String crawlerpath;

    @Autowired
    CoreNlpNer coreNlpNer;

    @Autowired
    OpenNlpNer openNlpNer;

    public void indexXocuments() throws IOException {

        IndexResponse response = client.prepareIndex("twitter", "tweet")
                .setSource(jsonBuilder()
                        .startObject()
                        .field("user", "kimchy")
                        .field("postDate", new Date())
                        .field("message", "trying out Elasticsearch")
                        .endObject()
                )
                .get();
    }

    public void indexFiles() {
        files = new LinkedList<>();
        walk(crawlerpath);
        files.forEach(file -> {
            String content = extractFileContet(file);
//            Map<Integer, CorefChain> extractor = coreNlpNer.extract(content);
            List<String> names = openNlpNer.findNames(content);
            System.out.println(names);
            try {
                IndexResponse response = client.prepareIndex("content", "content")
                        .setSource(jsonBuilder()
                                .startObject()
                                .field("body", content)
                                .field("insertDate", new Date())
                                .field("names", names)
                                .endObject()
                        )
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private String extractFileContet(File file) {
        return Extractor.extractFileContet(file);
    }

    private void walk(String path) {

        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) return;

        for (File f : list) {
            if (f.isDirectory()) {
                walk(f.getAbsolutePath());
                System.out.println("Dir:" + f.getAbsoluteFile());
            } else {
                files.add(f);
                System.out.println("File:" + f.getAbsoluteFile());
            }
        }
    }
}
