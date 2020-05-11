/**
 * Copyright © 2020 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.nihttpsuite;

import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.*;
import org.apache.hc.core5.http.protocol.*;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Server {

    private final int port;

    private final Map<String, Map<String, HttpRouteHandler>> staticRoutes = new HashMap<>();
    private final List<HttpRouteHandler> pathVariableRoutes = new ArrayList<>();

    private Server(int port, RouteBuilder routeBuilder) {
        this.port = port;


        for (HttpRouteHandler routeHandler : routeBuilder.routeHandlers) {
            if (!routeHandler.isContainPathVariable()) {
                if (! staticRoutes.containsKey(routeHandler.path)) {
                    staticRoutes.put(routeHandler.path, new HashMap<>());
                }
                staticRoutes.get(routeHandler.path).put(routeHandler.httpMethod, routeHandler);
            } else {
                pathVariableRoutes.add(routeHandler);
            }
        }
    }


    public static ServerBuilder configure() {
        return new ServerBuilder();
    }

    public static final class ServerBuilder {

        private int port = 8080;

        private final RouteBuilder routeBuilder = new RouteBuilder(this);

        private ServerBuilder() {
        }

        public ServerBuilder listenerPort(int port) {
            this.port = port;
            return this;
        }

        public RouteBuilder route() {
            return routeBuilder;
        }

        public Server build() {
            return new Server(port, routeBuilder);
        }
    }

    public static final class RouteBuilder {
        private final ServerBuilder serverBuilder;

        private final List<HttpRouteHandler> routeHandlers = new ArrayList<>();

        private RouteBuilder(ServerBuilder serverBuilder) {
            this.serverBuilder = serverBuilder;
        }

        public RouteBuilder get(String path, RequestHandler handler) {
            return method("GET", path, handler);
        }

        public RouteBuilder post(String path, RequestHandler handler) {
            return method("POST", path, handler);
        }

        public RouteBuilder put(String path, RequestHandler handler) {
            return method("PUT", path, handler);
        }

        public RouteBuilder delete(String path, RequestHandler handler) {
            return method("DELETE", path, handler);
        }

        public RouteBuilder options(String path, RequestHandler handler) {
            return method("OPTIONS", path, handler);
        }

        public RouteBuilder method(String httpMethod, String path, RequestHandler handler) {
            routeHandlers.add(new HttpRouteHandler(httpMethod, path, handler));
            return this;
        }

        public ServerBuilder end() {
            return serverBuilder;
        }

        public Server build() {
            return serverBuilder.build();
        }
    }

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile(".*\\{[^/}]+\\}.*");
    private static final Pattern PATH_VARIABLE_REPLACE = Pattern.compile("\\{([^/}]+)\\}");

    private static final class HttpRouteHandler {
        private final String httpMethod;
        private final String path;
        private final RequestHandler handler;
        private final boolean containPathVariable;
        private final Pattern routeMatcher;

        private HttpRouteHandler(String httpMethod, String path, RequestHandler handler) {
            this.httpMethod = httpMethod;
            this.path = path;
            this.handler = handler;
            this.containPathVariable = PATH_VARIABLE_PATTERN.matcher(path).matches();
            this.routeMatcher = containPathVariable ? Pattern.compile("^" + PATH_VARIABLE_REPLACE.matcher(path).replaceAll("(?<$1>\\[^/]+\\)") + "$") : null;
        }

        boolean isContainPathVariable() {
            return containPathVariable;
        }

        public Matcher match(String requestPath) {
            var matcher = routeMatcher.matcher(requestPath);
            if (matcher.find()) {
                return matcher;
            } else {
                return null;
            }
        }
    }

    private void dispatch(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws IOException {
        String requestPath = "/";
        String rawQuery = "";
        try {
            var uri = request.getUri();
            requestPath = uri.getPath();
            rawQuery = uri.getRawQuery();
        } catch (URISyntaxException e) {
            //
        }
        String method = request.getMethod();
        if (staticRoutes.containsKey(requestPath) && staticRoutes.get(requestPath).containsKey(method)) {
            staticRoutes.get(requestPath).get(method).handler.handle(new RequestWrapper(request, requestPath, rawQuery, context, null), new ResponseWrapper(response));
            return;
        } else {
            for (var handler : pathVariableRoutes) {
                Matcher matcher = handler.match(requestPath);
                if (matcher != null) {
                    handler.handler.handle(new RequestWrapper(request, requestPath, rawQuery, context, matcher), new ResponseWrapper(response));
                    return;
                }
            }

        }

        response.setCode(HttpStatus.SC_NOT_FOUND);
        response.setEntity(new StringEntity("404 not found", ContentType.TEXT_HTML));
    }

    private static class RequestWrapper implements Request {
        private final ClassicHttpRequest request;
        private final Context context;
        private final String path;
        private final Map<String, List<String>> queryParams = new HashMap<>();
        private final Matcher matcher;

        private RequestWrapper(ClassicHttpRequest request, String path, String rawQuery, HttpContext context, Matcher matcher) {
            this.context = new ContextWrapper(context);
            this.request = request;
            this.path = path;
            for (var qp : URLEncodedUtils.parse(rawQuery, StandardCharsets.UTF_8)) {
                var name = qp.getName();
                if (!queryParams.containsKey(name)) {
                    queryParams.put(name, new ArrayList<>());
                }
                queryParams.get(name).add(qp.getValue());
            }
            this.matcher = matcher;
        }

        public List<String> getHeaderNames() {
            return Stream.of(request.getHeaders()).map(Header::getName).collect(Collectors.toUnmodifiableList());
        }

        @Override
        public Context context() {
            return context;
        }

        @Override
        public List<String> getHeaders(String name) {
            return Stream.of(request.getHeaders(name)).map(Header::getValue).collect(Collectors.toUnmodifiableList());
        }

        @Override
        public Optional<String> getHeader(String name) {
            return Stream.of(request.getHeaders(name)).map(Header::getValue).findFirst();
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public List<String> getQueryParameters(String name) {
            return Collections.unmodifiableList(queryParams.getOrDefault(name, List.of()));
        }

        @Override
        public Optional<String> getQueryParameter(String name) {
            return getQueryParameters(name).stream().findFirst();
        }

        @Override
        public String getPathVariable(String name) {
            return matcher == null ? null : matcher.group(name);
        }
    }

    private static class ResponseWrapper implements Response {
        private final ClassicHttpResponse response;

        private ResponseWrapper(ClassicHttpResponse response) {
            this.response = response;
        }


        @Override
        public Response code(int code) {
            response.setCode(code);
            return this;
        }

        @Override
        public Response header(String name, String value) {
            response.setHeader(name, value);
            return this;
        }

        @Override
        public Response addHeader(String name, String value) {
            response.addHeader(name, value);
            return this;
        }

        @Override
        public Response body(String body, String contentType, Charset charset) {
            response.setEntity(new StringEntity(body, ContentType.create(contentType, charset)));
            return this;
        }

        @Override
        public Response body(File file, String contentType, Charset charset) {
            response.setEntity(new FileEntity(file, ContentType.create(contentType, charset)));
            return this;
        }

        @Override
        public Response body(Path path, String contentType, Charset charset) {
            response.setEntity(new PathEntity(path, ContentType.create(contentType, charset)));
            return this;
        }

        @Override
        public Response body(InputStream inputStream, String contentType, Charset charset) {
            response.setEntity(new InputStreamEntity(inputStream, ContentType.create(contentType, charset)));
            return this;
        }

        @Override
        public Response body(byte[] body, String contentType, Charset charset) {
            response.setEntity(new ByteArrayEntity(body, ContentType.create(contentType, charset)));
            return this;
        }

        @Override
        public Response body(ByteBuffer body, String contentType, Charset charset) {
            response.setEntity(new ByteBufferEntity(body, ContentType.create(contentType, charset)));
            return this;
        }
    }

    private static class ContextWrapper implements Context {
        private final HttpContext context;

        private ContextWrapper(HttpContext context) {
            this.context = context;
        }
    }

    public void start() throws IOException, InterruptedException {
        HttpRequestHandler dispatcher = this::dispatch;
        HttpProcessor httpProcessor = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseContent())
                .add(new ResponseConnControl())
                .build();
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(30))
                .setSoReuseAddress(true)
                .setTcpNoDelay(true)
                .build();
        HttpServer server = ServerBootstrap.bootstrap()
                .setListenerPort(port)
                .setHttpProcessor(httpProcessor)
                .setSocketConfig(socketConfig)
                .setExceptionListener(new ExceptionListener() {

                    private void handle(Exception ex) {
                        //don't print socket related errors
                        if (! (ex instanceof ConnectionClosedException || ex instanceof SocketTimeoutException || ex instanceof SocketException)) {
                            ex.printStackTrace(System.err);
                        }
                    }

                    @Override
                    public void onError(Exception ex) {
                        handle(ex);
                    }

                    @Override
                    public void onError(HttpConnection connection, Exception ex) {
                        handle(ex);
                    }
                })
                .register("*", dispatcher)
                .create();
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.close(CloseMode.GRACEFUL)));
        System.out.println("Listening on port " + port);
        server.awaitTermination(TimeValue.MAX_VALUE);
    }
}

