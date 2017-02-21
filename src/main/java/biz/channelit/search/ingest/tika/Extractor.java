package biz.channelit.search.ingest.tika;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.*;

/**
 * Created by hp on 2/20/17.
 */
public class Extractor {
    public static String extractFileContet(File file) {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        try (InputStream stream = new FileInputStream(file)) {
            parser.parse(stream, handler, metadata);
            return handler.toString();
        } catch (IOException | TikaException | SAXException e) {
            e.printStackTrace();
        }
        return null;
    }
}
