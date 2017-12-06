package intelligence.discoverer.elastic;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
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
