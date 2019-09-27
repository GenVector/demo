package com.richinfoai.server.security;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.richinfoai.server.utlis.FormatUtil;
import lombok.val;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CasAuthEntryPoint implements AuthenticationEntryPoint {

    private CasAuthenticationEntryPoint casAuthenticationEntryPoint;

    public CasAuthEntryPoint(final String loginUrl, final ServiceProperties serviceProperties) {
        casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setLoginUrl(loginUrl);
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties);
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        String authUrl = "";
        val builder = (new URIBuilder()).setScheme(request.getScheme()).setHost(request.getServerName()).setPort(request.getServerPort()).setPath("/auth/redirect");
        if (request.getHeader("Referer") != null) {
            authUrl = builder.addParameter("url", request.getHeader("Referer")).toString();
        }

        if (request.getServletPath().equals("/auth/redirect")) {
            casAuthenticationEntryPoint.commence(request, response, authException);
        } else if (request.getServletPath().equals("/auth/checkLoginStatus")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            FormatUtil.writeBackJson("401", response);
        } else {
            if (authUrl.length() > 0) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
                response.setHeader("Access-Control-Allow-Credentials", "true");
                val json = new JsonObject();
                json.add("authUrl", new JsonPrimitive(authUrl));
                response.getWriter().write(json.toString());
            } else {
                casAuthenticationEntryPoint.commence(request, response, authException);
            }
        }
    }
}


