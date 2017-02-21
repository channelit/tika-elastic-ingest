package biz.channelit.search.ingest.wvec;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WVec {

    private static Logger log = LoggerFactory.getLogger(WVec.class);

    public static void main(String[] args) throws Exception {

        SentenceIterator iter = new BasicLineIterator("wvec/articles_01.txt");
        TokenizerFactory t = new DefaultTokenizerFactory();
        log.info("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(3)
                .layerSize(500)
                .seed(6)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();
        vec.fit();
        WordVectorSerializer.writeWord2VecModel(vec, "wvec/articles_model_04.txt");

    }
}