package com.richinfoai.server.utlis;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class BodyFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //TODO something
    }

    @Override
    public void destroy() {
        //TODO something
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        //取Body数据
        String uri = request.getRequestURI();
        BodyRequestWrapper requestWrapper = new BodyRequestWrapper(request);
        //TODO something
        filterChain.doFilter(requestWrapper != null ? requestWrapper : request, servletResponse);

    }
}
