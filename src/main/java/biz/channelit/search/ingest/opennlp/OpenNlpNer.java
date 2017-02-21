package biz.channelit.search.ingest.opennlp;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
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

    @Autowired
    SentenceDetectorME sdetector;

    public List<String> findNames(String text) {
        String[] sentences = sdetector.sentDetect(text);
        List<String> names = new ArrayList<>();
        for (String sentence: sentences) {
            if (sentence.trim().length() > 40) {
                String[] tokens = tokenizer.tokenize(sentence);
                Span nameSpans[] = nameFinder.find(tokens);
                for(Span s: nameSpans)
                    names.add(String.join(" ", Arrays.copyOfRange(tokens,s.getStart(), s.getEnd())));
            }
        }
        return names;
    }
}
