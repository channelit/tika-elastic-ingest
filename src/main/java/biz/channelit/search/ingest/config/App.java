package biz.channelit.search.ingest.config;

import com.asprise.ocr.Ocr;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

@Controller
@EnableAutoConfiguration
@ComponentScan("biz.channelit.search.ingest")
public class App {

    @Value("${elastic.cluster.name}")
    String elasticCluster;

    @Value("${elastic.host.name}")
    String elasticHost;

    @Value("${nlp.corenlp.enabled}")
    Boolean corenlpEnabled;

    @RequestMapping("/status")
    @ResponseBody
    String home() throws IOException {
        return "Indexer!";
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public StanfordCoreNLP getCoreNlp() {
        StanfordCoreNLP pipeline = null;
        if (corenlpEnabled) {
            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
            pipeline = new StanfordCoreNLP(props);
        }
        return pipeline;
    }

    @Bean
    public TransportClient esClient() throws UnknownHostException {
        Settings settings = Settings.builder()
                .put("cluster.name", elasticCluster).build();

        if (elasticHost.equalsIgnoreCase("localhost")) {
            return new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getLocalHost(), 9300));
        } else {
            return new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(elasticHost), 9300));
        }
    };

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

}