package intelligence.discoverer.tika;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.*;

/**
 * Created by hp on 2/20/17.
 */
public class Extractor {
    public static String extractFileContent(File file) {
        try {
            return extractFileContent(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String extractFileContent(InputStream stream) {
        Parser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);

        TesseractOCRConfig config = new TesseractOCRConfig();
        PDFParserConfig pdfConfig = new PDFParserConfig();
//        pdfConfig.setExtractInlineImages(true);

        ParseContext parseContext = new ParseContext();
        parseContext.set(TesseractOCRConfig.class, config);
        parseContext.set(PDFParserConfig.class, pdfConfig);
        //need to add this to make sure recursive parsing happens!
        parseContext.set(Parser.class, parser);

        Metadata metadata = new Metadata();
        try {
            parser.parse(stream, handler, metadata, parseContext);
            return handler.toString();
        } catch (IOException | TikaException | SAXException e) {
            e.printStackTrace();
        }
        return null;
    }
}
