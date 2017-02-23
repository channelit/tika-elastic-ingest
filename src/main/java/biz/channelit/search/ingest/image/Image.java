package biz.channelit.search.ingest.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Created by hp on 2/22/17.
 */
@Service
public class Image {

    @Autowired
    com.asprise.ocr.Ocr ocr;

    public String getOcr(File file) {
        return ocr.recognize(new File[] {file}, com.asprise.ocr.Ocr.RECOGNIZE_TYPE_ALL, com.asprise.ocr.Ocr.OUTPUT_FORMAT_PLAINTEXT);

    }
}
