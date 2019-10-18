package com.richinfoai.server.utlis;

import com.richinfoai.server.dao.entity.ProbeStatistics;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
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

    private void commonFileProcess(String filename, InputStream is) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(getClass().getResource("/").getPath() + filename));
            int b = 0;
            while ((b = is.read()) != -1) {
                fos.write(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void imageProcess(String filename, String ext, InputStream is) throws IOException {
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName(ext);
        ImageReader ir = irs.hasNext() ? irs.next() : null;
        if (ir == null)
            return;
        ir.setInput(ImageIO.createImageInputStream(is));//必须转换为ImageInputStream，否则异常

        ImageReadParam rp = ir.getDefaultReadParam();
        Rectangle rect = new Rectangle(0, 0, 200, 200);
        rp.setSourceRegion(rect);

        int imageNum = ir.getNumImages(true);//allowSearch必须为true，否则有些图片格式imageNum为-1。

        System.out.println("imageNum:" + imageNum);

        for (int imageIndex = 0; imageIndex < imageNum; imageIndex++) {
            BufferedImage bi = ir.read(imageIndex, rp);
            ImageIO.write(bi, ext, new File(getClass().getResource("/").getPath() + filename));
        }
    }

    private void FileProcess(Part part) throws IOException {
        System.out.println("part.getName(): " + part.getName());

        if (part.getName().equals("file")) {
            String cd = part.getHeader("Content-Disposition");
            String[] cds = cd.split(";");
            String filename = cds[2].substring(cds[2].indexOf("=") + 1).substring(cds[2].lastIndexOf("//") + 1).replace("\"", "");
            String ext = filename.substring(filename.lastIndexOf(".") + 1);

            System.out.println("filename:" + filename);
            System.out.println("ext:" + ext);

            InputStream is = part.getInputStream();

            if (Arrays.binarySearch(ImageIO.getReaderFormatNames(), ext) >= 0)
                imageProcess(filename, ext, is);
            else {
                commonFileProcess(filename, is);
            }
        }
    }
}