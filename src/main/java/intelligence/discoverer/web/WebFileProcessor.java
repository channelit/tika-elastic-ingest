package intelligence.discoverer.web;

import intelligence.discoverer.opennlp.OpenNlpNer;
import intelligence.discoverer.tika.Extractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@RestController
public class WebFileProcessor {

    @Autowired
    OpenNlpNer openNlpNer;


    @RequestMapping("/file/upload")
    public Map<String, List<String>> handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        String content = Extractor.extractFileContent(file.getInputStream());
        return openNlpNer.getAll(content);
    }
}
