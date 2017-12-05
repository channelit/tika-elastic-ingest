package intelligence.discoverer.elastic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import intelligence.discoverer.elastic.EntityTransformer;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Service
public class FileProcessor {

    @Autowired
    BulkProcessor bulkProcessor;

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

    private final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(parserUrl);
    private final URI uri = builder.build().encode().toUri();


    public void processFile(Path filePath) throws IOException {
        byte[] bFile = Files.readAllBytes(filePath);
        ByteArrayResource resource = new ByteArrayResource(bFile) {
            @Override
            public String getFilename() {
                return filePath.getFileName().toString();
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> data = new LinkedMultiValueMap<>();
        data.add("file", resource);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(data, headers);
        ResponseEntity<String> responseEntity =
                restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        String resp = responseEntity.getBody();
        Map<String, Object> map = entityTransformer.getFieldValues(resp);
        bulkProcessor.add(new IndexRequest(defaultIndex, defaultType).source(map));

    }
}
