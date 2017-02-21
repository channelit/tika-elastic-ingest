package biz.channelit.search.ingest.corenlp;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by hp on 2/20/17.
 */
@Service
public class Ner {

    @Autowired
    public StanfordCoreNLP pipeline;

    public Map<Integer, CorefChain> extract(String text) {

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                String word = token.get(TextAnnotation.class);
                String pos = token.get(PartOfSpeechAnnotation.class);
                String ne = token.get(NamedEntityTagAnnotation.class);
                System.out.println("word: " + word + " pos: " + pos + " ne:" + ne);
            }
            Tree tree = sentence.get(TreeAnnotation.class);
            System.out.println("parse tree:\n" + tree);

            SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
            System.out.println("dependency graph:\n" + dependencies);
        }

        Map<Integer, CorefChain> graph =
                document.get(CorefChainAnnotation.class);

        return graph;
    }
}
