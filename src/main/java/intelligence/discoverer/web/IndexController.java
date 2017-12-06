package intelligence.discoverer.web;

import intelligence.discoverer.crawler.FileCrawler;
import intelligence.discoverer.elastic.Indexer;
import intelligence.discoverer.elastic.Setup;
import intelligence.discoverer.location.Geo;
import intelligence.discoverer.tweeter.NerTagger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

/**
 * Created by hp on 2/14/17.
 */

@Controller
public class IndexController {

    @Autowired
    Indexer indexer;

    @Autowired
    Setup setup;

    @Autowired
    Geo geo;

    @Autowired
    NerTagger nerTagger;

    @Autowired
    FileCrawler crawler;

    @RequestMapping("/index")
    String index() throws IOException {
        indexer.indexFiles();
        return "ok";
    }

    @RequestMapping("/geo")
    String geo() throws IOException {
        geo.indexGeoFiles();
        return "ok";
    }

    @RequestMapping(path = "/setup", method = RequestMethod.GET, produces = "text/json")
    String setup(@RequestParam("index") String index) throws IOException {
        return setup.putSettings(index);
    }


    @RequestMapping(path = "/ner", method = RequestMethod.GET, produces = "text/json")
    String ner(@RequestParam("index") String index, @RequestParam("type") String type, @RequestParam("q") String query) throws IOException {
        indexer.populateNer(index, type, query);
        return "done";
    }

    @RequestMapping(path = "/tweeter", method = RequestMethod.GET, produces = "text/json")
    String tweeter(@RequestParam("file") String file) throws IOException {
        nerTagger.getNers("/Users/hp/workbench/projects/gmu/tweets/" + file);
        return "done";
    }

    @RequestMapping(path = "/crawler", method = RequestMethod.GET, produces = "text/json")
    String crawler() throws IOException {
        crawler.startCrawler();
        return "done";
    }

}
