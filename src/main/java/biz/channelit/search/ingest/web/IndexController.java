package biz.channelit.search.ingest.web;

import biz.channelit.search.ingest.elastic.Indexer;
import biz.channelit.search.ingest.elastic.Setup;
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

    @RequestMapping("/index")
    String index() throws IOException {
        indexer.indexFiles();
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

}
