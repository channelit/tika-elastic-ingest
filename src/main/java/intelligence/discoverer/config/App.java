package intelligence.discoverer.config;

import com.asprise.ocr.Ocr;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import intelligence.discoverer.web.AfiResponseErrorHandler;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@EnableAutoConfiguration
@EnableScheduling
@ComponentScan("intelligence.discoverer")
@SpringBootApplication
@EnableAsync
public class App {

    @Value("${elastic.cluster.name}")
    String elasticCluster;

    @Value("${elastic.host}")
    String elasticHost;

    @Value("${elastic.port}")
    Integer elasticPort;

    @Value("${nlp.corenlp.enabled}")
    Boolean corenlpEnabled;

    @Value("${parser.url}")
    String parserUrl;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public StanfordCoreNLP getCoreNlp() {
        StanfordCoreNLP pipeline = null;
        if (corenlpEnabled) {
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment");
            pipeline = new StanfordCoreNLP(props);
        }
        return pipeline;
    }

    @Bean(name = "esclient")
    public TransportClient esClient() throws UnknownHostException {
        Settings settings = Settings.builder()
                .put("cluster.name", elasticCluster).build();
        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName(elasticHost), elasticPort));
        return client;
    }

    @Bean
    public Tokenizer tokenizer() throws IOException {
        File file = new ClassPathResource("opennlp/en-token.bin").getFile();
        InputStream is = new FileInputStream(file);
        TokenizerModel model = new TokenizerModel(is);
        Tokenizer tokenizer = new TokenizerME(model);
        return tokenizer;
    }

    @Bean(name = "personNameFinder")
    public NameFinderME personNameFinder() throws IOException {
        return getNameFinderME("opennlp/en-ner-person.bin");
    }

    @Bean(name = "orgNameFinder")
    public NameFinderME orgNameFinder() throws IOException {
        return getNameFinderME("opennlp/en-ner-organization.bin");
    }

    @Bean(name = "locationFinder")
    public NameFinderME locationFinder() throws IOException {
        return getNameFinderME("opennlp/en-ner-location.bin");
    }

    private NameFinderME getNameFinderME(String modelFile) throws IOException {
        File file = new ClassPathResource(modelFile).getFile();
        InputStream is = new FileInputStream(file);
        TokenNameFinderModel model = new TokenNameFinderModel(is);
        is.close();
        return new NameFinderME(model);
    }

    @Bean
    public SentenceDetectorME sentenceDetectorME() throws IOException {
        File file = new ClassPathResource("opennlp/en-sent.bin").getFile();
        InputStream is = new FileInputStream(file);
        SentenceModel model = new SentenceModel(is);
        SentenceDetectorME sdetector = new SentenceDetectorME(model);
        return sdetector;
    }

    @Bean
    public Ocr ocr() {
        Ocr.setUp(); // one time setup
        Ocr ocr = new Ocr(); // create a new OCR engine
        ocr.startEngine("eng", Ocr.SPEED_FASTEST); // English
        return ocr;
    }

    @Bean("esBulkProcessor")
    @Scope("prototype")
    public BulkProcessor bulkProcessor(@Autowired TransportClient client) {
        BulkProcessor bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long l, BulkRequest bulkRequest) {

            }

            @Override
            public void afterBulk(long l, BulkRequest bulkRequest, BulkResponse bulkResponse) {
                System.out.println(bulkResponse.buildFailureMessage());
            }

            @Override
            public void afterBulk(long l, BulkRequest bulkRequest, Throwable throwable) {

            }
        })
                .setBulkActions(5)
                .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
//                .setFlushInterval(TimeValue.timeValueSeconds(5))
//                .setConcurrentRequests(5)
//                .setBackoffPolicy(
//                        BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3)
//                )
                .build();
        return bulkProcessor;
    }

    @Bean
    public RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.setErrorHandler(new AfiResponseErrorHandler());
        return restTemplate;
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(this.getClass().getSimpleName() + "-%d").build();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
//        executor.setQueueCapacity(10);
        executor.setThreadFactory(threadFactory);
        return executor;
    }

    @Bean
    public URI getUri() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(parserUrl);
        return builder.build().encode().toUri();
    }
}
