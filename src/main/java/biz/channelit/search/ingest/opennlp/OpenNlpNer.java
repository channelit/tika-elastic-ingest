package biz.channelit.search.ingest.opennlp;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hp on 2/21/17.
 */

@Service
public class OpenNlpNer {

    @Autowired
    Tokenizer tokenizer;

    @Autowired
    NameFinderME nameFinder;

    public List<String> findNames(String text) {
        Span nameSpans[] = nameFinder.find(tokenizer.tokenize(text));
        List<String> names = new ArrayList<>();
        for(Span s: nameSpans)
            names.add(s.toString());
        return names;
    }
}
