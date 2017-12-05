package gov.dhs.cbp.afi.ext.elastic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
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

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Service
public class FileProcessor {


    @Autowired
    RestTemplate restTemplate;

    @Autowired
    TransportClient client;

    @Value("${crawler.path}")
    String crawlerpath;

    @Value("${elastic.default.type}")
    String defaultType;

    @Value("${elastic.default.index}")
    String defaultIndex;

    @Value("${parser.url}")
    String parserUrl;

    private final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(parserUrl);
    private final URI uri = builder.build().encode().toUri();


    public void processFile(Path filePath) throws IOException {
        BulkRequestBuilder bulkRequest = client.prepareBulk();

        byte[] bFile = Files.readAllBytes(filePath);
        ByteArrayResource resource = new ByteArrayResource(bFile) {
            @Override
            public String getFilename() {
                return filePath.getFileName().toString();
            }
        };
        MultiValueMap<String, Object> data = new LinkedMultiValueMap<>();
        data.add("file", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(data, headers);

        try {
            ResponseEntity<String> responseEntity =
                    restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
            String resp = responseEntity.getBody();
            System.out.println(resp);

            JsonParser parser = new JsonParser();
            JsonObject o = parser.parse(resp).getAsJsonObject();

            List<String> person = new ArrayList<>();
            List<String> company = new ArrayList<>();
            List<String> place = new ArrayList<>();
            List<GeoPoint> geoPoint = new ArrayList<>();
            XContentBuilder jsonDoc = jsonBuilder()
                    .startObject()
                    .field("body", o.get("text").getAsString())
                    .field("insertDate", new Date())
                    .field("file",o.get("fileName").getAsString())
                    .field("url", "text")
                    .field("filetype", "text")
                    .field("person",person)
                    .field("company", company)
                    .field("place", place)
                    .field("location", geoPoint)
                    .endObject();
            bulkRequest.add(client.prepareIndex(defaultIndex, defaultType)
                    .setSource(jsonDoc));
            bulkRequest.get();

        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }
    }
}
