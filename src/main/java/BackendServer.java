import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@WebServlet("/backend")
public class BackendServer extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Allow", "GET, POST, OPTIONS");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {

        try {
            long start = System.currentTimeMillis();
            System.out.println("BackendServer: doGet");

            response.setContentType("image/jpeg");
            ServletOutputStream out = response.getOutputStream();
            String ine = request.getParameter("ine");
            System.out.println("From BackendServer: RemoteAddr"+request.getRemoteAddr());
            System.out.println("From BackendServer: RemotePort"+request.getRemotePort());
            System.out.println("From BackendServer: LocalPort"+request.getLocalPort());
            System.out.println("From BackendServer: LocalAddr"+request.getLocalAddr());
            System.out.println("From BackendServer: LocalNAme"+request.getLocalName());
            System.out.println("From BackendServer: ServerName"+request.getServerName());
            InputStream file = new FileInputStream("/opt/images/"+ine+".jpeg");
            byte [] buffer = new byte[185000];
            int len;
            while ((len = file.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            long end = System.currentTimeMillis();
            System.out.println("Processing took: " + (end - start) + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
