package biz.channelit.search.ingest.elastic;

import jdk.nashorn.internal.parser.JSONParser;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by hp on 2/17/17.
 */

@Service
public class Setup {

    @Autowired
    TransportClient client;

    @Value("${elastic.mapping.folder}")
    String mappingFolder;

    public void createMappings(String index, String type) throws IOException {
        File mappingFile = new ClassPathResource(mappingFolder + type + ".json").getFile();
        String jsonMapping = new String(Files.readAllBytes(mappingFile.toPath()));
        client.admin().indices().preparePutMapping(index)
                .setType(type)
                .setSource(jsonMapping)
                .get();
    }
}
