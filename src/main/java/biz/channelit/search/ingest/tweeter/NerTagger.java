package biz.channelit.search.ingest.tweeter;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class NerTagger {

    private static String del = "\\|";

    @Autowired
    StanfordCoreNLP pipeline;

    public void getNers(String fileName) throws IOException {
        List<String> list = new ArrayList<>();
        String outFile = fileName.replace(".txt", "_ner.txt");
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(outFile), StandardOpenOption.CREATE);
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(
                    line-> {
                        if (line.contains("tweet_text")) {
                            nerLines(line).forEach(ner -> {
                                try {
                                    writer.write(ner + "\n");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> nerLines(String line) {
        String[] values = line.split(del);
        String id = values[0];
        String in = values[2];
        List<String> results = new ArrayList<>();
        Annotation annotation;
        annotation = new Annotation(in);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        sentences.forEach(sentence-> {
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                if (ne.equalsIgnoreCase("location") || ne.equalsIgnoreCase("person")) {
                    results.add(id + "|ner_" + ne + "_id|" + cleanId(token.value()));
                }
            }
            results.add(id + "|ner_sentiment_id|" + cleanId(sentence.get(SentimentCoreAnnotations.SentimentClass.class)));
        });
        return results;
    }

    private String cleanId(String s) {
        return s.replaceAll(" ", "_").toLowerCase();
    }
}
