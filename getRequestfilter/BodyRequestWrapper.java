package com.richinfoai.server.utlis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class BodyRequestWrapper extends HttpServletRequestWrapper {

    //private  String body;
    private final byte[] binary;


    public BodyRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.binary = RequestReadUtils.saveRequestBodyBinary(request);
        //this.body = RequestReadUtils.saveRequestBodyByInputStream(request);
        //this.body = RequestReadUtils.saveRequestBodyByReader(request);

    }

//    public String getBody() {
//        return body;
//    }

    public byte[] getBinary() {
        return binary;
    }


    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream bais = new ByteArrayInputStream(getBinary());
        return new ServletInputStream() {

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() {
                return bais.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }



}