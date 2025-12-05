package com.springboot.craftkit.framework.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts JSON input to a Flat Map.
 *
 * <p>
 * Contains an internal ObjectMapper initialized with:
 * findAndRegisterModules(), JavaTimeModule, Jdk8Module modules registered,
 * disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) setting,
 * setDateFormat(new StdDateFormat().withColonInTimeZone(true)) setting
 * </p>
 *
 * <p>
 * Conversion example:
 * <pre>
 * input:
 * {
 *    "port":
 *    {
 *        "@alias": "defaultHttp",
 *        "enabled": true,
 *        "number": 10092,
 *        "protocol": "http",
 *        "keepAliveTimeout": 20000,
 *        "null": null,
 *        "array": [],
 *        "array2": [1,2,[3,4,[6,7]]],
 *        "threadPool":
 *        {
 *            "@enabled": false,
 *            "max": 150.99,
 *            "threadPriority": 5
 *        },
 *        "extendedProperties":
 *        {
 *            "property":
 *            [
 *                {
 *                    "@name": "connectionTimeout",
 *                    "$": "20000"
 *                }
 *            ]
 *        }
 *    }
 * }
 *
 * output:
 * { port.@alias=defaultHttp,
 *   port.enabled=true,
 *   port.number=10092,
 *   port.protocol=http,
 *   port.keepAliveTimeout=20000,
 *   port.null=null,
 *   port.array=null,
 *   port.array2[0]=1,
 *   port.array2[1]=2,
 *   port.array2[2][0]=3,
 *   port.array2[2][1]=4,
 *   port.array2[2][2][0]=6,
 *   port.array2[2][2][1]=7,
 *   port.threadPool.@enabled=false,
 *   port.threadPool.max=150.99,
 *   port.threadPool.threadPriority=5,
 *   port.extendedProperties.property[0].@name=connectionTimeout,
 *   port.extendedProperties.property[0].$=20000}
 * </pre>
 * </p>
 */
public class JsonFlattener {

    private static final Logger log = LoggerFactory.getLogger(JsonFlattener.class);

    static final ObjectMapper internalObjectMapper;

    static {
        internalObjectMapper = new ObjectMapper().findAndRegisterModules()
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setDateFormat(new StdDateFormat().withColonInTimeZone(true));
    }

    /**
     * Converts a JSON string to a flat map.
     *
     * @param json json string
     * @return flat map
     * @throws IOException IOException
     */
    public static Map<String, String> toFlatMap(String json) throws IOException {
        return toFlatMap(internalObjectMapper, json);
    }

    /**
     * Converts a JSON InputStream to a flat map.
     *
     * @param inputStream inputStream
     * @return flat map
     * @throws IOException IOException
     */
    public static Map<String, String> toFlatMap(InputStream inputStream) throws IOException {
        return toFlatMap(internalObjectMapper, inputStream);
    }

    /**
     * Converts a JSON string to a flat map using a custom ObjectMapper.
     * Use this when you want to apply custom ObjectMapper deserialize policies.
     *
     * @param objectMapper objectMapper
     * @param json json string
     * @return flat map
     * @throws IOException IOException
     */
    public static Map<String, String> toFlatMap(ObjectMapper objectMapper, String json) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        deserialize("", objectMapper.readTree(json), map);
        return map;
    }

    /**
     * Converts a JSON InputStream to a flat map using a custom ObjectMapper.
     * Use this when you want to apply custom ObjectMapper deserialize policies.
     *
     * @param objectMapper objectMapper
     * @param inputStream inputStream
     * @return flat map
     * @throws IOException IOException
     */
    public static Map<String, String> toFlatMap(ObjectMapper objectMapper, InputStream inputStream) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        deserialize("", objectMapper.readTree(inputStream), map);
        return map;
    }

    private static void deserialize(String currentPath, JsonNode jsonNode, Map<String, String> map) {

        if (jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
            String pathPrefix = currentPath.isEmpty() ? "" : currentPath + ".";

            while (iter.hasNext()) {
                Map.Entry<String, JsonNode> entry = iter.next();
                deserialize(pathPrefix + entry.getKey(), entry.getValue(), map);
            }
        } else if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;

            if (arrayNode.isEmpty()) {
                map.put(currentPath, "null");
            } else {

                for (int i = 0; i < arrayNode.size(); i++) {
                    deserialize(currentPath + "[" + i + "]", arrayNode.get(i), map);
                }
            }
        } else if (jsonNode.isValueNode()) {
            ValueNode valueNode = (ValueNode) jsonNode;
            map.put(currentPath, valueNode.asText());
        } else {
            // Not Object, array, or value
            log.warn("JsonFlattener deserialize unchecked case {} {}", currentPath, jsonNode.toString());
            map.put(currentPath, jsonNode.asText());
        }
    }

}
