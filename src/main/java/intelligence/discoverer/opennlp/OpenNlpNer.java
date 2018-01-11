package intelligence.discoverer.opennlp;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by hp on 2/21/17.
 */

@Service
public class OpenNlpNer {

    @Autowired
    Tokenizer tokenizer;

    @Autowired
    NameFinderME orgNameFinder;

    @Autowired
    NameFinderME personNameFinder;

    @Autowired
    NameFinderME locationFinder;

    @Autowired
    SentenceDetectorME sdetector;

    public List<String> findNer(NameFinderME nameFinder, String[] tokens) {
        List<String> names = new ArrayList<>();
        try {
            Span nameSpans[] = nameFinder.find(tokens);
            for (Span s : nameSpans)
                names.add(String.join(" ", Arrays.copyOfRange(tokens, s.getStart(), s.getEnd())));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return names;
    }

    public Map<String, List<String>> getAll(String text) {
        Map<String, List<String>> out = new HashMap<>();
        String[] sentences = sdetector.sentDetect(text);
        try {
            for (String sentence : sentences) {
                if (sentence.trim().length() > 30) {
                    String[] tokens = tokenizer.tokenize(sentence);
                    out.put("persons", findNer(personNameFinder, tokens));
                    out.put("companies", findNer(orgNameFinder, tokens));
                    out.put("locations", findNer(locationFinder, tokens));
                    out.put("ner", new ArrayList<>());
                    out.put("text", Arrays.asList(text));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }
}
