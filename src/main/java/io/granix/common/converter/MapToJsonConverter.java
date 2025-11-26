package io.granix.common.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.Map;

@Converter(autoApply = false)
@ApplicationScoped
public class MapToJsonConverter implements AttributeConverter<Map<String, Object>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> stringObjectMap) {
        if(stringObjectMap == null)
            return null;

        try {
            return objectMapper.writeValueAsString(stringObjectMap);
        } catch (JsonProcessingException jsonProcessingException) {
            throw new IllegalArgumentException("Error Converting Map to JSON");
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String s) {
        if(s == null || s.isEmpty())
            return null;
        try{
            return objectMapper.readValue(s, Map.class);
        } catch (IOException e){
            throw new IllegalArgumentException("Error converting JSON to Map");
        }
    }
}
