package biz.channelit.search.ingest.elastic;

import biz.channelit.search.ingest.corenlp.CoreNlpNer;
import biz.channelit.search.ingest.opennlp.OpenNlpNer;
import biz.channelit.search.ingest.tika.Extractor;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
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
    List<File> subFiles;

    List<String> fileFilter = new LinkedList<>();

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
        //fileFilter.add("pdf");
        //fileFilter.add("doc");
        //fileFilter.add("docx");
        fileFilter.add("jpg");

        files = new LinkedList<>();
        int ctr = 0;
        walk(crawlerpath);
        while (ctr + 20 < files.size()) {
            subFiles = files.subList(ctr, ctr + 20);
            ctr += 20;
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            subFiles.forEach(file -> {
                String type = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
                String content = "";
                if (fileFilter.contains(type)) {
                    content = extractFileContet(file).replaceAll("[^A-Za-z. ]", " ").replaceAll(" +", " ");
                }
//            Map<String, List<String>> map = openNlpNer.getAll(content);
                try {
                    bulkRequest.add(client.prepareIndex(defaultIndex, defaultType)
                            .setSource(jsonBuilder()
                                            .startObject()
                                            .field("body", content)
                                            .field("insertDate", new Date())
                                            .field("file", file.getName())
                                            .field("url", file.getAbsolutePath())
                                            .field("filetype", type)
//                                .map(map)
                                            .endObject()
                            ));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            bulkRequest.get();
        }
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

    public SearchResponse search(String index, String type, String query,  Integer from) {
        QueryBuilder qb = queryStringQuery(query);
        return client.prepareSearch(index).setTypes(type).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(qb).setFetchSource("body",null).setFrom(from)
                .setSize(10).execute().actionGet();
    }

    public void populateNer(String index, String type, String query) {
        boolean hasMore = true;
        int from = 0;
        while(hasMore) {
            SearchResponse searchResponse = search(index, type, query, from);
            hasMore = (searchResponse.getHits().getHits().length > 0);
            searchResponse.getHits().forEach((hit) -> {
                Map source = hit.getSource();
                String content = (String) source.get("body");
                if (content.trim().length() > 0) {
                    Map<String, List<String>> map = openNlpNer.getAll(content);
                    try {
                        update(index, type, hit.getId(), map);
                    } catch (IOException | ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            from += 10;
        }
    }
}
