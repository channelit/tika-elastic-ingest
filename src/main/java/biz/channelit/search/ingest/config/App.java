package biz.channelit.search.ingest.config;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    @Bean
    public NameFinderME tokenNameFinderModel() throws IOException {
        File file = new ClassPathResource("opennlp/en-ner-person.bin").getFile();
        InputStream is = new FileInputStream(file);
        TokenNameFinderModel model = new TokenNameFinderModel(is);
        is.close();
        NameFinderME nameFinder = new NameFinderME(model);
        return nameFinder;
    }

}