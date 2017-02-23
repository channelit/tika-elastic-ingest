package biz.channelit.search.ingest.location;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Created by hp on 2/23/17.
 */
@Service
public class Geo {

    @Value("${geo.path}")
    String geoPath;

    @Autowired
    TransportClient client;

    public void indexGeoFiles() throws IOException {
        int ctr = 0;

        BufferedReader br = new BufferedReader(new FileReader(geoPath));
        String content = null;
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        while ((content = br.readLine()) != null) {
            String[] location = content.split("\t");
            bulkRequest.add(client.prepareIndex("geo", "us", location[0])
                    .setSource(jsonBuilder()
                            .startObject()
                            .field("searchfield", location[1] + "" + location[2])
                            .field("location", "[" + location[4] + ", " + location[5] + "]")
                            .field("state", location[10])
                            .field("country", location[8])
                            .endObject()
                    ));
            ctr ++;
            if (ctr > 20) {
                bulkRequest.get();
                bulkRequest = client.prepareBulk();
                ctr = 0;
            }
        }
        bulkRequest.get();
    }

    public String findLocation(String location) {
        QueryBuilder qb = queryStringQuery(location);
        SearchResponse response = client.prepareSearch("geo").setTypes("us").setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(qb).setFetchSource("location", null)
                .setSize(10).execute().actionGet();
        return response.getHits().hits()[0].getFields().get("location").getValues().get(0).toString();
    }

}
