package com.tahiti.auth.controller;

import com.google.gson.Gson;
import com.tahiti.auth.exception.BadRequestException;
import okhttp3.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("auth")
public class AuthController extends BaseController {

    CookieJar cookieJar = new CookieJar() {
        private final Map<String, List<Cookie>> cookiesMap = new HashMap<String, List<Cookie>>();

        @Override
        public void saveFromResponse(HttpUrl httpUrl, List<Cookie> cookies) {//可以做保存cookies操作
            // TODO Auto-generated method stub
            if (httpUrl.toString().equals(logUrl))
                myCookies = cookies;
            cookiesMap.put(httpUrl.toString(), cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {//加载新的cookies
            // TODO Auto-generated method stub
            List<Cookie> cookiesList = cookiesMap.get(url.toString());
            return cookiesList != null ? cookiesList : new ArrayList<Cookie>();
        }
    };

    private MediaType FORM = MediaType.parse("application/x-www-form-urlencoded");
    private Gson gson = new Gson();
    List<Cookie> myCookies;  //我们所需要的cookie
    private String logUrl = "http://community-mt.test.richinfoai.com/login";
    private String checkUrl = "http://community-mt.test.richinfoai.com/auth/checkLoginStatus";
    OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .cookieJar(cookieJar)
            .build();


    @ResponseBody
    @GetMapping("/test")
    public void test() {
        String username = "rizhao-1";
        String password = "123456";
        String submit = "Login";
        FormBody body = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("submit", submit).build();
        String resStr;
        Request request = new Request.Builder()
                .url(logUrl)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(body)
                .build();
        try {
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new BadRequestException("Request vector failed. " + response.toString());
            }
            if (response.body() == null) {
                throw new BadRequestException("response body is null !");
            }
            StringBuilder cookieStr=new StringBuilder();
            for(Cookie cookie : myCookies){
                cookieStr.append(cookie.name()).append("=").append(cookie.value()+";");
            }


            request = new Request.Builder()
                    .url(checkUrl)
                    .header("Cookie", cookieStr.toString())
                    .get()
                    .build();

            httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new BadRequestException("Request vector failed. " + response.toString());
            }
            if (response.body() == null) {
                throw new BadRequestException("response body is null !");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
