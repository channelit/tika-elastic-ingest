package intelligence.discoverer.elastic;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Service
public class EntityTransformer {


    public Map<String, Object> getFieldValues(String json) {
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(json).getAsJsonObject();
        Map<String, Object> fieldVal = new HashMap<>();
        o.keySet().forEach(key->{
            fieldVal.put(key, o.get(key));
        });
        return fieldVal;
    }
}
