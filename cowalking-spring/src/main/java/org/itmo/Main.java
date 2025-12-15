
package org.itmo;

import org.eclipse.jetty.server.Server;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import jakarta.servlet.ServletContext;
import org.itmo.config.AppConfig;
import org.itmo.config.SecurityConfig; 
import org.itmo.config.WebConfig;

import java.io.IOException;
import java.net.ServerSocket;


import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.FilterHolder; 



import org.springframework.web.filter.DelegatingFilterProxy; 
import jakarta.servlet.DispatcherType; 
import java.util.EnumSet; 


import jakarta.servlet.MultipartConfigElement;

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
        

        
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(AppConfig.class, SecurityConfig.class); 
        context.addEventListener(new ContextLoaderListener(rootContext));

        
        
        
        DelegatingFilterProxy securityFilterChainProxy = new DelegatingFilterProxy("springSecurityFilterChain");
        
        
        FilterHolder filterHolder = new FilterHolder(securityFilterChainProxy); 
        context.addFilter(filterHolder, "/*", null); 
        

        
        AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
        webContext.register(WebConfig.class); 
        webContext.setParent(rootContext); 

        
        org.springframework.web.servlet.DispatcherServlet dispatcherServlet = new org.springframework.web.servlet.DispatcherServlet(webContext);

        ServletHolder servletHolder = new ServletHolder(dispatcherServlet);
        context.addServlet(servletHolder, "/"); 

        
        
        MultipartConfigElement multipartConfig = new MultipartConfigElement(
                null, 
                10 * 1024 * 1024, 
                10 * 1024 * 1024, 
                0 
        );
        
        context.getServletHandler().getServlets()[0].getRegistration().setMultipartConfig(multipartConfig);
        

        server.start();
        server.join();
    }

    private static int findFreePort(int start, int end) {
        for (int port = start; port <= end; port++) {
            try (ServerSocket socket = new ServerSocket(port)) {
                socket.setReuseAddress(true);
                return port;
            } catch (IOException ignored) {
                
            }
        }
        return -1;
    }
}