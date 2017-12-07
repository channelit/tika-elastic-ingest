package intelligence.discoverer.elastic;

import intelligence.discoverer.opennlp.OpenNlpNer;
import intelligence.discoverer.tika.Extractor;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public String indexFile(Path file, BasicFileAttributes attrs) throws IOException {
        Map<String, Object> map = entityExtractorClient.processFile(file);
        map.put("size", attrs.size());
        map.put("lastAccessTime", attrs.lastAccessTime());
        map.put("createTime", attrs.creationTime());
        map.put("fullPath", file.getFileName());
        map.put("lastModifiedTime", attrs.lastModifiedTime());
        bulkProcessor.add(new IndexRequest(defaultIndex, defaultType).source(map));
        return "ok";
    }

    public String indexFileLocal(MultipartFile file) throws IOException {
        String content = Extractor.extractFileContent(file.getInputStream());
        Map<String, List<String>> map = openNlpNer.getAll(content);
        map.put("content", Stream.of(content).collect(Collectors.toList()));
        map.put("fullPath", Stream.of(file.getOriginalFilename()).collect(Collectors.toList()));
        bulkProcessor.add(new IndexRequest(defaultIndex, defaultType).source(map));
        return "ok";
    }
}
