package intelligence.discoverer.location;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.GeoPoint;
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

    public GeoPoint findLocation(String location) {
        QueryBuilder qb = queryStringQuery(location);
        SearchResponse response = client.prepareSearch("geo").setTypes("us").setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(qb).setFetchSource("location", null)
                .setSize(10).get();
        if (response.getHits().hits().length > 0) {
            String out = response.getHits().getAt(0).getSource().get("location").toString();
            out = out.substring(1, out.length()-1);
            double lat =  new Double(out.split(",")[0]);
            double lon =  new Double(out.split(",")[1]);
            GeoPoint geoLocation =new GeoPoint(lat, lon);
            System.out.println(geoLocation);
            return geoLocation;
        }
        return null;
    }

}
