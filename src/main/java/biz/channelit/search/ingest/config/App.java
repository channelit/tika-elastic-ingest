package biz.channelit.search.ingest.config;

import biz.channelit.search.ingest.elastic.Indexer;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Controller
@EnableAutoConfiguration
@ComponentScan("biz.channelit.search.ingest")
public class App {

    @Value("${elastic.cluster.name}")
    String elasticCluster;

    @Value("${elastic.host.name}")
    String elasticHost;

    @RequestMapping("/status")
    @ResponseBody
    String home() throws IOException {
        return "Indexer!";
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(App.class, args);
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
}