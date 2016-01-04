package classes;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PathMatchingFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
    }
    public void destroy() {
    }
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
  
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;
        
        final PrintWriter writer = resp.getWriter();
        writer.println("Filter output:");
        writer.println("Request URI: " + req.getRequestURI());
        writer.println("Context path" + req.getContextPath());
        writer.println("Servlet path: " + req.getServletPath());
        writer.println("Path info: " + req.getPathInfo());
    }
}
