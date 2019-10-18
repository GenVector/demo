package com.richinfoai.server.utlis;

import com.google.common.base.Strings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

public class BodyRequestWrapper extends HttpServletRequestWrapper {

    //private  String body;
    //private final byte[] binary;
    private Collection<Part> parts;


    public BodyRequestWrapper(HttpServletRequest request) throws IOException,ServletException {
        super(request);
        //json body
        //this.binary = RequestReadUtils.saveRequestBodyBinary(request);
        //RequestPart
        this.parts = request.getParts();
        //直接获取字符流也没有问题
        //this.body = RequestReadUtils.saveRequestBodyByInputStream(request);
        //this.body = RequestReadUtils.saveRequestBodyByReader(request);
        //有文件上传时获取Parts

    }

//    public String getBody() {
//        return body;
//    }

    public byte[] getBinary() {
        return null;
    }


    @Override
    public Part getPart(String name) throws IOException, ServletException {
        for (Part part : parts) {
            if (!Strings.isNullOrEmpty(part.getName()) && part.getName().equals(name))
                return part;
        }
        return null;
    }

    @Override
    public Collection<Part> getParts() throws IOException,
            ServletException {
        return this.parts;
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