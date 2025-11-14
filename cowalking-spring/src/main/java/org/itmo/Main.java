package org.itmo;

import org.eclipse.jetty.server.Server;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.itmo.config.AppConfig;
import org.itmo.config.SecurityConfig;
import org.itmo.config.WebConfig;

import java.io.IOException;
import java.net.ServerSocket;

// --- Исправлен импорт ---
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
// --- Конец исправленного импорта ---

public class Main {

    private static final int START_PORT = 8080;
    private static final int END_PORT = 10000;

    public static void main(String[] args) throws Exception {
        int port = findFreePort(START_PORT, END_PORT);
        if (port == -1) {
            throw new IllegalStateException("Нет свободных портов в диапазоне " + START_PORT + "-" + END_PORT);
        }
        System.out.println("Starting server on port: " + port);

        Server server = new Server(port);

        // --- Используем ServletContextHandler ---
        ServletContextHandler context = new ServletContextHandler(server, "/");
        context.setContextPath("/");

        // 1. Создаем и регистрируем корневой контекст (root context) через ContextLoaderListener
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(AppConfig.class, SecurityConfig.class);
        // Обратите внимание: refresh() вызывается автоматически ContextLoaderListener при инициализации
        context.addEventListener(new ContextLoaderListener(rootContext));

        // 2. Создаем веб-контекст (web context) и устанавливаем ему родителя (корневой контекст)
        AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
        webContext.register(WebConfig.class); // Регистрируем веб-конфигурацию
        webContext.setParent(rootContext); // Устанавливаем иерархию контекстов
        // ВАЖНО: НЕ вызываем refresh() здесь! DispatcherServlet сделает это сам.

        // 3. Создаем DispatcherServlet и передаем ему веб-контекст
        DispatcherServlet dispatcherServlet = new DispatcherServlet(webContext);

        ServletHolder servletHolder = new ServletHolder(dispatcherServlet);
        context.addServlet(servletHolder, "/"); // Маппим на корень

        server.start();
        server.join();
    }

    private static int findFreePort(int start, int end) {
        for (int port = start; port <= end; port++) {
            try (ServerSocket socket = new ServerSocket(port)) {
                socket.setReuseAddress(true);
                return port;
            } catch (IOException ignored) {
                // Порт занят, продолжаем цикл
            }
        }
        return -1;
    }
}