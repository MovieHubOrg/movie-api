package com.movie.api.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class JSONUtils {

    //public static
    public static <T> T getDataObject(String json, String field, Class<T> returnType) throws Exception {
        ObjectMapper mapper = getMapper();
        JsonNode node = mapper.readValue(json, JsonNode.class);
        String[] fields = field.split("\\.");

        JsonNode lastNode = node;
        for (int i = 0; i < fields.length; i++) {
            if (checkField(lastNode, fields[i])) {
                if (i == fields.length - 1) {
                    return mapper.convertValue(lastNode.get(fields[i]), returnType);
                } else {
                    lastNode = lastNode.get(fields[i]);
                }
            }
        }
        return null;
    }

    private static boolean checkField(JsonNode node, String field) {
        return node.has(field);
    }

    public static ObjectMapper getMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        objectMapper.setDateFormat(df);

        return objectMapper;
    }
}
