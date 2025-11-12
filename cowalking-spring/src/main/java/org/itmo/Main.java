package org.itmo;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import org.itmo.config.AppConfig;
import org.itmo.config.SecurityConfig;
import org.itmo.config.WebConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;

// --- Исправлен импорт ---
import org.eclipse.jetty.servlet.ServletContextHandler;
// --- Конец исправленного импорта ---
import org.eclipse.jetty.servlet.ServletHolder;

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

        WebAppContext context = new WebAppContext();
        context.setContextPath("/");

        // Указываем путь к ресурсам (шаблонам, статике), где Spring Boot обычно их ищет
        URL resourceUrl = Main.class.getClassLoader().getResource("/");
        if (resourceUrl != null) {
            context.setResourceBase(resourceUrl.toURI().toString());
        } else {
            // Резервный путь, если ресурсы не найдены как ресурсы класслоадера
            context.setResourceBase("./src/main/resources");
        }

        // Настройка Spring Context
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(AppConfig.class, SecurityConfig.class);

        context.addEventListener(new ContextLoaderListener(rootContext));

        // Регистрация Spring MVC DispatcherServlet
        org.springframework.web.servlet.DispatcherServlet dispatcherServlet =
                new org.springframework.web.servlet.DispatcherServlet(
                        new AnnotationConfigWebApplicationContext() {{
                            register(WebConfig.class);
                            setParent(rootContext);
                        }}
                );

        // --- Исправлено: используем правильный класс ServletContextHandler ---
        ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/");
        servletContextHandler.addServlet(new ServletHolder(dispatcherServlet), "/");
        servletContextHandler.addEventListener(new ServletContextListener() {
            @Override
            public void contextInitialized(ServletContextEvent sce) {
                ServletContext ctx = sce.getServletContext();
                // Убедимся, что Spring Context инициализирован правильно
                ctx.setAttribute("org.springframework.web.context.WebApplicationContext.ROOT", rootContext);
            }
        });

        server.setHandler(servletContextHandler);

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