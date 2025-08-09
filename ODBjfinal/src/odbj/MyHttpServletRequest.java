package odbj;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

public class MyHttpServletRequest implements HttpServletRequest {

    HttpServletRequest req;
    Boolean isodb;
    ServletInputStream is;
    ObjectInputStream ois;

    static List<String> clients = List.of("localhost:2001","localhost:2002","localhost:8080");
    static List<Integer> servers = List.of(8080,2001,2002);

    public MyHttpServletRequest(HttpServletRequest req) throws IOException {
        this.req = req;
        isodb = servers.contains(req.getLocalPort());
//        if (isodb) {
            is = req.getInputStream();
//        }
    }

    public MyHttpServletRequest(HttpServletRequest req, boolean isodb) throws IOException {
        this.req = req;
        this.isodb = isodb;
        is = req.getInputStream();
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return "";
    }

    /*@Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

    }*/

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public String getContentType() {
        return "";
    }

    public ServletInputStream getInputStream() throws IOException {
        ServletInputStream is = this.is;
        ois = new ObjectInputStream(is);
        return new MyServletInputStream(is, isodb, ois);
    }

    // Récupère la valeur d'un paramètre de la requête
    public String getParameter(String name) {
        return req.getParameter(name) ;
    }

    // Récupère tous les paramètres de la requête
    public Map<String, String[]> getParameterMap() {
        return req.getParameterMap();
    }

    // Récupère les noms de tous les paramètres
    public Enumeration<String> getParameterNames() {
        return req.getParameterNames();
    }

    // Récupère toutes les valeurs d'un paramètre donné
    public String[] getParameterValues(String name) {
        return req.getParameterValues(name) ;
    }

    // Récupère la méthode HTTP (GET, POST, etc.)
    public String getMethod() {
        return req.getMethod();
    }

    @Override
    public String getPathInfo() {
        return "";
    }

    @Override
    public String getPathTranslated() {
        return "";
    }

    // Récupère l'URI de la requête
    public String getRequestURI() {
        return req.getRequestURI();
    }

    // Récupère l'URL complète de la requête
    public StringBuffer getRequestURL() {
        return req.getRequestURL();
    }

    @Override
    public String getServletPath() {
        return "";
    }

    // Récupère la chaîne de requête (après le ?)
    public String getQueryString() {
        return req.getQueryString() ;
    }

    @Override
    public String getRemoteUser() {
        return "";
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return "";
    }

    // Récupère la session HTTP associée
    public HttpSession getSession(boolean create) {
        return req.getSession(create) ;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return "";
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return req.isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String s, String s1) throws ServletException {
        req.login(s,s1);
    }

    @Override
    public void logout() throws ServletException {
        req.logout();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return List.of();
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) throws IOException, ServletException {
        return null;
    }

    // Récupère un header HTTP spécifique
    public String getHeader(String name) {
        return req.getHeader(name) ;
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String s) {
        return 0;
    }

    @Override
    public String getAuthType() {
        return "";
    }

    // Récupère tous les cookies de la requête
    public Cookie[] getCookies() {
        return req.getCookies() ;
    }

    @Override
    public long getDateHeader(String s) {
        return 0;
    }

    // Récupère le chemin contextuel de l'application
    public String getContextPath() {
        return req.getContextPath() ;
    }

    public String getProtocol() {
        return req.getProtocol();
    }

    public String getScheme() {
        return req.getScheme();
    }

    public String getServerName() {
        return req.getServerName();
    }

    public int getServerPort() {
        return req.getServerPort();
    }

    public String getRemoteAddr() {
        return req.getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return "";
    }

    /*@Override
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

    }*/

    public BufferedReader getReader() throws IOException {
        return req.getReader();
    }

    public Locale getLocale() {
        return req.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return "";
    }

    @Override
    public String getLocalAddr() {
        return "";
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    /*@Override
    public String getRequestId() {
        return "";
    }*/

    /*@Override
    public String getProtocolRequestId() {
        return "";
    }*/

    /*@Override
    public ServletConnection getServletConnection() {
        return null;
    }*/

}