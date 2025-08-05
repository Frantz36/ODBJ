package odbj;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class MyHttpServletResponse implements HttpServletResponse {

    HttpServletResponse resp;
    Boolean isodb;
    ServletOutputStream os;
    ObjectOutputStream oos;

    static List<String> clients = List.of("localhost:2001","localhost:2002","localhost:8080");
    static List<Integer> servers = List.of(2001,2002,8080);

    public MyHttpServletResponse(HttpServletResponse resp) throws IOException {
        this.resp = resp;
        isodb = true;
        if (isodb) {
            os = resp.getOutputStream();
        }
    }

    public MyHttpServletResponse(HttpServletResponse resp, boolean isodb) throws IOException {
        this.resp = resp;
        this.isodb = isodb;
//        if (isodb) {
            os = resp.getOutputStream();
//        }
    }

    public ServletOutputStream getOutputStream() throws IOException {
        ServletOutputStream os = this.os;
        oos = new ObjectOutputStream(new ByteArrayOutputStream());
        return new MyServletOutputStream(os, isodb, oos);
    }

    @Override
    public String getCharacterEncoding() {
        return resp.getCharacterEncoding();
    }

    public String getContentType() {
        return resp.getContentType();
    }

    public void setContentType(String type) {
        resp.setContentType(type);
    }

    @Override
    public void setBufferSize(int i) {
        resp.setBufferSize(i);
    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    public void setStatus(int sc) {
        resp.setStatus(sc);
    }

    public void setStatus(int sc, String sm) {
        resp.setStatus(sc);
    }

    @Override
    public int getStatus() {
        return resp.getStatus();
    }

    public void setHeader(String name, String value) {
        resp.setHeader(name, value);
    }

    @Override
    public void addHeader(String s, String s1) {
        resp.addHeader(s,s1);
    }

    @Override
    public void setIntHeader(String s, int i) {
        resp.setIntHeader(s,i);
    }

    @Override
    public void addIntHeader(String s, int i) {
        resp.addIntHeader(s,i);
    }

    public String getHeader(String name) {
        return resp.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return List.of();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return List.of();
    }

    public void addCookie(Cookie cookie) {
        resp.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String s) {
        return false;
    }

    @Override
    public String encodeURL(String s) {
        return resp.encodeURL(s);
    }

    @Override
    public String encodeRedirectURL(String s) {
        return resp.encodeRedirectURL(s);
    }

    @Override
    public void sendError(int i, String s) throws IOException {
        resp.sendError(i,s);
    }

    @Override
    public void sendError(int i) throws IOException {
        resp.sendError(i);
    }

    public void sendRedirect(String location) throws IOException, IOException {
        resp.sendRedirect(location);
    }

    @Override
    public void setDateHeader(String s, long l) {
        resp.setDateHeader(s,l);
    }

    @Override
    public void addDateHeader(String s, long l) {
        resp.addDateHeader(s,l);
    }

    public void flushBuffer() throws IOException {
        resp.flushBuffer();
    }

    public void resetBuffer() {
        resp.resetBuffer();
    }

    public boolean isCommitted() {
        return resp.isCommitted();
    }

    @Override
    public void reset() {
        resp.reset();
    }

    @Override
    public void setLocale(Locale locale) {
        resp.setLocale(locale);
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    public PrintWriter getWriter() throws IOException {
        return resp.getWriter();
    }

    @Override
    public void setCharacterEncoding(String s) {
        resp.setCharacterEncoding(s);
    }

    @Override
    public void setContentLength(int i) {
        resp.setContentLength(i);
    }

    @Override
    public void setContentLengthLong(long l) {
        resp.setContentLengthLong(l);
    }

}
