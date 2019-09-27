package com.richinfoai.server.security;

import com.google.gson.JsonParser;
import com.richinfoai.server.model.auth.CommunityUser;
import com.richinfoai.server.model.auth.RichUser;
import com.richinfoai.server.service.AuthService;
import com.richinfoai.server.utlis.FormatUtil;
import com.richinfoai.server.utlis.GsonUtil;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import okhttp3.*;
import okio.Okio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@Log4j2
public class CommunityUserDetailsService implements UserDetailsService {
    public CommunityUserDetailsService(AuthService authService) {
        this.authService = authService;
    }

    private final OkHttpClient client = new OkHttpClient.Builder()
            .build();
    private final JsonParser jsonParser = new JsonParser();

    @Value("${auth.header.name:}")
    private String authHeaderName;
    @Value("${auth.header.value:}")
    private String authHeaderValue;
    @Value("${app.auth.url}")
    String authUrl;

    private AuthService authService;

    // 从数据库根据username获取
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), username);
        Request request = new Request.Builder()
                .header("Accept", "*/*").addHeader(authHeaderName, authHeaderValue)
                .url(authUrl)
                .post(body)
                .build();
        RichUser richUser;
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                return null;
            }
            richUser = GsonUtil.gson.fromJson(response.body().string(), RichUser.class);
            log.info("login success---" + richUser);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        List<GrantedAuthority> authorities = authService.getAuthoritiesByUserName(username);
        log.info("find auth success---" + authorities);
        CommunityUser user = new CommunityUser(FormatUtil.convertStr(richUser.getAttributes().get("username")), FormatUtil.convertStr(richUser.getAttributes().get("password")), authorities);
        user.setPlatform("App");
        return user;
    }
}
