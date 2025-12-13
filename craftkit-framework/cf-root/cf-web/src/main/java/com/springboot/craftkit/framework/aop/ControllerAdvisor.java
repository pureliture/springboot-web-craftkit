package com.springboot.craftkit.framework.aop;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.springboot.craftkit.framework.json.JsonFlattener;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.springframework.core.Ordered;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Supports processing common requirements (e.g., determining MNO or MVNO DB) 
 * by reading request information before executing application Controllers.
 */
public interface ControllerAdvisor extends Ordered {

    /**
     * Executed before method invocation.
     * 
     * @param request HttpServletRequest wrapped with HttpServletRequestWrapper allowing repeated body reads
     * @param joinPoint AOP pointcut argument at the Before timing when Controller method is invoked, 
     *                  providing access to arguments, method signature, return type, etc.
     * @param mappingUrl URL mapped to the Controller method extracted from client call, containing pathVariable names
     * @param headerMap HTTP headers extracted and provided as a Map. Note: values are List&lt;String&gt; type
     * @param pathVariableMap Path variable name-value Map created by mapping mappingUrl with actual httpRequest URI
     * @param queryParameterMap Query or form parameters extracted and provided as Map. Note: values are List&lt;String&gt; type
     */
    default void preController(HttpServletRequest request, JoinPoint joinPoint, String mappingUrl, 
            Map<String, List<String>> headerMap, Map<String, String> pathVariableMap,
            Map<String, List<String>> queryParameterMap) {
    }

    /**
     * Executed after method return.
     * 
     * @param request HttpServletRequest wrapped with HttpServletRequestWrapper allowing repeated body reads
     * @param joinPoint AOP pointcut argument at the Before timing when Controller method is invoked
     * @param mappingUrl URL mapped to the Controller method extracted from client call
     * @param headerMap HTTP headers extracted and provided as a Map
     * @param pathVariableMap Path variable name-value Map
     * @param queryParameterMap Query or form parameters extracted and provided as Map
     * @param returnObject Return value
     */
    default void postReturn(HttpServletRequest request, JoinPoint joinPoint, String mappingUrl, 
            Map<String, List<String>> headerMap, Map<String, String> pathVariableMap, 
            Map<String, List<String>> queryParameterMap, Object returnObject) {
    }

    /**
     * Executed after method Exception throwing.
     * 
     * @param request HttpServletRequest wrapped with HttpServletRequestWrapper allowing repeated body reads
     * @param joinPoint AOP pointcut argument at the Before timing when Controller method is invoked
     * @param mappingUrl URL mapped to the Controller method extracted from client call
     * @param headerMap HTTP headers extracted and provided as a Map
     * @param pathVariableMap Path variable name-value Map
     * @param queryParameterMap Query or form parameters extracted and provided as Map
     * @param exception Exception thrown
     */
    default void postThrowing(HttpServletRequest request, JoinPoint joinPoint, String mappingUrl, 
            Map<String, List<String>> headerMap, Map<String, String> pathVariableMap, 
            Map<String, List<String>> queryParameterMap, Exception exception) {
    }

    /**
     * If multiple ControllerAdvisor beans are created, they are executed in order according to Order value.
     * Beans with higher Order values are executed first.
     * If not specified, priority is not guaranteed.
     */
    @Override
    default int getOrder() {
        return 0;
    }

    /**
     * Reads JSON body and returns as a flat map.
     * <p>
     * flat map: A Map representing JSON in 1-depth flat structure instead of hierarchical.
     * See JsonFlattener documentation for examples.
     * </p>
     * 
     * @see JsonFlattener
     * @see JsonFlattener#toFlatMap(InputStream)
     * @param request HttpServletRequest wrapped with HttpServletRequestWrapper allowing repeated body reads
     * @return Map&lt;String, String&gt; JSON converted to flat map
     */
    default Map<String, String> getFlatMap(HttpServletRequest request) throws IOException {
        return JsonFlattener.toFlatMap(request.getInputStream());
    }

    /**
     * Returns body as Jayway JsonPath's DocumentContext. Values can be searched and extracted using JsonPath expressions.
     * 
     * <p>
     *  Refer to <a href="https://github.com/json-path/JsonPath">Jayway JsonPath page</a>
     * </p>
     * 
     * <pre>
     * Example:
     * 
     * ReadContext readContext = getJsonPathDocument(request);
     * List&lt;String&gt; authorsOfBooksWithISBN = readContext.read("$.store.book[?(@.isbn)].author");
     * </pre>
     * 
     * @param request HttpServletRequest wrapped with HttpServletRequestWrapper allowing repeated body reads
     * @return ReadContext com.jayway.jsonpath.ReadContext
     */
    default ReadContext getJsonPathDocument(HttpServletRequest request) throws IOException {

        try (InputStream inputStream = request.getInputStream()) {
            return JsonPath.parse(inputStream);
        }
    }

}
