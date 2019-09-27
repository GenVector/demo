package com.richinfoai.server.security;


import com.google.gson.Gson;
import com.richinfoai.server.model.auth.CommunityUser;
import com.richinfoai.server.utlis.FormatUtil;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * 登录成功后的处理
 *
 * @author boston
 */
@Log4j2
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //获得授权后可得到用户信息

        val praincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CommunityUser communityUser = (CommunityUser) praincipal;
        Gson gson = new Gson();
        WebAuthenticationDetails details = (WebAuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
        FormatUtil.writeBackJson(gson.toJson(communityUser), response);
        log.info(communityUser.getUsername() + "登录成功!");
    }


}

