package com.example.jdmn.editor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class DmnEditorServer {
    private static final String HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8090;
    private static final Duration TEST_TIMEOUT = Duration.ofMinutes(15);

    private final Path projectRoot;
    private final Path dmnDirectory;
    private final HttpServer server;

    private DmnEditorServer(int port, Path projectRoot) throws IOException {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        this.dmnDirectory = this.projectRoot.resolve("src/main/resources/dmn").normalize();
        this.server = HttpServer.create(new InetSocketAddress(HOST, port), 0);
        this.server.setExecutor(Executors.newCachedThreadPool());
        this.server.createContext("/api/files", this::handleFilesIndex);
        this.server.createContext("/api/files/", this::handleFileByName);
        this.server.createContext("/api/tests", this::handleTests);
        this.server.createContext("/", this::handleStatic);
    }

    public static void main(String[] args) throws Exception {
        int port = parsePort(args);
        DmnEditorServer editorServer = new DmnEditorServer(port, Path.of("."));
        editorServer.start();
    }

    private static int parsePort(String[] args) {
        if (args != null && args.length > 0) {
            return Integer.parseInt(args[0]);
        }
        String value = System.getenv("DMN_EDITOR_PORT");
        return value != null && !value.isBlank() ? Integer.parseInt(value) : DEFAULT_PORT;
    }

    private void start() {
        if (!Files.isDirectory(dmnDirectory)) {
            throw new IllegalStateException("DMN directory not found: " + dmnDirectory);
        }
        server.start();
        System.out.printf(Locale.ROOT, "DMN editor running at http://%s:%d%n", HOST, server.getAddress().getPort());
        System.out.printf(Locale.ROOT, "Editing files under: %s%n", dmnDirectory);
        System.out.println("Press Ctrl+C to stop.");
    }

    private void handleFilesIndex(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange, "GET");
                return;
            }
            List<String> files = listDmnFiles();
            sendJson(exchange, 200, toJsonArray(files));
        } catch (Exception e) {
            sendApiError(exchange, 500, "Failed to list DMN files", e);
        } finally {
            exchange.close();
        }
    }

    private void handleFileByName(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String encodedName = extractFileName(exchange.getRequestURI());
            if (encodedName == null || encodedName.isBlank()) {
                sendApiError(exchange, 400, "Missing DMN file name", null);
                return;
            }
            String fileName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8);
            if ("GET".equalsIgnoreCase(method)) {
                Path file = resolveDmnFile(fileName, false);
                byte[] bytes = Files.readAllBytes(file);
                send(exchange, 200, "application/xml; charset=utf-8", bytes);
                return;
            }
            if ("PUT".equalsIgnoreCase(method)) {
                Path file = resolveDmnFile(fileName, true);
                String body = readBody(exchange.getRequestBody());
                if (body.isBlank()) {
                    sendApiError(exchange, 400, "DMN content must not be empty", null);
                    return;
                }
                Files.writeString(file, body, StandardCharsets.UTF_8);
                sendJson(exchange, 200, "{\"saved\":true,\"file\":" + toJsonString(fileName) + "}");
                return;
            }
            sendMethodNotAllowed(exchange, "GET, PUT");
        } catch (IllegalArgumentException e) {
            sendApiError(exchange, 400, e.getMessage(), null);
        } catch (java.nio.file.NoSuchFileException e) {
            sendApiError(exchange, 404, "DMN file not found", null);
        } catch (Exception e) {
            sendApiError(exchange, 500, "Failed to process DMN file request", e);
        } finally {
            exchange.close();
        }
    }

    private void handleTests(HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange, "POST");
                return;
            }
            ProcessResult result = runCommand(List.of("mvn", "clean", "test"));
            String json = "{"
                    + "\"exitCode\":" + result.exitCode + ","
                    + "\"timedOut\":" + result.timedOut + ","
                    + "\"durationMs\":" + result.durationMs + ","
                    + "\"output\":" + toJsonString(result.output)
                    + "}";
            sendJson(exchange, 200, json);
        } catch (Exception e) {
            sendApiError(exchange, 500, "Failed to run tests", e);
        } finally {
            exchange.close();
        }
    }

    private void handleStatic(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange, "GET");
                return;
            }
            String path = exchange.getRequestURI().getPath();
            String resource = switch (path) {
                case "/", "/index.html" -> "editor/index.html";
                case "/app.js" -> "editor/app.js";
                case "/styles.css" -> "editor/styles.css";
                default -> null;
            };
            if (resource == null) {
                send(exchange, 404, "text/plain; charset=utf-8", "Not found".getBytes(StandardCharsets.UTF_8));
                return;
            }
            byte[] content = readClasspathResource(resource);
            String contentType = contentTypeFor(resource);
            send(exchange, 200, contentType, content);
        } catch (Exception e) {
            send(exchange, 500, "text/plain; charset=utf-8", "Internal server error".getBytes(StandardCharsets.UTF_8));
        } finally {
            exchange.close();
        }
    }

    private List<String> listDmnFiles() throws IOException {
        try (Stream<Path> stream = Files.list(dmnDirectory)) {
            return stream
                    .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".dmn"))
                    .map(path -> path.getFileName().toString())
                    .sorted(Comparator.naturalOrder())
                    .toList();
        }
    }

    private Path resolveDmnFile(String fileName, boolean allowCreate) throws IOException {
        if (!fileName.endsWith(".dmn")) {
            throw new IllegalArgumentException("File name must end with .dmn");
        }
        if (fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("Only root-level DMN files are supported");
        }
        Path resolved = dmnDirectory.resolve(fileName).normalize();
        if (!resolved.startsWith(dmnDirectory)) {
            throw new IllegalArgumentException("Invalid file path");
        }
        if (!allowCreate && !Files.exists(resolved)) {
            throw new java.nio.file.NoSuchFileException(resolved.toString());
        }
        if (allowCreate && !Files.exists(resolved)) {
            Files.createFile(resolved);
        }
        return resolved;
    }

    private ProcessResult runCommand(List<String> command) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        ProcessBuilder processBuilder = new ProcessBuilder(new ArrayList<>(command));
        processBuilder.directory(projectRoot.toFile());
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        StringBuffer outputBuffer = new StringBuffer();
        Thread streamReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuffer.append(line).append('\n');
                }
            } catch (IOException e) {
                outputBuffer.append("[stream read error] ").append(e.getMessage()).append('\n');
            }
        }, "dmn-editor-mvn-reader");
        streamReader.setDaemon(true);
        streamReader.start();

        boolean finished = process.waitFor(TEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        int exitCode;
        boolean timedOut = false;
        if (!finished) {
            timedOut = true;
            process.destroyForcibly();
            process.waitFor(5, TimeUnit.SECONDS);
            exitCode = -1;
        } else {
            exitCode = process.exitValue();
        }
        streamReader.join(TimeUnit.SECONDS.toMillis(2));
        return new ProcessResult(exitCode, timedOut, System.currentTimeMillis() - start, outputBuffer.toString());
    }

    private String extractFileName(URI uri) {
        String path = uri.getPath();
        String prefix = "/api/files/";
        return path.startsWith(prefix) ? path.substring(prefix.length()) : null;
    }

    private static String readBody(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static byte[] readClasspathResource(String resourcePath) throws IOException {
        try (InputStream inputStream = DmnEditorServer.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return inputStream.readAllBytes();
        }
    }

    private static String contentTypeFor(String resourcePath) {
        return switch (resourcePath) {
            case "editor/index.html" -> "text/html; charset=utf-8";
            case "editor/app.js" -> "application/javascript; charset=utf-8";
            case "editor/styles.css" -> "text/css; charset=utf-8";
            default -> "text/plain; charset=utf-8";
        };
    }

    private static String toJsonArray(List<String> values) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(toJsonString(values.get(i)));
        }
        builder.append(']');
        return builder.toString();
    }

    private static String toJsonString(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder escaped = new StringBuilder(value.length() + 2);
        escaped.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (c < 0x20) {
                        escaped.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                }
            }
        }
        escaped.append('"');
        return escaped.toString();
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        send(exchange, status, "application/json; charset=utf-8", json.getBytes(StandardCharsets.UTF_8));
    }

    private static void sendApiError(HttpExchange exchange, int status, String message, Exception exception) throws IOException {
        String details = exception == null ? "" : (": " + exception.getMessage());
        String body = "{\"error\":" + toJsonString(message + details) + "}";
        sendJson(exchange, status, body);
    }

    private static void sendMethodNotAllowed(HttpExchange exchange, String allowed) throws IOException {
        exchange.getResponseHeaders().set("Allow", allowed);
        send(exchange, 405, "text/plain; charset=utf-8", "Method not allowed".getBytes(StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange exchange, int statusCode, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store, no-cache, must-revalidate");
        exchange.sendResponseHeaders(statusCode, body.length);
        exchange.getResponseBody().write(body);
    }

    private record ProcessResult(int exitCode, boolean timedOut, long durationMs, String output) {
    }
}
