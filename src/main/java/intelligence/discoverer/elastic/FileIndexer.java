package intelligence.discoverer.elastic;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

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

    public void indexFile(Path file) throws IOException {
        bulkProcessor.add(new IndexRequest(defaultIndex, defaultType).source(entityExtractorClient.processFile(file)));
    }
}
