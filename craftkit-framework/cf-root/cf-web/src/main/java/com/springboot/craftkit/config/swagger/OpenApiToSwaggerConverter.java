package com.springboot.craftkit.config.swagger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

/**
 * OpenAPI 3.0 to Swagger 2.0 Converter.
 * <p>
 * This class transforms an OpenAPI 3.0 JSON document into a Swagger 2.0 JSON document
 * by applying the following transformation rules:
 * </p>
 *
 * <pre>
 * | OpenAPI 3.0 요소                | Swagger 2.0 대응 요소                 | 설명                                                                      |
 * |---------------------------------|--------------------------------------|--------------------------------------------------------------------------|
 * | openapi                         | swagger                              | 값 `3.0.x` → `2.0`으로 변경.                                               |
 * | info                            | info                                 | 변환 없이 그대로 유지.                                                     |
 * | servers[0].url                  | host, basePath, schemes              | `servers`의 첫 번째 URL 을 분석하여 `host`, `basePath`, `schemes`로 분리.     |
 * | tags                            | tags                                 | 변환 없이 그대로 유지.                                                     |
 * | paths                           | paths                                | 대부분 동일, 하위 요소에서 추가 변환 적용.                                 |
 * | parameters[].schema             | parameters[].schema                  | `parameters[].schema`는 유지하되, 내부의 `$ref` 경로를 변경.                |
 * | paths.[method].requestBody      | parameters                           | `requestBody`를 제거하고, `in: body` 유형의 `parameters`로 변환.             |
 * | paths.[method].requestBody.content | parameters[].schema                | `content`의 `schema`를 `parameters[].schema`로 옮기고 `$ref` 경로를 변경.    |
 * | paths.[method].responses        | responses                            | 유지하되, `responses` 하위의 `content.schema`를 직접 포함.                  |
 * | paths.[method].responses.content | responses.[statusCode].schema       | `responses.content` 하위의 MIME 타입 제거, `schema`를 직접 이동.            |
 * | components.schemas              | definitions                          | 이름 변경. `components.schemas` → `definitions`.                          |
 * | components.schemas[].$ref       | definitions[].$ref                   | `#/components/schemas` → `#/definitions`로 경로를 변경.                     |
 * | components.schemas[].items      | definitions[].items                  | 배열 타입의 `items` 안에 있는 `$ref`도 변환.                                |
 * | components.securitySchemes      | securityDefinitions                  | 이름 변경. `components.securitySchemes` → `securityDefinitions`.           |
 * | components                      | -                                    | `callbacks`는 Swagger 2.0에서 지원하지 않으므로 제거.                       |
 * | paths.[method].consumes         | paths.[method].consumes              | `requestBody.content`에서 MIME 타입을 추출하여 `consumes` 배열에 추가.       |
 * | paths.[method].produces         | paths.[method].produces              | `responses.content`에서 MIME 타입을 추출하여 `produces` 배열에 추가.         |
 * </pre>
 *
 * <p>
 * This converter ensures that Swagger 2.0-compatible JSON is generated while maintaining as much
 * fidelity to the original OpenAPI 3.0 document as possible.
 * </p>
 */
public class OpenApiToSwaggerConverter {

    private final ObjectMapper mapper = new ObjectMapper();

    public byte[] convert(byte[] inputBytes) throws IOException {
        // Parse the input JSON
        JsonNode openApiJson = mapper.readTree(inputBytes);

        // Create a new JSON object for Swagger 2.0
        ObjectNode swaggerJson = mapper.createObjectNode();

        // Copy and transform major sections
        swaggerJson.put("swagger", "2.0");
        swaggerJson.set("info", openApiJson.path("info"));
        swaggerJson.set("tags", openApiJson.path("tags"));
        transformServersToSwagger(openApiJson, swaggerJson);
        transformComponentsToSwagger(openApiJson, swaggerJson);

        // Transform paths
        ObjectNode paths = mapper.createObjectNode();
        JsonNode openApiPaths = openApiJson.path("paths");
        openApiPaths.fields().forEachRemaining(entry -> {
            String path = entry.getKey();
            ObjectNode pathMethods = (ObjectNode) entry.getValue();
            ObjectNode transformedMethods = mapper.createObjectNode();

            pathMethods.fields().forEachRemaining(methodEntry -> {
                String method = methodEntry.getKey();
                ObjectNode methodContent = methodEntry.getValue().deepCopy();

                // Transform parameters
                transformParametersSchemaRefs(methodContent);

                // Transform requestBody
                if (methodContent.has("requestBody")) {
                    transformRequestBodyToParameters(methodContent);
                }

                // Transform responses
                transformResponses(methodContent);

                transformedMethods.set(method, methodContent);
            });

            paths.set(path, transformedMethods);
        });
        swaggerJson.set("paths", paths);

        // Convert final Swagger 2.0 JSON to byte[]
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(swaggerJson);
    }

    private void transformServersToSwagger(JsonNode openApiJson, ObjectNode swaggerJson) {
        if (openApiJson.has("servers") && openApiJson.get("servers").isArray()) {
            String serverUrl = openApiJson.get("servers").get(0).get("url").asText();
            String[] urlParts = serverUrl.split("://");
            if (urlParts.length == 2) {
                String scheme = urlParts[0];
                String hostAndPath = urlParts[1];
                String[] hostParts = hostAndPath.split("/", 2);
                String host = hostParts[0];
                String basePath = "/" + (hostParts.length > 1 ? hostParts[1] : "");

                swaggerJson.put("host", host);
                swaggerJson.put("basePath", basePath);
                swaggerJson.putArray("schemes").add(scheme);
            }
        }
    }

    private void transformComponentsToSwagger(JsonNode openApiJson, ObjectNode swaggerJson) {
        if (openApiJson.has("components")) {
            ObjectNode components = (ObjectNode) openApiJson.get("components");

            // components.schemas -> definitions
            if (components.has("schemas")) {
                ObjectNode schemas = (ObjectNode) components.get("schemas");
                replaceSchemaRefPaths(schemas);
                swaggerJson.set("definitions", schemas);
            }

            // components.securitySchemes -> securityDefinitions
            if (components.has("securitySchemes")) {
                swaggerJson.set("securityDefinitions", components.get("securitySchemes"));
            }

            // Remove callbacks (not supported in Swagger 2.0)
            components.remove("callbacks");
        }
    }

    private void transformParametersSchemaRefs(ObjectNode methodContent) {
        if (methodContent.has("parameters")) {
            methodContent.withArray("parameters").forEach(parameter -> {
                if (parameter.has("schema")) {
                    JsonNode schema = parameter.get("schema");
                    replaceRefPath(schema);
                }
            });
        }
    }

    private void transformRequestBodyToParameters(ObjectNode methodContent) {
        JsonNode requestBody = methodContent.get("requestBody");
        JsonNode contentNode = requestBody.path("content");

        if (contentNode.isObject()) {
            ObjectNode parameter = mapper.createObjectNode();
            parameter.put("in", "body");
            parameter.put("name", "body");

            // Extract schema and replace $ref paths
            contentNode.fields().forEachRemaining(contentEntry -> {
                String mimeType = contentEntry.getKey();
                JsonNode schema = contentEntry.getValue().path("schema");
                replaceRefPath(schema);

                parameter.set("schema", schema);
                methodContent.withArray("consumes").add(mimeType);
            });

            methodContent.remove("requestBody");
            methodContent.withArray("parameters").add(parameter);
        }
    }

    private void transformResponses(ObjectNode methodContent) {
        if (methodContent.has("responses")) {
            JsonNode responses = methodContent.get("responses");

            responses.fields().forEachRemaining(responseEntry -> {
                JsonNode response = responseEntry.getValue();
                if (response.has("content")) {
                    JsonNode content = response.get("content");

                    content.fields().forEachRemaining(contentEntry -> {
                        String mimeType = contentEntry.getKey();
                        JsonNode schema = contentEntry.getValue().path("schema");
                        replaceRefPath(schema);

                        methodContent.withArray("produces").add(mimeType);
                    });

                    ((ObjectNode) response).remove("content");
                }
            });
        }
    }

    private void replaceSchemaRefPaths(ObjectNode schemas) {
        schemas.fields().forEachRemaining(entry -> {
            JsonNode schema = entry.getValue();
            replaceRefPath(schema);
        });
    }

    private void replaceRefPath(JsonNode schema) {
        if (schema.isObject()) {
            ObjectNode schemaObject = (ObjectNode) schema;

            // Replace $ref
            if (schemaObject.has("$ref")) {
                String refPath = schemaObject.get("$ref").asText();
                if (refPath.startsWith("#/components/schemas")) {
                    schemaObject.put("$ref", refPath.replace("#/components/schemas", "#/definitions"));
                }
            }

            // Process items recursively
            if (schemaObject.has("items")) {
                replaceRefPath(schemaObject.get("items"));
            }

            // Process nested schema properties
            schemaObject.fields().forEachRemaining(field -> {
                JsonNode child = field.getValue();
                if (child.isObject() || child.isArray()) {
                    replaceRefPath(child);
                }
            });
        }
    }

}
