package intelligence.discoverer.elastic;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Collectors;
import static org.elasticsearch.index.query.QueryBuilders.*;


@Service
public class FileInfoLogger {

    @Value("${file.field}")
    String fileNameField;

    @Value("${file.index}")
    String fileIndex;

    private Path filePath;

    @Autowired
    @Qualifier("esclient")
    Client client;

    @Async
    public void printToFile(String... str) throws IOException {
        Files.write(filePath, Arrays.stream(str).collect(Collectors.joining("|")).concat(System.lineSeparator()).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public void setFilePath(Path filePath) throws IOException {
        if (!Files.exists(filePath))
            Files.createFile(filePath);
        this.filePath = filePath;
    }


    public boolean fileIndexed(String fileName) {
        SearchResponse response = client.prepareSearch()
                .setTypes()
                .setFetchSource(false)
                .setIndices(fileIndex)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(termQuery(
                        fileNameField,
                        fileName))
                .execute()
                .actionGet();
        return (response.getHits().totalHits > 0);
    }


}
