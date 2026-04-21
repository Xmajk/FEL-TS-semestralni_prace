package cz.cvut.fel.ts.ts_semestralni_prace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileStorageService {

    private final ObjectMapper objectMapper;
    private final String dataDir;

    public FileStorageService(
        ObjectMapper objectMapper,
        @Value("${app.data.dir:./data}") String dataDir
    ) {
        this.objectMapper = objectMapper;
        this.dataDir = dataDir.endsWith("/") ? dataDir : dataDir + "/";
        new File(this.dataDir).mkdirs();
    }

    public <T> List<T> readList(String filename, Class<T> type) {
        File file = new File(dataDir + filename);
        if (!file.exists()) return new ArrayList<>();
        try {
            return objectMapper.readValue(
                file,
                objectMapper
                    .getTypeFactory()
                    .constructCollectionType(List.class, type)
            );
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public <T> void writeList(String filename, List<T> data) {
        try {
            objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValue(new File(dataDir + filename), data);
        } catch (IOException e) {
            throw new RuntimeException("Nelze zapsat soubor: " + filename, e);
        }
    }
}
