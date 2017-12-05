package intelligence.discoverer.elastic;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EntityTransformer {

    public Map<String,Object> getFieldValues(String resp) {
        return new HashMap<>();
    }
}
