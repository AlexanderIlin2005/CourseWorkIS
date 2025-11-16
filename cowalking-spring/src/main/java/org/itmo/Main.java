// src/main/java/org/itmo/Main.java
package org.itmo;

import org.eclipse.jetty.server.Server;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import jakarta.servlet.ServletContext;
import org.itmo.config.AppConfig;
import org.itmo.config.SecurityConfig; // Убедитесь, что импортирован
import org.itmo.config.WebConfig;

import java.io.IOException;
import java.net.ServerSocket;

// --- Импорты для Jetty ---
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.FilterHolder; // <-- Нужен для обертывания фильтра
// --- Конец импорт---

// --- Импорты для Spring Security ---
import org.springframework.web.filter.DelegatingFilterProxy; // <-- Импортируем фильтр
// --- Конец импорт---

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

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // 1. Создаем и регистрируем корневой контекст (root context) через ContextLoaderListener
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(AppConfig.class, SecurityConfig.class);
        context.addEventListener(new ContextLoaderListener(rootContext));

        // --- РУЧНАЯ РЕГИСТРАЦИЯ ФИЛЬТРА Spring Security ---
        // Создаем DelegatingFilterProxy для 'springSecurityFilterChain'
        // Это имя бина, которое Spring Security автоматически создает из SecurityFilterChain
        DelegatingFilterProxy securityFilterChainProxy = new DelegatingFilterProxy("springSecurityFilterChain");
        // Устанавливаем родительский контекст напрямую (альтернатива setApplicationContextAttribute)
        // Это гарантирует, что фильтр будет искать бин 'springSecurityFilterChain' в rootContext
        // ---> УБРАНА ПРОБЛЕМНАЯ СТРОКА: securityFilterChainProxy.setApplicationContext(rootContext);
        // ---> Вместо этого, rely на стандартное поведение DelegatingFilterProxy и ContextLoaderListener.
        // Регистрируем фильтр на все пути ("/*") ПЕРЕД DispatcherServlet
        FilterHolder filterHolder = new FilterHolder(securityFilterChainProxy);
        context.addFilter(filterHolder, "/*", null); // Регистрируем фильтр на все пути ПЕРЕД DispatcherServlet
        // --- КОНЕЦ РУЧНОЙ РЕГИСТРАЦИИ ---

        // 2. Создаем веб-контекст (web context) и устанавливаем ему родителя (корневой контекст)
        AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
        webContext.register(WebConfig.class);
        webContext.setParent(rootContext); // Устанавливаем иерархию контекстов

        // 3. Создаем DispatcherServlet и передаем ему веб-контекст
        org.springframework.web.servlet.DispatcherServlet dispatcherServlet = new org.springframework.web.servlet.DispatcherServlet(webContext);

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