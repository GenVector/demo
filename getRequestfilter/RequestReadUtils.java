package com.richinfoai.server.utlis;

import com.richinfoai.server.dao.entity.ProbeStatistics;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Map;

public class RequestReadUtils {

    private static final int BUFFER_SIZE = 1024 * 8;

    public static String readBody(HttpServletRequest request) throws IOException {
        BufferedReader bufferedReader = request.getReader();
        StringWriter writer = new StringWriter();
        write(bufferedReader, writer);
        return writer.getBuffer().toString();
    }

    public static long write(Reader reader, Writer writer) throws IOException {
        return write(reader, writer, BUFFER_SIZE);
    }

    public static long write(Reader reader, Writer writer, int bufferSize) throws IOException {
        int read;
        long total = 0;
        char[] buf = new char[bufferSize];
        while ((read = reader.read(buf)) != -1) {
            writer.write(buf, 0, read);
            total += read;
        }
        return total;
    }

    public static byte[] saveRequestBodyBinary(HttpServletRequest request) {
        //binary
        int len = request.getContentLength();
        byte[] buffer = new byte[len];
        ServletInputStream in = null;

        try {
            in = request.getInputStream();
            in.read(buffer, 0, len);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return buffer;
    }

    public static String saveRequestBodyByInputStream(HttpServletRequest request) {
        //char method 2
        StringBuilder sb = new StringBuilder("");
        InputStream is = null;
        try {
            is = request.getInputStream();
            sb = new StringBuilder();
            byte[] b = new byte[4096];
            for (int n; (n = is.read(b)) != -1; ) {
                sb.append(new String(b, 0, n));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public static String saveRequestBodyByReader(HttpServletRequest request) {
        //char method 1
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder("");
        try {
            br = request.getReader();
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}