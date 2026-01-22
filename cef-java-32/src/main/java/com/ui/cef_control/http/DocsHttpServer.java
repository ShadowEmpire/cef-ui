package com.ui.cef_control.http;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.OutputStream;

public class DocsHttpServer {
    private final Path docsPath;
    private final int requestedPort;
    private HttpServer httpServer;
    private int boundPort;

    public DocsHttpServer(String docsPath, int requestedPort) throws IOException {
        this.docsPath = Paths.get(docsPath);
        this.requestedPort = requestedPort;
        validatePath();
    }

    private void validatePath() throws IOException {
        if (!Files.isDirectory(docsPath)) {
            throw new IOException("Docs path is not a directory: " + docsPath);
        }
    }

    public void start() throws IOException {
        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", requestedPort);
        httpServer = HttpServer.create(addr, 0);
        httpServer.createContext("/", new DocsHandler(docsPath));
        httpServer.start();
        boundPort = httpServer.getAddress().getPort();
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
    }

    public boolean isRunning() {
        return httpServer != null;
    }

    public String getBaseUrl() {
        if (!isRunning()) {
            return null;
        }
        return "http://127.0.0.1:" + boundPort;
    }

    public int getBoundPort() {
        return boundPort;
    }

    private static class DocsHandler implements HttpHandler {
        private final Path docsRoot;

        DocsHandler(Path docsRoot) {
            this.docsRoot = docsRoot;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            if (path.contains("..")) {
                exchange.sendResponseHeaders(400, 0);
                exchange.close();
                return;
            }

            Path filePath = docsRoot.resolve(path);

            if (Files.isRegularFile(filePath)) {
                serveFile(exchange, filePath);
            } else {
                Path indexPath = docsRoot.resolve("index.html");
                if (Files.isRegularFile(indexPath)) {
                    serveFile(exchange, indexPath);
                } else {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.close();
                }
            }
        }

        private void serveFile(HttpExchange exchange, Path filePath) throws IOException {
            byte[] content = Files.readAllBytes(filePath);
            String contentType = guessContentType(filePath);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, content.length);
            OutputStream os = exchange.getResponseBody();
            os.write(content);
            os.close();
        }

        private String guessContentType(Path filePath) {
            String name = filePath.getFileName().toString().toLowerCase();
            if (name.endsWith(".html")) return "text/html; charset=utf-8";
            if (name.endsWith(".css")) return "text/css";
            if (name.endsWith(".js")) return "application/javascript";
            if (name.endsWith(".json")) return "application/json";
            if (name.endsWith(".svg")) return "image/svg+xml";
            if (name.endsWith(".png")) return "image/png";
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
            if (name.endsWith(".gif")) return "image/gif";
            if (name.endsWith(".webp")) return "image/webp";
            if (name.endsWith(".woff")) return "font/woff";
            if (name.endsWith(".woff2")) return "font/woff2";
            if (name.endsWith(".ttf")) return "font/ttf";
            return "application/octet-stream";
        }
    }
}

