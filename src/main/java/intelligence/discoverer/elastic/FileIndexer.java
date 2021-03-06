package intelligence.discoverer.elastic;

import intelligence.discoverer.opennlp.OpenNlpNer;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileIndexer {

    @Autowired
    @Qualifier("esBulkProcessor")
    BulkProcessor bulkProcessor;

    @Value("${elastic.default.type}")
    String defaultType;

    @Value("${elastic.default.index}")
    String defaultIndex;

    @Autowired
    @Qualifier("esclient")
    Client client;

    @Autowired
    EntityExtractorClient entityExtractorClient;

    @Autowired
    OpenNlpNer openNlpNer;

    @Value("${file.maxsize}")
    Long fileMaxSize;

    public String indexFile(Path file, BasicFileAttributes attrs) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("file_size", attrs.size());
        map.put("file_accessed", attrs.lastAccessTime());
        map.put("file_created", attrs.creationTime());
        map.put("file_url", file.toAbsolutePath().toString());
        map.put("file_modified", attrs.lastModifiedTime());
        map.put("category", "IMP");
        if (attrs.size() < fileMaxSize) {
            map.putAll(entityExtractorClient.processFile(file));
        } else {
            map.put("error", "large");
        }
        bulkProcessor.add(new IndexRequest(defaultIndex, defaultType).source(map));
        return "ok";
    }

}
