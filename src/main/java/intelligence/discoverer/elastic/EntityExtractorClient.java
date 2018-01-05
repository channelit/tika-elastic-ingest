package intelligence.discoverer.elastic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class EntityExtractorClient {

    @Autowired
    RestTemplate restTemplate;

    @Value("${crawler.path}")
    String crawlerpath;

    @Value("${elastic.default.type}")
    String defaultType;

    @Value("${elastic.default.index}")
    String defaultIndex;

    @Value("${parser.url}")
    String parserUrl;

    @Autowired
    EntityTransformer entityTransformer;

    @Autowired
    URI uri;


    public Map<String, Object> processFile(Path filePath) throws IOException {
        byte[] bFile = Files.readAllBytes(filePath);
        ByteArrayResource resource = new ByteArrayResource(bFile) {
            @Override
            public String getFilename() {
                return filePath.getFileName().toString();
            }
        };
        Map<String, Object> map = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> data = new LinkedMultiValueMap<>();
        data.add("file", resource);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(data, headers);
        ResponseEntity<String> responseEntity =
                restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        if (!responseEntity.getStatusCode().is5xxServerError()) {
            String resp = responseEntity.getBody();
            map = entityTransformer.getFieldValues(resp);
        }
        return map;

    }
}
