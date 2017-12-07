package intelligence.discoverer.elastic;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Service
public class EntityTransformer {

    private final String fieldMapFilePath = "src/main/resources/settings/fieldmap.yml";

    private Properties fieldMap;

    public EntityTransformer() throws IOException {
        fieldMap.load(new FileInputStream(fieldMapFilePath));
    }

    public Map<String,Object> getFieldValues(String json) {
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(json).getAsJsonObject();
        Map<String, List<?>> fieldVal = new HashMap<>();
        fieldMap.keySet().forEach(k-> fieldVal.put(k.toString(), new ArrayList<>()));
        String type = o.get("type").getAsString();
        switch (type){

        }
        return new HashMap<>();
    }
}
