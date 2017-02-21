package biz.channelit.search.ingest.elastic;

import jdk.nashorn.internal.parser.JSONParser;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle.type;

/**
 * Created by hp on 2/17/17.
 */

@Service
public class Setup {

    @Autowired
    TransportClient client;

    @Value("${elastic.settings.folder}")
    String settingsFolder;

    public String putSettings(String index) throws IOException {
        File mappingFile = new ClassPathResource(settingsFolder + "/" + index + ".json").getFile();
        String jsonSettings = new String(Files.readAllBytes(mappingFile.toPath()));
        CreateIndexResponse response = null;
        try {
            response = client.admin().indices().prepareCreate(index)
                    .setSettings(jsonSettings)
                    .get();
        } catch (Exception e){
            e.printStackTrace();
        }
        return response.toString();
    }

}
