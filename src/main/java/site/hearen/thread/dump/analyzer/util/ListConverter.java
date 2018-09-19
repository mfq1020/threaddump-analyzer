package site.hearen.thread.dump.analyzer.util;

import java.util.List;

import javax.persistence.AttributeConverter;

import lombok.extern.slf4j.Slf4j;

@javax.persistence.Converter(autoApply = true)
@Slf4j
public class ListConverter implements AttributeConverter<List, String> {

    @Override
    public String convertToDatabaseColumn(List attribute) {
        return ThreadUtils.convertListToString(attribute);
    }

    @Override
    public List convertToEntityAttribute(String dbData) {
        return ThreadUtils.getListFromString(dbData);
    }
}

