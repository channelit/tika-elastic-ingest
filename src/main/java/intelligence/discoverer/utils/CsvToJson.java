package intelligence.discoverer.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;


import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CsvToJson {

    public static <object> void main(String[] args) throws Exception {
        File input = new File("/Users/hp/workbench/data/geotegra/p_nyc_locations.csv");
        File output = new File("/Users/hp/workbench/data/geotegra/p_nyc_locations.json");

        CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
        CsvMapper csvMapper = new CsvMapper();

        // Read data from CSV file
        List<object> readAll = (List<object>) csvMapper.readerFor(Map.class).with(csvSchema).readValues(input).readAll();

        readAll.forEach(o -> {
            ((LinkedHashMap) o).put("id", UUID.randomUUID());
        });
        ObjectMapper mapper = new ObjectMapper();

        // Write JSON formated data to output.json file
        mapper.writerWithDefaultPrettyPrinter().writeValue(output, readAll);

        // Write JSON formated data to stdout
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readAll));
    }


}
