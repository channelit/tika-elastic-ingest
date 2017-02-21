package biz.channelit.search.ingest.elastic;

import biz.channelit.search.ingest.corenlp.CoreNlpNer;
import biz.channelit.search.ingest.opennlp.OpenNlpNer;
import biz.channelit.search.ingest.tika.Extractor;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import static org.elasticsearch.index.query.QueryBuilders.*;

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

    @Value("${elastic.default.type}")
    String defaultType;

    @Value("${elastic.default.index}")
    String defaultIndex;

    @Autowired
    CoreNlpNer coreNlpNer;

    @Autowired
    OpenNlpNer openNlpNer;

    public void indexFiles() {
        files = new LinkedList<>();
        walk(crawlerpath);
        files.forEach(file -> {
            String content = extractFileContet(file).replaceAll("[^A-Za-z ]", " ").replaceAll(" +", " ");
//            Map<String, List<String>> map = openNlpNer.getAll(content);
            try {
                IndexResponse response = client.prepareIndex(defaultIndex, defaultType)
                        .setSource(jsonBuilder()
                                .startObject()
                                .field("body", content)
                                .field("insertDate", new Date())
//                                .map(map)
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

    public UpdateResponse update(String index, String type, String id, Map<String, List<String>> map) throws IOException, ExecutionException, InterruptedException {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index(index).type(type);
        updateRequest.id(id);
        updateRequest.doc(map);
        return client.update(updateRequest).get();
    }

    public SearchResponse search(String index, String type, String query) {
        QueryBuilder qb = queryStringQuery(query);
        return client.prepareSearch(index).setTypes(type).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(qb).setFetchSource("body",null)
                .setSize(10).execute().actionGet();
    }

    public void populateNer(String index, String type, String query) {
        SearchResponse searchResponse = search(index, type, query);
        searchResponse.getHits().forEach((hit) -> {
            Map source = hit.getSource();
            String content = (String) source.get("body");
            Map<String, List<String>> map = openNlpNer.getAll(content);
            try {
                update(index,type, hit.getId(),map);
            } catch (IOException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
