/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springboot.craftkit.framework.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * URL 을 수정한 다음 변경사항을 적용하기 위해 리다이렉트하거나
 * 요청을 래핑하는 {@link jakarta.servlet.Filter}입니다.
 *
 * <p>다음과 같이 인스턴스를 생성할 수 있습니다:
 *
 * <pre>
 * UrlHandlerFilter filter = UrlHandlerFilter
 *    .trailingSlashHandler("/path1/**").redirect(HttpStatus.PERMANENT_REDIRECT)
 *    .trailingSlashHandler("/path2/**").wrapRequest()
 *    .build();
 * </pre>
 *
 * <p>이 {@code Filter}는 {@link org.springframework.web.filter.ForwardedHeaderFilter} 이후에
 * 그리고 모든 보안 필터들 이전에 순서가 지정되어야 합니다.
 *
 * <p>참고: 이 파일은 spring-framework 6.2 부터 적용되어지는 기능을 복사하여 사용하였습니다.
 *
 * @since 6.2
 */
public final class UrlHandlerFilter extends OncePerRequestFilter {

    private static final Log logger = LogFactory.getLog(UrlHandlerFilter.class);


    private final MultiValueMap<Handler, PathPattern> handlers;


    private UrlHandlerFilter(MultiValueMap<Handler, PathPattern> handlers) {
        this.handlers = new LinkedMultiValueMap<>(handlers);
    }


    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull FilterChain chain)
            throws ServletException, IOException {

        RequestPath previousPath = (RequestPath) request.getAttribute(ServletRequestPathUtils.PATH_ATTRIBUTE);
        RequestPath path = previousPath;
        try {
            if (path == null) {
                path = ServletRequestPathUtils.parseAndCache(request);
            }
            for (Map.Entry<Handler, List<PathPattern>> entry : this.handlers.entrySet()) {
                if (!entry.getKey().supports(request, path)) {
                    continue;
                }
                for (PathPattern pattern : entry.getValue()) {
                    if (pattern.matches(path)) {
                        entry.getKey().handle(request, response, chain);
                        return;
                    }
                }
            }
        }
        finally {
            if (previousPath != null) {
                ServletRequestPathUtils.setParsedRequestPath(previousPath, request);
            }
        }

        chain.doFilter(request, response);
    }


    /**
     * URL 의 후행 슬래시를 처리하는 핸들러를 추가하여 빌더를 생성합니다.
     * @param pathPatterns 핸들러에 매핑할 경로 패턴들입니다. 예를 들어,
     * <code>"/path/&#42;"</code>, <code>"/path/&#42;&#42;"</code>,
     * <code>"/path/foo/"</code> 등이 있습니다.
     * @return 후행 슬래시 핸들러를 구성할 수 있는 스펙을 반환합니다
     * @see Builder#trailingSlashHandler(String...)
     */
    public static Builder.TrailingSlashSpec trailingSlashHandler(String... pathPatterns) {
        return new DefaultBuilder().trailingSlashHandler(pathPatterns);
    }


    /**
     * Builder for {@link UrlHandlerFilter}.
     */
    public interface Builder {

        /**
         * URL 의 후행 슬래시를 처리하는 핸들러를 추가합니다.
         * @param pathPatterns 핸들러에 매핑할 경로 패턴들입니다. 예를 들어,
         * <code>"/path/&#42;"</code>, <code>"/path/&#42;&#42;"</code>,
         * <code>"/path/foo/"</code> 등이 있습니다.
         * @return 핸들러를 구성할 수 있는 스펙을 반환합니다
         */
        TrailingSlashSpec trailingSlashHandler(String... pathPatterns);

        /**
         * Build the {@link UrlHandlerFilter} instance.
         */
        UrlHandlerFilter build();


        /**
         * 후행 슬래시 핸들러를 구성하기 위한 스펙입니다.
         */
        interface TrailingSlashSpec {

            /**
             * URL 에 후행 슬래시가 있을 때 핸들러가 호출되기 직전에
             * 호출될 요청 컨슈머를 구성합니다.
             */
            TrailingSlashSpec intercept(Consumer<HttpServletRequest> consumer);

            /**
             * 동일한 URL 에서 후행 슬래시를 제거한 URL 로 리다이렉트하여
             * 요청을 처리합니다.
             * @param status 사용할 리다이렉트 상태 코드
             * @return 더 많은 핸들러를 추가하고 필터 인스턴스를 생성할 수 있는
             * 최상위 {@link Builder}를 반환합니다.
             */
            Builder redirect(HttpStatus status);

            /**
             * 후행 슬래시를 제거하기 위해 요청을 래핑하고
             * 나머지 필터 체인으로 위임하여 요청을 처리합니다.
             * @return 더 많은 핸들러를 추가하고 필터 인스턴스를 생성할 수 있는
             * 최상위 {@link Builder}를 반환합니다.
             */
            Builder wrapRequest();
        }
    }


    /**
     * 기본 {@link Builder} 구현체입니다.
     */
    private static final class DefaultBuilder implements Builder {

        private final PathPatternParser patternParser = new PathPatternParser();

        private final MultiValueMap<Handler, PathPattern> handlers = new LinkedMultiValueMap<>();

        @Override
        public TrailingSlashSpec trailingSlashHandler(String... patterns) {
            return new DefaultTrailingSlashSpec(patterns);
        }

        private DefaultBuilder addHandler(List<PathPattern> pathPatterns, Handler handler) {
            pathPatterns.forEach(pattern -> this.handlers.add(handler, pattern));
            return this;
        }

        @Override
        public UrlHandlerFilter build() {
            return new UrlHandlerFilter(this.handlers);
        }

        private final class DefaultTrailingSlashSpec implements TrailingSlashSpec {

            private final List<PathPattern> pathPatterns;

            @SuppressWarnings("NullableProblems")
            @Nullable
            private Consumer<HttpServletRequest> interceptor;

            private DefaultTrailingSlashSpec(String[] patterns) {
                this.pathPatterns = Arrays.stream(patterns)
                        .map(pattern -> pattern.endsWith("**") || pattern.endsWith("/") ? pattern : pattern + "/")
                        .map(patternParser::parse)
                        .toList();
            }

            @Override
            public TrailingSlashSpec intercept(Consumer<HttpServletRequest> consumer) {
                this.interceptor = (this.interceptor != null ? this.interceptor.andThen(consumer) : consumer);
                return this;
            }

            @Override
            public Builder redirect(HttpStatus status) {
                Handler handler = new RedirectTrailingSlashHandler(status, this.interceptor);
                return DefaultBuilder.this.addHandler(this.pathPatterns, handler);
            }

            @Override
            public Builder wrapRequest() {
                Handler handler = new RequestWrappingTrailingSlashHandler(this.interceptor);
                return DefaultBuilder.this.addHandler(this.pathPatterns, handler);
            }
        }
    }


    /**
     * 요청을 처리하는 다양한 방법을 캡슐화하는 내부 핸들러입니다.
     */
    private interface Handler {

        /**
         * Whether the handler handles the given request.
         */
        @SuppressWarnings("unused")
        boolean supports(HttpServletRequest request, RequestPath path);

        /**
         * Handle the request, possibly delegating to the rest of the filter chain.
         */
        @SuppressWarnings("unused")
        void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException;
    }


    /**
     * 후행 슬래시 {@link Handler} 구현체들의 기본 클래스입니다.
     */
    private abstract static class AbstractTrailingSlashHandler implements Handler {

        private static final Consumer<HttpServletRequest> defaultInterceptor = request -> {
            if (logger.isTraceEnabled()) {
                logger.trace("Handling trailing slash URL: " +
                        request.getMethod() + " " + request.getRequestURI());
            }
        };

        private final Consumer<HttpServletRequest> interceptor;

        protected AbstractTrailingSlashHandler(@Nullable Consumer<HttpServletRequest> interceptor) {
            this.interceptor = (interceptor != null ? interceptor : defaultInterceptor);
        }

        @Override
        public boolean supports(HttpServletRequest request, RequestPath path) {
            List<PathContainer.Element> elements = path.pathWithinApplication().elements();
            return (elements.size() > 1 && elements.get(elements.size() - 1).value().equals("/"));
        }

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {

            this.interceptor.accept(request);
            handleInternal(request, response, chain);
        }

        protected abstract void handleInternal(
                HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException;

        protected String trimTrailingSlash(String path) {
            int index = (StringUtils.hasLength(path) ? path.lastIndexOf('/') : -1);
            return (index != -1 ? path.substring(0, index) : path);
        }
    }


    /**
     * 리다이렉트를 보내는 경로 핸들러입니다.
     */
    private static final class RedirectTrailingSlashHandler extends AbstractTrailingSlashHandler {

        private final HttpStatus httpStatus;

        RedirectTrailingSlashHandler(HttpStatus httpStatus, @Nullable Consumer<HttpServletRequest> interceptor) {
            super(interceptor);
            this.httpStatus = httpStatus;
        }

        @SuppressWarnings("unused")
        @Override
        public void handleInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws IOException {

            response.resetBuffer();
            response.setStatus(this.httpStatus.value());
            response.setHeader(HttpHeaders.LOCATION, trimTrailingSlash(request.getRequestURI()));
            response.flushBuffer();
        }
    }


    /**
     * 요청을 래핑하고 처리를 계속하는 경로 핸들러입니다.
     */
    private static final class RequestWrappingTrailingSlashHandler extends AbstractTrailingSlashHandler {

        RequestWrappingTrailingSlashHandler(@Nullable Consumer<HttpServletRequest> interceptor) {
            super(interceptor);
        }

        @Override
        public void handleInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {

            String servletPath = request.getServletPath();
            String pathInfo = request.getPathInfo();
            boolean hasPathInfo = StringUtils.hasText(pathInfo);

            request = new TrailingSlashHttpServletRequest(
                    request,
                    trimTrailingSlash(request.getRequestURI()),
                    trimTrailingSlash(request.getRequestURL().toString()),
                    hasPathInfo ? servletPath : trimTrailingSlash(servletPath),
                    hasPathInfo ? trimTrailingSlash(pathInfo) : pathInfo);

            chain.doFilter(request, response);
        }
    }


    /**
     * 수정된 경로 정보를 반환하도록 요청을 래핑합니다.
     */
    private static class TrailingSlashHttpServletRequest extends HttpServletRequestWrapper {

        private final String requestURI;

        private final StringBuffer requestURL;

        private final String servletPath;

        private final String pathInfo;

        TrailingSlashHttpServletRequest(HttpServletRequest request,
                String requestURI, String requestURL, String servletPath, String pathInfo) {

            super(request);
            this.requestURI = requestURI;
            this.requestURL = new StringBuffer(requestURL);
            this.servletPath = servletPath;
            this.pathInfo = pathInfo;
        }

        @Override
        public String getRequestURI() {
            return this.requestURI;
        }

        @Override
        public StringBuffer getRequestURL() {
            return this.requestURL;
        }

        @Override
        public String getServletPath() {
            return this.servletPath;
        }

        @Override
        public String getPathInfo() {
            return this.pathInfo;
        }
    }

}
