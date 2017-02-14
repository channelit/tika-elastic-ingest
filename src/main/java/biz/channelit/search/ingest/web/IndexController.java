package biz.channelit.search.ingest.web;

import biz.channelit.search.ingest.elastic.Indexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * Created by hp on 2/14/17.
 */

@Controller
public class IndexController {

    @Autowired
    Indexer indexer;

    @RequestMapping("/index")
    String index() throws IOException {
        indexer.indexFiles();
        return "ok";
    }
}
